package tg.univlome.saas.marketing.automation.domain.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowProducerService;

/**
 * Job Quartz déclenché à l'expiration d'un nœud {@code WAIT}.
 *
 * <p>Ce job est persisté en base de données (tables {@code QRTZ_*}) et
 * survit aux redémarrages du serveur. À l'heure programmée, Quartz
 * l'exécute et il remet simplement le {@link WorkflowStepMessage}
 * dans la file RabbitMQ pour que le {@code WorkflowConsumer} reprenne
 * l'exécution du parcours normalement.</p>
 *
 * <p>Le job implémente {@link Job} directement (pas {@code QuartzJobBean})
 * afin de pouvoir utiliser {@code @Autowired} via
 * {@code SpringBeanJobFactory}, configuré automatiquement par
 * {@code spring-boot-starter-quartz}.</p>
 *
 * <h3>Clés stockées dans le {@link JobDataMap}</h3>
 * <ul>
 *   <li>{@code STEP_MESSAGE_JSON} — le {@link WorkflowStepMessage} sérialisé en JSON</li>
 * </ul>
 */
@Slf4j
@Component
public class WorkflowResumeJob implements Job {

    /** Clé utilisée pour stocker/récupérer le message dans le JobDataMap. */
    public static final String KEY_STEP_MESSAGE_JSON = "STEP_MESSAGE_JSON";

    @Autowired
    private WorkflowProducerService producerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String stepMessageJson = dataMap.getString(KEY_STEP_MESSAGE_JSON);

        log.info("WorkflowResumeJob déclenché — désérialisation du message de reprise...");

        try {
            // Désérialiser le message depuis le JSON stocké dans le JobDataMap
            WorkflowStepMessage message = objectMapper.readValue(stepMessageJson, WorkflowStepMessage.class);

            log.info("-> Reprise de l'exécution [{}] au nœud [{}] après expiration du WAIT.",
                    message.executionTrackingId(), message.nodeId());

            // Remettre le message dans la file RabbitMQ : le WorkflowConsumer prend le relais
            producerService.sendStepToQueue(message);

        } catch (JsonProcessingException e) {
            // Une erreur de désérialisation est fatale : le job ne doit pas se relancer
            throw new JobExecutionException(
                    "Impossible de désérialiser le WorkflowStepMessage depuis le JobDataMap : " + e.getMessage(),
                    false // refireImmediately = false, inutile de réessayer sur un JSON invalide
            );
        }
    }
}
