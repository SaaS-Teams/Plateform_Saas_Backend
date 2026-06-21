package tg.univlome.saas.marketing.contact.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.contact.application.dtos.response.ConsentLogResponse;
import tg.univlome.saas.marketing.contact.domain.services.ConsentLogService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/contacts/{contactTrackingId}/consent-logs")
@RequiredArgsConstructor
@Tag(name = "Logs de Consentement (RGPD)", description = "API de consultation de l'historique des consentements (lecture seule)")
public class ConsentLogController {

    private final ConsentLogService consentLogService;

    @Operation(summary = "Historique RGPD d'un contact", description = "Récupère la liste paginée de toutes les actions de consentement (OPT_IN, OPT_OUT) effectuées par un contact.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique renvoyé avec succès"),
            @ApiResponse(responseCode = "404", description = "Contact introuvable")
    })
    @GetMapping
    public ResponseEntity<Page<ConsentLogResponse>> getConsentHistory(
            @Parameter(description = "UUID public du contact") @PathVariable UUID contactTrackingId,
            @Parameter(description = "Paramètres de pagination") Pageable pageable) {
        Page<ConsentLogResponse> response = consentLogService.getContactConsentHistory(contactTrackingId, pageable);
        return ResponseEntity.ok(response);
    }
}
