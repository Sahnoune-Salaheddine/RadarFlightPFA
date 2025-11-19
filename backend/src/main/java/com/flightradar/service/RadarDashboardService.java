package com.flightradar.service;

import com.flightradar.model.*;
import com.flightradar.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour le Dashboard CENTRE RADAR
 * Gère la vue radar, les communications ATC, et les autorisations
 */
@Service
@Slf4j
public class RadarDashboardService {
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private RadarCenterRepository radarCenterRepository;
    
    @Autowired
    private AirportRepository airportRepository;
    
    @Autowired
    private ATISDataRepository atisDataRepository;
    
    @Autowired
    private ATCMessageRepository atcMessageRepository;
    
    @Autowired
    private ConflictDetectionService conflictDetectionService;
    
    @Autowired
    private WeatherDataRepository weatherDataRepository;
    
    /**
     * Récupère toutes les données du dashboard radar pour un aéroport
     */
    public Map<String, Object> getRadarDashboard(Long airportId) {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Vérifier que l'aéroport existe
        Airport airport = airportRepository.findById(airportId)
            .orElseThrow(() -> new RuntimeException("Aéroport non trouvé: " + airportId));
        
        dashboard.put("airport", Map.of(
            "id", airport.getId(),
            "name", airport.getName(),
            "codeIATA", airport.getCodeIATA(),
            "latitude", airport.getLatitude(),
            "longitude", airport.getLongitude()
        ));
        
        // Récupérer le centre radar de l'aéroport (optionnel)
        Optional<RadarCenter> radarCenterOpt = radarCenterRepository.findByAirportId(airportId);
        Long radarCenterId = null;
        if (radarCenterOpt.isPresent()) {
            radarCenterId = radarCenterOpt.get().getId();
        }
        
        // ========== Avions dans le secteur ==========
        dashboard.put("aircraftInSector", getAircraftInSector(airportId));
        
        // ========== Conflits potentiels ==========
        dashboard.put("conflicts", getConflicts(airportId));
        
        // ========== Météo ATIS ==========
        dashboard.put("atis", getATISData(airportId));
        
        // ========== Demandes d'autorisation en attente ==========
        dashboard.put("pendingRequests", getPendingTakeoffRequests(airportId));
        
        // ========== Historique communications ATC ==========
        if (radarCenterId != null) {
            dashboard.put("atcHistory", getATCHistory(radarCenterId));
        } else {
            dashboard.put("atcHistory", new ArrayList<>());
        }
        
        return dashboard;
    }
    
    /**
     * Récupère les avions dans le secteur (rayon de 50 km autour de l'aéroport)
     */
    private List<Map<String, Object>> getAircraftInSector(Long airportId) {
        Airport airport = airportRepository.findById(airportId)
            .orElseThrow(() -> new RuntimeException("Aéroport non trouvé: " + airportId));
        
        List<Aircraft> allAircraft = aircraftRepository.findAll();
        List<Map<String, Object>> aircraftInSector = new ArrayList<>();
        
        double radiusKm = 50.0; // Rayon du secteur en km
        
        for (Aircraft aircraft : allAircraft) {
            if (aircraft.getPositionLat() != null && aircraft.getPositionLon() != null) {
                double distance = calculateDistance(
                    airport.getLatitude(), airport.getLongitude(),
                    aircraft.getPositionLat(), aircraft.getPositionLon()
                );
                
                if (distance <= radiusKm) {
                    Map<String, Object> aircraftData = new HashMap<>();
                    aircraftData.put("id", aircraft.getId());
                    aircraftData.put("registration", aircraft.getRegistration());
                    aircraftData.put("model", aircraft.getModel());
                    aircraftData.put("status", aircraft.getStatus());
                    aircraftData.put("latitude", aircraft.getPositionLat());
                    aircraftData.put("longitude", aircraft.getPositionLon());
                    aircraftData.put("altitude", aircraft.getAltitude());
                    aircraftData.put("altitudeFeet", aircraft.getAltitude() != null ? aircraft.getAltitude() * 3.28084 : 0);
                    aircraftData.put("speed", aircraft.getSpeed());
                    aircraftData.put("airSpeed", aircraft.getAirSpeed());
                    aircraftData.put("heading", aircraft.getHeading());
                    aircraftData.put("verticalSpeed", aircraft.getVerticalSpeed());
                    aircraftData.put("transponderCode", aircraft.getTransponderCode());
                    aircraftData.put("distance", distance);
                    
                    aircraftInSector.add(aircraftData);
                }
            }
        }
        
        return aircraftInSector;
    }
    
    /**
     * Récupère les conflits potentiels dans le secteur
     */
    private List<Map<String, Object>> getConflicts(Long airportId) {
        // Utiliser le service de détection de conflits
        // Pour l'instant, retourner une liste vide
        // TODO: Intégrer avec ConflictDetectionService
        return new ArrayList<>();
    }
    
    /**
     * Récupère les données ATIS de l'aéroport
     */
    private Map<String, Object> getATISData(Long airportId) {
        Optional<ATISData> atisOpt = atisDataRepository.findFirstByAirportIdOrderByTimestampDesc(airportId);
        
        if (atisOpt.isPresent()) {
            ATISData atis = atisOpt.get();
            Map<String, Object> atisData = new HashMap<>();
            atisData.put("vent", atis.getVent());
            atisData.put("visibilité", atis.getVisibilité());
            atisData.put("pression", atis.getPression());
            atisData.put("turbulence", atis.getTurbulence());
            atisData.put("temperature", atis.getTemperature());
            atisData.put("conditions", atis.getConditions());
            atisData.put("pisteEnService", atis.getPisteEnService());
            atisData.put("timestamp", atis.getTimestamp());
            return atisData;
        }
        
        // Si pas de données ATIS, utiliser les données météo
        Optional<WeatherData> weatherOpt = weatherDataRepository.findFirstByAirportIdOrderByTimestampDesc(airportId);
        if (weatherOpt.isPresent()) {
            WeatherData weather = weatherOpt.get();
            Map<String, Object> atisData = new HashMap<>();
            atisData.put("vent", weather.getWindSpeed());
            atisData.put("visibilité", weather.getVisibility());
            atisData.put("pression", weather.getPressure());
            atisData.put("turbulence", weather.getConditions());
            atisData.put("temperature", weather.getTemperature());
            atisData.put("conditions", weather.getConditions());
            atisData.put("pisteEnService", "N/A");
            return atisData;
        }
        
        return new HashMap<>();
    }
    
    /**
     * Récupère les demandes d'autorisation en attente
     */
    private List<Map<String, Object>> getPendingTakeoffRequests(Long airportId) {
        // TODO: Implémenter un système de suivi des demandes
        // Pour l'instant, retourner une liste vide
        return new ArrayList<>();
    }
    
    /**
     * Récupère l'historique des communications ATC
     */
    private List<Map<String, Object>> getATCHistory(Long radarId) {
        return atcMessageRepository.findByRadarIdOrderByTimestampDesc(radarId).stream()
            .map(msg -> {
                Map<String, Object> messageData = new HashMap<>();
                messageData.put("id", msg.getId());
                messageData.put("aircraftId", msg.getAircraftId());
                messageData.put("pilotId", msg.getPilotId());
                messageData.put("message", msg.getMessage());
                messageData.put("type", msg.getType());
                messageData.put("timestamp", msg.getTimestamp());
                return messageData;
            })
            .limit(50) // Derniers 50 messages
            .collect(Collectors.toList());
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
}

