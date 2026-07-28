package tg.univlome.saas.marketing.automation.domain.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;

/**
 * Service de planification des nœuds {@code WAIT} via Quartz Scheduler.
 *
 * <p>Crée un job persistant (tables {@code QRTZ_*} en PostgreSQL) qui
 * sera déclenché par Quartz à l'heure calculée, même si le serveur
 * redémarre entre-temps.</p>
 *
 * <p>Flux d'exécution d'un nœud WAIT :</p>
 * <pre>
 *   WorkflowEngineServiceImpl
 *       └─ scheduleWaitNode(message, days)
 *           └─ Quartz persiste le job en BDD
 *               └─ [après X jours] WorkflowResumeJob.execute()
 *                   └─ producerService.sendStepToQueue(message)
 *                       └─ WorkflowConsumer reprend le parcours
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowSchedulingService {

    private final Scheduler quartzScheduler;
    private final ObjectMapper objectMapper;

    /**
     * Planifie la reprise d'un nœud {@code WAIT} après un délai en minutes.
     *
     * <p>Crée un {@link JobDetail} unique par exécution (basé sur son
     * {@code executionTrackingId}) et un {@link Trigger} one-shot qui
     * se déclenche exactement une fois à l'heure calculée.</p>
     *
     * @param message             le message de l'étape SUIVANTE à envoyer dans RabbitMQ
     *                            quand le délai expire (le moteur a déjà calculé {@code nextStepId})
     * @param waitDurationInDays  le nombre de jours d'attente défini dans le nœud
     * @throws SchedulerException         si Quartz rencontre une erreur de planification
     * @throws JsonProcessingException    si la sérialisation du message échoue
     */
    public void scheduleWaitNode(WorkflowStepMessage message, int waitDurationInDays)
            throws SchedulerException, JsonProcessingException {

        // Sérialiser le message en JSON pour le stocker dans le JobDataMap persistant
        String stepMessageJson = objectMapper.writeValueAsString(message);

        // Clé unique par exécution pour éviter toute collision entre workflows
        String jobId = "wait-resume-" + message.executionTrackingId().toString();
        JobKey jobKey = new JobKey(jobId, "workflow-wait-group");

        // Construction du JobDetail : lié à WorkflowResumeJob, persistant, non concurrent
        JobDetail jobDetail = JobBuilder.newJob(WorkflowResumeJob.class)
                .withIdentity(jobKey)
                .withDescription("Reprise du workflow après nœud WAIT — exécution : "
                        + message.executionTrackingId())
                // storeDurably = false : le job se supprime automatiquement après exécution
                .storeDurably(false)
                // Le JobDataMap est persisté dans QRTZ_JOB_DETAILS
                .usingJobData(WorkflowResumeJob.KEY_STEP_MESSAGE_JSON, stepMessageJson)
                .build();

        // Calcul de l'heure de déclenchement
        Instant fireAt = Instant.now().plus(waitDurationInDays, ChronoUnit.DAYS);

        // Trigger one-shot : se déclenche une seule fois à la date calculée
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-" + jobId, "workflow-wait-triggers")
                .withDescription("Déclenchement unique après " + waitDurationInDays
                        + " jour(s) pour l'exécution " + message.executionTrackingId())
                .startAt(Date.from(fireAt))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(0)  // one-shot : ne se répète pas
                        .withMisfireHandlingInstructionFireNow()) // si le serveur était down, déclenche immédiatement au redémarrage
                .build();

        quartzScheduler.scheduleJob(jobDetail, trigger);

        log.info("-> Nœud WAIT planifié : l'exécution [{}] reprendra dans {} jour(s) (à {}).",
                message.executionTrackingId(), waitDurationInDays, fireAt);
    }
}
