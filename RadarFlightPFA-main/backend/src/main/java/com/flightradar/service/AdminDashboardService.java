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
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    /**
     * Récupère toutes les données du dashboard admin
     */
    public Map<String, Object> getAdminDashboard() {
        try {
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
        } catch (Exception e) {
            log.error("Erreur lors de la construction du dashboard admin", e);
            // Retourner un dashboard minimal en cas d'erreur
            Map<String, Object> errorDashboard = new HashMap<>();
            errorDashboard.put("error", "Erreur lors du chargement des données");
            errorDashboard.put("message", e.getMessage());
            errorDashboard.put("aircraftInFlight", 0);
            errorDashboard.put("pilotsConnected", 0);
            errorDashboard.put("trafficByAirport", new HashMap<>());
            errorDashboard.put("radarCentersStatus", new java.util.ArrayList<>());
            return errorDashboard;
        }
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
        try {
            return (int) pilotRepository.findAll().stream()
                .filter(p -> {
                    // Vérifier si le pilote a un avion assigné (via assignedAircraftId ou via relation)
                    if (p.getAssignedAircraftId() != null) {
                        return true;
                    }
                    // Vérifier via la relation aircraft (OneToOne, donc vérifier != null)
                    if (p.getAircraft() != null) {
                        return true;
                    }
                    return false;
                })
                .count();
        } catch (Exception e) {
            log.error("Erreur lors du calcul des pilotes connectés", e);
            return 0;
        }
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
        try {
            return radarCenterRepository.findAll().stream()
                .map(radar -> {
                    try {
                        Map<String, Object> status = new HashMap<>();
                        status.put("id", radar.getId());
                        status.put("name", radar.getName());
                        status.put("code", radar.getCode());
                        
                        // Ajouter le statut et la portée si disponibles
                        if (radar.getStatus() != null) {
                            status.put("status", radar.getStatus().toString());
                        }
                        if (radar.getRange() != null) {
                            status.put("range", radar.getRange());
                        }
                        if (radar.getFrequency() != null) {
                            status.put("frequency", radar.getFrequency());
                        }
                        
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
                    } catch (Exception e) {
                        log.warn("Erreur lors du traitement d'un radar (ID: {}): {}", radar.getId(), e.getMessage());
                        Map<String, Object> errorStatus = new HashMap<>();
                        errorStatus.put("id", radar.getId());
                        errorStatus.put("name", radar.getName() != null ? radar.getName() : "Unknown");
                        errorStatus.put("code", radar.getCode() != null ? radar.getCode() : "UNKNOWN");
                        errorStatus.put("aircraftTracked", 0);
                        errorStatus.put("load", 0);
                        errorStatus.put("error", true);
                        return errorStatus;
                    }
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du statut des radars", e);
            return new java.util.ArrayList<>();
        }
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
    
    // ========== NOUVELLES MÉTHODES POUR VUE D'ENSEMBLE DES OPÉRATIONS ==========
    
    /**
     * A) Nombre total de vols sur un intervalle de temps
     */
    public Map<String, Object> getTrafficStatistics(String period) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        
        switch (period.toUpperCase()) {
            case "DAY":
            case "JOUR":
                start = now.minusDays(1);
                break;
            case "WEEK":
            case "SEMAINE":
                start = now.minusWeeks(1);
                break;
            case "MONTH":
            case "MOIS":
                start = now.minusMonths(1);
                break;
            default:
                start = now.minusDays(1);
        }
        
        List<Flight> flightsInPeriod = flightRepository.findAll().stream()
            .filter(f -> f.getScheduledDeparture() != null && 
                       f.getScheduledDeparture().isAfter(start) &&
                       f.getScheduledDeparture().isBefore(now))
            .collect(Collectors.toList());
        
        stats.put("totalFlights", flightsInPeriod.size());
        stats.put("period", period);
        stats.put("startDate", start);
        stats.put("endDate", now);
        
        // Répartition par statut
        Map<String, Long> byStatus = flightsInPeriod.stream()
            .collect(Collectors.groupingBy(
                f -> f.getFlightStatus() != null ? f.getFlightStatus().toString() : "UNKNOWN",
                Collectors.counting()
            ));
        stats.put("byStatus", byStatus);
        
        // Répartition par jour (pour graphique)
        Map<String, Long> byDay = flightsInPeriod.stream()
            .collect(Collectors.groupingBy(
                f -> f.getScheduledDeparture().toLocalDate().toString(),
                Collectors.counting()
            ));
        stats.put("byDay", byDay);
        
        return stats;
    }
    
    /**
     * B) KPI de performance détaillés
     */
    public Map<String, Object> getPerformanceKPIs() {
        Map<String, Object> kpis = new HashMap<>();
        
        List<Flight> allFlights = flightRepository.findAll();
        long totalFlights = allFlights.size();
        
        // Retards
        Map<String, Object> delays = getDelaysStatistics();
        kpis.put("totalDelays", delays.get("totalDelayMinutes"));
        kpis.put("delayedFlights", delays.get("delayedFlights"));
        kpis.put("averageDelay", delays.get("averageDelay"));
        
        // Vols annulés
        long cancelledFlights = allFlights.stream()
            .filter(f -> f.getFlightStatus() != null && 
                        f.getFlightStatus().toString().equals("ANNULE"))
            .count();
        kpis.put("cancelledFlights", cancelledFlights);
        
        // Vols à l'heure
        long onTimeFlights = allFlights.stream()
            .filter(f -> {
                if (f.getScheduledDeparture() != null && f.getActualDeparture() != null) {
                    long delay = java.time.Duration.between(
                        f.getScheduledDeparture(), 
                        f.getActualDeparture()
                    ).toMinutes();
                    return delay <= 0 || delay <= 15; // Tolérance de 15 minutes
                }
                return false;
            })
            .count();
        
        double onTimePercentage = totalFlights > 0 ? (onTimeFlights * 100.0) / totalFlights : 0;
        kpis.put("onTimeFlights", onTimeFlights);
        kpis.put("onTimePercentage", Math.round(onTimePercentage * 100.0) / 100.0);
        
        // Efficacité opérationnelle
        kpis.put("operationalEfficiency", Math.round(onTimePercentage * 100.0) / 100.0);
        
        return kpis;
    }
    
    /**
     * C) Liste complète des utilisateurs avec statut
     */
    public List<Map<String, Object>> getAllUsersWithStatus() {
        return userRepository.findAll().stream()
            .map(user -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("role", user.getRole().toString());
                userData.put("airportId", user.getAirportId());
                userData.put("pilotId", user.getPilotId());
                
                // Déterminer le statut actif/inactif
                // Un utilisateur est actif s'il a un avion assigné (PILOTE) ou un aéroport (CENTRE_RADAR)
                boolean isActive = false;
                if (user.getRole() == Role.PILOTE && user.getPilotId() != null) {
                    isActive = pilotRepository.findById(user.getPilotId())
                        .map(p -> p.getAssignedAircraftId() != null)
                        .orElse(false);
                } else if (user.getRole() == Role.CENTRE_RADAR && user.getAirportId() != null) {
                    isActive = true; // Les centres radar sont toujours actifs s'ils ont un aéroport
                } else if (user.getRole() == Role.ADMIN) {
                    isActive = true; // Les admins sont toujours actifs
                }
                
                userData.put("isActive", isActive);
                
                // Informations supplémentaires selon le rôle
                if (user.getRole() == Role.PILOTE && user.getPilotId() != null) {
                    pilotRepository.findById(user.getPilotId()).ifPresent(pilot -> {
                        userData.put("pilotName", pilot.getFirstName() + " " + pilot.getLastName());
                        userData.put("license", pilot.getLicense());
                    });
                }
                
                return userData;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * D) Statut détaillé des systèmes radar
     */
    public List<Map<String, Object>> getRadarSystemsStatus() {
        return radarCenterRepository.findAll().stream()
            .map(radar -> {
                Map<String, Object> status = new HashMap<>();
                status.put("id", radar.getId());
                status.put("name", radar.getName());
                status.put("code", radar.getCode());
                status.put("frequency", radar.getFrequency());
                
                if (radar.getAirport() != null) {
                    status.put("airportId", radar.getAirport().getId());
                    status.put("airportName", radar.getAirport().getName());
                    status.put("airportCode", radar.getAirport().getCodeIATA());
                    
                    // Calculer la charge et le statut
                    List<Aircraft> aircraftInSector = aircraftRepository.findByAirportId(radar.getAirport().getId());
                    int inFlight = (int) aircraftInSector.stream()
                        .filter(a -> a.getStatus() == AircraftStatus.EN_VOL)
                        .count();
                    
                    int load = calculateRadarLoad(inFlight);
                    status.put("aircraftTracked", inFlight);
                    status.put("load", load);
                    
                    // Déterminer le statut de santé
                    String healthStatus;
                    if (load >= 90) {
                        healthStatus = "CRITICAL";
                    } else if (load >= 70) {
                        healthStatus = "WARNING";
                    } else {
                        healthStatus = "HEALTHY";
                    }
                    status.put("healthStatus", healthStatus);
                    status.put("isAvailable", true); // Simplifié - toujours disponible
                } else {
                    status.put("aircraftTracked", 0);
                    status.put("load", 0);
                    status.put("healthStatus", "UNKNOWN");
                    status.put("isAvailable", false);
                }
                
                return status;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * E) Météo globale avec alertes SIGMET/AIRMET
     */
    public Map<String, Object> getGlobalWeather() {
        Map<String, Object> weather = new HashMap<>();
        
        List<WeatherData> allWeather = weatherDataRepository.findAll();
        List<WeatherData> alerts = weatherDataRepository.findByAlertTrue();
        
        weather.put("totalStations", allWeather.size());
        weather.put("activeAlerts", alerts.size());
        
        // Alertes par type
        List<Map<String, Object>> alertDetails = alerts.stream()
            .map(w -> {
                Map<String, Object> alert = new HashMap<>();
                alert.put("airportId", w.getAirport().getId());
                alert.put("airportName", w.getAirport().getName());
                alert.put("airportCode", w.getAirport().getCodeIATA());
                alert.put("conditions", w.getConditions());
                alert.put("windSpeed", w.getWindSpeed());
                alert.put("windDirection", w.getWindDirection());
                alert.put("visibility", w.getVisibility());
                alert.put("temperature", w.getTemperature());
                alert.put("timestamp", w.getTimestamp());
                
                // Déterminer le type d'alerte (SIGMET/AIRMET simplifié)
                String alertType = "AIRMET";
                if (w.getWindSpeed() != null && w.getWindSpeed() > 50) {
                    alertType = "SIGMET"; // Vent fort = SIGMET
                } else if (w.getVisibility() != null && w.getVisibility() < 1.0) {
                    alertType = "SIGMET"; // Visibilité très basse = SIGMET
                }
                alert.put("alertType", alertType);
                
                // Criticité
                String severity = "LOW";
                if (w.getWindSpeed() != null && w.getWindSpeed() > 60) {
                    severity = "HIGH";
                } else if (w.getWindSpeed() != null && w.getWindSpeed() > 40 || 
                          (w.getVisibility() != null && w.getVisibility() < 0.5)) {
                    severity = "MEDIUM";
                }
                alert.put("severity", severity);
                
                return alert;
            })
            .collect(Collectors.toList());
        
        weather.put("alerts", alertDetails);
        
        // Indicateurs critiques
        long strongWinds = allWeather.stream()
            .filter(w -> w.getWindSpeed() != null && w.getWindSpeed() > 30)
            .count();
        long lowVisibility = allWeather.stream()
            .filter(w -> w.getVisibility() != null && w.getVisibility() < 3.0)
            .count();
        
        weather.put("strongWindsCount", strongWinds);
        weather.put("lowVisibilityCount", lowVisibility);
        
        return weather;
    }
    
    /**
     * F) Journal d'activité avec filtres
     */
    public Map<String, Object> getActivityLogs(Long userId, String activityType, String severity, 
                                                LocalDateTime startDate, LocalDateTime endDate, 
                                                int page, int size) {
        Map<String, Object> result = new HashMap<>();
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size);
        
        ActivityLog.ActivityType typeEnum = null;
        if (activityType != null && !activityType.isEmpty()) {
            try {
                typeEnum = ActivityLog.ActivityType.valueOf(activityType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Type invalide, on ignore
            }
        }
        
        ActivityLog.LogSeverity severityEnum = null;
        if (severity != null && !severity.isEmpty()) {
            try {
                severityEnum = ActivityLog.LogSeverity.valueOf(severity.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Sévérité invalide, on ignore
            }
        }
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        org.springframework.data.domain.Page<ActivityLog> logsPage = 
            activityLogRepository.findWithFilters(userId, typeEnum, severityEnum, startDate, endDate, pageable);
        
        List<Map<String, Object>> logs = logsPage.getContent().stream()
            .map(log -> {
                Map<String, Object> logData = new HashMap<>();
                logData.put("id", log.getId());
                logData.put("userId", log.getUserId());
                logData.put("username", log.getUsername());
                logData.put("activityType", log.getActivityType().toString());
                logData.put("description", log.getDescription());
                logData.put("entityType", log.getEntityType());
                logData.put("entityId", log.getEntityId());
                logData.put("timestamp", log.getTimestamp());
                logData.put("severity", log.getSeverity().toString());
                logData.put("ipAddress", log.getIpAddress());
                return logData;
            })
            .collect(Collectors.toList());
        
        result.put("logs", logs);
        result.put("totalElements", logsPage.getTotalElements());
        result.put("totalPages", logsPage.getTotalPages());
        result.put("currentPage", logsPage.getNumber());
        result.put("pageSize", logsPage.getSize());
        
        return result;
    }
    
    /**
     * G) Alertes et notifications consolidées
     */
    public Map<String, Object> getAllAlerts() {
        Map<String, Object> alerts = new HashMap<>();
        
        // Alertes météo
        List<Map<String, Object>> weatherAlerts = getWeatherAlerts();
        
        // Alertes radar (problèmes de charge)
        List<Map<String, Object>> radarAlerts = getRadarCentersStatus().stream()
            .filter(radar -> {
                Integer load = (Integer) radar.get("load");
                return load != null && load >= 80;
            })
            .map(radar -> {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "RADAR_OVERLOAD");
                alert.put("radarName", radar.get("name"));
                alert.put("radarCode", radar.get("code"));
                alert.put("load", radar.get("load"));
                alert.put("severity", ((Integer) radar.get("load")) >= 90 ? "HIGH" : "MEDIUM");
                alert.put("timestamp", LocalDateTime.now());
                return alert;
            })
            .collect(Collectors.toList());
        
        // Alertes de performance (retards importants)
        Map<String, Object> performanceKPIs = getPerformanceKPIs();
        List<Map<String, Object>> performanceAlerts = new java.util.ArrayList<>();
        
        if ((Long) performanceKPIs.get("delayedFlights") > 10) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "PERFORMANCE_DEGRADATION");
            alert.put("description", "Nombre élevé de vols retardés");
            alert.put("delayedFlights", performanceKPIs.get("delayedFlights"));
            alert.put("severity", "MEDIUM");
            alert.put("timestamp", LocalDateTime.now());
            performanceAlerts.add(alert);
        }
        
        // Consolidation
        List<Map<String, Object>> allAlerts = new java.util.ArrayList<>();
        allAlerts.addAll(weatherAlerts.stream()
            .map(w -> {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "WEATHER");
                alert.put("description", "Alerte météo: " + w.get("conditions"));
                alert.put("airportName", w.get("airportName"));
                alert.put("severity", "MEDIUM");
                alert.put("timestamp", w.get("timestamp"));
                return alert;
            })
            .collect(Collectors.toList()));
        allAlerts.addAll(radarAlerts);
        allAlerts.addAll(performanceAlerts);
        
        // Trier par criticité et date
        allAlerts.sort((a1, a2) -> {
            String s1 = (String) a1.get("severity");
            String s2 = (String) a2.get("severity");
            int severityCompare = getSeverityOrder(s2) - getSeverityOrder(s1);
            if (severityCompare != 0) return severityCompare;
            
            LocalDateTime t1 = (LocalDateTime) a1.get("timestamp");
            LocalDateTime t2 = (LocalDateTime) a2.get("timestamp");
            return t2.compareTo(t1);
        });
        
        alerts.put("allAlerts", allAlerts);
        alerts.put("highPriority", allAlerts.stream()
            .filter(a -> "HIGH".equals(a.get("severity")))
            .count());
        alerts.put("mediumPriority", allAlerts.stream()
            .filter(a -> "MEDIUM".equals(a.get("severity")))
            .count());
        alerts.put("lowPriority", allAlerts.stream()
            .filter(a -> "LOW".equals(a.get("severity")))
            .count());
        
        return alerts;
    }
    
    private int getSeverityOrder(String severity) {
        switch (severity) {
            case "HIGH": return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }
    
    /**
     * H) Rapports et analytics
     */
    public Map<String, Object> getReportsAnalytics(String period) {
        Map<String, Object> reports = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        
        switch (period.toUpperCase()) {
            case "DAY":
            case "JOUR":
                start = now.minusDays(1);
                break;
            case "WEEK":
            case "SEMAINE":
                start = now.minusWeeks(1);
                break;
            case "MONTH":
            case "MOIS":
                start = now.minusMonths(1);
                break;
            default:
                start = now.minusDays(1);
        }
        
        // Statistiques de trafic
        Map<String, Object> trafficStats = getTrafficStatistics(period);
        reports.put("trafficStats", trafficStats);
        
        // KPIs de performance
        Map<String, Object> performanceKPIs = getPerformanceKPIs();
        reports.put("performanceKPIs", performanceKPIs);
        
        // Tendances
        Map<String, Object> trends = new HashMap<>();
        
        // Tendances de trafic (comparaison avec période précédente)
        LocalDateTime previousStart = start.minus(
            java.time.Duration.between(start, now)
        );
        long currentFlights = ((Number) trafficStats.get("totalFlights")).longValue();
        long previousFlights = flightRepository.findAll().stream()
            .filter(f -> f.getScheduledDeparture() != null && 
                       f.getScheduledDeparture().isAfter(previousStart) &&
                       f.getScheduledDeparture().isBefore(start))
            .count();
        
        double trafficChange = previousFlights > 0 ? 
            ((currentFlights - previousFlights) * 100.0) / previousFlights : 0;
        trends.put("trafficChange", Math.round(trafficChange * 100.0) / 100.0);
        
        // Tendances d'incidents (retards)
        trends.put("delaysTrend", "STABLE"); // Simplifié
        
        reports.put("trends", trends);
        reports.put("period", period);
        reports.put("generatedAt", LocalDateTime.now());
        
        return reports;
    }
}

