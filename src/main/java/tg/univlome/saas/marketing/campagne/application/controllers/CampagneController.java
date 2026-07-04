package tg.univlome.saas.marketing.campagne.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.campagne.application.dtos.request.CampagneRequest;
import tg.univlome.saas.marketing.campagne.application.dtos.response.CampagneResponse;
import tg.univlome.saas.marketing.campagne.domain.services.CampagneService;

@RestController
@RequestMapping("/api/campagnes")
@RequiredArgsConstructor
@Tag(
        name = "Campagnes",
        description = "Endpoints pour la gestion du cycle de vie des campagnes marketing. "
                + "L'accès aux ressources est sécurisé exclusivement via le Tracking ID (UUID)."
)
public class CampagneController {

    private final CampagneService campagneService;

    @PostMapping
    @Operation(
            summary = "Créer une nouvelle campagne",
            description = "Enregistre une nouvelle campagne en base de données. "
                    + "Le statut initial sera défini automatiquement sur BROUILLON."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "La campagne a été créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides (ex: nom manquant, date dans le passé)")
    })
    public ResponseEntity<CampagneResponse> createCampagne(@Valid @RequestBody CampagneRequest request) {
        CampagneResponse response = campagneService.createCampagne(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Lister toutes les campagnes",
            description = "Retourne la liste complète des campagnes marketing enregistrées dans le système."
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    public ResponseEntity<List<CampagneResponse>> getAllCampagnes() {
        return ResponseEntity.ok(campagneService.getAllCampagnes());
    }

    @GetMapping("/{trackingId}")
    @Operation(
            summary = "Obtenir les détails d'une campagne",
            description = "Récupère les informations complètes d'une campagne "
                    + "spécifique en utilisant son identifiant de suivi unique (UUID)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campagne trouvée et retournée avec succès"),
            @ApiResponse(responseCode = "404", description = "Aucune campagne trouvée avec ce Tracking ID")
    })

    public ResponseEntity<CampagneResponse> getCampagneByTrackingId(
            @Parameter(description = "L'identifiant unique UUID de la campagne", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID trackingId) {
        return ResponseEntity.ok(campagneService.getCampagneByTrackingId(trackingId));
    }

    @PutMapping("/{trackingId}")
    @Operation(
            summary = "Modifier une campagne existante",
            description = "Met à jour les informations d'une campagne (nom, sujet, contenu, "
                    + "date de planification). Seules les campagnes n'ayant pas encore "
                    + "été envoyées devraient être modifiées."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "La campagne a été mise à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Données de mise à jour invalides"),
            @ApiResponse(responseCode = "404", description = "Aucune campagne trouvée avec ce Tracking ID")
    })
    public ResponseEntity<CampagneResponse> updateCampagne(
            @Parameter(description = "L'identifiant unique UUID de la campagne à modifier")
            @PathVariable UUID trackingId,
            @Valid @RequestBody CampagneRequest request) {
        return ResponseEntity.ok(campagneService.updateCampagne(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(
            summary = "Supprimer une campagne",
            description = "Supprime définitivement une campagne du système."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "La campagne a été supprimée avec succès (Aucun contenu retourné)"),
            @ApiResponse(responseCode = "404", description = "Aucune campagne trouvée avec ce Tracking ID")
    })
    public ResponseEntity<Void> deleteCampagne(
            @Parameter(description = "L'identifiant unique UUID de la campagne à supprimer")
            @PathVariable UUID trackingId) {
        campagneService.deleteCampagne(trackingId);
        return ResponseEntity.noContent().build();
    }
}
