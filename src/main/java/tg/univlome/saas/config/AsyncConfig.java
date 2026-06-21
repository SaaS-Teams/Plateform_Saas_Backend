package tg.univlome.saas.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration pour l'exécution de tâches asynchrones (ex: envoi d'emails).
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final int QUEUE_CAPACITY = 500;

    /**
     * Configure le pool de threads utilisé pour les méthodes annotées avec @Async.
     *
     * @return L'exécuteur de tâches asynchrones configuré.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Nombre minimum de threads maintenus en vie (pour être réactif)
        executor.setCorePoolSize(CORE_POOL_SIZE);
        
        // Nombre maximum de threads en cas de pic de charge
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        
        // Taille maximale de la file d'attente des tâches en attente d'un thread
        executor.setQueueCapacity(QUEUE_CAPACITY);
        
        // Préfixe pour le nommage des threads (très utile pour le débogage dans les logs)
        executor.setThreadNamePrefix("AsyncThread-");
        
        executor.initialize();
        return executor;
    }
}
