package com.flightradar.controller;

import com.flightradar.model.User;
import com.flightradar.repository.UserRepository;
import com.flightradar.service.RadarDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur pour le Dashboard CENTRE RADAR
 * Expose les endpoints pour le dashboard radar
 */
@RestController
@RequestMapping("/api/radar/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
@PreAuthorize("hasAnyRole('CENTRE_RADAR', 'ADMIN')")
public class RadarDashboardController {
    
    @Autowired
    private RadarDashboardService radarDashboardService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * GET /api/radar/dashboard
     * Récupère toutes les données du dashboard radar pour l'aéroport de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getRadarDashboard(Authentication authentication) {
        try {
            // Récupérer l'utilisateur depuis l'Authentication (stocké par JwtAuthenticationFilter)
            User user;
            if (authentication.getPrincipal() instanceof User) {
                user = (User) authentication.getPrincipal();
            } else {
                // Fallback: récupérer par username si ce n'est pas un objet User
                String username = authentication.getName();
                user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + username));
            }
            
            // Récupérer l'aéroport associé au centre radar
            Long airportId = user.getAirportId();
            if (airportId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun aéroport associé à ce centre radar"));
            }
            
            Map<String, Object> dashboard = radarDashboardService.getRadarDashboard(airportId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du dashboard radar", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/radar/dashboard/aircraft
     * Récupère uniquement les avions dans le secteur
     */
    @GetMapping("/aircraft")
    public ResponseEntity<Map<String, Object>> getAircraftInSector(Authentication authentication) {
        try {
            // Récupérer l'utilisateur depuis l'Authentication
            User user;
            if (authentication.getPrincipal() instanceof User) {
                user = (User) authentication.getPrincipal();
            } else {
                String username = authentication.getName();
                user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + username));
            }
            
            Long airportId = user.getAirportId();
            if (airportId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun aéroport associé à ce centre radar"));
            }
            
            Map<String, Object> dashboard = radarDashboardService.getRadarDashboard(airportId);
            return ResponseEntity.ok(Map.of("aircraft", dashboard.get("aircraftInSector")));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des avions", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/radar/dashboard/atis
     * Récupère les données ATIS
     */
    @GetMapping("/atis")
    public ResponseEntity<Map<String, Object>> getATIS(Authentication authentication) {
        try {
            // Récupérer l'utilisateur depuis l'Authentication
            User user;
            if (authentication.getPrincipal() instanceof User) {
                user = (User) authentication.getPrincipal();
            } else {
                String username = authentication.getName();
                user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + username));
            }
            
            Long airportId = user.getAirportId();
            if (airportId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun aéroport associé à ce centre radar"));
            }
            
            Map<String, Object> dashboard = radarDashboardService.getRadarDashboard(airportId);
            return ResponseEntity.ok(Map.of("atis", dashboard.get("atis")));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des données ATIS", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}

