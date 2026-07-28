package tg.univlome.saas.web.controllers.mobile;

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
import tg.univlome.saas.shared.domain.services.AuthService;
import tg.univlome.saas.web.dtos.auth.AuthResponse;
import tg.univlome.saas.web.dtos.auth.LoginRequest;

/**
 * Contrôleur d'authentification mobile (connexion & génération JWT).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mobile/auth")
@RequiredArgsConstructor
@Tag(name = "Mobile Authentication", description = "Endpoints d'authentification et de login pour l'application mobile")
public class MobileAuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authentification mobile de l'étudiant et obtention du token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("[API MOBILE AUTH] Demande de connexion pour: {}", request.email());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
