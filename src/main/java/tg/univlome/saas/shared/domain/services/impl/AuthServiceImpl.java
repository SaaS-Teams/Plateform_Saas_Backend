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
    private final tg.univlome.saas.shared.repositories.WorkspaceRepository workspaceRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse register(tg.univlome.saas.web.dtos.auth.RegisterRequest request) {
        log.info("[AUTH SERVICE] Inscription d'un nouvel utilisateur pour l'email [{}]", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new tg.univlome.saas.shared.exceptions.ConflictException(
                    "Un compte existe déjà avec l'adresse e-mail : " + request.email());
        }

        // Étape A : S'assurer qu'aucun filtre Tenant n'est actif pendant l'inscription initiale
        tg.univlome.saas.shared.security.tenant.TenantContextHolder.clear();

        // Étape B & C : Créer et sauvegarder le nouveau Workspace
        String workspaceName = (request.companyName() != null && !request.companyName().isBlank())
                ? request.companyName().trim()
                : "Espace de " + (request.firstName() != null ? request.firstName().trim() : request.email());

        tg.univlome.saas.shared.domain.models.Workspace newWorkspace = tg.univlome.saas.shared.domain.models.Workspace.builder()
                .name(workspaceName)
                .active(true)
                .build();

        tg.univlome.saas.shared.domain.models.Workspace savedWorkspace = workspaceRepository.save(newWorkspace);
        log.info("[AUTH SERVICE] Workspace provisionné avec succès — workspaceTrackingId: [{}]", savedWorkspace.getTrackingId());

        // Étape D & E : Créer et sauvegarder l'utilisateur avec son workspaceTrackingId
        String encodedPassword = passwordEncoder.encode(request.password());

        User newUser = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .workspaceTrackingId(savedWorkspace.getTrackingId())
                .onboardingCompleted(true)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("[AUTH SERVICE] Utilisateur créé avec succès — onboardingUuid: [{}]", savedUser.getOnboardingUuid());

        String jwtToken = jwtUtils.generateToken(savedUser.getEmail());

        return new AuthResponse(
                jwtToken,
                savedUser.getOnboardingUuid(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getOnboardingCompleted()
        );
    }

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
