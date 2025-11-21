package com.flightradar.service;

import com.flightradar.model.*;
import com.flightradar.model.dto.PilotDashboardDTO;
import com.flightradar.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PilotDashboardService
 */
@ExtendWith(MockitoExtension.class)
class PilotDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PilotRepository pilotRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private CommunicationRepository communicationRepository;

    @Mock
    private RadarCenterRepository radarCenterRepository;

    @InjectMocks
    private PilotDashboardService pilotDashboardService;

    private User user;
    private Pilot pilot;
    private Aircraft aircraft;
    private Airport airport;

    @BeforeEach
    void setUp() {
        // Créer un utilisateur
        user = new User();
        user.setId(1L);
        user.setUsername("pilote_cmn1");
        user.setRole(Role.PILOTE);

        // Créer un aéroport
        airport = new Airport();
        airport.setId(1L);
        airport.setName("Aéroport Mohammed V");
        airport.setCodeIATA("CMN");
        airport.setLatitude(33.5731);
        airport.setLongitude(-7.5898);

        // Créer un pilote
        pilot = new Pilot();
        pilot.setId(1L);
        pilot.setName("Pilote CMN 1");
        pilot.setLicense("CMN1");
        pilot.setUser(user);
        pilot.setAirport(airport);

        // Créer un avion
        aircraft = new Aircraft();
        aircraft.setId(1L);
        aircraft.setRegistration("CN-AT01");
        aircraft.setModel("A320");
        aircraft.setStatus(AircraftStatus.AU_SOL);
        aircraft.setPilot(pilot);
        aircraft.setAirport(airport);
        aircraft.setPositionLat(33.5731);
        aircraft.setPositionLon(-7.5898);
        aircraft.setAltitude(0.0);
        aircraft.setSpeed(0.0);
        aircraft.setHeading(0.0);

        // Lier l'avion au pilote (relation bidirectionnelle)
        pilot.setAircraft(aircraft);
    }

    @Test
    void testGetPilotDashboard_Success() {
        // Arrange
        when(userRepository.findByUsername("pilote_cmn1")).thenReturn(Optional.of(user));
        when(pilotRepository.findByUserId(1L)).thenReturn(Optional.of(pilot));
        when(aircraftRepository.findByPilotId(1L)).thenReturn(Optional.of(aircraft));
        when(flightRepository.findByAircraftIdAndFlightStatusNot(1L, FlightStatus.TERMINE))
            .thenReturn(Optional.empty());

        // Act
        PilotDashboardDTO result = pilotDashboardService.getPilotDashboard("pilote_cmn1");

        // Assert
        assertNotNull(result);
        assertEquals("N/A", result.getFlightNumber());
        assertEquals("A320", result.getAircraftType());
        assertEquals(33.5731, result.getLatitude());
        assertEquals(-7.5898, result.getLongitude());
        assertEquals(0.0, result.getAltitude());

        verify(userRepository, times(1)).findByUsername("pilote_cmn1");
        verify(pilotRepository, times(1)).findByUserId(1L);
        verify(aircraftRepository, atMost(1)).findByPilotId(1L);
    }

    @Test
    void testGetPilotDashboard_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("pilote_inexistant")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pilotDashboardService.getPilotDashboard("pilote_inexistant");
        });

        assertTrue(exception.getMessage().contains("Pilote non trouvé"));
        verify(userRepository, times(1)).findByUsername("pilote_inexistant");
        verify(pilotRepository, never()).findByUserId(any());
    }

    @Test
    void testGetPilotDashboard_PilotNotFound() {
        // Arrange
        when(userRepository.findByUsername("pilote_cmn1")).thenReturn(Optional.of(user));
        when(pilotRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pilotDashboardService.getPilotDashboard("pilote_cmn1");
        });

        assertTrue(exception.getMessage().contains("Profil pilote non trouvé"));
        verify(userRepository, times(1)).findByUsername("pilote_cmn1");
        verify(pilotRepository, times(1)).findByUserId(1L);
    }

    @Test
    void testGetPilotDashboard_NoAircraftAssigned() {
        // Arrange
        pilot.setAircraft(null); // Pas d'avion assigné
        when(userRepository.findByUsername("pilote_cmn1")).thenReturn(Optional.of(user));
        when(pilotRepository.findByUserId(1L)).thenReturn(Optional.of(pilot));
        when(aircraftRepository.findByPilotId(1L)).thenReturn(Optional.empty());
        when(aircraftRepository.findByPilotUsername("pilote_cmn1")).thenReturn(Optional.empty());
        when(aircraftRepository.findUnassignedAircraft()).thenReturn(java.util.Collections.emptyList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pilotDashboardService.getPilotDashboard("pilote_cmn1");
        });

        assertTrue(exception.getMessage().contains("NO_AIRCRAFT_ASSIGNED"));
        verify(userRepository, times(1)).findByUsername("pilote_cmn1");
        verify(pilotRepository, times(1)).findByUserId(1L);
        verify(aircraftRepository, atLeastOnce()).findByPilotId(1L);
    }

    @Test
    void testGetPilotDashboard_OnePilotOneAircraft() {
        // Arrange
        when(userRepository.findByUsername("pilote_cmn1")).thenReturn(Optional.of(user));
        when(pilotRepository.findByUserId(1L)).thenReturn(Optional.of(pilot));
        when(aircraftRepository.findByPilotId(1L)).thenReturn(Optional.of(aircraft));
        when(flightRepository.findByAircraftIdAndFlightStatusNot(1L, FlightStatus.TERMINE))
            .thenReturn(Optional.empty());

        // Act
        PilotDashboardDTO result = pilotDashboardService.getPilotDashboard("pilote_cmn1");

        // Assert
        assertNotNull(result);
        // Vérifier qu'un seul avion est retourné (pas de liste)
        verify(aircraftRepository, atMost(1)).findByPilotId(1L);
    }

    @Test
    void testGetPilotDashboard_WithFlight() {
        // Arrange
        Flight flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("AT1001");
        flight.setAirline("Royal Air Maroc");
        flight.setDepartureAirport(airport);
        flight.setArrivalAirport(airport);
        flight.setFlightStatus(FlightStatus.EN_COURS);

        when(userRepository.findByUsername("pilote_cmn1")).thenReturn(Optional.of(user));
        when(pilotRepository.findByUserId(1L)).thenReturn(Optional.of(pilot));
        when(aircraftRepository.findByPilotId(1L)).thenReturn(Optional.of(aircraft));
        when(flightRepository.findByAircraftIdAndFlightStatusNot(1L, FlightStatus.TERMINE))
            .thenReturn(Optional.of(flight));

        // Act
        PilotDashboardDTO result = pilotDashboardService.getPilotDashboard("pilote_cmn1");

        // Assert
        assertNotNull(result);
        assertEquals("AT1001", result.getFlightNumber());
        assertEquals("Royal Air Maroc", result.getAirline());
    }
}

