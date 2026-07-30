package tg.univlome.saas.marketing.analytique.application.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tg.univlome.saas.marketing.analytique.domain.service.impl.AnalyticsService;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    // Security mock beans
    @MockBean
    private tg.univlome.saas.shared.security.JwtUtils jwtUtils;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private tg.univlome.saas.shared.security.RateLimitFilter rateLimitFilter;

    @MockBean
    private tg.univlome.saas.shared.security.tenant.TenantFilter tenantFilter;

    @MockBean
    private tg.univlome.saas.shared.repositories.UserRepository userRepository;

    @Test
    @WithMockUser
    void shouldReturnOkWhenReceivingWebhookEvents() throws Exception {
        String payload = """
                [
                  {
                    "email": "test@example.com",
                    "timestamp": 1629811200,
                    "event": "open",
                    "ip": "192.168.1.1",
                    "useragent": "Mozilla",
                    "tracking_id": "%s"
                  }
                ]
                """.formatted(UUID.randomUUID().toString());

        mockMvc.perform(post("/api/webhooks/sendgrid")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }
}
