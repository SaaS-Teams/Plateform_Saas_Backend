package tg.univlome.saas.mobile.onboarding.domain.services;

import java.util.UUID;
import tg.univlome.saas.web.dtos.mobile.OnboardingResponse;
import tg.univlome.saas.web.dtos.mobile.OnboardingStep1Request;
import tg.univlome.saas.web.dtos.mobile.OnboardingStep2Request;

/**
 * Contrat du service de gestion du tunnel d'onboarding mobile.
 */
public interface MobileOnboardingService {

    /**
     * Étape 1 : Inscription initiale (création compte + identifiants).
     */
    OnboardingResponse processStep1(OnboardingStep1Request request);

    /**
     * Étape 2 : Complément d'informations personnelles (nom & prénom).
     */
    OnboardingResponse processStep2(UUID onboardingUuid, OnboardingStep2Request request);

    /**
     * Finalisation de l'onboarding : passage du statut onboardingCompleted à true.
     */
    OnboardingResponse finalizeOnboarding(UUID onboardingUuid);
}
