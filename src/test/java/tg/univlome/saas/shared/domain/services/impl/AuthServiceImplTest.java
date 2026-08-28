package tg.univlome.saas.shared.domain.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tg.univlome.saas.shared.domain.models.User;
import tg.univlome.saas.shared.domain.models.Workspace;
import tg.univlome.saas.shared.exceptions.ConflictException;
import tg.univlome.saas.shared.repositories.UserRepository;
import tg.univlome.saas.shared.repositories.WorkspaceRepository;
import tg.univlome.saas.shared.security.JwtUtils;
import tg.univlome.saas.web.dtos.auth.AuthResponse;
import tg.univlome.saas.web.dtos.auth.RegisterRequest;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_ShouldProvisionWorkspaceAndSaveUser() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "owner@company.com",
                "Password123!",
                "Alice",
                "Smith",
                "Acme Corp"
        );

        UUID workspaceTrackingId = UUID.randomUUID();
        Workspace savedWorkspace = Workspace.builder()
                .id(1L)
                .trackingId(workspaceTrackingId)
                .name("Acme Corp")
                .active(true)
                .build();

        UUID userOnboardingUuid = UUID.randomUUID();
        User savedUser = User.builder()
                .id(10L)
                .onboardingUuid(userOnboardingUuid)
                .email("owner@company.com")
                .firstName("Alice")
                .lastName("Smith")
                .workspaceTrackingId(workspaceTrackingId)
                .onboardingCompleted(true)
                .build();

        when(userRepository.existsByEmail("owner@company.com")).thenReturn(false);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(savedWorkspace);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtils.generateToken("owner@company.com")).thenReturn("mocked.jwt.token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mocked.jwt.token");
        assertThat(response.email()).isEqualTo("owner@company.com");
        assertThat(response.firstName()).isEqualTo("Alice");

        verify(workspaceRepository).save(any(Workspace.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowConflictExceptionWhenEmailExists() {
        RegisterRequest request = new RegisterRequest(
                "existing@company.com",
                "Password123!",
                "Bob",
                "Marley",
                "Reggae Ltd"
        );

        when(userRepository.existsByEmail("existing@company.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Un compte existe déjà avec l'adresse e-mail");
    }
}
