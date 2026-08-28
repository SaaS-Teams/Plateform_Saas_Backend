package tg.univlome.saas.marketing.analytique.domain.service.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DailyStats;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DashboardResponse;
import tg.univlome.saas.marketing.analytique.application.dtos.responses.WorkflowAnalyticsResponse;
import tg.univlome.saas.marketing.analytique.domain.enums.EmailEventType;
import tg.univlome.saas.marketing.analytique.domain.services.DashboardService;
import tg.univlome.saas.marketing.analytique.repositories.EmailEventLogRepository;
import tg.univlome.saas.marketing.analytique.repositories.projections.DailyStatsProjection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmailEventLogRepository repository;

    @Override
    public DashboardResponse getGlobalDashboardStats() {
        log.info("Calcul des statistiques globales du tableau de bord analytique...");

        long totalSent = repository.countByEventType(EmailEventType.DELIVERED);
        long totalOpened = repository.countByEventType(EmailEventType.OPENED);
        long totalClicked = repository.countByEventType(EmailEventType.CLICKED);
        long totalBounced = repository.countByEventType(EmailEventType.BOUNCED);

        // Calcul des taux avec protection contre la division par zéro
        double openRate = totalSent > 0 ? (double) totalOpened / totalSent * 100.0 : 0.0;
        double clickRate = totalSent > 0 ? (double) totalClicked / totalSent * 100.0 : 0.0;

        openRate = Math.round(openRate * 100.0) / 100.0;
        clickRate = Math.round(clickRate * 100.0) / 100.0;

        // Récupération et cartographie des statistiques journalières pour la série temporelle
        List<DailyStatsProjection> projections = repository.findDailyStatsGroupedByDate();
        List<DailyStats> chartData = (projections != null && !projections.isEmpty())
                ? projections.stream()
                .map(p -> new DailyStats(
                        parseDateSafely(p.getDate()),
                        p.getOpens(),
                        p.getClicks()
                ))
                .toList()
                : Collections.emptyList();

        return new DashboardResponse(
                totalSent,
                totalOpened,
                totalClicked,
                totalBounced,
                openRate,
                clickRate,
                chartData
        );
    }

    @Override
    public WorkflowAnalyticsResponse getWorkflowAnalytics(UUID trackingId) {
        log.info("Calcul des statistiques analytiques pour le workflow : {}", trackingId);

        long sent = repository.countByTrackingIdAndEventType(trackingId, EmailEventType.DELIVERED);
        long opened = repository.countByTrackingIdAndEventType(trackingId, EmailEventType.OPENED);
        long clicked = repository.countByTrackingIdAndEventType(trackingId, EmailEventType.CLICKED);
        long bounced = repository.countByTrackingIdAndEventType(trackingId, EmailEventType.BOUNCED);

        double openRate = sent > 0 ? (double) opened / sent * 100.0 : 0.0;
        double clickRate = sent > 0 ? (double) clicked / sent * 100.0 : 0.0;
        double bounceRate = sent > 0 ? (double) bounced / sent * 100.0 : 0.0;

        return new WorkflowAnalyticsResponse(
                trackingId,
                sent,
                opened,
                clicked,
                bounced,
                Math.round(openRate * 100.0) / 100.0,
                Math.round(clickRate * 100.0) / 100.0,
                Math.round(bounceRate * 100.0) / 100.0
        );
    }

    private LocalDate parseDateSafely(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            log.warn("Impossible de parser la date [{}] : {}", dateStr, e.getMessage());
            return LocalDate.now();
        }
    }
}
