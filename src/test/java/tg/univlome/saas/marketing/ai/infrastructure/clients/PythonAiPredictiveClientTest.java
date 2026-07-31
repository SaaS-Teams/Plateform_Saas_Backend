package tg.univlome.saas.marketing.ai.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class PythonAiPredictiveClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PythonAiPredictiveClient pythonClient;

    @Test
    void fetchLeadScoreFromPython_ShouldReturnResponseMapWhenSuccess() {
        // Arrange
        Map<String, Object> mockResponse = Map.of(
                "score", 85.5,
                "risk_level", "HIGH_CONVERSION_PROSPECT",
                "intent_analysis", "Achat imminent"
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(eq("http://localhost:8000/api/v1/predict/lead-score"), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        Map<String, Object> result = pythonClient.fetchLeadScoreFromPython("c123", 10, 3, 200, "Bonjour");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("score")).isEqualTo(85.5);
        assertThat(result.get("risk_level")).isEqualTo("HIGH_CONVERSION_PROSPECT");
    }

    @Test
    void fetchLeadScoreFromPython_ShouldReturnFallbackMapWhenError() {
        // Arrange
        when(restTemplate.postForEntity(eq("http://localhost:8000/api/v1/predict/lead-score"), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        // Act
        Map<String, Object> result = pythonClient.fetchLeadScoreFromPython("c123", 10, 3, 200, "Bonjour");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("score")).isEqualTo(50.0);
        assertThat(result.get("risk_level")).isEqualTo("UNKNOWN");
    }
}
