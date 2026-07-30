package tg.univlome.saas.shared.security.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantContextHolderTest {

    @BeforeEach
    @AfterEach
    void setUpAndClean() {
        TenantContextHolder.clear();
    }

    @Test
    void shouldSetAndGetTenantIdCorrectly() {
        UUID expectedTenantId = UUID.randomUUID();
        TenantContextHolder.setTenantId(expectedTenantId);

        assertThat(TenantContextHolder.getTenantId()).isEqualTo(expectedTenantId);
    }

    @Test
    void shouldClearTenantIdCorrectly() {
        UUID tenantId = UUID.randomUUID();
        TenantContextHolder.setTenantId(tenantId);
        TenantContextHolder.clear();

        assertThat(TenantContextHolder.getTenantId()).isNull();
    }
}
