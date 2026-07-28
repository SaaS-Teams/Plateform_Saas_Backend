package tg.univlome.saas.marketing.analytique.domain.service.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.analytique.application.dtos.responses.WorkflowAnalyticsResponse;
import tg.univlome.saas.marketing.analytique.domain.enums.EmailEventType;
import tg.univlome.saas.marketing.analytique.domain.services.DashboardService;
import tg.univlome.saas.marketing.analytique.repositories.EmailEventLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmailEventLogRepository repository;

    @Override
    public WorkflowAnalyticsResponse getWorkflowAnalytics(UUID trackingId) {
        long sent = repository.countByTrackingIdAndEventType(trackingId, EmailEventType.DELIVERED);
        long opened = repository.countByTrackingIdAndEventType(trackingId, EmailEventType.OPENED);
        long clicked = repository.countByTrackingIdAndEventType(trackingId, EmailEventType.CLICKED);
        long bounced = repository.countByTrackingIdAndEventType(trackingId, EmailEventType.BOUNCED);

        double openRate = sent > 0 ? (double) opened / sent * 100 : 0.0;
        double clickRate = sent > 0 ? (double) clicked / sent * 100 : 0.0;
        double bounceRate = sent > 0 ? (double) bounced / sent * 100 : 0.0;

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
}
