package tg.univlome.saas.marketing.automation.domain.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowRequest;
import tg.univlome.saas.marketing.automation.application.dtos.responses.WorkflowResponse;
import tg.univlome.saas.marketing.automation.application.mappers.WorkflowMapper;
import tg.univlome.saas.marketing.automation.domain.enums.WorkflowStatus;
import tg.univlome.saas.marketing.automation.domain.models.Workflow;
import tg.univlome.saas.marketing.automation.repositories.WorkflowRepository;
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
class WorkflowServiceImplTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowMapper workflowMapper;

    @InjectMocks
    private WorkflowServiceImpl workflowService;

    @Test
    void shouldCreateWorkflowSuccessfully() {
        // Given
        WorkflowRequest request = new WorkflowRequest("Test Workflow", "Description", WorkflowStatus.ACTIVE, "MANUAL", "{}");
        Workflow workflow = new Workflow();
        workflow.setId(1L);
        workflow.setTrackingId(UUID.randomUUID());

        WorkflowResponse expectedResponse = new WorkflowResponse(workflow.getTrackingId(), "Test Workflow", "Description", WorkflowStatus.ACTIVE, "MANUAL", "{}", LocalDateTime.now(), LocalDateTime.now());

        given(workflowMapper.toEntity(request)).willReturn(workflow);
        given(workflowRepository.save(workflow)).willReturn(workflow);
        given(workflowMapper.toResponse(workflow)).willReturn(expectedResponse);

        // When
        WorkflowResponse result = workflowService.createWorkflow(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.trackingId()).isEqualTo(workflow.getTrackingId());
        verify(workflowRepository).save(workflow);
    }

    @Test
    void shouldReturnWorkflowWhenExists() {
        // Given
        UUID trackingId = UUID.randomUUID();
        Workflow workflow = new Workflow();
        workflow.setTrackingId(trackingId);

        WorkflowResponse expectedResponse = new WorkflowResponse(trackingId, "Test Workflow", "Description", WorkflowStatus.ACTIVE, "MANUAL", "{}", LocalDateTime.now(), LocalDateTime.now());

        given(workflowRepository.findByTrackingId(trackingId)).willReturn(Optional.of(workflow));
        given(workflowMapper.toResponse(workflow)).willReturn(expectedResponse);

        // When
        WorkflowResponse result = workflowService.getWorkflowByTrackingId(trackingId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.trackingId()).isEqualTo(trackingId);
    }

    @Test
    void shouldThrowResourceNotFoundWhenWorkflowDoesNotExist() {
        // Given
        UUID trackingId = UUID.randomUUID();
        given(workflowRepository.findByTrackingId(trackingId)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> workflowService.getWorkflowByTrackingId(trackingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Scénario introuvable");
    }

    @Test
    void shouldReturnAllWorkflows() {
        // Given
        Workflow workflow = new Workflow();
        List<Workflow> workflows = List.of(workflow);
        WorkflowResponse response = new WorkflowResponse(UUID.randomUUID(), "Test", "Desc", WorkflowStatus.ACTIVE, "MANUAL", "{}", LocalDateTime.now(), LocalDateTime.now());
        
        given(workflowRepository.findAll()).willReturn(workflows);
        given(workflowMapper.toResponseList(workflows)).willReturn(List.of(response));

        // When
        List<WorkflowResponse> results = workflowService.getAllWorkflows();

        // Then
        assertThat(results).hasSize(1);
    }

    @Test
    void shouldUpdateWorkflowWhenExists() {
        // Given
        UUID trackingId = UUID.randomUUID();
        WorkflowRequest request = new WorkflowRequest("Updated Name", "Updated Desc", WorkflowStatus.INACTIVE, "MANUAL", "{}");
        Workflow existingWorkflow = new Workflow();
        existingWorkflow.setTrackingId(trackingId);
        
        WorkflowResponse expectedResponse = new WorkflowResponse(trackingId, "Updated Name", "Updated Desc", WorkflowStatus.INACTIVE, "MANUAL", "{}", LocalDateTime.now(), LocalDateTime.now());

        given(workflowRepository.findByTrackingId(trackingId)).willReturn(Optional.of(existingWorkflow));
        given(workflowRepository.save(any(Workflow.class))).willReturn(existingWorkflow);
        given(workflowMapper.toResponse(existingWorkflow)).willReturn(expectedResponse);

        // When
        WorkflowResponse result = workflowService.updateWorkflow(trackingId, request);

        // Then
        assertThat(result.name()).isEqualTo("Updated Name");
        verify(workflowRepository).save(existingWorkflow);
    }

    @Test
    void shouldDeleteWorkflowWhenExists() {
        // Given
        UUID trackingId = UUID.randomUUID();
        Workflow existingWorkflow = new Workflow();
        given(workflowRepository.findByTrackingId(trackingId)).willReturn(Optional.of(existingWorkflow));

        // When
        workflowService.deleteWorkflow(trackingId);

        // Then
        verify(workflowRepository).delete(existingWorkflow);
    }
}
