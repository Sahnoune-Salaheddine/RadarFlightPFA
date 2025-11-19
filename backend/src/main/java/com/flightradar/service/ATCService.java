package com.flightradar.service;

import com.flightradar.model.Aircraft;
import com.flightradar.model.AircraftStatus;
import com.flightradar.model.Airport;
import com.flightradar.model.WeatherData;
import com.flightradar.repository.AircraftRepository;
import com.flightradar.repository.AirportRepository;
import com.flightradar.repository.WeatherDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service ATC (Air Traffic Control)
 * Gère les autorisations de décollage/atterrissage selon les règles ICAO/FAA
 */
@Service
@Slf4j
public class ATCService {
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private AirportRepository airportRepository;
    
    @Autowired
    private WeatherDataRepository weatherDataRepository;
    
    @Autowired
    private ConflictDetectionService conflictDetectionService;
    
    // ========== RÈGLES ICAO/FAA ==========
    
    /**
     * Visibilité minimale pour décollage (CAT I)
     * ICAO: 550m (1800ft) minimum
     */
    private static final double MIN_VISIBILITY_TAKEOFF = 0.55; // km
    
    /**
     * Vent maximum pour décollage
     * FAA: 30 kt (55 km/h) maximum
     */
    private static final double MAX_WIND_SPEED_TAKEOFF = 55.0; // km/h
    
    /**
     * Vent travers maximum pour décollage
     * FAA: 15 kt (28 km/h) maximum
     */
    private static final double MAX_CROSSWIND_TAKEOFF = 28.0; // km/h
    
    /**
     * Distance minimale entre avions (séparation minimale)
     * ICAO: 3 NM (5.5 km) minimum
     */
    private static final double MIN_SEPARATION_DISTANCE = 5.5; // km
    
    /**
     * Demander autorisation de décollage
     * Analyse en temps réel : trafic, météo, piste, risques
     */
    public Map<String, Object> requestTakeoffClearance(Long aircraftId) {
        log.info("Demande d'autorisation de décollage pour l'avion ID: {}", aircraftId);
        
        Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
        if (aircraftOpt.isEmpty()) {
            return createResponse("REFUSED", "Avion non trouvé", null);
        }
        
        Aircraft aircraft = aircraftOpt.get();
        
        // Vérifier que l'avion est au sol
        if (aircraft.getStatus() != AircraftStatus.AU_SOL) {
            return createResponse("REFUSED", 
                "L'avion n'est pas au sol. Statut actuel: " + aircraft.getStatus(), null);
        }
        
        // Vérifier que l'avion a un aéroport
        if (aircraft.getAirport() == null) {
            return createResponse("REFUSED", "Aucun aéroport assigné à l'avion", null);
        }
        
        Airport airport = aircraft.getAirport();
        
        // ========== ANALYSE EN TEMPS RÉEL ==========
        
        // 1. Vérifier la disponibilité de la piste
        boolean runwayClear = isRunwayClear(airport.getId());
        if (!runwayClear) {
            return createResponse("PENDING", 
                "Piste occupée. Veuillez patienter.", 
                "Un autre avion est en train de décoller ou d'atterrir.");
        }
        
        // 2. Vérifier les conditions météo
        Map<String, Object> weatherCheck = checkWeatherConditions(airport.getId());
        if (!(Boolean) weatherCheck.get("suitable")) {
            return createResponse("REFUSED", 
                "Conditions météo défavorables", 
                (String) weatherCheck.get("reason"));
        }
        
        // 3. Vérifier le trafic aérien (séparation minimale)
        Map<String, Object> trafficCheck = checkAirTraffic(aircraft, airport);
        if (!(Boolean) trafficCheck.get("safe")) {
            return createResponse("PENDING", 
                "Trafic aérien dense. Veuillez patienter.", 
                (String) trafficCheck.get("reason"));
        }
        
        // 4. Vérifier les alertes météo critiques
        List<String> criticalAlerts = checkCriticalWeatherAlerts(airport.getId());
        if (!criticalAlerts.isEmpty()) {
            return createResponse("REFUSED", 
                "Alertes météo critiques détectées", 
                String.join(", ", criticalAlerts));
        }
        
        // 5. Vérifier les risques potentiels
        Map<String, Object> riskCheck = checkPotentialRisks(aircraft, airport);
        if ((Integer) riskCheck.get("riskLevel") > 50) {
            return createResponse("REFUSED", 
                "Risques potentiels détectés", 
                (String) riskCheck.get("reason"));
        }
        
        // ========== AUTORISATION ACCORDÉE ==========
        log.info("Autorisation de décollage accordée pour l'avion ID: {}", aircraftId);
        
        return createResponse("GRANTED", 
            "Autorisation de décollage accordée", 
            "Toutes les conditions sont remplies. Vous pouvez décoller.");
    }
    
    /**
     * Vérifier si la piste est libre
     */
    private boolean isRunwayClear(Long airportId) {
        List<Aircraft> aircraftAtAirport = aircraftRepository.findByAirportId(airportId);
        
        for (Aircraft ac : aircraftAtAirport) {
            if (ac.getStatus() == AircraftStatus.DECOLLAGE || 
                ac.getStatus() == AircraftStatus.ATTERRISSAGE) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Vérifier les conditions météo selon ICAO/FAA
     */
    private Map<String, Object> checkWeatherConditions(Long airportId) {
        Map<String, Object> result = new HashMap<>();
        result.put("suitable", true);
        result.put("reason", "");
        
        Optional<WeatherData> weatherOpt = weatherDataRepository
            .findFirstByAirportIdOrderByTimestampDesc(airportId);
        
        if (weatherOpt.isEmpty()) {
            result.put("suitable", false);
            result.put("reason", "Données météo indisponibles");
            return result;
        }
        
        WeatherData weather = weatherOpt.get();
        
        // 1. Vérifier la visibilité minimale (550m minimum)
        if (weather.getVisibility() != null && weather.getVisibility() < MIN_VISIBILITY_TAKEOFF) {
            result.put("suitable", false);
            result.put("reason", String.format(
                "Visibilité insuffisante: %.2f km (minimum requis: %.2f km)", 
                weather.getVisibility(), MIN_VISIBILITY_TAKEOFF));
            return result;
        }
        
        // 2. Vérifier la vitesse du vent (max 55 km/h)
        if (weather.getWindSpeed() != null && weather.getWindSpeed() > MAX_WIND_SPEED_TAKEOFF) {
            result.put("suitable", false);
            result.put("reason", String.format(
                "Vent trop fort: %.1f km/h (maximum autorisé: %.1f km/h)", 
                weather.getWindSpeed(), MAX_WIND_SPEED_TAKEOFF));
            return result;
        }
        
        // 3. Vérifier le vent travers (max 28 km/h)
        if (weather.getCrosswind() != null && weather.getCrosswind() > MAX_CROSSWIND_TAKEOFF) {
            result.put("suitable", false);
            result.put("reason", String.format(
                "Vent travers trop fort: %.1f km/h (maximum autorisé: %.1f km/h)", 
                weather.getCrosswind(), MAX_CROSSWIND_TAKEOFF));
            return result;
        }
        
        return result;
    }
    
    /**
     * Vérifier le trafic aérien (séparation minimale)
     */
    private Map<String, Object> checkAirTraffic(Aircraft aircraft, Airport airport) {
        Map<String, Object> result = new HashMap<>();
        result.put("safe", true);
        result.put("reason", "");
        
        List<Aircraft> allAircraft = aircraftRepository.findAll();
        
        // Vérifier la distance avec les autres avions en vol
        for (Aircraft other : allAircraft) {
            if (other.getId().equals(aircraft.getId())) continue;
            if (other.getStatus() != AircraftStatus.EN_VOL) continue;
            if (other.getPositionLat() == null || other.getPositionLon() == null) continue;
            if (aircraft.getPositionLat() == null || aircraft.getPositionLon() == null) continue;
            
            double distance = calculateDistance(
                aircraft.getPositionLat(), aircraft.getPositionLon(),
                other.getPositionLat(), other.getPositionLon()
            );
            
            if (distance < MIN_SEPARATION_DISTANCE) {
                result.put("safe", false);
                result.put("reason", String.format(
                    "Avion trop proche: %.2f km (séparation minimale: %.2f km)", 
                    distance, MIN_SEPARATION_DISTANCE));
                return result;
            }
        }
        
        return result;
    }
    
    /**
     * Vérifier les alertes météo critiques
     */
    private List<String> checkCriticalWeatherAlerts(Long airportId) {
        List<String> alerts = new java.util.ArrayList<>();
        
        Optional<WeatherData> weatherOpt = weatherDataRepository
            .findFirstByAirportIdOrderByTimestampDesc(airportId);
        
        if (weatherOpt.isEmpty()) return alerts;
        
        WeatherData weather = weatherOpt.get();
        
        // Vérifier les conditions critiques
        if (weather.getAlert() != null && weather.getAlert()) {
            String conditions = weather.getConditions() != null ? weather.getConditions() : "";
            
            // Storm (tempête)
            if (conditions.toLowerCase().contains("storm") || 
                conditions.toLowerCase().contains("thunderstorm")) {
                alerts.add("Tempête détectée");
            }
            
            // Wind shear (cisaillement de vent)
            if (weather.getWindSpeed() != null && weather.getWindSpeed() > 80) {
                alerts.add("Cisaillement de vent possible");
            }
            
            // Turbulence sévère
            if (conditions.toLowerCase().contains("severe turbulence")) {
                alerts.add("Turbulence sévère");
            }
        }
        
        return alerts;
    }
    
    /**
     * Vérifier les risques potentiels
     */
    private Map<String, Object> checkPotentialRisks(Aircraft aircraft, Airport airport) {
        Map<String, Object> result = new HashMap<>();
        int riskLevel = 0;
        StringBuilder reasons = new StringBuilder();
        
        // Vérifier les conflits de trajectoire potentiels
        // Vérifier avec les autres avions en vol
        List<Aircraft> aircraftInFlight = aircraftRepository.findByStatus(AircraftStatus.EN_VOL);
        for (Aircraft other : aircraftInFlight) {
            if (other.getId().equals(aircraft.getId())) continue;
            if (other.getPositionLat() == null || other.getPositionLon() == null) continue;
            if (aircraft.getPositionLat() == null || aircraft.getPositionLon() == null) continue;
            
            double distance = calculateDistance(
                aircraft.getPositionLat(), aircraft.getPositionLon(),
                other.getPositionLat(), other.getPositionLon()
            );
            
            if (distance < MIN_SEPARATION_DISTANCE) {
                riskLevel += 30;
                reasons.append("Conflit de trajectoire potentiel détecté. ");
                break;
            }
        }
        
        // Vérifier l'état de l'avion
        if (aircraft.getTransponderCode() == null || aircraft.getTransponderCode().isEmpty()) {
            riskLevel += 20;
            reasons.append("Code transpondeur manquant. ");
        }
        
        result.put("riskLevel", riskLevel);
        result.put("reason", reasons.toString().isEmpty() ? "Aucun risque détecté" : reasons.toString());
        
        return result;
    }
    
    /**
     * Calculer la distance entre deux points GPS (formule de Haversine)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Créer une réponse standardisée
     */
    private Map<String, Object> createResponse(String status, String message, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status); // "GRANTED", "REFUSED", "PENDING"
        response.put("message", message);
        response.put("details", details);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}

