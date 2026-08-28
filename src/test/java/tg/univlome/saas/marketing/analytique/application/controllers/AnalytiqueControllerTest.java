package tg.univlome.saas.marketing.analytique.application.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tg.univlome.saas.marketing.analytique.application.dtos.response.DashboardResponse;
import tg.univlome.saas.marketing.analytique.domain.service.AnalytiqueService;

@WebMvcTest(AnalytiqueController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalytiqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalytiqueService analytiqueService;

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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getDashboardData_ShouldReturnStatusOkAndJson() throws Exception {
        // Arrange
        DashboardResponse mockResponse = new DashboardResponse(
                1000L, 500L, 200L, 20L,
                50.0, 20.0,
                List.of()
        );
        when(analytiqueService.getDashboardData()).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/analytiques/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSent").value(1000))
                .andExpect(jsonPath("$.totalOpened").value(500))
                .andExpect(jsonPath("$.openRate").value(50.0));
    }
}
