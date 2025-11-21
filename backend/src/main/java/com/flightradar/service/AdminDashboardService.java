package com.flightradar.service;

import com.flightradar.model.*;
import com.flightradar.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour le Dashboard ADMIN
 * Calcule tous les KPIs aéronautiques réels
 */
@Service
@Slf4j
public class AdminDashboardService {
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private PilotRepository pilotRepository;
    
    @Autowired
    private AirportRepository airportRepository;
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private RadarCenterRepository radarCenterRepository;
    
    @Autowired
    private WeatherDataRepository weatherDataRepository;
    
    @Autowired
    private ConflictDetectionService conflictDetectionService;
    
    /**
     * Récupère toutes les données du dashboard admin
     */
    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // ========== KPIs Temps Réel ==========
        dashboard.put("aircraftInFlight", getAircraftInFlightCount());
        dashboard.put("pilotsConnected", getPilotsConnectedCount());
        dashboard.put("trafficByAirport", getTrafficByAirport());
        dashboard.put("radarCentersStatus", getRadarCentersStatus());
        dashboard.put("takeoffsLandingsToday", getTakeoffsLandingsToday());
        dashboard.put("delays", getDelaysStatistics());
        dashboard.put("weatherAlerts", getWeatherAlerts());
        dashboard.put("safetyIndicators", getSafetyIndicators());
        
        // ========== KPIs Performance ==========
        dashboard.put("atcPerformance", getATCPerformance());
        dashboard.put("inefficiency3D", getInefficiency3D());
        dashboard.put("trafficLoad", getTrafficLoad());
        dashboard.put("airportCapacity", getAirportCapacity());
        dashboard.put("dman", getDMAN());
        
        return dashboard;
    }
    
    /**
     * Nombre total d'avions en vol
     */
    private Integer getAircraftInFlightCount() {
        return aircraftRepository.findByStatus(AircraftStatus.EN_VOL).size();
    }
    
    /**
     * Nombre de pilotes connectés (simplifié - tous les pilotes avec avion assigné)
     */
    private Integer getPilotsConnectedCount() {
        return (int) pilotRepository.findAll().stream()
            .filter(p -> p.getAssignedAircraftId() != null)
            .count();
    }
    
    /**
     * Trafic en temps réel par aéroport
     */
    private Map<String, Object> getTrafficByAirport() {
        Map<String, Object> traffic = new HashMap<>();
        
        List<Airport> airports = airportRepository.findAll();
        for (Airport airport : airports) {
            List<Aircraft> aircraftAtAirport = aircraftRepository.findByAirportId(airport.getId());
            int inFlight = (int) aircraftAtAirport.stream()
                .filter(a -> a.getStatus() == AircraftStatus.EN_VOL)
                .count();
            int onGround = (int) aircraftAtAirport.stream()
                .filter(a -> a.getStatus() == AircraftStatus.AU_SOL)
                .count();
            
            Map<String, Object> airportTraffic = new HashMap<>();
            airportTraffic.put("inFlight", inFlight);
            airportTraffic.put("onGround", onGround);
            airportTraffic.put("total", aircraftAtAirport.size());
            
            traffic.put(airport.getCodeIATA(), airportTraffic);
        }
        
        return traffic;
    }
    
    /**
     * Statut des centres radar (charge, nombre d'avions suivis)
     */
    private List<Map<String, Object>> getRadarCentersStatus() {
        return radarCenterRepository.findAll().stream()
            .map(radar -> {
                Map<String, Object> status = new HashMap<>();
                status.put("id", radar.getId());
                status.put("name", radar.getName());
                status.put("code", radar.getCode());
                
                // Calculer la charge (nombre d'avions dans le secteur)
                if (radar.getAirport() != null) {
                    List<Aircraft> aircraftInSector = aircraftRepository.findByAirportId(radar.getAirport().getId());
                    int inFlight = (int) aircraftInSector.stream()
                        .filter(a -> a.getStatus() == AircraftStatus.EN_VOL)
                        .count();
                    
                    status.put("aircraftTracked", inFlight);
                    status.put("load", calculateRadarLoad(inFlight));
                } else {
                    status.put("aircraftTracked", 0);
                    status.put("load", 0);
                }
                
                return status;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Calculer la charge du radar (0-100%)
     */
    private Integer calculateRadarLoad(int aircraftCount) {
        // Capacité maximale estimée : 50 avions par radar
        int maxCapacity = 50;
        return Math.min(100, (aircraftCount * 100) / maxCapacity);
    }
    
    /**
     * Nombre de décollages / atterrissages du jour
     */
    private Map<String, Object> getTakeoffsLandingsToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        
        List<Flight> flightsToday = flightRepository.findAll().stream()
            .filter(f -> {
                if (f.getActualDeparture() != null) {
                    return !f.getActualDeparture().isBefore(startOfDay) && 
                           !f.getActualDeparture().isAfter(endOfDay);
                }
                return false;
            })
            .collect(Collectors.toList());
        
        long takeoffs = flightsToday.stream()
            .filter(f -> f.getActualDeparture() != null)
            .count();
        
        long landings = flightsToday.stream()
            .filter(f -> f.getActualArrival() != null && 
                        !f.getActualArrival().isBefore(startOfDay) && 
                        !f.getActualArrival().isAfter(endOfDay))
            .count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("takeoffs", takeoffs);
        result.put("landings", landings);
        result.put("total", takeoffs + landings);
        
        return result;
    }
    
    /**
     * Retards cumulés + retards moyens par aéroport
     */
    private Map<String, Object> getDelaysStatistics() {
        Map<String, Object> delays = new HashMap<>();
        
        List<Flight> flights = flightRepository.findAll();
        
        long totalDelayMinutes = 0;
        int delayedFlights = 0;
        
        for (Flight flight : flights) {
            if (flight.getScheduledDeparture() != null && flight.getActualDeparture() != null) {
                long delay = java.time.Duration.between(
                    flight.getScheduledDeparture(), 
                    flight.getActualDeparture()
                ).toMinutes();
                
                if (delay > 0) {
                    totalDelayMinutes += delay;
                    delayedFlights++;
                }
            }
        }
        
        delays.put("totalDelayMinutes", totalDelayMinutes);
        delays.put("delayedFlights", delayedFlights);
        delays.put("averageDelay", delayedFlights > 0 ? totalDelayMinutes / delayedFlights : 0);
        
        // Retards par aéroport
        Map<String, Object> delaysByAirport = new HashMap<>();
        List<Airport> airports = airportRepository.findAll();
        
        for (Airport airport : airports) {
            List<Flight> airportFlights = flightRepository.findByDepartureAirportId(airport.getId());
            long airportDelay = 0;
            int airportDelayedFlights = 0;
            
            for (Flight flight : airportFlights) {
                if (flight.getScheduledDeparture() != null && flight.getActualDeparture() != null) {
                    long delay = java.time.Duration.between(
                        flight.getScheduledDeparture(), 
                        flight.getActualDeparture()
                    ).toMinutes();
                    
                    if (delay > 0) {
                        airportDelay += delay;
                        airportDelayedFlights++;
                    }
                }
            }
            
            Map<String, Object> airportDelayStats = new HashMap<>();
            airportDelayStats.put("totalDelay", airportDelay);
            airportDelayStats.put("delayedFlights", airportDelayedFlights);
            airportDelayStats.put("averageDelay", airportDelayedFlights > 0 ? airportDelay / airportDelayedFlights : 0);
            
            delaysByAirport.put(airport.getCodeIATA(), airportDelayStats);
        }
        
        delays.put("byAirport", delaysByAirport);
        
        return delays;
    }
    
    /**
     * Alertes météo globales
     */
    private List<Map<String, Object>> getWeatherAlerts() {
        return weatherDataRepository.findByAlertTrue().stream()
            .map(weather -> {
                Map<String, Object> alert = new HashMap<>();
                alert.put("airportId", weather.getAirport().getId());
                alert.put("airportName", weather.getAirport().getName());
                alert.put("conditions", weather.getConditions());
                alert.put("timestamp", weather.getTimestamp());
                return alert;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Indicateurs de sécurité
     */
    private Map<String, Object> getSafetyIndicators() {
        Map<String, Object> safety = new HashMap<>();
        
        // Conflits détectés
        // Note: conflictDetectionService.detectConflicts() retourne void
        // On doit utiliser une autre méthode ou calculer manuellement
        List<Aircraft> aircraftInFlight = aircraftRepository.findByStatus(AircraftStatus.EN_VOL);
        int potentialConflicts = 0;
        
        for (int i = 0; i < aircraftInFlight.size(); i++) {
            for (int j = i + 1; j < aircraftInFlight.size(); j++) {
                Aircraft a1 = aircraftInFlight.get(i);
                Aircraft a2 = aircraftInFlight.get(j);
                
                if (a1.getPositionLat() != null && a1.getPositionLon() != null &&
                    a2.getPositionLat() != null && a2.getPositionLon() != null) {
                    
                    double distance = calculateDistance(
                        a1.getPositionLat(), a1.getPositionLon(),
                        a2.getPositionLat(), a2.getPositionLon()
                    );
                    
                    if (distance < 5.5) { // Distance minimale de sécurité
                        potentialConflicts++;
                    }
                }
            }
        }
        
        safety.put("potentialConflicts", potentialConflicts);
        safety.put("aircraftInFlight", aircraftInFlight.size());
        safety.put("safetyScore", calculateSafetyScore(aircraftInFlight.size(), potentialConflicts));
        
        return safety;
    }
    
    /**
     * Performance ATC
     */
    private Map<String, Object> getATCPerformance() {
        Map<String, Object> performance = new HashMap<>();
        
        // Métriques simplifiées
        int totalAircraft = aircraftRepository.findAll().size();
        int inFlight = aircraftRepository.findByStatus(AircraftStatus.EN_VOL).size();
        
        performance.put("totalAircraft", totalAircraft);
        performance.put("aircraftInFlight", inFlight);
        performance.put("efficiency", totalAircraft > 0 ? (inFlight * 100) / totalAircraft : 0);
        
        return performance;
    }
    
    /**
     * Inefficacité 3D (différence entre route prévue et route réelle)
     */
    private Map<String, Object> getInefficiency3D() {
        Map<String, Object> inefficiency = new HashMap<>();
        
        // Calcul simplifié : comparer trajectoire prévue vs réelle
        List<Aircraft> aircraft = aircraftRepository.findAll();
        double totalDeviation = 0;
        int aircraftWithDeviation = 0;
        
        for (Aircraft ac : aircraft) {
            if (ac.getTrajectoirePrévue() != null && ac.getTrajectoireRéelle() != null) {
                // TODO: Parser JSON et calculer déviation
                // Pour l'instant, on retourne des valeurs par défaut
                aircraftWithDeviation++;
            }
        }
        
        inefficiency.put("aircraftWithDeviation", aircraftWithDeviation);
        inefficiency.put("averageDeviation", aircraftWithDeviation > 0 ? totalDeviation / aircraftWithDeviation : 0);
        
        return inefficiency;
    }
    
    /**
     * Charge trafic à 15 min / 60 min
     */
    private Map<String, Object> getTrafficLoad() {
        Map<String, Object> load = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in15Min = now.plusMinutes(15);
        LocalDateTime in60Min = now.plusMinutes(60);
        
        // Vols prévus dans les 15 prochaines minutes
        long flightsIn15Min = flightRepository.findAll().stream()
            .filter(f -> f.getScheduledDeparture() != null &&
                        f.getScheduledDeparture().isAfter(now) &&
                        f.getScheduledDeparture().isBefore(in15Min))
            .count();
        
        // Vols prévus dans les 60 prochaines minutes
        long flightsIn60Min = flightRepository.findAll().stream()
            .filter(f -> f.getScheduledDeparture() != null &&
                        f.getScheduledDeparture().isAfter(now) &&
                        f.getScheduledDeparture().isBefore(in60Min))
            .count();
        
        load.put("next15Minutes", flightsIn15Min);
        load.put("next60Minutes", flightsIn60Min);
        
        return load;
    }
    
    /**
     * Capacité aéroports
     */
    private Map<String, Object> getAirportCapacity() {
        Map<String, Object> capacity = new HashMap<>();
        
        List<Airport> airports = airportRepository.findAll();
        for (Airport airport : airports) {
            List<Aircraft> aircraftAtAirport = aircraftRepository.findByAirportId(airport.getId());
            int currentLoad = aircraftAtAirport.size();
            
            // Capacité estimée : 20 avions par aéroport
            int maxCapacity = 20;
            int utilization = (currentLoad * 100) / maxCapacity;
            
            Map<String, Object> airportCapacity = new HashMap<>();
            airportCapacity.put("currentLoad", currentLoad);
            airportCapacity.put("maxCapacity", maxCapacity);
            airportCapacity.put("utilization", utilization);
            
            capacity.put(airport.getCodeIATA(), airportCapacity);
        }
        
        return capacity;
    }
    
    /**
     * DMAN (Departure Manager) : Temps cible de décollage (TTOT)
     */
    private Map<String, Object> getDMAN() {
        Map<String, Object> dman = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        List<Flight> upcomingFlights = flightRepository.findAll().stream()
            .filter(f -> f.getScheduledDeparture() != null &&
                        f.getScheduledDeparture().isAfter(now))
            .sorted((f1, f2) -> f1.getScheduledDeparture().compareTo(f2.getScheduledDeparture()))
            .limit(10)
            .collect(Collectors.toList());
        
        List<Map<String, Object>> ttotList = upcomingFlights.stream()
            .map(f -> {
                Map<String, Object> ttot = new HashMap<>();
                ttot.put("flightNumber", f.getFlightNumber());
                ttot.put("aircraftId", f.getAircraft() != null ? f.getAircraft().getId() : null);
                ttot.put("scheduledDeparture", f.getScheduledDeparture());
                ttot.put("targetTakeoffTime", f.getScheduledDeparture()); // TTOT = Scheduled pour l'instant
                return ttot;
            })
            .collect(Collectors.toList());
        
        dman.put("upcomingFlights", ttotList);
        dman.put("count", ttotList.size());
        
        return dman;
    }
    
    /**
     * Calculer le score de sécurité (0-100)
     */
    private Integer calculateSafetyScore(int aircraftInFlight, int conflicts) {
        if (aircraftInFlight == 0) return 100;
        
        double conflictRate = (double) conflicts / aircraftInFlight;
        int score = 100 - (int)(conflictRate * 100);
        return Math.max(0, Math.min(100, score));
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

