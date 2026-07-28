package tg.univlome.saas.marketing.analytique.application.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DashboardResponse;
import tg.univlome.saas.marketing.analytique.domain.service.AnalytiqueService;

@RestController
@RequestMapping("/api/analytiques")
@RequiredArgsConstructor
@Tag(name = "Analytique & Dashboard", 
        description = "Endpoints pour récupérer les statistiques globales et les données des graphiques du SaaS")
public class AnalytiqueController {

    private final AnalytiqueService analytiqueService;

    @GetMapping("/dashboard")
    @Operation(
            summary = "Obtenir les données du tableau de bord",
            description = "Retourne tous les KPIs (campagnes, emails, taux de réussite) " 
                    + "et les séries temporelles nécessaires pour construire les graphiques du front-end."
    )
    @ApiResponse(responseCode = "200", description = "Données analytiques récupérées avec succès")
    public ResponseEntity<DashboardResponse> getDashboardData() {
        return ResponseEntity.ok(analytiqueService.getDashboardData());
    }
}
