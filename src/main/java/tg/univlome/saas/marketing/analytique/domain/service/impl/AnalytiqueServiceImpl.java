package tg.univlome.saas.marketing.analytique.domain.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DailyStats;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DashboardResponse;
import tg.univlome.saas.marketing.analytique.domain.service.AnalytiqueService;
import tg.univlome.saas.marketing.analytique.repositories.projections.DailyStatsProjection;
import tg.univlome.saas.marketing.campagne.domain.enums.CampagneStatus;
import tg.univlome.saas.marketing.campagne.repositories.CampagneRepository;
import tg.univlome.saas.marketing.email.domain.enums.EmailStatus;
import tg.univlome.saas.marketing.email.repositories.EmailLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalytiqueServiceImpl implements AnalytiqueService {

    private final CampagneRepository campagneRepository;
    private final EmailLogRepository emailLogRepository;

    private static final double PERCENTAGE_MULTIPLIER = 100.0;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData() {
        log.info("Génération des données du tableau de bord analytique");

        long totalCampaigns = campagneRepository.count();
        long draftCampaigns = campagneRepository.countByStatut(CampagneStatus.BROUILLON);
        long scheduledCampaigns = campagneRepository.countByStatut(CampagneStatus.PROGRAMMEE);
        long sentCampaigns = campagneRepository.countByStatut(CampagneStatus.TERMINEE);

        long totalEmailsSent = emailLogRepository.count();
        long totalSuccessfulEmails = emailLogRepository.countByStatus(EmailStatus.SENT);
        long totalFailedEmails = emailLogRepository.countByStatus(EmailStatus.FAILED);

        double overallSuccessRate = 0.0;
        if (totalEmailsSent > 0) {
            overallSuccessRate = ((double) totalSuccessfulEmails / totalEmailsSent) * PERCENTAGE_MULTIPLIER;
            overallSuccessRate = Math.round(overallSuccessRate * PERCENTAGE_MULTIPLIER) / PERCENTAGE_MULTIPLIER;
        }

        List<DailyStatsProjection> projections = emailLogRepository.getDailyStatistics();

        List<DailyStats> dailyStatsList = projections.stream()
                .map(p -> new DailyStats(
                        p.getDate(),
                        p.getSuccessCount(),
                        p.getFailureCount()
                ))
                .collect(Collectors.toList());

        return new DashboardResponse(
                totalCampaigns,
                draftCampaigns,
                scheduledCampaigns,
                sentCampaigns,
                totalEmailsSent,
                totalSuccessfulEmails,
                totalFailedEmails,
                overallSuccessRate,
                dailyStatsList
        );
    }
}
