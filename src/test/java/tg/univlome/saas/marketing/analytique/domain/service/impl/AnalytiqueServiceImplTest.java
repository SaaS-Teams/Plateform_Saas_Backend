package tg.univlome.saas.marketing.analytique.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DashboardResponse;
import tg.univlome.saas.marketing.analytique.domain.enums.EmailEventType;
import tg.univlome.saas.marketing.analytique.repositories.EmailEventLogRepository;
import tg.univlome.saas.marketing.analytique.repositories.projections.DailyStatsProjection;

@ExtendWith(MockitoExtension.class)
class AnalytiqueServiceImplTest {

    @Mock
    private EmailEventLogRepository emailEventLogRepository;

    @InjectMocks
    private AnalytiqueServiceImpl analytiqueService;

    @Test
    void getDashboardData_ShouldReturnCorrectlyCalculatedDashboard() {
        // Arrange
        when(emailEventLogRepository.countByEventType(EmailEventType.DELIVERED)).thenReturn(200L);
        when(emailEventLogRepository.countByEventType(EmailEventType.OPENED)).thenReturn(100L);
        when(emailEventLogRepository.countByEventType(EmailEventType.CLICKED)).thenReturn(50L);
        when(emailEventLogRepository.countByEventType(EmailEventType.BOUNCED)).thenReturn(10L);

        DailyStatsProjection mockProjection = mock(DailyStatsProjection.class);
        when(mockProjection.getDate()).thenReturn("2026-07-05");
        when(mockProjection.getOpens()).thenReturn(50L);
        when(mockProjection.getClicks()).thenReturn(20L);

        when(emailEventLogRepository.findDailyStatsGroupedByDate()).thenReturn(List.of(mockProjection));

        // Act
        DashboardResponse result = analytiqueService.getDashboardData();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.totalSent()).isEqualTo(200L);
        assertThat(result.totalOpened()).isEqualTo(100L);
        assertThat(result.totalClicked()).isEqualTo(50L);
        assertThat(result.totalBounced()).isEqualTo(10L);

        // (100 / 200) * 100 = 50.0
        assertThat(result.openRate()).isEqualTo(50.0);
        // (50 / 200) * 100 = 25.0
        assertThat(result.clickRate()).isEqualTo(25.0);

        assertThat(result.chartData()).hasSize(1);
        assertThat(result.chartData().get(0).date()).isEqualTo(LocalDate.of(2026, 7, 5));
        assertThat(result.chartData().get(0).opens()).isEqualTo(50L);
        assertThat(result.chartData().get(0).clicks()).isEqualTo(20L);
    }
}
