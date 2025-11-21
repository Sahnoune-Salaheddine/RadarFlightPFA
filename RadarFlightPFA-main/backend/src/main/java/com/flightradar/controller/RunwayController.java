package com.flightradar.controller;

import com.flightradar.model.Runway;
import com.flightradar.repository.RunwayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour les pistes
 */
@RestController
@RequestMapping("/api/runways")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class RunwayController {
    
    @Autowired
    private RunwayRepository runwayRepository;
    
    /**
     * GET /api/runways/airport/{airportId}
     * Récupère toutes les pistes d'un aéroport
     */
    @GetMapping("/airport/{airportId}")
    public ResponseEntity<List<Runway>> getRunwaysByAirport(@PathVariable Long airportId) {
        return ResponseEntity.ok(runwayRepository.findByAirportId(airportId));
    }
}

