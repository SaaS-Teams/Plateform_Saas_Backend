package tg.univlome.saas.marketing.automation.domain.services;

import tg.univlome.saas.web.dtos.workflow.SaveWorkflowRequest;
import tg.univlome.saas.web.dtos.workflow.WorkflowCanvasResponse;

/**
 * Contrat du service de gestion du constructeur visuel de workflow (Canvas No-Code).
 */
public interface WorkflowBuilderService {

    /**
     * Sauvegarde la configuration visuelle du canvas (nœuds et liens) pour un workflow donné.
     *
     * @param workflowId l'identifiant du workflow en base de données
     * @param request    la structure visuelle envoyée par le frontend
     * @return la réponse avec le graphe mis à jour
     */
    WorkflowCanvasResponse saveCanvas(Long workflowId, SaveWorkflowRequest request);

    /**
     * Récupère et reconstruit le graphe visuel d'un workflow à partir de sa définition enregistrée.
     *
     * @param workflowId l'identifiant du workflow
     * @return le graphe visuel complet
     */
    WorkflowCanvasResponse getCanvas(Long workflowId);
}
