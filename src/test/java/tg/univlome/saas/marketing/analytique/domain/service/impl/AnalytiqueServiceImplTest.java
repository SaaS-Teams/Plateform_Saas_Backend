package tg.univlome.saas.marketing.analytique.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DashboardResponse;
import tg.univlome.saas.marketing.analytique.repositories.projections.DailyStatsProjection;
import tg.univlome.saas.marketing.campagne.domain.enums.CampagneStatus;
import tg.univlome.saas.marketing.campagne.repositories.CampagneRepository;
import tg.univlome.saas.marketing.email.domain.enums.EmailStatus;
import tg.univlome.saas.marketing.email.repositories.EmailLogRepository;

@ExtendWith(MockitoExtension.class)
class AnalytiqueServiceImplTest {

    @Mock
    private CampagneRepository campagneRepository;

    @Mock
    private EmailLogRepository emailLogRepository;

    @InjectMocks
    private AnalytiqueServiceImpl analytiqueService;

    @Test
    void getDashboardData_ShouldReturnCorrectlyCalculatedDashboard() {
        // Arrange
        when(campagneRepository.count()).thenReturn(10L);
        when(campagneRepository.countByStatut(CampagneStatus.BROUILLON)).thenReturn(2L);
        when(campagneRepository.countByStatut(CampagneStatus.PROGRAMMEE)).thenReturn(3L);
        when(campagneRepository.countByStatut(CampagneStatus.TERMINEE)).thenReturn(5L);

        when(emailLogRepository.count()).thenReturn(200L);
        when(emailLogRepository.countByStatus(EmailStatus.SENT)).thenReturn(190L);
        when(emailLogRepository.countByStatus(EmailStatus.FAILED)).thenReturn(10L);

        DailyStatsProjection mockProjection = mock(DailyStatsProjection.class);
        when(mockProjection.getDate()).thenReturn(LocalDateTime.of(2026, 7, 5, 0, 0));
        when(mockProjection.getSuccessCount()).thenReturn(50L);
        when(mockProjection.getFailureCount()).thenReturn(2L);

        when(emailLogRepository.getDailyStatistics()).thenReturn(List.of(mockProjection));

        // Act
        DashboardResponse result = analytiqueService.getDashboardData();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.totalCampaigns()).isEqualTo(10L);
        assertThat(result.draftCampaigns()).isEqualTo(2L);
        assertThat(result.totalEmailsSent()).isEqualTo(200L);
        
        // (190 / 200) * 100 = 95.0
        assertThat(result.overallSuccessRate()).isEqualTo(95.0);

        assertThat(result.dailyStatsList()).hasSize(1);
        assertThat(result.dailyStatsList().getFirst().date()).isEqualTo(LocalDateTime.of(2026, 7, 5, 0, 0));
        assertThat(result.dailyStatsList().getFirst().successCount()).isEqualTo(50L);
    }
}
