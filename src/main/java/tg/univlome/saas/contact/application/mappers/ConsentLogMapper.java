package tg.univlome.saas.marketing.contact.application.mappers;

import org.springframework.stereotype.Component;
import tg.univlome.saas.marketing.contact.application.dtos.response.ConsentLogResponse;
import tg.univlome.saas.marketing.contact.domain.models.ConsentLog;

import java.util.ArrayList;
import java.util.List;


@Component
public class ConsentLogMapper {

    public ConsentLogResponse toResponse(ConsentLog log) {
        if (log == null) {
            throw new IllegalArgumentException("Le log de consentement ne peut pas être null");
        }

        return new ConsentLogResponse(
                // On récupère le trackingId du contact parent !
                log.getContact().getTrackingId(),
                log.getTrackingId(),
                log.getAction(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }

    public List<ConsentLogResponse> toResponseList(List<ConsentLog> logs) {
        if (logs == null) {
            return new ArrayList<>();
        }
        return logs.stream()
                .map(this::toResponse)
                .toList();
    }
}
