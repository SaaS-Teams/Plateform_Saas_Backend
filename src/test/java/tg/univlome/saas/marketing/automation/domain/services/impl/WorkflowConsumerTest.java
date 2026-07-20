package tg.univlome.saas.marketing.automation.domain.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class WorkflowConsumerTest {

    @InjectMocks
    private WorkflowConsumer workflowConsumer;

    @Test
    void shouldReceiveStepMessageSuccessfully() {
        // Given
        UUID trackingId = UUID.randomUUID();
        WorkflowStepMessage message = new WorkflowStepMessage(trackingId, "node_1", "SEND_EMAIL");

        // When / Then
        // As there is no external dependency yet, we just ensure no exception is thrown
        assertThatCode(() -> workflowConsumer.receiveStepMessage(message))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenMessageProcessingFails() {
        // Given
        // This is a placeholder test. Since the consumer currently just logs and doesn't throw on normal inputs,
        // to test the catch block, we would typically mock a service it calls. 
        // For now, if we pass null, it might throw a NullPointerException depending on logging or other usage.
        
        // When / Then
        assertThatThrownBy(() -> workflowConsumer.receiveStepMessage(null))
                .isInstanceOf(NullPointerException.class);
    }
}
