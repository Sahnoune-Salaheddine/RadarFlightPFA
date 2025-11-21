package com.flightradar.controller;

import com.flightradar.model.Aircraft;
import com.flightradar.model.AircraftStatus;
import com.flightradar.model.dto.LiveAircraft;
import com.flightradar.service.AircraftService;
import com.flightradar.service.OpenSkyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur REST pour les avions
 */
@RestController
@RequestMapping("/api/aircraft")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AircraftController {
    
    @Autowired
    private AircraftService aircraftService;
    
    @Autowired
    private OpenSkyService openSkyService;
    
    /**
     * GET /api/aircraft
     * Récupère tous les avions
     */
    @GetMapping
    public ResponseEntity<List<Aircraft>> getAllAircraft() {
        return ResponseEntity.ok(aircraftService.getAllAircraft());
    }
    
    /**
     * GET /api/aircraft/{id}
     * Récupère un avion par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Aircraft> getAircraftById(@PathVariable Long id) {
        Optional<Aircraft> aircraft = aircraftService.getAircraftById(id);
        return aircraft.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/aircraft/airport/{airportId}
     * Récupère les avions d'un aéroport
     */
    @GetMapping("/airport/{airportId}")
    public ResponseEntity<List<Aircraft>> getAircraftByAirport(@PathVariable Long airportId) {
        return ResponseEntity.ok(aircraftService.getAircraftByAirport(airportId));
    }
    
    /**
     * GET /api/aircraft/in-flight
     * Récupère tous les avions en vol
     */
    @GetMapping("/in-flight")
    public ResponseEntity<List<Aircraft>> getAircraftInFlight() {
        return ResponseEntity.ok(aircraftService.getAircraftInFlight());
    }
    
    /**
     * PUT /api/aircraft/{id}/updatePosition
     * Met à jour la position d'un avion
     */
    @PutMapping("/{id}/updatePosition")
    public ResponseEntity<Aircraft> updatePosition(
            @PathVariable Long id,
            @RequestBody Map<String, Double> position) {
        Aircraft aircraft = aircraftService.updatePosition(
            id,
            position.get("latitude"),
            position.get("longitude"),
            position.get("altitude"),
            position.get("speed"),
            position.get("heading")
        );
        
        if (aircraft != null) {
            return ResponseEntity.ok(aircraft);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * PUT /api/aircraft/{id}/status
     * Change le statut d'un avion
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Aircraft> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusData) {
        AircraftStatus status = AircraftStatus.valueOf(statusData.get("status"));
        Aircraft aircraft = aircraftService.changeStatus(id, status);
        
        if (aircraft != null) {
            return ResponseEntity.ok(aircraft);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * POST /api/aircraft/{id}/start-flight
     * Démarre un vol
     */
    @PostMapping("/{id}/start-flight")
    public ResponseEntity<Aircraft> startFlight(
            @PathVariable Long id,
            @RequestBody Map<String, Long> flightData) {
        Aircraft aircraft = aircraftService.startFlight(id, flightData.get("destinationAirportId"));
        
        if (aircraft != null) {
            return ResponseEntity.ok(aircraft);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * GET /api/aircraft/live
     * Récupère les avions en temps réel depuis OpenSky Network
     * 
     * @return Liste des avions live avec statut et radarStatus calculés
     */
    @GetMapping("/live")
    public ResponseEntity<List<LiveAircraft>> getLiveAircraft() {
        List<LiveAircraft> liveAircraft = openSkyService.getLiveAircraft();
        return ResponseEntity.ok(liveAircraft);
    }
    
    /**
     * GET /api/aircraft/live/country/{countryCode}
     * Récupère les avions live filtrés par pays
     * 
     * @param countryCode Code pays (ex: "Morocco", "France")
     * @return Liste filtrée
     */
    @GetMapping("/live/country/{countryCode}")
    public ResponseEntity<List<LiveAircraft>> getLiveAircraftByCountry(@PathVariable String countryCode) {
        List<LiveAircraft> liveAircraft = openSkyService.getLiveAircraftByCountry(countryCode);
        return ResponseEntity.ok(liveAircraft);
    }
    
    /**
     * GET /api/aircraft/live/radar-status/{status}
     * Récupère les avions live filtrés par statut radar
     * 
     * @param status Statut radar (ok, warning, danger)
     * @return Liste filtrée
     */
    @GetMapping("/live/radar-status/{status}")
    public ResponseEntity<List<LiveAircraft>> getLiveAircraftByRadarStatus(@PathVariable String status) {
        List<LiveAircraft> liveAircraft = openSkyService.getLiveAircraftByRadarStatus(status);
        return ResponseEntity.ok(liveAircraft);
    }
    
    /**
     * GET /api/aircraft/live/{icao24}
     * Récupère un avion live spécifique par son ICAO24
     * 
     * @param icao24 Identifiant ICAO24 (hexadécimal)
     * @return Avion live ou 404
     */
    @GetMapping("/live/{icao24}")
    public ResponseEntity<LiveAircraft> getLiveAircraftByIcao24(@PathVariable String icao24) {
        LiveAircraft aircraft = openSkyService.getLiveAircraftByIcao24(icao24);
        if (aircraft != null) {
            return ResponseEntity.ok(aircraft);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * GET /api/aircraft/pilot/{username}
     * Récupère l'avion d'un pilote par son username
     * 
     * @param username Username du pilote
     * @return Avion du pilote ou 404
     */
    @GetMapping("/pilot/{username}")
    public ResponseEntity<Aircraft> getAircraftByPilotUsername(@PathVariable String username) {
        Optional<Aircraft> aircraft = aircraftService.getAircraftByPilotUsername(username);
        return aircraft.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
}

