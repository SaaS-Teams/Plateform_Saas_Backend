package tg.univlome.saas.marketing.analytique.application.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DashboardResponse;
import tg.univlome.saas.marketing.analytique.domain.service.AnalytiqueService;

import java.util.List;

import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalytiqueController.class)
class AnalytiqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalytiqueService analytiqueService;

    @MockBean
    private tg.univlome.saas.shared.security.JwtUtils jwtUtils;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getDashboardData_ShouldReturnStatusOkAndJson() throws Exception {
        // Arrange
        DashboardResponse mockResponse = new DashboardResponse(
                15, 5, 2, 8, 
                1000, 980, 20, 98.0, 
                List.of() // Liste vide pour simplifier le test du contrôleur
        );
        when(analytiqueService.getDashboardData()).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/analytiques/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCampaigns").value(15))
                .andExpect(jsonPath("$.totalEmailsSent").value(1000))
                .andExpect(jsonPath("$.overallSuccessRate").value(98.0));
    }
}
