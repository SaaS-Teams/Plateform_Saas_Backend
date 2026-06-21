package tg.univlome.saas.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuration CORS globale pour autoriser les requêtes du frontend.
 */
@Configuration
public class CorsConfig {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Crée le filtre CORS utilisé par Spring Boot et Spring Security.
     *
     * @return Le filtre CORS configuré.
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Autoriser spécifiquement l'URL du frontend (depuis .env via properties)
        config.setAllowedOrigins(List.of(frontendUrl));
        
        // Autoriser tous les headers HTTP
        config.setAllowedHeaders(List.of("*"));
        
        // Autoriser les méthodes REST courantes
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Autoriser l'envoi d'en-têtes d'authentification (ex: token JWT, cookies)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Appliquer cette configuration sur toutes les routes de l'application
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
