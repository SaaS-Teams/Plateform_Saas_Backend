package tg.univlome.saas.marketing.ai.domain.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.ai.application.dtos.request.GenerateEmailRequest;
import tg.univlome.saas.marketing.ai.application.dtos.response.GeneratedContentResponse;
import tg.univlome.saas.marketing.ai.domain.services.GenerativeAiService;

@Slf4j
@Service
public class GenerativeAiServiceImpl implements GenerativeAiService {

    private static final String SYSTEM_PROMPT = """
            Tu es un expert en copywriting marketing. Ton but est de rédiger des e-mails hautement convertisseurs.
            Ne renvoie que le corps de l'e-mail, sans introduction.
            """;

    private final ChatClient chatClient;

    public GenerativeAiServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Override
    public GeneratedContentResponse generateEmailContent(GenerateEmailRequest request) {
        log.info("[AI SERVICE] Génération de contenu d'email pour le thème [{}] (Ton: {}, Langue: {})",
                request.topic(), request.tone(), request.language());

        String tone = (request.tone() != null && !request.tone().isBlank())
                ? request.tone() : "professionnel et persuasif";
        String audience = (request.targetAudience() != null && !request.targetAudience().isBlank())
                ? request.targetAudience() : "tous les clients";
        String language = (request.language() != null && !request.language().isBlank())
                ? request.language() : "Français";

        String userPrompt = String.format(
                "Rédige un e-mail marketing sur le sujet : '%s'. Ton à employer : '%s'. Public cible : '%s'. Rédige en langue : '%s'.",
                request.topic(),
                tone,
                audience,
                language
        );

        String generatedText = this.chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

        log.info("[AI SERVICE] Génération réussie pour le thème [{}]", request.topic());
        return new GeneratedContentResponse(generatedText);
    }
}
