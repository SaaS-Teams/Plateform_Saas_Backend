package tg.univlome.saas.marketing.automation.domain.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;
import tg.univlome.saas.marketing.automation.domain.enums.ExecutionStatus;
import tg.univlome.saas.marketing.automation.domain.models.Workflow;
import tg.univlome.saas.marketing.automation.domain.models.WorkflowExecutionLog;
import tg.univlome.saas.marketing.automation.domain.services.ConditionEvaluatorService;
import tg.univlome.saas.marketing.automation.domain.services.SmsService;
import tg.univlome.saas.marketing.automation.domain.services.WebhookService;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowProducerService;
import tg.univlome.saas.marketing.automation.repositories.WorkflowExecutionLogRepository;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.repositories.ContactSegmentRepository;
import tg.univlome.saas.marketing.contact.repositories.SegmentRepository;
import tg.univlome.saas.marketing.email.application.dtos.requests.EmailMessage;
import tg.univlome.saas.marketing.email.domain.services.EmailService;

@ExtendWith(MockitoExtension.class)
class WorkflowEngineServiceImplTest {

    @Mock
    private WorkflowExecutionLogRepository executionRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private WorkflowProducerService producerService;

    @Mock
    private ConditionEvaluatorService conditionEvaluatorService;

    @Mock
    private WorkflowSchedulingService schedulingService;

    @Mock
    private SmsService smsService;

    @Mock
    private WebhookService webhookService;

    @Mock
    private SegmentRepository segmentRepository;

    @Mock
    private ContactSegmentRepository contactSegmentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WorkflowEngineServiceImpl engineService;

    @BeforeEach
    void setUp() {
        engineService = new WorkflowEngineServiceImpl(
                executionRepository,
                emailService,
                objectMapper,
                producerService,
                conditionEvaluatorService,
                schedulingService,
                smsService,
                webhookService,
                segmentRepository,
                contactSegmentRepository
        );
    }

    @Test
    void processStep_shouldHandleEmailActionAndCallProducerForNextStep() {
        // Given
        UUID executionTrackingId = UUID.randomUUID();
        WorkflowStepMessage message = new WorkflowStepMessage(executionTrackingId, "node_1", "AUTO_CONTINUE");

        String flowDataJson = "{"
                + "  \"nodes\": ["
                + "    { \"id\": \"node_1\", \"type\": \"ACTION_EMAIL\", \"data\": { \"templateId\": \"tpl-123\", \"subject\": \"Test Subject\" }, \"next\": \"node_2\" }"
                + "  ]"
                + "}";

        WorkflowExecutionLog execution = new WorkflowExecutionLog();
        execution.setExecutionTrackingId(executionTrackingId);
        execution.setStatus(ExecutionStatus.PENDING);
        Workflow workflow = new Workflow();
        workflow.setFlowData(flowDataJson);
        execution.setWorkflow(workflow);
        Contact contact = new Contact();
        contact.setEmail("contact@test.com");
        execution.setContact(contact);

        when(executionRepository.findByExecutionTrackingId(executionTrackingId)).thenReturn(Optional.of(execution));

        // When
        engineService.processStep(message);

        // Then
        assertEquals(ExecutionStatus.IN_PROGRESS, execution.getStatus());
        assertEquals("node_2", execution.getCurrentNodeId());

        ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailService, times(1)).sendEmail(emailCaptor.capture());
        EmailMessage sentEmail = emailCaptor.getValue();
        assertEquals("contact@test.com", sentEmail.to());
        assertEquals("Test Subject", sentEmail.subject());
        assertFalse(sentEmail.isHtml());

        ArgumentCaptor<WorkflowStepMessage> producerCaptor = ArgumentCaptor.forClass(WorkflowStepMessage.class);
        verify(producerService, times(1)).sendStepToQueue(producerCaptor.capture());
        WorkflowStepMessage nextMessage = producerCaptor.getValue();
        assertEquals("node_2", nextMessage.nodeId());
        assertEquals("AUTO_CONTINUE", nextMessage.actionType());

        verify(executionRepository, times(1)).save(execution);
    }

    @Test
    void processStep_shouldCompleteExecutionWhenNextStepIdIsEmpty() {
        // Given
        UUID executionTrackingId = UUID.randomUUID();
        WorkflowStepMessage message = new WorkflowStepMessage(executionTrackingId, "node_1", "AUTO_CONTINUE");

        String flowDataJson = "{"
                + "  \"nodes\": ["
                + "    { \"id\": \"node_1\", \"type\": \"UNKNOWN\", \"data\": {} }"
                + "  ]"
                + "}";

        WorkflowExecutionLog execution = new WorkflowExecutionLog();
        execution.setExecutionTrackingId(executionTrackingId);
        execution.setStatus(ExecutionStatus.IN_PROGRESS);
        Workflow workflow = new Workflow();
        workflow.setFlowData(flowDataJson);
        execution.setWorkflow(workflow);

        when(executionRepository.findByExecutionTrackingId(executionTrackingId)).thenReturn(Optional.of(execution));

        // When
        engineService.processStep(message);

        // Then
        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        assertNull(execution.getCurrentNodeId());

        verify(producerService, never()).sendStepToQueue(any());
        verify(executionRepository, times(1)).save(execution);
    }

    @Test
    void processStep_shouldSetFailedStatusWhenNodeDoesNotExist() {
        // Given
        UUID executionTrackingId = UUID.randomUUID();
        WorkflowStepMessage message = new WorkflowStepMessage(executionTrackingId, "node_not_found", "AUTO_CONTINUE");

        String flowDataJson = "{"
                + "  \"nodes\": ["
                + "    { \"id\": \"node_1\", \"type\": \"WAIT\", \"data\": { \"days\": 1 } }"
                + "  ]"
                + "}";

        WorkflowExecutionLog execution = new WorkflowExecutionLog();
        execution.setExecutionTrackingId(executionTrackingId);
        execution.setStatus(ExecutionStatus.IN_PROGRESS);
        Workflow workflow = new Workflow();
        workflow.setFlowData(flowDataJson);
        execution.setWorkflow(workflow);

        when(executionRepository.findByExecutionTrackingId(executionTrackingId)).thenReturn(Optional.of(execution));

        // When
        engineService.processStep(message);

        // Then
        assertEquals(ExecutionStatus.FAILED, execution.getStatus());
        assertNotNull(execution.getErrorDetails());
        assertTrue(execution.getErrorDetails().contains("Nœud introuvable dans le scénario : node_not_found"));

        verify(producerService, never()).sendStepToQueue(any());
        verify(executionRepository, times(1)).save(execution);
    }
}
