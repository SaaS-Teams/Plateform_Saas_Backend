package tg.univlome.saas.marketing.automation.domain.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowEngineService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowConsumerTest {

    @Mock
    private WorkflowEngineService engineService;

    @InjectMocks
    private WorkflowConsumer workflowConsumer;

    @Test
    void shouldDelegateMessageToEngineSuccessfully() {
        // Given
        UUID trackingId = UUID.randomUUID();
        WorkflowStepMessage message = new WorkflowStepMessage(trackingId, "node_1", "SEND_EMAIL");

        // When
        workflowConsumer.receiveStepMessage(message);

        // Then
        verify(engineService, times(1)).processStep(message);
    }

    @Test
    void shouldThrowExceptionWhenEngineFails() {
        // Given
        UUID trackingId = UUID.randomUUID();
        WorkflowStepMessage message = new WorkflowStepMessage(trackingId, "node_1", "SEND_EMAIL");
        
        doThrow(new RuntimeException("Test Exception")).when(engineService).processStep(message);

        // When / Then
        assertThatThrownBy(() -> workflowConsumer.receiveStepMessage(message))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test Exception");
                
        verify(engineService, times(1)).processStep(message);
    }
}
