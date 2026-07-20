package tg.univlome.saas.marketing.automation.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.marketing.automation.domain.models.WorkflowExecutionLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowExecutionLogRepository extends JpaRepository<WorkflowExecutionLog, Long> {
    Optional<WorkflowExecutionLog> findByExecutionTrackingId(UUID executionTrackingId);
    List<WorkflowExecutionLog> findByWorkflowId(Long workflowId);
    List<WorkflowExecutionLog> findByContactId(Long contactId);
}