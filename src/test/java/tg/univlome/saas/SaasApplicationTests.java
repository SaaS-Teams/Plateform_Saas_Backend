package tg.univlome.saas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"sendgrid.api-key=test-key"})
class SaasApplicationTests {

    @Test
    void contextLoads() {
    }

}
