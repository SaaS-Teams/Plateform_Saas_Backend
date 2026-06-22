package tg.univlome.saas.marketing.contact.application.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tg.univlome.saas.marketing.contact.application.dtos.response.ConsentLogResponse;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;
import tg.univlome.saas.marketing.contact.domain.models.ConsentLog;
import tg.univlome.saas.marketing.contact.domain.models.Contact;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConsentLogMapperTest {

    private ConsentLogMapper consentLogMapper;

    @BeforeEach
    void setUp() {
        consentLogMapper = new ConsentLogMapper();
    }

    @Test
    void shouldConvertEntityToResponseWithParentId() {
        // --- 1. ARRANGE ---
        // Il nous faut un vrai contact pour tester la liaison
        Contact contactParent = new Contact();
        UUID contactTrackingId = contactParent.getTrackingId(); // On garde son UUID en mémoire

        ConsentLog log = new ConsentLog(contactParent, ConsentAction.GRANTED, "192.168.1.1");

        // --- 2. ACT ---
        ConsentLogResponse response = consentLogMapper.toResponse(log);

        // --- 3. ASSERT ---
        assertNotNull(response);
        assertEquals(ConsentAction.GRANTED, response.action());
        assertEquals("192.168.1.1", response.ipAddress());

        // Le test le plus important : le log a-t-il bien récupéré l'ID de son parent ?
        assertEquals(contactTrackingId, response.trackingId());

        // Et a-t-il bien son propre ID unique ?
        assertEquals(log.getTrackingId(), response.trackingId());
    }

    @Test
    void shouldThrowExceptionWhenLogIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            consentLogMapper.toResponse(null);
        });
    }
}
