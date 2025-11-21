package com.flightradar.controller;

import com.flightradar.service.AdminDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
            // Retourner un objet avec l'erreur au lieu d'un body vide
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Erreur lors du chargement des données: " + e.getMessage());
            errorResponse.put("aircraftInFlight", 0);
            errorResponse.put("pilotsConnected", 0);
            errorResponse.put("trafficByAirport", new java.util.HashMap<>());
            errorResponse.put("radarCentersStatus", new java.util.ArrayList<>());
            return ResponseEntity.status(500).body(errorResponse);
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
    
    /**
     * GET /api/admin/operations/traffic
     * A) Nombre total de vols / trafic sur un intervalle
     */
    @GetMapping("/operations/traffic")
    public ResponseEntity<Map<String, Object>> getTrafficStatistics(
            @RequestParam(defaultValue = "DAY") String period) {
        try {
            Map<String, Object> stats = adminDashboardService.getTrafficStatistics(period);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques de trafic", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/operations/performance
     * B) KPI de performance
     */
    @GetMapping("/operations/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceKPIs() {
        try {
            Map<String, Object> kpis = adminDashboardService.getPerformanceKPIs();
            return ResponseEntity.ok(kpis);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des KPIs de performance", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/operations/users
     * C) Liste complète des utilisateurs / rôles
     */
    @GetMapping("/operations/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            java.util.List<Map<String, Object>> users = adminDashboardService.getAllUsersWithStatus();
            Map<String, Object> response = Map.of("users", users, "total", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/operations/radar-systems
     * D) Systèmes radar / infrastructure
     */
    @GetMapping("/operations/radar-systems")
    public ResponseEntity<Map<String, Object>> getRadarSystemsStatus() {
        try {
            java.util.List<Map<String, Object>> systems = adminDashboardService.getRadarSystemsStatus();
            Map<String, Object> response = Map.of("systems", systems, "total", systems.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du statut des systèmes radar", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/operations/weather
     * E) Météo globale
     */
    @GetMapping("/operations/weather")
    public ResponseEntity<Map<String, Object>> getGlobalWeather() {
        try {
            Map<String, Object> weather = adminDashboardService.getGlobalWeather();
            return ResponseEntity.ok(weather);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la météo globale", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/operations/logs
     * F) Journal / logs
     */
    @GetMapping("/operations/logs")
    public ResponseEntity<Map<String, Object>> getActivityLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String activityType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;
            
            Map<String, Object> logs = adminDashboardService.getActivityLogs(
                userId, activityType, severity, start, end, page, size);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des logs", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/operations/alerts
     * G) Alertes & notifications
     */
    @GetMapping("/operations/alerts")
    public ResponseEntity<Map<String, Object>> getAllAlerts() {
        try {
            Map<String, Object> alerts = adminDashboardService.getAllAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des alertes", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/admin/operations/reports
     * H) Rapports / analytics
     */
    @GetMapping("/operations/reports")
    public ResponseEntity<Map<String, Object>> getReportsAnalytics(
            @RequestParam(defaultValue = "WEEK") String period) {
        try {
            Map<String, Object> reports = adminDashboardService.getReportsAnalytics(period);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Erreur lors de la génération des rapports", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

