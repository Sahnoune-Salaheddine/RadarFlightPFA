package com.flightradar.controller;

import com.flightradar.model.Airport;
import com.flightradar.model.WeatherData;
import com.flightradar.repository.AirportRepository;
import com.flightradar.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour les aéroports
 */
@RestController
@RequestMapping("/api/airports")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AirportController {
    
    @Autowired
    private AirportRepository airportRepository;
    
    @Autowired
    private WeatherService weatherService;
    
    /**
     * GET /api/airports
     * Récupère tous les aéroports
     */
    @GetMapping
    public ResponseEntity<List<Airport>> getAllAirports() {
        return ResponseEntity.ok(airportRepository.findAll());
    }
    
    /**
     * GET /api/airports/{id}
     * Récupère un aéroport par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Airport> getAirportById(@PathVariable Long id) {
        Optional<Airport> airport = airportRepository.findById(id);
        return airport.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/airports/code/{codeIATA}
     * Récupère un aéroport par code IATA
     */
    @GetMapping("/code/{codeIATA}")
    public ResponseEntity<Airport> getAirportByCode(@PathVariable String codeIATA) {
        Optional<Airport> airport = airportRepository.findByCodeIATA(codeIATA);
        return airport.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/airports/{id}/weather
     * Récupère les données météo actuelles d'un aéroport
     */
    @GetMapping("/{id}/weather")
    public ResponseEntity<WeatherData> getAirportWeather(@PathVariable Long id) {
        Optional<WeatherData> weather = weatherService.getCurrentWeather(id);
        return weather.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
}

