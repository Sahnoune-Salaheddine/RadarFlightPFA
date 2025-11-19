package com.flightradar.controller;

import com.flightradar.service.AdminDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur pour le Dashboard ADMIN
 * Expose les endpoints pour récupérer tous les KPIs aéronautiques
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    
    @Autowired
    private AdminDashboardService adminDashboardService;
    
    /**
     * GET /api/admin/dashboard
     * Récupère toutes les données du dashboard admin
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        try {
            Map<String, Object> dashboard = adminDashboardService.getAdminDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du dashboard admin", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/kpis
     * Récupère uniquement les KPIs temps réel
     */
    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKPIs() {
        try {
            Map<String, Object> dashboard = adminDashboardService.getAdminDashboard();
            // Extraire uniquement les KPIs
            Map<String, Object> kpis = Map.of(
                "aircraftInFlight", dashboard.get("aircraftInFlight"),
                "pilotsConnected", dashboard.get("pilotsConnected"),
                "trafficByAirport", dashboard.get("trafficByAirport"),
                "radarCentersStatus", dashboard.get("radarCentersStatus"),
                "takeoffsLandingsToday", dashboard.get("takeoffsLandingsToday"),
                "delays", dashboard.get("delays"),
                "weatherAlerts", dashboard.get("weatherAlerts"),
                "safetyIndicators", dashboard.get("safetyIndicators")
            );
            return ResponseEntity.ok(kpis);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des KPIs", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/statistics
     * Récupère les statistiques de performance
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> dashboard = adminDashboardService.getAdminDashboard();
            // Extraire uniquement les statistiques
            Map<String, Object> statistics = Map.of(
                "atcPerformance", dashboard.get("atcPerformance"),
                "inefficiency3D", dashboard.get("inefficiency3D"),
                "trafficLoad", dashboard.get("trafficLoad"),
                "airportCapacity", dashboard.get("airportCapacity"),
                "dman", dashboard.get("dman")
            );
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

