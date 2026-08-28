package tg.univlome.saas.marketing.analytique.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DashboardResponse;
import tg.univlome.saas.marketing.analytique.application.dtos.responses.WorkflowAnalyticsResponse;
import tg.univlome.saas.marketing.analytique.domain.services.DashboardService;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics & Dashboard", description = "API REST du tableau de bord d'analyse des campagnes et scénarios")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Récupération des statistiques globales du tableau de bord (KPIs & séries temporelles)")
    public ResponseEntity<DashboardResponse> getGlobalDashboardStats() {
        log.info("Récupération des statistiques globales du tableau de bord...");
        DashboardResponse stats = dashboardService.getGlobalDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/workflows/{trackingId}")
    @Operation(summary = "Récupération des statistiques d'un scénario de workflow spécifique via son UUID")
    public ResponseEntity<WorkflowAnalyticsResponse> getWorkflowAnalytics(@PathVariable UUID trackingId) {
        log.info("Récupération des statistiques analytiques pour le workflow : {}", trackingId);
        WorkflowAnalyticsResponse analytics = dashboardService.getWorkflowAnalytics(trackingId);
        return ResponseEntity.ok(analytics);
    }
}
