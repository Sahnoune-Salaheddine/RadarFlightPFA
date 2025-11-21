package com.flightradar.controller;

import com.flightradar.service.ATCService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur ATC (Air Traffic Control)
 * Gère les autorisations de décollage/atterrissage
 */
@RestController
@RequestMapping("/api/atc")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
public class ATCController {
    
    @Autowired
    private ATCService atcService;
    
    /**
     * POST /api/atc/request-takeoff-clearance
     * Demander une autorisation de décollage
     * 
     * @param requestBody Contient aircraftId
     * @return Réponse avec statut (GRANTED, REFUSED, PENDING) et message explicatif
     */
    @PostMapping("/request-takeoff-clearance")
    public ResponseEntity<Map<String, Object>> requestTakeoffClearance(
            @RequestBody Map<String, Long> requestBody) {
        try {
            Long aircraftId = requestBody.get("aircraftId");
            if (aircraftId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Map<String, Object> response = atcService.requestTakeoffClearance(aircraftId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la demande d'autorisation de décollage", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/atc/clearance-status/{aircraftId}
     * Récupérer le statut d'une autorisation
     * 
     * @param aircraftId ID de l'avion
     * @return Statut de l'autorisation
     */
    @GetMapping("/clearance-status/{aircraftId}")
    public ResponseEntity<Map<String, Object>> getClearanceStatus(@PathVariable Long aircraftId) {
        try {
            // Pour l'instant, on refait une demande pour obtenir le statut
            // TODO: Implémenter un système de suivi des autorisations
            Map<String, Object> response = atcService.requestTakeoffClearance(aircraftId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du statut d'autorisation", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

