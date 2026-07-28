package tg.univlome.saas.web.controllers.mobile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.mobile.onboarding.domain.services.MobileOnboardingService;
import tg.univlome.saas.web.dtos.mobile.OnboardingResponse;
import tg.univlome.saas.web.dtos.mobile.OnboardingStep1Request;
import tg.univlome.saas.web.dtos.mobile.OnboardingStep2Request;

/**
 * API REST publique pour le tunnel d'onboarding mobile séquentiel.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mobile/onboarding")
@RequiredArgsConstructor
@Tag(name = "Mobile Onboarding", description = "Endpoints du tunnel d'inscription séquentiel mobile")
public class MobileOnboardingController {

    private final MobileOnboardingService mobileOnboardingService;

    @PostMapping("/step1")
    @Operation(summary = "Étape 1 : Inscription et identifiants de connexion")
    public ResponseEntity<OnboardingResponse> processStep1(
            @Valid @RequestBody OnboardingStep1Request request) {
        log.info("[API MOBILE] Requête Étape 1 pour email: {}", request.email());
        OnboardingResponse response = mobileOnboardingService.processStep1(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/step2/{uuid}")
    @Operation(summary = "Étape 2 : Nom et prénom")
    public ResponseEntity<OnboardingResponse> processStep2(
            @PathVariable("uuid") UUID onboardingUuid,
            @Valid @RequestBody OnboardingStep2Request request) {
        log.info("[API MOBILE] Requête Étape 2 pour uuid: {}", onboardingUuid);
        OnboardingResponse response = mobileOnboardingService.processStep2(onboardingUuid, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/finalize/{uuid}")
    @Operation(summary = "Étape 3 : Finalisation de l'onboarding mobile")
    public ResponseEntity<OnboardingResponse> finalizeOnboarding(
            @PathVariable("uuid") UUID onboardingUuid) {
        log.info("[API MOBILE] Requête Finalisation pour uuid: {}", onboardingUuid);
        OnboardingResponse response = mobileOnboardingService.finalizeOnboarding(onboardingUuid);
        return ResponseEntity.ok(response);
    }
}
