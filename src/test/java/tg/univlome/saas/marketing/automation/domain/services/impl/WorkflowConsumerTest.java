package tg.univlome.saas.marketing.automation.domain.services.impl;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;
import tg.univlome.saas.marketing.automation.domain.models.Workflow;
import tg.univlome.saas.marketing.automation.domain.models.WorkflowExecutionLog;
import tg.univlome.saas.marketing.automation.domain.services.RateLimitingService;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowEngineService;
import tg.univlome.saas.marketing.automation.repositories.WorkflowExecutionLogRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowConsumerTest {

    @Mock
    private WorkflowEngineService engineService;

    @Mock
    private RateLimitingService rateLimitingService;

    @Mock
    private WorkflowExecutionLogRepository executionLogRepository;

    @Mock
    private Channel channel;

    @InjectMocks
    private WorkflowConsumer workflowConsumer;

    private UUID trackingId;
    private UUID workflowTrackingId;
    private WorkflowStepMessage message;
    private WorkflowExecutionLog executionLog;

    @BeforeEach
    void setUp() {
        trackingId = UUID.randomUUID();
        workflowTrackingId = UUID.randomUUID();
        message = new WorkflowStepMessage(trackingId, "node_1", "SEND_EMAIL");

        Workflow workflow = new Workflow();
        workflow.setTrackingId(workflowTrackingId);

        executionLog = new WorkflowExecutionLog();
        executionLog.setWorkflow(workflow);
    }

    @Test
    void shouldProcessMessageWhenRateLimitAllowed() throws Exception {
        // Given
        when(executionLogRepository.findByExecutionTrackingId(trackingId)).thenReturn(Optional.of(executionLog));
        when(rateLimitingService.isAllowed(eq(workflowTrackingId.toString()), anyInt())).thenReturn(true);

        // When
        workflowConsumer.receiveStepMessage(message, channel, 123L);

        // Then
        verify(engineService, times(1)).processStep(message);
        verify(channel, times(1)).basicAck(123L, false);
    }

    @Test
    void shouldRequeueMessageWhenRateLimitReached() throws Exception {
        // Given
        when(executionLogRepository.findByExecutionTrackingId(trackingId)).thenReturn(Optional.of(executionLog));
        when(rateLimitingService.isAllowed(eq(workflowTrackingId.toString()), anyInt())).thenReturn(false);

        // When
        workflowConsumer.receiveStepMessage(message, channel, 123L);

        // Then
        verify(engineService, never()).processStep(any());
        // Requeue parameter is true when rate limit is reached
        verify(channel, times(1)).basicNack(123L, false, true);
    }

    @Test
    void shouldRejectMessageOnException() throws Exception {
        // Given
        when(executionLogRepository.findByExecutionTrackingId(trackingId)).thenReturn(Optional.of(executionLog));
        when(rateLimitingService.isAllowed(eq(workflowTrackingId.toString()), anyInt())).thenReturn(true);
        doThrow(new RuntimeException("Test Exception")).when(engineService).processStep(message);

        // When
        workflowConsumer.receiveStepMessage(message, channel, 123L);

        // Then
        verify(engineService, times(1)).processStep(message);
        // Requeue parameter is false when engine crashes
        verify(channel, times(1)).basicNack(123L, false, false);
    }
}
