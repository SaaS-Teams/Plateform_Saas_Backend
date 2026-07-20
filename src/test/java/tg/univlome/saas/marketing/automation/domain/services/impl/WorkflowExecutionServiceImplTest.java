package tg.univlome.saas.marketing.automation.domain.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowExecutionRequest;
import tg.univlome.saas.marketing.automation.application.dtos.responses.WorkflowExecutionResponse;
import tg.univlome.saas.marketing.automation.application.mappers.WorkflowExecutionMapper;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowProducerService;
import tg.univlome.saas.marketing.automation.domain.enums.ExecutionStatus;
import tg.univlome.saas.marketing.automation.domain.models.Workflow;
import tg.univlome.saas.marketing.automation.domain.models.WorkflowExecutionLog;
import tg.univlome.saas.marketing.automation.repositories.WorkflowExecutionLogRepository;
import tg.univlome.saas.marketing.automation.repositories.WorkflowRepository;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository;
import tg.univlome.saas.shared.exceptions.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkflowExecutionServiceImplTest {

    @Mock
    private WorkflowExecutionLogRepository executionRepository;

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private WorkflowExecutionMapper executionMapper;

    @Mock
    private WorkflowProducerService producerService;

    @InjectMocks
    private WorkflowExecutionServiceImpl executionService;

    @Test
    void shouldStartExecutionSuccessfully() {
        // Given
        UUID workflowTrackingId = UUID.randomUUID();
        Long contactId = 1L;
        WorkflowExecutionRequest request = new WorkflowExecutionRequest(workflowTrackingId, contactId, ExecutionStatus.IN_PROGRESS, "START", null, LocalDateTime.now(), null);

        Workflow workflow = new Workflow();
        workflow.setId(10L);

        Contact contact = new Contact();
        contact.setId(contactId);

        WorkflowExecutionLog log = new WorkflowExecutionLog();
        log.setExecutionTrackingId(UUID.randomUUID());

        WorkflowExecutionResponse expectedResponse = new WorkflowExecutionResponse(log.getExecutionTrackingId(), workflowTrackingId, contactId, ExecutionStatus.IN_PROGRESS, "START", null, LocalDateTime.now(), null);

        given(workflowRepository.findByTrackingId(workflowTrackingId)).willReturn(Optional.of(workflow));
        given(contactRepository.findById(contactId)).willReturn(Optional.of(contact));
        given(executionMapper.toEntity(request, workflow, contact)).willReturn(log);
        given(executionRepository.save(log)).willReturn(log);
        given(executionMapper.toResponse(log)).willReturn(expectedResponse);

        // When
        WorkflowExecutionResponse result = executionService.startExecution(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.executionTrackingId()).isEqualTo(log.getExecutionTrackingId());
        verify(executionRepository).save(log);
    }

    @Test
    void shouldThrowResourceNotFoundWhenWorkflowMissingOnStart() {
        // Given
        UUID workflowTrackingId = UUID.randomUUID();
        WorkflowExecutionRequest request = new WorkflowExecutionRequest(workflowTrackingId, 1L, ExecutionStatus.IN_PROGRESS, "START", null, LocalDateTime.now(), null);

        given(workflowRepository.findByTrackingId(workflowTrackingId)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> executionService.startExecution(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Scénario introuvable");
    }

    @Test
    void shouldGetExecutionByTrackingId() {
        // Given
        UUID trackingId = UUID.randomUUID();
        WorkflowExecutionLog log = new WorkflowExecutionLog();
        WorkflowExecutionResponse expectedResponse = new WorkflowExecutionResponse(trackingId, UUID.randomUUID(), 1L, ExecutionStatus.IN_PROGRESS, "START", null, LocalDateTime.now(), null);

        given(executionRepository.findByExecutionTrackingId(trackingId)).willReturn(Optional.of(log));
        given(executionMapper.toResponse(log)).willReturn(expectedResponse);

        // When
        WorkflowExecutionResponse result = executionService.getExecutionByTrackingId(trackingId);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGetExecutionsByWorkflow() {
        // Given
        UUID workflowTrackingId = UUID.randomUUID();
        Workflow workflow = new Workflow();
        workflow.setId(10L);
        List<WorkflowExecutionLog> logs = List.of(new WorkflowExecutionLog());

        given(workflowRepository.findByTrackingId(workflowTrackingId)).willReturn(Optional.of(workflow));
        given(executionRepository.findByWorkflowId(10L)).willReturn(logs);
        given(executionMapper.toResponseList(logs)).willReturn(List.of(new WorkflowExecutionResponse(UUID.randomUUID(), workflowTrackingId, 1L, ExecutionStatus.IN_PROGRESS, "START", null, LocalDateTime.now(), null)));

        // When
        List<WorkflowExecutionResponse> results = executionService.getExecutionsByWorkflow(workflowTrackingId);

        // Then
        assertThat(results).hasSize(1);
    }

    @Test
    void shouldUpdateExecutionStatus() {
        // Given
        UUID trackingId = UUID.randomUUID();
        WorkflowExecutionLog log = new WorkflowExecutionLog();
        log.setStatus(ExecutionStatus.IN_PROGRESS);

        given(executionRepository.findByExecutionTrackingId(trackingId)).willReturn(Optional.of(log));
        given(executionRepository.save(any())).willReturn(log);
        given(executionMapper.toResponse(log)).willReturn(new WorkflowExecutionResponse(trackingId, UUID.randomUUID(), 1L, ExecutionStatus.COMPLETED, "END", null, LocalDateTime.now(), LocalDateTime.now()));

        // When
        WorkflowExecutionResponse result = executionService.updateExecutionStatus(trackingId, ExecutionStatus.COMPLETED, "END", null);

        // Then
        assertThat(log.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
        assertThat(log.getCompletedAt()).isNotNull();
        verify(executionRepository).save(log);
    }
}
