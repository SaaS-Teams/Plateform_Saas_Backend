package tg.univlome.saas.marketing.analytique.domain.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.analytique.application.dtos.request.SendGridEventRequest;
import tg.univlome.saas.marketing.analytique.domain.enums.EmailEventType;
import tg.univlome.saas.marketing.analytique.domain.models.EmailEventLog;
import tg.univlome.saas.marketing.analytique.repositories.EmailEventLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EmailEventLogRepository repository;

    public void processSendGridEvent(SendGridEventRequest request) {
        // 1. On utilise request.trackingId() au lieu de getTrackingId() car c'est un record
        if (request.trackingId() == null) {
            log.warn("Événement ignoré : Aucun trackingId trouvé pour l'e-mail {}", request.email());
            return;
        }

        // 2. Conversion du type SendGrid vers notre Enum interne
        EmailEventType type = mapSendGridEventToEnum(request.event());
        if (type == null) {
            return; // On ignore les événements qui ne nous intéressent pas
        }

        // 3. Conversion du Timestamp Unix en LocalDateTime
        LocalDateTime eventTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(request.timestamp()),
                ZoneId.systemDefault()
        );

        // 4. Création et sauvegarde du document dans MongoDB
        EmailEventLog logEntry = EmailEventLog.builder()
                .trackingId(request.trackingId())
                .contactEmail(request.email())
                .eventType(type)
                .timestamp(eventTime)
                .ipAddress(request.ip())
                .userAgent(request.useragent())
                .clickedUrl(request.url())
                .build();

        repository.save(logEntry);
        log.info("📊 Événement [{}] enregistré pour le workflow [{}]", type, request.trackingId());
    }

    private EmailEventType mapSendGridEventToEnum(String sendGridEvent) {
        if (sendGridEvent == null) {
            return null;
        }
        return switch (sendGridEvent.toLowerCase()) {
            case "delivered" -> EmailEventType.DELIVERED;
            case "open" -> EmailEventType.OPENED;
            case "click" -> EmailEventType.CLICKED;
            case "bounce" -> EmailEventType.BOUNCED;
            case "spamreport" -> EmailEventType.SPAM_REPORT;
            default -> null; // Les autres événements (ex: processed, deferred) sont ignorés
        };
    }
}
