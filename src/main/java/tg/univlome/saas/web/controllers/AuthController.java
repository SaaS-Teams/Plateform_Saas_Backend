package tg.univlome.saas.web.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.shared.domain.services.AuthService;
import tg.univlome.saas.web.dtos.auth.AuthResponse;
import tg.univlome.saas.web.dtos.auth.LoginRequest;
import tg.univlome.saas.web.dtos.auth.RegisterRequest;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints d'inscription, de connexion et de gestion de session SaaS")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur et provisionnement automatique de son Workspace SaaS")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Requête d'inscription reçue pour l'email : {}", request.email());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur et génération du token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Requête de connexion reçue pour l'email : {}", request.email());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
