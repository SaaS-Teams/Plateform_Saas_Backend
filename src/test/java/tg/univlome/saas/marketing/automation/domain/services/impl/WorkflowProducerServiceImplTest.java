package tg.univlome.saas.marketing.automation.domain.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkflowProducerServiceImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private WorkflowProducerServiceImpl workflowProducerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workflowProducerService, "exchange", "test-exchange");
        ReflectionTestUtils.setField(workflowProducerService, "routingKey", "test.routing.key");
    }

    @Test
    void shouldSendStepToQueue() {
        // Given
        UUID trackingId = UUID.randomUUID();
        WorkflowStepMessage message = new WorkflowStepMessage(trackingId, "node_1", "SEND_EMAIL");

        // When
        workflowProducerService.sendStepToQueue(message);

        // Then
        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("test.routing.key"), eq(message));
    }
}
