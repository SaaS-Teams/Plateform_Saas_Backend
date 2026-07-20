package tg.univlome.saas.marketing.automation.application.dtos.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record WorkflowNode(
        @JsonProperty("id")
        String nodeId,

        String type,

        Map<String, Object> data,

        // Jackson lira "next", mais en Java c'est "nextStepId"
        @JsonProperty("next")
        String nextStepId
) {}
