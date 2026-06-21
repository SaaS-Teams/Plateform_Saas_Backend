package tg.univlome.saas.config;

import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration pour la gestion du cache distribué avec Redis.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final int DEFAULT_TTL_MINUTES = 60;

    /**
     * Configure le gestionnaire de cache Redis (RedisCacheManager).
     * Les données seront stockées en JSON pour être lisibles si on inspecte Redis.
     *
     * @param connectionFactory La factory de connexion à Redis fournie par Spring Boot.
     * @return Le CacheManager configuré.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                // Durée de vie par défaut des données en cache (1 heure)
                .entryTtl(Duration.ofMinutes(DEFAULT_TTL_MINUTES))
                // On évite de mettre en cache des valeurs nulles
                .disableCachingNullValues()
                // Les clés seront sérialisées en texte simple (String)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                // Les valeurs (les objets Java) seront sérialisés en JSON
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                // Vous pourrez ajouter ici des configurations spécifiques par cache
                // Exemple : .withCacheConfiguration("contacts", cacheConfiguration.entryTtl(Duration.ofMinutes(10)))
                .build();
    }
}
