package com.flightradar.controller;

import com.flightradar.model.*;
import com.flightradar.repository.*;
import com.flightradar.service.FlightManagementService;
import com.flightradar.service.FlightSimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour FlightController
 * 
 * Note: Tests simplifiés sans Spring Context pour éviter les problèmes de configuration.
 * Ces tests vérifient la logique métier du contrôleur directement.
 */
@ExtendWith(MockitoExtension.class)
class FlightControllerTest {

    @Mock
    private FlightSimulationService flightSimulationService;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PilotRepository pilotRepository;

    @Mock
    private FlightManagementService flightManagementService;

    @InjectMocks
    private FlightController flightController;

    private Flight testFlight;

    @BeforeEach
    void setUp() {
        testFlight = new Flight();
        testFlight.setId(1L);
        testFlight.setFlightNumber("TEST001");
        testFlight.setAirline("Royal Air Maroc");
        testFlight.setFlightStatus(FlightStatus.PLANIFIE);
    }

    @Test
    void testCreateFlight_WithValidData_ShouldReturnSuccess() {
        // Arrange
        Map<String, Object> flightData = new HashMap<>();
        flightData.put("flightNumber", "TEST001");
        flightData.put("airline", "Royal Air Maroc");
        flightData.put("aircraftId", 1L);
        flightData.put("departureAirportId", 1L);
        flightData.put("arrivalAirportId", 2L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");

        when(flightManagementService.createFlight(any(Map.class), any(String.class)))
            .thenReturn(testFlight);

        // Act
        ResponseEntity<?> response = flightController.createFlight(flightData, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
    }

    @Test
    void testCreateFlight_WithInvalidData_ShouldReturnBadRequest() {
        // Arrange
        Map<String, Object> flightData = new HashMap<>();
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        
        when(flightManagementService.createFlight(any(Map.class), any(String.class)))
            .thenThrow(new IllegalArgumentException("Le numéro de vol est obligatoire"));

        // Act
        ResponseEntity<?> response = flightController.createFlight(flightData, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("VALIDATION_ERROR", body.get("type"));
    }

    @Test
    void testGetAllFlights_ShouldReturnList() {
        // Arrange
        List<Flight> flights = Arrays.asList(testFlight);
        when(flightRepository.findAll()).thenReturn(flights);

        // Act
        ResponseEntity<List<Map<String, Object>>> response = flightController.getAllFlights();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }
}

