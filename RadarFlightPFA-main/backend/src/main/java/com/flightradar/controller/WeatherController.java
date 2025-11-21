package com.flightradar.controller;

import com.flightradar.model.WeatherData;
import com.flightradar.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour les données météorologiques
 */
@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class WeatherController {
    
    @Autowired
    private WeatherService weatherService;
    
    /**
     * GET /api/weather/airport/{airportId}
     * Récupère les données météo actuelles d'un aéroport
     */
    @GetMapping("/airport/{airportId}")
    public ResponseEntity<WeatherData> getAirportWeather(@PathVariable Long airportId) {
        Optional<WeatherData> weather = weatherService.getCurrentWeather(airportId);
        return weather.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/weather/alerts
     * Récupère toutes les alertes météo actives
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<WeatherData>> getWeatherAlerts() {
        return ResponseEntity.ok(weatherService.getWeatherAlerts());
    }
}

