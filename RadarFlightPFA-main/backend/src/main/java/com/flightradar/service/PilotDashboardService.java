package com.flightradar.service;

import com.flightradar.model.*;
import com.flightradar.model.dto.PilotDashboardDTO;
import com.flightradar.repository.*;
import com.flightradar.model.ReceiverType;
import com.flightradar.model.SenderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour le Dashboard Pilote
 * Rassemble toutes les informations nécessaires pour afficher le dashboard complet
 */
@Service
@Slf4j
public class PilotDashboardService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PilotRepository pilotRepository;
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private WeatherDataRepository weatherDataRepository;
    
    @Autowired
    private CommunicationRepository communicationRepository;
    
    @Autowired
    private RadarCenterRepository radarCenterRepository;
    
    /**
     * Récupérer toutes les données du dashboard pour un pilote
     */
    public PilotDashboardDTO getPilotDashboard(String username) {
        log.info("Récupération du dashboard pour le pilote: {}", username);
        
        // 1. Trouver le User
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Pilote non trouvé: " + username);
        }
        
        User user = userOpt.get();
        
        // 2. Trouver le Pilot
        // Utiliser findByUserId qui devrait retourner un seul résultat
        Optional<Pilot> pilotOpt = pilotRepository.findByUserId(user.getId());
        
        if (pilotOpt.isEmpty()) {
            log.error("Profil pilote non trouvé pour l'utilisateur: {} (user_id: {})", username, user.getId());
            throw new RuntimeException("Profil pilote non trouvé pour: " + username);
        }
        
        Pilot pilot = pilotOpt.get();
        log.debug("Pilote trouvé: {} (pilot_id: {})", username, pilot.getId());
        
        // 3. Trouver l'Aircraft du pilote (relation OneToOne)
        // Méthode 1: Utiliser la relation JPA directe (peut être null si fetch LAZY)
        Aircraft aircraft = pilot.getAircraft();
        log.debug("Avion depuis relation JPA: {}", aircraft != null ? aircraft.getRegistration() : "null");
        
        // Méthode 2: Si la relation n'est pas chargée, utiliser le repository
        if (aircraft == null) {
            log.debug("Relation JPA non chargée, recherche via repository pour pilot_id: {}", pilot.getId());
            Optional<Aircraft> aircraftOpt = aircraftRepository.findByPilotId(pilot.getId());
            log.debug("Résultat findByPilotId: {}", aircraftOpt.isPresent() ? aircraftOpt.get().getRegistration() : "vide");
            
            if (aircraftOpt.isEmpty()) {
                // Essayer de trouver un avion par username
                log.debug("Tentative de recherche par username: {}", username);
                aircraftOpt = aircraftRepository.findByPilotUsername(username);
                log.debug("Résultat findByPilotUsername: {}", aircraftOpt.isPresent() ? aircraftOpt.get().getRegistration() : "vide");
            }
            
            if (aircraftOpt.isEmpty()) {
                log.error("Aucun avion assigné au pilote: {} (pilot_id: {})", username, pilot.getId());
                
                // Essayer de trouver un avion disponible à l'aéroport du pilote
                if (pilot.getAirport() != null) {
                    final Long airportId = pilot.getAirport().getId(); // Variable finale pour la lambda
                    log.info("Tentative de trouver un avion disponible à l'aéroport: {}", airportId);
                    List<Aircraft> availableAircraftList = aircraftRepository.findUnassignedAircraft();
                    availableAircraftList = availableAircraftList.stream()
                        .filter(a -> a.getAirport() != null && a.getAirport().getId().equals(airportId))
                        .collect(Collectors.toList());
                    
                    if (!availableAircraftList.isEmpty()) {
                        log.warn("Avion disponible trouvé mais non assigné. Assignation automatique...");
                        Aircraft foundAircraft = availableAircraftList.get(0);
                        foundAircraft.setPilot(pilot);
                        aircraftRepository.save(foundAircraft);
                        // Rafraîchir le pilote pour charger la relation
                        pilot = pilotRepository.findById(pilot.getId()).orElse(pilot);
                        aircraft = pilot.getAircraft();
                        log.info("Avion {} assigné automatiquement au pilote {}", foundAircraft.getRegistration(), username);
                        aircraft = foundAircraft; // Utiliser l'avion assigné
                    }
                }
                
                // Si toujours pas d'avion après toutes les tentatives
                if (aircraft == null) {
                    log.error("Aucun avion trouvé pour le pilote {} après toutes les tentatives", username);
                    throw new RuntimeException("NO_AIRCRAFT_ASSIGNED: Aucun avion assigné au pilote. Veuillez contacter l'administrateur.");
                }
            } else {
                aircraft = aircraftOpt.get();
                log.debug("Avion trouvé via repository: {}", aircraft.getRegistration());
            }
        } else {
            log.debug("Avion trouvé via relation JPA: {}", aircraft.getRegistration());
        }
        
        // Vérification finale que l'avion existe
        if (aircraft == null) {
            log.error("Aucun avion trouvé pour le pilote {} après toutes les tentatives", username);
            throw new RuntimeException("NO_AIRCRAFT_ASSIGNED: Aucun avion assigné au pilote. Veuillez contacter l'administrateur.");
        }
        
        // 4. Trouver le Flight actif
        Optional<Flight> flightOpt = flightRepository.findByAircraftIdAndFlightStatusNot(
            aircraft.getId(), FlightStatus.TERMINE);
        
        Flight flight = flightOpt.orElse(null);
        
        // 5. Construire le DTO
        PilotDashboardDTO dto = new PilotDashboardDTO();
        
        // ========== 1. Informations générales du vol ==========
        if (flight != null) {
            dto.setFlightNumber(flight.getFlightNumber() != null ? flight.getFlightNumber() : "N/A");
            dto.setAirline(flight.getAirline() != null ? flight.getAirline() : "N/A");
            dto.setAircraftType(aircraft.getModel() != null ? aircraft.getModel() : "N/A");
            
            if (flight.getDepartureAirport() != null) {
                dto.setDepartureAirport(flight.getDepartureAirport().getName() + " (" + 
                    flight.getDepartureAirport().getCodeIATA() + ")");
            } else {
                dto.setDepartureAirport("N/A");
            }
            
            if (flight.getArrivalAirport() != null) {
                dto.setArrivalAirport(flight.getArrivalAirport().getName() + " (" + 
                    flight.getArrivalAirport().getCodeIATA() + ")");
            } else {
                dto.setArrivalAirport("N/A");
            }
            
            if (flight.getDepartureAirport() != null && flight.getArrivalAirport() != null) {
                dto.setRoute(flight.getDepartureAirport().getCodeIATA() + " → " + 
                    flight.getArrivalAirport().getCodeIATA());
            } else {
                dto.setRoute("N/A");
            }
        } else {
            dto.setFlightNumber("N/A");
            dto.setAirline("N/A");
            dto.setAircraftType(aircraft.getModel() != null ? aircraft.getModel() : "N/A");
            dto.setDepartureAirport("N/A");
            dto.setArrivalAirport("N/A");
            dto.setRoute("N/A");
        }
        
        // ========== 2. Position & mouvement (ADS-B) ==========
        dto.setLatitude(aircraft.getPositionLat());
        dto.setLongitude(aircraft.getPositionLon());
        dto.setAltitude(aircraft.getAltitude());
        dto.setAltitudeFeet(aircraft.getAltitude() != null ? aircraft.getAltitude() * 3.28084 : 0.0);
        dto.setGroundSpeed(aircraft.getSpeed());
        dto.setAirSpeed(aircraft.getAirSpeed() != null ? aircraft.getAirSpeed() : aircraft.getSpeed());
        dto.setHeading(aircraft.getHeading());
        dto.setVerticalSpeed(aircraft.getVerticalSpeed() != null ? aircraft.getVerticalSpeed() : 0.0);
        
        // ========== 3. Statut du vol ==========
        dto.setFlightStatus(mapAircraftStatusToFlightStatus(aircraft.getStatus()));
        if (flight != null) {
            dto.setActualDeparture(flight.getActualDeparture());
            dto.setActualArrival(flight.getActualArrival());
            dto.setScheduledDeparture(flight.getScheduledDeparture());
            dto.setScheduledArrival(flight.getScheduledArrival());
            
            // Calculer le retard
            if (flight.getScheduledDeparture() != null && flight.getActualDeparture() != null) {
                Duration delay = Duration.between(flight.getScheduledDeparture(), flight.getActualDeparture());
                dto.setDelayMinutes(delay.toMinutes());
            } else {
                dto.setDelayMinutes(0L);
            }
        }
        dto.setGate("N/A"); // TODO: Ajouter gate dans le modèle
        dto.setRunway("N/A"); // TODO: Ajouter runway dans le modèle
        
        // ========== 4. Météo du vol ==========
        Airport currentAirport = aircraft.getAirport();
        if (currentAirport != null) {
            Optional<WeatherData> weatherOpt = weatherDataRepository
                .findFirstByAirportIdOrderByTimestampDesc(currentAirport.getId());
            
            if (weatherOpt.isPresent()) {
                WeatherData weather = weatherOpt.get();
                PilotDashboardDTO.WeatherInfoDTO weatherInfo = new PilotDashboardDTO.WeatherInfoDTO();
                weatherInfo.setWindSpeed(weather.getWindSpeed());
                weatherInfo.setWindDirection(weather.getWindDirection());
                weatherInfo.setVisibility(weather.getVisibility());
                weatherInfo.setPrecipitation(weather.getConditions());
                weatherInfo.setTurbulence(weather.getConditions());
                weatherInfo.setTemperature(weather.getTemperature());
                weatherInfo.setPressure(weather.getPressure());
                
                List<String> alerts = new ArrayList<>();
                if (weather.getAlert() != null && weather.getAlert()) {
                    alerts.add("Alerte météo active");
                }
                weatherInfo.setWeatherAlerts(alerts);
                
                dto.setWeather(weatherInfo);
            }
        }
        
        // ========== 5. Communications et contrôle aérien (ATC) ==========
        List<Communication> communications = communicationRepository
            .findByReceiverTypeAndReceiverId(ReceiverType.AIRCRAFT, aircraft.getId());
        communications.addAll(communicationRepository
            .findBySenderTypeAndSenderId(SenderType.AIRCRAFT, aircraft.getId()));
        // Trier par timestamp décroissant
        communications.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        if (!communications.isEmpty()) {
            Communication lastComm = communications.get(0);
            dto.setLastATCMessage(lastComm.getMessage());
            
            // Instructions en cours
            List<String> instructions = communications.stream()
                .filter(c -> c.getSenderType() == SenderType.RADAR)
                .limit(5)
                .map(Communication::getMessage)
                .collect(Collectors.toList());
            dto.setCurrentInstructions(instructions);
            
            // Historique ATC
            List<PilotDashboardDTO.ATCMessageDTO> atcHistory = communications.stream()
                .limit(20)
                .map(c -> new PilotDashboardDTO.ATCMessageDTO(
                    c.getTimestamp(),
                    c.getMessage(),
                    c.getSenderType() == SenderType.RADAR ? "ATC" : "PILOT"
                ))
                .collect(Collectors.toList());
            dto.setAtcHistory(atcHistory);
        }
        
        // Centre radar responsable
        if (currentAirport != null) {
            try {
                // Gérer le cas où il y a plusieurs centres radar pour le même aéroport
                List<RadarCenter> radarCenters = radarCenterRepository.findAllByAirportId(currentAirport.getId());
                
                if (!radarCenters.isEmpty()) {
                    // Prendre le premier centre radar trouvé
                    dto.setRadarCenterName(radarCenters.get(0).getName());
                }
            } catch (Exception e) {
                log.warn("Erreur lors de la récupération du centre radar pour l'aéroport {}: {}", 
                    currentAirport.getId(), e.getMessage());
            }
        }
        
        // ========== 6. Sécurité / Suivi ADS-B ==========
        dto.setTransponderCode(aircraft.getTransponderCode() != null ? 
            aircraft.getTransponderCode() : "1200");
        
        // Trajectoire (simplifiée - juste la position actuelle)
        List<PilotDashboardDTO.PositionDTO> trajectory = new ArrayList<>();
        if (aircraft.getPositionLat() != null && aircraft.getPositionLon() != null) {
            trajectory.add(new PilotDashboardDTO.PositionDTO(
                aircraft.getPositionLat(),
                aircraft.getPositionLon(),
                aircraft.getAltitude(),
                aircraft.getLastUpdate()
            ));
        }
        dto.setTrajectory(trajectory);
        
        // Alertes
        List<PilotDashboardDTO.AlertDTO> alerts = new ArrayList<>();
        if (aircraft.getTransponderCode() == null || aircraft.getTransponderCode().isEmpty()) {
            alerts.add(new PilotDashboardDTO.AlertDTO(
                "TECHNICAL", "MEDIUM", "Code transpondeur manquant", LocalDateTime.now()
            ));
        }
        dto.setAlerts(alerts);
        
        // Niveau de risque
        dto.setRiskLevel("LOW"); // TODO: Calculer le niveau de risque
        
        // ========== 7. KPIs ==========
        PilotDashboardDTO.KPIsDTO kpis = calculateKPIs(aircraft, flight);
        dto.setKpis(kpis);
        
        return dto;
    }
    
    /**
     * Calculer les KPIs
     */
    private PilotDashboardDTO.KPIsDTO calculateKPIs(Aircraft aircraft, Flight flight) {
        PilotDashboardDTO.KPIsDTO kpis = new PilotDashboardDTO.KPIsDTO();
        
        // Distance restante
        if (flight != null && aircraft.getPositionLat() != null && aircraft.getPositionLon() != null) {
            Airport destination = flight.getArrivalAirport();
            double distance = calculateDistance(
                aircraft.getPositionLat(), aircraft.getPositionLon(),
                destination.getLatitude(), destination.getLongitude()
            );
            kpis.setRemainingDistance(distance);
            
            // ETA (Estimated Time of Arrival)
            if (aircraft.getSpeed() != null && aircraft.getSpeed() > 0) {
                double hours = distance / aircraft.getSpeed();
                kpis.setEstimatedArrival(LocalDateTime.now().plusHours((long) hours));
            }
        }
        
        // Consommation carburant estimée (simplifiée)
        kpis.setEstimatedFuelConsumption(aircraft.getSpeed() != null ? 
            aircraft.getSpeed() * 0.1 : 0.0); // 0.1 L/km
        
        // Niveau de carburant (simulé)
        kpis.setFuelLevel(75.0); // TODO: Ajouter dans le modèle
        
        // Vitesse moyenne
        kpis.setAverageSpeed(aircraft.getSpeed());
        
        // Altitude stable
        kpis.setStableAltitude(aircraft.getVerticalSpeed() != null && 
            Math.abs(aircraft.getVerticalSpeed()) < 5.0);
        
        // Turbulence détectée
        kpis.setTurbulenceDetected(false); // TODO: Calculer depuis météo
        
        // Sévérité météo (0-100%)
        kpis.setWeatherSeverity(20); // TODO: Calculer depuis météo
        
        // Indice de risque de trajectoire
        kpis.setTrajectoryRiskIndex(15); // TODO: Calculer depuis conflits
        
        // Densité de trafic dans 30 km
        kpis.setTrafficDensity30km(calculateTrafficDensity(aircraft, 30.0));
        
        // Score d'état avion
        kpis.setAircraftHealthScore(calculateAircraftHealthScore(aircraft));
        
        return kpis;
    }
    
    /**
     * Calculer la densité de trafic dans un rayon donné
     */
    private Integer calculateTrafficDensity(Aircraft aircraft, double radiusKm) {
        if (aircraft.getPositionLat() == null || aircraft.getPositionLon() == null) {
            return 0;
        }
        
        List<Aircraft> allAircraft = aircraftRepository.findAll();
        int count = 0;
        
        for (Aircraft other : allAircraft) {
            if (other.getId().equals(aircraft.getId())) continue;
            if (other.getPositionLat() == null || other.getPositionLon() == null) continue;
            if (other.getStatus() != AircraftStatus.EN_VOL) continue;
            
            double distance = calculateDistance(
                aircraft.getPositionLat(), aircraft.getPositionLon(),
                other.getPositionLat(), other.getPositionLon()
            );
            
            if (distance <= radiusKm) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Calculer le score de santé de l'avion (0-100)
     */
    private Integer calculateAircraftHealthScore(Aircraft aircraft) {
        int score = 100;
        
        // Pénalités
        if (aircraft.getTransponderCode() == null || aircraft.getTransponderCode().isEmpty()) {
            score -= 20;
        }
        
        if (aircraft.getPositionLat() == null || aircraft.getPositionLon() == null) {
            score -= 30;
        }
        
        if (aircraft.getSpeed() == null || aircraft.getSpeed() < 0) {
            score -= 10;
        }
        
        return Math.max(0, score);
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
     * Mapper AircraftStatus vers FlightStatus textuel
     */
    private String mapAircraftStatusToFlightStatus(AircraftStatus status) {
        if (status == null) return "Inconnu";
        return switch (status) {
            case AU_SOL -> "Au sol";
            case DECOLLAGE -> "Décollé";
            case EN_VOL -> "En vol";
            case ATTERRISSAGE -> "Atterrissage";
            case EN_ATTENTE -> "En attente";
        };
    }
}

