package tg.univlome.saas.marketing.contact.application.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tg.univlome.saas.marketing.contact.application.dtos.request.SegmentRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.SegmentResponse;
import tg.univlome.saas.marketing.contact.domain.models.Segment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SegmentMapperTest {

    private SegmentMapper segmentMapper;

    @BeforeEach
    void setUp() {
        segmentMapper = new SegmentMapper();
    }

    @Test
    void shouldConvertRequestToEntity() {
        // --- ARRANGE ---
        String rulesJson = "{\"condition\":\"AND\",\"rules\":[]}";
        SegmentRequest request = new SegmentRequest("Acheteurs VIP", "Clients avec CA > 1000", rulesJson);

        // --- ACT ---
        Segment entity = segmentMapper.toEntity(request);

        // --- ASSERT ---
        assertNotNull(entity);
        assertEquals("Acheteurs VIP", entity.getName());
        assertEquals(rulesJson, entity.getRulesJson());
    }

    @Test
    void shouldConvertEntityToResponse() {
        // --- ARRANGE ---
        Segment segment = new Segment();
        segment.setName("Inactifs");
        segment.setRulesJson("{}");

        // --- ACT ---
        SegmentResponse response = segmentMapper.toResponse(segment);

        // --- ASSERT ---
        assertNotNull(response);
        assertEquals("Inactifs", response.name());
        assertEquals(segment.getTrackingId(), segment.getId());
    }
}