package tg.univlome.saas.marketing.analytique.domain.service.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DailyStats;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DashboardResponse;
import tg.univlome.saas.marketing.analytique.domain.enums.EmailEventType;
import tg.univlome.saas.marketing.analytique.domain.service.AnalytiqueService;
import tg.univlome.saas.marketing.analytique.repositories.EmailEventLogRepository;
import tg.univlome.saas.marketing.analytique.repositories.projections.DailyStatsProjection;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalytiqueServiceImpl implements AnalytiqueService {

    private final EmailEventLogRepository emailEventLogRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData() {
        log.info("Génération des données du tableau de bord analytique via AnalytiqueService");

        long totalSent = emailEventLogRepository.countByEventType(EmailEventType.DELIVERED);
        long totalOpened = emailEventLogRepository.countByEventType(EmailEventType.OPENED);
        long totalClicked = emailEventLogRepository.countByEventType(EmailEventType.CLICKED);
        long totalBounced = emailEventLogRepository.countByEventType(EmailEventType.BOUNCED);

        double openRate = totalSent > 0 ? (double) totalOpened / totalSent * 100.0 : 0.0;
        double clickRate = totalSent > 0 ? (double) totalClicked / totalSent * 100.0 : 0.0;

        openRate = Math.round(openRate * 100.0) / 100.0;
        clickRate = Math.round(clickRate * 100.0) / 100.0;

        List<DailyStatsProjection> projections = emailEventLogRepository.findDailyStatsGroupedByDate();
        List<DailyStats> dailyStatsList = (projections != null && !projections.isEmpty())
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
                dailyStatsList
        );
    }

    private LocalDate parseDateSafely(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}
