package tg.univlome.saas.marketing.contact.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.univlome.saas.marketing.contact.application.dtos.request.ContactRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ContactResponse;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentStatus;
import tg.univlome.saas.marketing.contact.domain.services.ContactService;

import java.util.UUID;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts (CRM)", description = "API de gestion des contacts, de leur profil et de leur statut RGPD")
public class ContactController {

    private final ContactService contactService;

    @Operation(summary = "Créer un nouveau contact", description = "Crée un contact et initialise son journal de consentement RGPD.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contact créé avec succès", content = @Content(schema = @Schema(implementation = ContactResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà existant", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            @Valid @RequestBody ContactRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        ContactResponse response = contactService.createContact(request, ipAddress);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Mettre à jour un contact", description = "Met à jour les informations d'un contact existant via son UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact mis à jour avec succès", content = @Content(schema = @Schema(implementation = ContactResponse.class))),
            @ApiResponse(responseCode = "404", description = "Contact introuvable", content = @Content)
    })
    @PutMapping("/{trackingId}")
    public ResponseEntity<ContactResponse> updateContact(
            @Parameter(description = "UUID public du contact") @PathVariable UUID trackingId,
            @Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.updateContact(trackingId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Récupérer un contact", description = "Obtient les détails complets d'un contact via son UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact trouvé", content = @Content(schema = @Schema(implementation = ContactResponse.class))),
            @ApiResponse(responseCode = "404", description = "Contact introuvable", content = @Content)
    })
    @GetMapping("/{trackingId}")
    public ResponseEntity<ContactResponse> getContact(
            @Parameter(description = "UUID public du contact") @PathVariable UUID trackingId) {
        ContactResponse response = contactService.getContactByTrackingId(trackingId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lister les contacts", description = "Récupère une liste paginée de tous les contacts.")
    @ApiResponse(responseCode = "200", description = "Liste paginée renvoyée avec succès")
    @GetMapping
    public ResponseEntity<Page<ContactResponse>> getAllContacts(
            @Parameter(description = "Paramètres de pagination (page, size, sort)") Pageable pageable) {
        Page<ContactResponse> response = contactService.getAllContacts(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Modifier le statut de consentement", description = "Met à jour le statut RGPD (OPT_IN, OPT_OUT) et ajoute une entrée dans l'historique de consentement.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès", content = @Content(schema = @Schema(implementation = ContactResponse.class))),
            @ApiResponse(responseCode = "404", description = "Contact introuvable", content = @Content)
    })
    @PatchMapping("/{trackingId}/consent")
    public ResponseEntity<ContactResponse> changeConsentStatus(
            @Parameter(description = "UUID public du contact") @PathVariable UUID trackingId,
            @Parameter(description = "Nouveau statut de consentement") @RequestParam ConsentStatus status,
            HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        ContactResponse response = contactService.changeConsentStatus(trackingId, status, ipAddress);
        return ResponseEntity.ok(response);
    }
}