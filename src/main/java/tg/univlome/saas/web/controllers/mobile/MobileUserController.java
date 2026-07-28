package tg.univlome.saas.web.controllers.mobile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.shared.domain.services.AuthService;
import tg.univlome.saas.web.dtos.auth.UserResponse;

/**
 * Contrôleur de profil utilisateur sécurisé pour l'application mobile.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mobile/users")
@RequiredArgsConstructor
@Tag(name = "Mobile Users", description = "Endpoints sécurisés d'état utilisateur mobile")
public class MobileUserController {

    private final AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Récupération du profil et de l'état d'onboarding de l'utilisateur connecté",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        log.info("[API MOBILE USERS] Récupération du profil /me pour l'utilisateur connecté: {}", currentUserEmail);

        UserResponse response = authService.getCurrentUser(currentUserEmail);
        return ResponseEntity.ok(response);
    }
}
