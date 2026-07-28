package tg.univlome.saas.mobile.onboarding.domain.services.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.mobile.onboarding.domain.services.MobileOnboardingService;
import tg.univlome.saas.shared.domain.models.User;
import tg.univlome.saas.shared.exceptions.ConflictException;
import tg.univlome.saas.shared.exceptions.ResourceNotFoundException;
import tg.univlome.saas.shared.repositories.UserRepository;
import tg.univlome.saas.web.dtos.mobile.OnboardingResponse;
import tg.univlome.saas.web.dtos.mobile.OnboardingStep1Request;
import tg.univlome.saas.web.dtos.mobile.OnboardingStep2Request;

/**
 * Implémentation du service de gestion du tunnel d'onboarding mobile s'appuyant sur l'entité User.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MobileOnboardingServiceImpl implements MobileOnboardingService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OnboardingResponse processStep1(OnboardingStep1Request request) {
        log.info("[ONBOARDING MOBILE] Étape 1 pour l'email [{}]", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Un compte existe déjà avec l'adresse e-mail : " + request.email());
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User newUser = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .onboardingCompleted(false)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("[ONBOARDING MOBILE] Étape 1 réussie — onboardingUuid: [{}]", savedUser.getOnboardingUuid());

        return new OnboardingResponse(savedUser.getOnboardingUuid(), savedUser.getOnboardingCompleted());
    }

    @Override
    @Transactional
    public OnboardingResponse processStep2(UUID onboardingUuid, OnboardingStep2Request request) {
        log.info("[ONBOARDING MOBILE] Étape 2 pour onboardingUuid [{}]", onboardingUuid);

        User user = userRepository.findByOnboardingUuid(onboardingUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouvé pour l'onboardingUuid : " + onboardingUuid));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User updatedUser = userRepository.save(user);
        log.info("[ONBOARDING MOBILE] Étape 2 réussie pour [{}]", onboardingUuid);

        return new OnboardingResponse(updatedUser.getOnboardingUuid(), updatedUser.getOnboardingCompleted());
    }

    @Override
    @Transactional
    public OnboardingResponse finalizeOnboarding(UUID onboardingUuid) {
        log.info("[ONBOARDING MOBILE] Finalisation pour onboardingUuid [{}]", onboardingUuid);

        User user = userRepository.findByOnboardingUuid(onboardingUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouvé pour l'onboardingUuid : " + onboardingUuid));

        user.setOnboardingCompleted(true);
        User finalizedUser = userRepository.save(user);
        log.info("[ONBOARDING MOBILE] Onboarding finalisé avec succès pour [{}]", onboardingUuid);

        return new OnboardingResponse(finalizedUser.getOnboardingUuid(), finalizedUser.getOnboardingCompleted());
    }
}
