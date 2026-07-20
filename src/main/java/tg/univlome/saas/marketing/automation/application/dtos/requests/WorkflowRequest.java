package tg.univlome.saas.marketing.automation.application.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tg.univlome.saas.marketing.automation.domain.enums.WorkflowStatus;

public record WorkflowRequest(
        @NotBlank(message = "Le nom du scénario est obligatoire")
        String name,

        String description,

        @NotNull(message = "Le statut est obligatoire")
        WorkflowStatus status,

        @NotBlank(message = "Le type de déclencheur est obligatoire")
        String triggerType,

        @NotBlank(message = "Les données du flux JSON sont obligatoires")
        String flowData
) {}