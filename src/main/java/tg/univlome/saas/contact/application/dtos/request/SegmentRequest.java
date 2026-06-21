package tg.univlome.saas.marketing.contact.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SegmentRequest(
        @NotBlank(message = "Le nom du segment est obligatoire")
        String name,

        String description,

        @NotNull(message = "Les règles du segment (JSON) ne peuvent pas être nulles")
        String rulesJson
) {}