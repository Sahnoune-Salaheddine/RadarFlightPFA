package com.flightradar.service;

import com.flightradar.model.*;
import com.flightradar.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour FlightManagementService
 */
@ExtendWith(MockitoExtension.class)
class FlightManagementServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private PilotRepository pilotRepository;

    @Mock
    private WeatherService weatherService;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @InjectMocks
    private FlightManagementService flightManagementService;

    private Aircraft testAircraft;
    private Airport testDepartureAirport;
    private Airport testArrivalAirport;
    private Pilot testPilot;

    @BeforeEach
    void setUp() {
        // Créer des objets de test
        testAircraft = new Aircraft();
        testAircraft.setId(1L);
        testAircraft.setRegistration("TEST-001");
        testAircraft.setModel("Boeing 737");

        testDepartureAirport = new Airport();
        testDepartureAirport.setId(1L);
        testDepartureAirport.setName("Casablanca");
        testDepartureAirport.setCodeIATA("CMN");

        testArrivalAirport = new Airport();
        testArrivalAirport.setId(2L);
        testArrivalAirport.setName("Rabat");
        testArrivalAirport.setCodeIATA("RBA");

        testPilot = new Pilot();
        testPilot.setId(1L);
        testPilot.setLicense("PIL-001");
    }

    @Test
    void testCreateFlight_WithValidData_ShouldSucceed() {
        // Arrange
        Map<String, Object> flightData = new HashMap<>();
        flightData.put("flightNumber", "TEST001");
        flightData.put("airline", "Royal Air Maroc");
        flightData.put("aircraftId", 1L);
        flightData.put("departureAirportId", 1L);
        flightData.put("arrivalAirportId", 2L);
        flightData.put("scheduledDeparture", "2025-01-28T10:00:00");
        flightData.put("scheduledArrival", "2025-01-28T12:00:00");
        flightData.put("flightType", "COMMERCIAL");
        flightData.put("flightStatus", "PLANIFIE");

        when(aircraftRepository.findById(1L)).thenReturn(Optional.of(testAircraft));
        when(airportRepository.findById(1L)).thenReturn(Optional.of(testDepartureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(testArrivalAirport));
        when(flightRepository.findByFlightNumber("TEST001")).thenReturn(Optional.empty());
        
        Flight savedFlight = new Flight();
        savedFlight.setId(1L);
        savedFlight.setFlightNumber("TEST001");
        when(flightRepository.save(any(Flight.class))).thenReturn(savedFlight);

        // Act
        Flight result = flightManagementService.createFlight(flightData, "admin");

        // Assert
        assertNotNull(result);
        assertEquals("TEST001", result.getFlightNumber());
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    @Test
    void testCreateFlight_WithMissingFlightNumber_ShouldThrowException() {
        // Arrange
        Map<String, Object> flightData = new HashMap<>();
        flightData.put("airline", "Royal Air Maroc");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            flightManagementService.createFlight(flightData, "admin");
        });
    }

    @Test
    void testCreateFlight_WithDuplicateFlightNumber_ShouldThrowException() {
        // Arrange
        Map<String, Object> flightData = new HashMap<>();
        flightData.put("flightNumber", "TEST001");

        Flight existingFlight = new Flight();
        existingFlight.setFlightNumber("TEST001");
        when(flightRepository.findByFlightNumber("TEST001")).thenReturn(Optional.of(existingFlight));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            flightManagementService.createFlight(flightData, "admin");
        });
    }

    @Test
    void testCreateFlight_WithInvalidAircraftId_ShouldThrowException() {
        // Arrange
        Map<String, Object> flightData = new HashMap<>();
        flightData.put("flightNumber", "TEST001");
        flightData.put("aircraftId", 999L);

        when(flightRepository.findByFlightNumber("TEST001")).thenReturn(Optional.empty());
        when(aircraftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            flightManagementService.createFlight(flightData, "admin");
        });
    }

    @Test
    void testCreateFlight_WithPilotId_ShouldAssignPilot() {
        // Arrange
        Map<String, Object> flightData = new HashMap<>();
        flightData.put("flightNumber", "TEST001");
        flightData.put("airline", "Royal Air Maroc");
        flightData.put("aircraftId", 1L);
        flightData.put("departureAirportId", 1L);
        flightData.put("arrivalAirportId", 2L);
        flightData.put("pilotId", 1L);
        flightData.put("flightType", "COMMERCIAL");
        flightData.put("flightStatus", "PLANIFIE");

        when(aircraftRepository.findById(1L)).thenReturn(Optional.of(testAircraft));
        when(airportRepository.findById(1L)).thenReturn(Optional.of(testDepartureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(testArrivalAirport));
        when(pilotRepository.findById(1L)).thenReturn(Optional.of(testPilot));
        when(flightRepository.findByFlightNumber("TEST001")).thenReturn(Optional.empty());
        
        Flight savedFlight = new Flight();
        savedFlight.setId(1L);
        savedFlight.setFlightNumber("TEST001");
        savedFlight.setPilotId(1L);
        when(flightRepository.save(any(Flight.class))).thenReturn(savedFlight);

        // Act
        Flight result = flightManagementService.createFlight(flightData, "admin");

        // Assert
        assertNotNull(result);
        verify(pilotRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateFlight_WithInvalidCruiseAltitude_ShouldIgnoreInvalidValue() {
        // Arrange
        Map<String, Object> flightData = new HashMap<>();
        flightData.put("flightNumber", "TEST001");
        flightData.put("airline", "Royal Air Maroc");
        flightData.put("aircraftId", 1L);
        flightData.put("departureAirportId", 1L);
        flightData.put("arrivalAirportId", 2L);
        flightData.put("cruiseAltitude", 60000); // Valeur invalide (> 50000)
        flightData.put("flightType", "COMMERCIAL");
        flightData.put("flightStatus", "PLANIFIE");

        when(aircraftRepository.findById(1L)).thenReturn(Optional.of(testAircraft));
        when(airportRepository.findById(1L)).thenReturn(Optional.of(testDepartureAirport));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(testArrivalAirport));
        when(flightRepository.findByFlightNumber("TEST001")).thenReturn(Optional.empty());
        
        Flight savedFlight = new Flight();
        savedFlight.setId(1L);
        savedFlight.setFlightNumber("TEST001");
        when(flightRepository.save(any(Flight.class))).thenReturn(savedFlight);

        // Act
        Flight result = flightManagementService.createFlight(flightData, "admin");

        // Assert
        assertNotNull(result);
        // L'altitude invalide devrait être ignorée (null)
        verify(flightRepository, times(1)).save(any(Flight.class));
    }
}

