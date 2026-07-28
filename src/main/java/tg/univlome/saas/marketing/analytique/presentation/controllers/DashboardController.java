package tg.univlome.saas.marketing.analytique.presentation.controllers;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.analytique.application.dtos.responses.WorkflowAnalyticsResponse;
import tg.univlome.saas.marketing.analytique.domain.services.DashboardService;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/workflows/{trackingId}")
    public ResponseEntity<WorkflowAnalyticsResponse> getWorkflowAnalytics(@PathVariable UUID trackingId) {
        log.info("Récupération des statistiques analytiques pour le workflow : {}", trackingId);
        WorkflowAnalyticsResponse analytics = dashboardService.getWorkflowAnalytics(trackingId);
        return ResponseEntity.ok(analytics);
    }
}
