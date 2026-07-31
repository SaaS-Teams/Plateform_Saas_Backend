package tg.univlome.saas.marketing.ai.domain.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import tg.univlome.saas.marketing.ai.application.dtos.request.GenerateEmailRequest;
import tg.univlome.saas.marketing.ai.application.dtos.response.GeneratedContentResponse;

@ExtendWith(MockitoExtension.class)
class GenerativeAiServiceImplTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private GenerativeAiServiceImpl generativeAiService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        generativeAiService = new GenerativeAiServiceImpl(chatClientBuilder);
    }

    @Test
    void generateEmailContent_ShouldReturnGeneratedContent() {
        // Arrange
        GenerateEmailRequest request = new GenerateEmailRequest(
                "Lancement nouveau produit SaaS",
                "Enthousiaste",
                "Entreprises PME",
                "Français"
        );

        String mockGeneratedText = "Découvrez notre nouvelle plateforme révolutionnaire !";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(mockGeneratedText);

        // Act
        GeneratedContentResponse response = generativeAiService.generateEmailContent(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo(mockGeneratedText);
    }
}
