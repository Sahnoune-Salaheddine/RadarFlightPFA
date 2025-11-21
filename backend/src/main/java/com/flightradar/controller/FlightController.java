package com.flightradar.controller;

import com.flightradar.model.Flight;
import com.flightradar.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur REST pour les vols
 */
@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class FlightController {
    
    @Autowired
    private FlightService flightService;
    
    /**
     * GET /api/flights
     * Récupère tous les vols
     */
    @GetMapping
    public ResponseEntity<List<Flight>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }
    
    /**
     * GET /api/flights/{id}
     * Récupère un vol par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Flight> getFlightById(@PathVariable Long id) {
        Optional<Flight> flight = flightService.getFlightById(id);
        return flight.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/flights/active
     * Récupère les vols en cours
     */
    @GetMapping("/active")
    public ResponseEntity<List<Flight>> getActiveFlights() {
        return ResponseEntity.ok(flightService.getActiveFlights());
    }
    
    /**
     * POST /api/flights
     * Crée un nouveau vol
     */
    @PostMapping
    public ResponseEntity<Flight> createFlight(@RequestBody Map<String, Object> flightData) {
        String flightNumber = (String) flightData.get("flightNumber");
        Long aircraftId = ((Number) flightData.get("aircraftId")).longValue();
        Long departureAirportId = ((Number) flightData.get("departureAirportId")).longValue();
        Long arrivalAirportId = ((Number) flightData.get("arrivalAirportId")).longValue();
        
        Flight flight = flightService.createFlight(
            flightNumber, aircraftId, departureAirportId, arrivalAirportId
        );
        
        if (flight != null) {
            return ResponseEntity.ok(flight);
        }
        return ResponseEntity.badRequest().build();
    }
    
    /**
     * POST /api/flights/{id}/start
     * Démarre un vol
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Flight> startFlight(@PathVariable Long id) {
        Flight flight = flightService.startFlight(id);
        if (flight != null) {
            return ResponseEntity.ok(flight);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * POST /api/flights/{id}/complete
     * Termine un vol
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<Flight> completeFlight(@PathVariable Long id) {
        Flight flight = flightService.completeFlight(id);
        if (flight != null) {
            return ResponseEntity.ok(flight);
        }
        return ResponseEntity.notFound().build();
    }
}

