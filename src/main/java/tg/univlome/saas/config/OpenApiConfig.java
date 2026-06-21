package tg.univlome.saas.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration de Swagger / OpenAPI.
 * Désactivé en production grâce à l'annotation Profile("!prod").
 */
@Configuration
@Profile("!prod")
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * Configuration générale de l'API avec le schéma de sécurité JWT.
     *
     * @return L'objet OpenAPI configuré.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SaaS Marketing Platform API")
                        .version("1.0")
                        .description("Plateforme SaaS de gestion du marketing digital : "
                                + "contacts, campagnes, emails, réseaux sociaux et analytics."))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme()));
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Insérez votre token JWT ici");
    }

    /* =======================================================
       Groupes d'API par module (Spring Modulith)
       ======================================================= */

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1 - Authentification")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi contactApi() {
        return GroupedOpenApi.builder()
                .group("2 - Contacts")
                .pathsToMatch("/api/v1/contacts/**")
                .build();
    }

    @Bean
    public GroupedOpenApi campagneApi() {
        return GroupedOpenApi.builder()
                .group("3 - Campagnes")
                .pathsToMatch("/api/v1/campagnes/**")
                .build();
    }

    @Bean
    public GroupedOpenApi emailApi() {
        return GroupedOpenApi.builder()
                .group("4 - Emails")
                .pathsToMatch("/api/v1/emails/**")
                .build();
    }

    @Bean
    public GroupedOpenApi analytiqueApi() {
        return GroupedOpenApi.builder()
                .group("5 - Analytique")
                .pathsToMatch("/api/v1/analytiques/**")
                .build();
    }
}
