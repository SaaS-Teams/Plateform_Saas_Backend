package tg.univlome.saas.marketing.analytique.domain.services;

import java.util.UUID;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DashboardResponse;
import tg.univlome.saas.marketing.analytique.application.dtos.responses.WorkflowAnalyticsResponse;

public interface DashboardService {

    DashboardResponse getGlobalDashboardStats();

    WorkflowAnalyticsResponse getWorkflowAnalytics(UUID trackingId);
}
