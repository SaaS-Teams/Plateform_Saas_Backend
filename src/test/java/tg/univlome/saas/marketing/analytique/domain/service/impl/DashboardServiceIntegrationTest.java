package tg.univlome.saas.marketing.analytique.domain.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.analytique.application.dtos.responses.WorkflowAnalyticsResponse;
import tg.univlome.saas.marketing.analytique.domain.enums.EmailEventType;
import tg.univlome.saas.marketing.analytique.repositories.EmailEventLogRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceIntegrationTest {

    @Mock
    private EmailEventLogRepository repository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void shouldCalculateAnalyticsCorrectly() {
        UUID trackingId = UUID.randomUUID();
        
        when(repository.countByTrackingIdAndEventType(trackingId, EmailEventType.DELIVERED)).thenReturn(10L);
        when(repository.countByTrackingIdAndEventType(trackingId, EmailEventType.OPENED)).thenReturn(5L);
        when(repository.countByTrackingIdAndEventType(trackingId, EmailEventType.CLICKED)).thenReturn(2L);
        when(repository.countByTrackingIdAndEventType(trackingId, EmailEventType.BOUNCED)).thenReturn(1L);

        // Act
        WorkflowAnalyticsResponse analytics = dashboardService.getWorkflowAnalytics(trackingId);

        // Assert
        assertThat(analytics.totalSent()).isEqualTo(10);
        assertThat(analytics.totalOpened()).isEqualTo(5);
        assertThat(analytics.totalClicked()).isEqualTo(2);
        assertThat(analytics.totalBounced()).isEqualTo(1);
        
        assertThat(analytics.openRate()).isEqualTo(50.0);
        assertThat(analytics.clickRate()).isEqualTo(20.0);
        assertThat(analytics.bounceRate()).isEqualTo(10.0);
    }
}
