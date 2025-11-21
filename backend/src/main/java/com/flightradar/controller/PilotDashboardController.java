package com.flightradar.controller;

import com.flightradar.model.dto.PilotDashboardDTO;
import com.flightradar.service.PilotDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour le Dashboard Pilote
 * Expose les endpoints pour récupérer toutes les informations du dashboard
 */
@RestController
@RequestMapping("/api/pilots")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
public class PilotDashboardController {
    
    @Autowired
    private PilotDashboardService pilotDashboardService;
    
    /**
     * GET /api/pilots/{username}/dashboard
     * Récupère toutes les données du dashboard pour un pilote
     * 
     * @param username Username du pilote
     * @return DTO complet avec toutes les informations du dashboard
     */
    @GetMapping("/{username}/dashboard")
    public ResponseEntity<PilotDashboardDTO> getPilotDashboard(@PathVariable String username) {
        try {
            PilotDashboardDTO dashboard = pilotDashboardService.getPilotDashboard(username);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du dashboard pour {}", username, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/pilots/{username}/aircraft
     * Récupère l'avion assigné à un pilote
     * 
     * @param username Username du pilote
     * @return Informations de l'avion
     */
    @GetMapping("/{username}/aircraft")
    public ResponseEntity<?> getPilotAircraft(@PathVariable String username) {
        try {
            PilotDashboardDTO dashboard = pilotDashboardService.getPilotDashboard(username);
            // Retourner juste les infos de l'avion (simplifié)
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'avion pour {}", username, e);
            return ResponseEntity.notFound().build();
        }
    }
}

