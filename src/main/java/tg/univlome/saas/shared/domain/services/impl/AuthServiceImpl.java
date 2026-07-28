package tg.univlome.saas.shared.domain.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.shared.domain.models.User;
import tg.univlome.saas.shared.domain.services.AuthService;
import tg.univlome.saas.shared.exceptions.ResourceNotFoundException;
import tg.univlome.saas.shared.repositories.UserRepository;
import tg.univlome.saas.shared.security.JwtUtils;
import tg.univlome.saas.web.dtos.auth.AuthResponse;
import tg.univlome.saas.web.dtos.auth.LoginRequest;
import tg.univlome.saas.web.dtos.auth.UserResponse;

/**
 * Implémentation du service d'authentification s'appuyant sur Spring Security et JwtUtils.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("[AUTH SERVICE] Tentative de connexion pour l'email [{}]", request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String jwtToken = jwtUtils.generateToken(authentication.getName());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé pour l'email : " + request.email()));

        log.info("[AUTH SERVICE] Connexion réussie pour [{}] — Onboarding complet: {}", user.getEmail(), user.getOnboardingCompleted());

        return new AuthResponse(
                jwtToken,
                user.getOnboardingUuid(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getOnboardingCompleted()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé pour l'email : " + email));

        return new UserResponse(
                user.getOnboardingUuid(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getOnboardingCompleted()
        );
    }
}
