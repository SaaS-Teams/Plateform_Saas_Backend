package tg.univlome.saas.marketing.ai.domain.services;

import tg.univlome.saas.marketing.ai.application.dtos.request.GenerateEmailRequest;
import tg.univlome.saas.marketing.ai.application.dtos.response.GeneratedContentResponse;

public interface GenerativeAiService {

    GeneratedContentResponse generateEmailContent(GenerateEmailRequest request);
}
