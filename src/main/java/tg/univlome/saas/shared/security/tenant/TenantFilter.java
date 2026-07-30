package tg.univlome.saas.shared.security.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tg.univlome.saas.shared.repositories.UserRepository;

/**
 * Filtre de sécurité interceptant chaque requête HTTP pour extraire le Tenant ID (workspaceTrackingId)
 * et l'injecter dans le {@link TenantContextHolder}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private static final String X_TENANT_ID_HEADER = "X-Tenant-ID";
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Tente d'abord d'extraire depuis l'en-tête HTTP X-Tenant-ID si fourni
            String headerTenantId = request.getHeader(X_TENANT_ID_HEADER);
            if (headerTenantId != null && !headerTenantId.isBlank()) {
                try {
                    UUID tenantUuid = UUID.fromString(headerTenantId);
                    TenantContextHolder.setTenantId(tenantUuid);
                } catch (IllegalArgumentException e) {
                    log.warn("[TENANT FILTER] En-tête X-Tenant-ID invalide : {}", headerTenantId);
                }
            } else {
                // 2. Sinon, résout l'utilisateur authentifié depuis le SecurityContextHolder
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()
                        && !"anonymousUser".equals(authentication.getPrincipal())) {
                    String username = authentication.getName();
                    userRepository.findByEmail(username).ifPresent(user -> {
                        if (user.getWorkspaceTrackingId() != null) {
                            TenantContextHolder.setTenantId(user.getWorkspaceTrackingId());
                        }
                    });
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            // Nettoyage impératif pour éviter toute fuite de mémoire entre requêtes HTTP Tomcat
            TenantContextHolder.clear();
        }
    }
}
