package tg.univlome.saas.shared.domain.services;

import tg.univlome.saas.web.dtos.auth.AuthResponse;
import tg.univlome.saas.web.dtos.auth.LoginRequest;
import tg.univlome.saas.web.dtos.auth.UserResponse;

/**
 * Contrat du service d'authentification et de gestion de session.
 */
public interface AuthService {

    /**
     * Authentifie un utilisateur et génère un token JWT enrichi du statut d'onboarding.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Récupère le profil de l'utilisateur actuellement connecté depuis le contexte de sécurité.
     */
    UserResponse getCurrentUser(String email);
}
