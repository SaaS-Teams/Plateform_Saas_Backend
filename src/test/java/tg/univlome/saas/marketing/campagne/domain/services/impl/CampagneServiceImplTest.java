package tg.univlome.saas.marketing.campagne.domain.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.campagne.application.dtos.request.CampagneRequest;
import tg.univlome.saas.marketing.campagne.application.dtos.response.CampagneResponse;
import tg.univlome.saas.marketing.campagne.application.mappers.CampagneMapper;
import tg.univlome.saas.marketing.campagne.domain.enums.CampagneStatus;
import tg.univlome.saas.marketing.campagne.domain.models.Campagne;
import tg.univlome.saas.marketing.campagne.repositories.CampagneRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampagneServiceImplTest {

    @Mock
    private CampagneRepository campagneRepository;

    @Mock
    private CampagneMapper campagneMapper;

    @InjectMocks
    private CampagneServiceImpl campagneService;

    @Test
    void shouldCreateCampagneSuccessfully() {
        // Arrange
        CampagneRequest request = new CampagneRequest("Promo Eté", "Soldes d'été", "Voici nos offres", LocalDateTime.now());
        Campagne mockCampagne = new Campagne();
        mockCampagne.setNom("Promo Eté");

        CampagneResponse expectedResponse = new CampagneResponse(1L, UUID.randomUUID(), "Promo Eté", "Soldes d'été", "Voici nos offres", CampagneStatus.BROUILLON, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());

        when(campagneMapper.toEntity(request)).thenReturn(mockCampagne);
        when(campagneRepository.save(any(Campagne.class))).thenReturn(mockCampagne);
        when(campagneMapper.toResponse(mockCampagne)).thenReturn(expectedResponse);

        // Act
        CampagneResponse result = campagneService.createCampagne(request);

        // Assert
        assertNotNull(result);
        assertEquals("Promo Eté", result.nom());
        verify(campagneRepository, times(1)).save(mockCampagne);
    }

    @Test
    void shouldGetCampagneByTrackingId() {
        // Arrange
        UUID trackingId = UUID.randomUUID();
        Campagne mockCampagne = new Campagne();
        mockCampagne.setTrackingId(trackingId);

        CampagneResponse expectedResponse = new CampagneResponse(1L, trackingId, "Nom", "Sujet", "Contenu", CampagneStatus.BROUILLON, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());

        when(campagneRepository.findByTrackingId(trackingId)).thenReturn(Optional.of(mockCampagne));
        when(campagneMapper.toResponse(mockCampagne)).thenReturn(expectedResponse);

        // Act
        CampagneResponse result = campagneService.getCampagneByTrackingId(trackingId);

        // Assert
        assertNotNull(result);
        assertEquals(trackingId, result.trackingId());
    }

    @Test
    void shouldThrowExceptionWhenCampagneNotFound() {
        // Arrange
        UUID trackingId = UUID.randomUUID();
        when(campagneRepository.findByTrackingId(trackingId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> campagneService.getCampagneByTrackingId(trackingId));
        assertTrue(exception.getMessage().contains("introuvable"));
    }

    @Test
    void shouldGetAllCampagnes() {
        // Arrange
        Campagne mockCampagne = new Campagne();
        when(campagneRepository.findAll()).thenReturn(List.of(mockCampagne));
        when(campagneMapper.toResponse(mockCampagne)).thenReturn(mock(CampagneResponse.class));

        // Act
        List<CampagneResponse> result = campagneService.getAllCampagnes();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldUpdateCampagne() {
        // Arrange
        UUID trackingId = UUID.randomUUID();
        CampagneRequest request = new CampagneRequest("Nouveau Nom", "Nouveau Sujet", "Nouveau Contenu", null);
        Campagne mockCampagne = new Campagne();

        when(campagneRepository.findByTrackingId(trackingId)).thenReturn(Optional.of(mockCampagne));
        when(campagneRepository.save(mockCampagne)).thenReturn(mockCampagne);
        when(campagneMapper.toResponse(mockCampagne)).thenReturn(mock(CampagneResponse.class));

        // Act
        CampagneResponse result = campagneService.updateCampagne(trackingId, request);

        // Assert
        assertNotNull(result);
        verify(campagneMapper, times(1)).updateEntityFromRequest(mockCampagne, request);
        verify(campagneRepository, times(1)).save(mockCampagne);
    }

    @Test
    void shouldDeleteCampagne() {
        // Arrange
        UUID trackingId = UUID.randomUUID();
        Campagne mockCampagne = new Campagne();
        when(campagneRepository.findByTrackingId(trackingId)).thenReturn(Optional.of(mockCampagne));

        // Act
        campagneService.deleteCampagne(trackingId);

        // Assert
        verify(campagneRepository, times(1)).delete(mockCampagne);
    }
}
