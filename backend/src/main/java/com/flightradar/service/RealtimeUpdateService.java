package com.flightradar.service;

import com.flightradar.model.Aircraft;
import com.flightradar.model.WeatherData;
import com.flightradar.service.AircraftService;
import com.flightradar.service.ConflictDetectionService;
import com.flightradar.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour envoyer les mises à jour en temps réel via WebSocket
 */
@Service
public class RealtimeUpdateService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private AircraftService aircraftService;
    
    @Autowired
    private WeatherService weatherService;
    
    @Autowired
    private ConflictDetectionService conflictDetectionService;
    
    /**
     * Envoie les positions des avions toutes les 5 secondes
     */
    @Scheduled(fixedRate = 5000)
    public void broadcastAircraftPositions() {
        List<Aircraft> aircraftList = aircraftService.getAllAircraft();
        
        Map<String, Object> update = new HashMap<>();
        update.put("type", "aircraft_positions");
        update.put("data", aircraftList);
        update.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/aircraft", update);
    }
    
    /**
     * Envoie les alertes météo toutes les 30 secondes
     */
    @Scheduled(fixedRate = 30000)
    public void broadcastWeatherAlerts() {
        List<WeatherData> alerts = weatherService.getWeatherAlerts();
        
        if (!alerts.isEmpty()) {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "weather_alerts");
            update.put("data", alerts);
            update.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/weather-alerts", update);
        }
    }
    
    /**
     * Envoie une mise à jour spécifique pour un avion
     */
    public void sendAircraftUpdate(Aircraft aircraft) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "aircraft_update");
        update.put("data", aircraft);
        update.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/aircraft/" + aircraft.getId(), update);
    }
    
    /**
     * Envoie une mise à jour météo pour un aéroport
     */
    public void sendWeatherUpdate(WeatherData weatherData) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "weather_update");
        update.put("data", weatherData);
        update.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/weather/" + weatherData.getAirport().getId(), update);
    }
    
    /**
     * Envoie les alertes de conflit toutes les 5 secondes
     */
    @Scheduled(fixedRate = 5000)
    public void broadcastConflictAlerts() {
        List<ConflictDetectionService.ConflictAlert> conflicts = conflictDetectionService.getActiveConflicts();
        
        if (!conflicts.isEmpty()) {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "conflict_alerts");
            update.put("data", conflicts);
            update.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/conflicts", update);
        }
    }
}

