package tg.univlome.saas.marketing.contact.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.contact.application.dtos.request.SegmentRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ContactResponse;
import tg.univlome.saas.marketing.contact.application.dtos.response.SegmentResponse;
import tg.univlome.saas.marketing.contact.domain.services.SegmentService;

@RestController
@RequestMapping("/segments")
@RequiredArgsConstructor
@Tag(name = "Segments (Ciblage)",
        description = "API de gestion des segments de contacts et des liaisons contact-segment")
public class SegmentController {

    private final SegmentService segmentService;

    @Operation(summary = "Créer un nouveau contact",
            description = "Crée un contact et initialise son journal "
                    + "de consentement RGPD.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Segment créé avec succès",
                content = @Content(schema = @Schema(implementation = SegmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou nom déjà pris", content = @Content)
    })
    @PostMapping
    public ResponseEntity<SegmentResponse> createSegment(@Valid @RequestBody SegmentRequest request) {
        SegmentResponse response = segmentService.createSegment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Récupérer un segment", description = "Obtient les détails d'un segment via son UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Segment trouvé",
                content = @Content(schema = @Schema(implementation = SegmentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Segment introuvable", content = @Content)
    })
    @GetMapping("/{trackingId}")
    public ResponseEntity<SegmentResponse> getSegment(
            @Parameter(description = "UUID public du segment") @PathVariable UUID trackingId) {
        SegmentResponse response = segmentService.getSegmentByTrackingId(trackingId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lister les segments", description = "Récupère une liste paginée de tous les segments.")
    @ApiResponse(responseCode = "200", description = "Liste paginée renvoyée avec succès")
    @GetMapping
    public ResponseEntity<Page<SegmentResponse>> getAllSegments(Pageable pageable) {
        Page<SegmentResponse> response = segmentService.getAllSegments(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ajouter un contact à un segment", description = "Lie un contact existant à un segment existant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact ajouté avec succès"),
            @ApiResponse(responseCode = "404", description = "Contact ou segment introuvable")
    })
    @PostMapping("/{segmentTrackingId}/contacts/{contactTrackingId}")
    public ResponseEntity<Void> addContactToSegment(
            @Parameter(description = "UUID du segment") @PathVariable UUID segmentTrackingId,
            @Parameter(description = "UUID du contact") @PathVariable UUID contactTrackingId) {
        segmentService.addContactToSegment(contactTrackingId, segmentTrackingId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Retirer un contact d'un segment", description = "Supprime la liaison entre un contact et un segment.")
    @ApiResponse(responseCode = "204", description = "Liaison supprimée avec succès")
    @DeleteMapping("/{segmentTrackingId}/contacts/{contactTrackingId}")
    public ResponseEntity<Void> removeContactFromSegment(
            @Parameter(description = "UUID du segment") @PathVariable UUID segmentTrackingId,
            @Parameter(description = "UUID du contact") @PathVariable UUID contactTrackingId) {
        segmentService.removeContactFromSegment(contactTrackingId, segmentTrackingId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lister les contacts d'un segment",
            description = "Récupère la liste paginée de tous les contacts appartenant à un segment spécifique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste paginée renvoyée avec succès"),
            @ApiResponse(responseCode = "404", description = "Segment introuvable")
    })
    @GetMapping("/{segmentTrackingId}/contacts")
    public ResponseEntity<Page<ContactResponse>> getContactsBySegment(
            @Parameter(description = "UUID du segment") @PathVariable UUID segmentTrackingId,
            Pageable pageable) {
        Page<ContactResponse> response = segmentService.getContactsBySegment(segmentTrackingId, pageable);
        return ResponseEntity.ok(response);
    }
}
