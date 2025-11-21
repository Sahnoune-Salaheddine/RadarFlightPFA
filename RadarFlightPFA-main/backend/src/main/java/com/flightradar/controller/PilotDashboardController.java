package com.flightradar.controller;

import com.flightradar.model.dto.PilotDashboardDTO;
import com.flightradar.service.PilotDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    @PreAuthorize("hasAnyRole('PILOTE', 'ADMIN')")
    public ResponseEntity<?> getPilotDashboard(@PathVariable String username) {
        log.info("Requête dashboard reçue pour: {}", username);
        try {
            PilotDashboardDTO dashboard = pilotDashboardService.getPilotDashboard(username);
            log.info("Dashboard récupéré avec succès pour: {}", username);
            return ResponseEntity.ok(dashboard);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération du dashboard pour {}: {}", username, e.getMessage());
            // Si c'est une erreur "NO_AIRCRAFT_ASSIGNED", retourner un message clair
            if (e.getMessage() != null && e.getMessage().contains("NO_AIRCRAFT_ASSIGNED")) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Aucun avion assigné au pilote",
                    "code", "NO_AIRCRAFT_ASSIGNED",
                    "message", "Veuillez contacter l'administrateur pour assigner un avion"
                ));
            }
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du dashboard pour {}", username, e);
            return ResponseEntity.status(500).body(Map.of("error", "Erreur interne du serveur"));
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
    @PreAuthorize("hasAnyRole('PILOTE', 'ADMIN')")
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

