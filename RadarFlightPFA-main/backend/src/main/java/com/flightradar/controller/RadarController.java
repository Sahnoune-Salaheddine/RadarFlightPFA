package com.flightradar.controller;

import com.flightradar.model.Communication;
import com.flightradar.service.RadarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour les communications radar
 */
@RestController
@RequestMapping("/api/radar")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class RadarController {
    
    @Autowired
    private RadarService radarService;
    
    /**
     * POST /api/radar/sendMessage
     * Envoie un message depuis un centre radar
     */
    @PostMapping("/sendMessage")
    public ResponseEntity<Communication> sendMessage(@RequestBody Map<String, Object> messageData) {
        Long radarCenterId = ((Number) messageData.get("radarCenterId")).longValue();
        String receiverType = (String) messageData.get("receiverType"); // AIRCRAFT ou AIRPORT
        Long receiverId = ((Number) messageData.get("receiverId")).longValue();
        String message = (String) messageData.get("message");
        
        Communication communication;
        if ("AIRCRAFT".equals(receiverType)) {
            communication = radarService.sendMessageToAircraft(radarCenterId, receiverId, message);
        } else {
            communication = radarService.sendMessageToAirport(radarCenterId, receiverId, message);
        }
        
        return ResponseEntity.ok(communication);
    }
    
    /**
     * GET /api/radar/messages
     * Récupère tous les messages d'un centre radar
     */
    @GetMapping("/messages")
    public ResponseEntity<List<Communication>> getRadarMessages(
            @RequestParam Long radarCenterId) {
        return ResponseEntity.ok(radarService.getRadarMessages(radarCenterId));
    }
    
    /**
     * GET /api/radar/aircraft/{aircraftId}/messages
     * Récupère les communications d'un avion
     */
    @GetMapping("/aircraft/{aircraftId}/messages")
    public ResponseEntity<List<Communication>> getAircraftMessages(@PathVariable Long aircraftId) {
        return ResponseEntity.ok(radarService.getAircraftCommunications(aircraftId));
    }
    
    /**
     * POST /api/radar/requestTakeoffClearance
     * Demande d'autorisation de décollage
     */
    @PostMapping("/requestTakeoffClearance")
    public ResponseEntity<Communication> requestTakeoffClearance(@RequestBody Map<String, Object> request) {
        Long radarCenterId = ((Number) request.get("radarCenterId")).longValue();
        Long aircraftId = ((Number) request.get("aircraftId")).longValue();
        
        Communication response = radarService.requestTakeoffClearance(radarCenterId, aircraftId);
        
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * POST /api/radar/requestLandingClearance
     * Demande d'autorisation d'atterrissage
     */
    @PostMapping("/requestLandingClearance")
    public ResponseEntity<Communication> requestLandingClearance(@RequestBody Map<String, Object> request) {
        Long radarCenterId = ((Number) request.get("radarCenterId")).longValue();
        Long aircraftId = ((Number) request.get("aircraftId")).longValue();
        
        Communication response = radarService.requestLandingClearance(radarCenterId, aircraftId);
        
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * GET /api/radar/runwayStatus/{airportId}
     * Vérifie si la piste est libre
     */
    @GetMapping("/runwayStatus/{airportId}")
    public ResponseEntity<Map<String, Object>> getRunwayStatus(@PathVariable Long airportId) {
        boolean isClear = radarService.isRunwayClear(airportId);
        boolean weatherSuitable = radarService.isWeatherSuitableForTakeoff(airportId);
        
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("runwayClear", isClear);
        status.put("weatherSuitable", weatherSuitable);
        status.put("canTakeoff", isClear && weatherSuitable);
        
        return ResponseEntity.ok(status);
    }
}

