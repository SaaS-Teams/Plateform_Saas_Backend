package tg.univlome.saas.shared.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtre HTTP de limitation de débit (Rate Limiting) basé sur Bucket4j.
 *
 * <p>Appliqué sur les routes {@code /api/v1/**}, il autorise un maximum de
 * 100 requêtes par minute et par adresse IP. Lorsque le quota est dépassé,
 * une réponse {@code 429 Too Many Requests} est renvoyée.</p>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 100;
    private static final int WINDOW_MINUTES = 1;
    private static final String API_PREFIX = "/api/v1/";

    /**
     * Cache en mémoire des buckets par adresse IP.
     */
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.startsWith(API_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        Bucket bucket = bucketCache.computeIfAbsent(clientIp, key -> createBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            sendTooManyRequestsResponse(response);
        }
    }

    /**
     * Crée un nouveau bucket avec la bande passante configurée.
     *
     * @return Un bucket Bucket4j configuré.
     */
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(MAX_REQUESTS)
                .refillGreedy(MAX_REQUESTS, Duration.ofMinutes(WINDOW_MINUTES))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Résout l'adresse IP du client, en tenant compte des proxys inverses
     * via l'en-tête {@code X-Forwarded-For}.
     *
     * @param request La requête HTTP.
     * @return L'adresse IP du client.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Envoie une réponse HTTP 429 Too Many Requests avec un message JSON.
     *
     * @param response La réponse HTTP.
     * @throws IOException En cas d'erreur d'écriture.
     */
    private void sendTooManyRequestsResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":429,"
                + "\"error\":\"Too Many Requests\","
                + "\"message\":\"Quota de requêtes dépassé. Veuillez réessayer dans quelques instants.\"}"
        );
    }
}
