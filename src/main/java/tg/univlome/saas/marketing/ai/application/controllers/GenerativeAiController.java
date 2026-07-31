package tg.univlome.saas.marketing.ai.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.ai.application.dtos.request.GenerateEmailRequest;
import tg.univlome.saas.marketing.ai.application.dtos.response.GeneratedContentResponse;
import tg.univlome.saas.marketing.ai.domain.services.GenerativeAiService;

@Slf4j
@RestController

@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "IA Générative", description = "Endpoints IA pour le copywriting d'emails marketing et la génération de contenu")
public class GenerativeAiController {

    private final GenerativeAiService generativeAiService;

    @PostMapping("/generate-email")
    @Operation(
            summary = "Générer le contenu d'un email marketing par IA",
            description = "Utilise l'intelligence artificielle pour générer un corps d'email "
                    + "optimisé pour la conversion d'après le sujet, le ton et l'audience cible."
    )
    public ResponseEntity<GeneratedContentResponse> generateEmail(@Valid @RequestBody GenerateEmailRequest request) {
        log.info("Demande de génération d'email IA reçue pour le sujet : {}", request.topic());
        GeneratedContentResponse response = generativeAiService.generateEmailContent(request);
        return ResponseEntity.ok(response);
    }
}
