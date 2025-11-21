package com.flightradar.service;

import com.flightradar.model.*;
import com.flightradar.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service pour simuler les vols en temps réel après autorisation de décollage
 * Gère la trajectoire, la position, l'altitude, la vitesse et l'ETA
 */
@Service
@Slf4j
public class FlightSimulationService {
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private AirportRepository airportRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private RealtimeUpdateService realtimeUpdateService;
    
    // Map pour stocker les vols en cours de simulation
    private final Map<Long, FlightSimulation> activeSimulations = new ConcurrentHashMap<>();
    
    // Constantes de simulation
    private static final double CRUISE_ALTITUDE = 10000.0; // 10 000 mètres (altitude de croisière)
    private static final double CLIMB_RATE = 10.0; // 10 m/s de montée
    private static final double DESCENT_RATE = 8.0; // 8 m/s de descente
    private static final double CRUISE_SPEED = 800.0; // 800 km/h en croisière
    private static final double TAKEOFF_SPEED = 250.0; // 250 km/h au décollage
    private static final double UPDATE_INTERVAL_SECONDS = 5.0; // Mise à jour toutes les 5 secondes
    
    /**
     * Classe interne pour stocker l'état d'une simulation de vol
     */
    private static class FlightSimulation {
        Long flightId;
        Long aircraftId;
        Airport departureAirport;
        Airport arrivalAirport;
        double currentLat;
        double currentLon;
        double currentAltitude;
        double currentSpeed;
        double currentHeading;
        LocalDateTime departureTime;
        LocalDateTime estimatedArrival;
        double totalDistance;
        double distanceRemaining;
        boolean isClimbing;
        boolean isDescending;
        boolean isCruising;
        
        FlightSimulation(Long flightId, Long aircraftId, Airport departure, Airport arrival) {
            this.flightId = flightId;
            this.aircraftId = aircraftId;
            this.departureAirport = departure; // Utilisé pour calculer la distance
            this.arrivalAirport = arrival;
            this.currentLat = departure.getLatitude();
            this.currentLon = departure.getLongitude();
            this.currentAltitude = 0.0;
            this.currentSpeed = TAKEOFF_SPEED;
            this.departureTime = LocalDateTime.now();
            this.isClimbing = true;
            this.isDescending = false;
            this.isCruising = false;
            
            // Calculer la distance totale
            this.totalDistance = calculateDistance(
                departure.getLatitude(), departure.getLongitude(),
                arrival.getLatitude(), arrival.getLongitude()
            );
            this.distanceRemaining = totalDistance;
            
            // Calculer l'ETA (temps de vol estimé)
            // Temps de montée : jusqu'à 10km à 10m/s = 1000 secondes = ~16 minutes
            // Temps de croisière : distance / vitesse
            // Temps de descente : ~15 minutes
            double climbTime = CRUISE_ALTITUDE / CLIMB_RATE; // secondes
            double cruiseTime = (totalDistance * 1000) / (CRUISE_SPEED / 3.6); // secondes (convertir km/h en m/s)
            double descentTime = CRUISE_ALTITUDE / DESCENT_RATE; // secondes
            double totalFlightTimeSeconds = climbTime + cruiseTime + descentTime;
            
            this.estimatedArrival = departureTime.plusSeconds((long) totalFlightTimeSeconds);
            
            // Calculer le cap initial
            this.currentHeading = calculateBearing(
                departure.getLatitude(), departure.getLongitude(),
                arrival.getLatitude(), arrival.getLongitude()
            );
        }
        
        private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
            final int R = 6371; // Rayon de la Terre en km
            
            double latDistance = Math.toRadians(lat2 - lat1);
            double lonDistance = Math.toRadians(lon2 - lon1);
            
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            
            return R * c;
        }
        
        private static double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
            double lat1Rad = Math.toRadians(lat1);
            double lat2Rad = Math.toRadians(lat2);
            double deltaLon = Math.toRadians(lon2 - lon1);
            
            double y = Math.sin(deltaLon) * Math.cos(lat2Rad);
            double x = Math.cos(lat1Rad) * Math.sin(lat2Rad)
                    - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLon);
            
            double bearing = Math.toDegrees(Math.atan2(y, x));
            return (bearing + 360) % 360; // Normaliser entre 0 et 360
        }
    }
    
    /**
     * Démarre la simulation d'un vol après autorisation de décollage
     * @param aircraftId ID de l'avion
     * @param departureAirportId ID de l'aéroport de départ
     * @param arrivalAirportId ID de l'aéroport d'arrivée
     * @return Flight créé ou mis à jour
     */
    public Flight startFlightSimulation(Long aircraftId, Long departureAirportId, Long arrivalAirportId) {
        log.info("Démarrage simulation vol - Avion: {}, Départ: {}, Arrivée: {}", 
            aircraftId, departureAirportId, arrivalAirportId);
        
        Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
        Optional<Airport> departureOpt = airportRepository.findById(departureAirportId);
        Optional<Airport> arrivalOpt = airportRepository.findById(arrivalAirportId);
        
        if (aircraftOpt.isEmpty() || departureOpt.isEmpty() || arrivalOpt.isEmpty()) {
            throw new RuntimeException("Avion ou aéroport non trouvé");
        }
        
        Aircraft aircraft = aircraftOpt.get();
        Airport departure = departureOpt.get();
        Airport arrival = arrivalOpt.get();
        
        // Vérifier qu'il n'y a pas déjà une simulation en cours
        if (activeSimulations.containsKey(aircraftId)) {
            log.warn("Simulation déjà en cours pour l'avion {}", aircraftId);
            return null;
        }
        
        // Créer ou mettre à jour le vol
        Flight flight;
        Optional<Flight> existingFlightOpt = flightRepository
            .findByAircraftIdAndFlightStatusNot(aircraftId, FlightStatus.TERMINE);
        
        if (existingFlightOpt.isPresent()) {
            flight = existingFlightOpt.get();
            flight.setFlightStatus(FlightStatus.EN_COURS);
            flight.setActualDeparture(LocalDateTime.now());
        } else {
            flight = new Flight();
            flight.setFlightNumber(generateFlightNumber(aircraft));
            flight.setAircraft(aircraft);
            flight.setDepartureAirport(departure);
            flight.setArrivalAirport(arrival);
            flight.setFlightStatus(FlightStatus.EN_COURS);
            flight.setActualDeparture(LocalDateTime.now());
            flight.setScheduledDeparture(LocalDateTime.now());
        }
        
        // Sauvegarder le vol d'abord pour obtenir l'ID
        flight = flightRepository.save(flight);
        Long flightId = flight.getId();
        
        // Créer la simulation avec l'ID du vol
        FlightSimulation simulation = new FlightSimulation(
            flightId != null ? flightId : 0L,
            aircraftId,
            departure,
            arrival
        );
        
        // Mettre à jour le vol avec l'ETA
        flight.setEstimatedArrival(simulation.estimatedArrival);
        flight.setScheduledArrival(simulation.estimatedArrival);
        flight = flightRepository.save(flight);
        simulation.flightId = flight.getId();
        
        // Mettre à jour l'avion
        aircraft.setStatus(AircraftStatus.EN_VOL);
        aircraft.setPositionLat(departure.getLatitude());
        aircraft.setPositionLon(departure.getLongitude());
        aircraft.setAltitude(0.0);
        aircraft.setSpeed(TAKEOFF_SPEED);
        aircraft.setHeading(simulation.currentHeading);
        aircraft.setAirport(null); // Plus à l'aéroport
        aircraftRepository.save(aircraft);
        
        // Démarrer la simulation asynchrone
        activeSimulations.put(aircraftId, simulation);
        startSimulationAsync(simulation);
        
        log.info("Simulation démarrée pour le vol {} - ETA: {}", 
            flight.getFlightNumber(), simulation.estimatedArrival);
        
        return flight;
    }
    
    /**
     * Démarre la simulation de manière asynchrone
     */
    @Async
    private void startSimulationAsync(FlightSimulation simulation) {
        log.info("Démarrage simulation asynchrone pour avion {}", simulation.aircraftId);
        
        while (activeSimulations.containsKey(simulation.aircraftId)) {
            try {
                updateFlightPosition(simulation);
                Thread.sleep((long) (UPDATE_INTERVAL_SECONDS * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Simulation interrompue pour avion {}", simulation.aircraftId);
                break;
            }
        }
    }
    
    /**
     * Met à jour la position de l'avion dans la simulation
     */
    private void updateFlightPosition(FlightSimulation sim) {
        Optional<Aircraft> aircraftOpt = aircraftRepository.findById(sim.aircraftId);
        if (aircraftOpt.isEmpty()) {
            log.error("Avion {} non trouvé, arrêt simulation", sim.aircraftId);
            activeSimulations.remove(sim.aircraftId);
            return;
        }
        
        Aircraft aircraft = aircraftOpt.get();
        
        // Calculer la distance parcourue (en km)
        double speedMs = sim.currentSpeed / 3.6; // Convertir km/h en m/s
        double distanceIncrement = (speedMs * UPDATE_INTERVAL_SECONDS) / 1000.0; // km
        
        // Calculer la nouvelle position
        double[] newPosition = calculateNewPosition(
            sim.currentLat, sim.currentLon,
            sim.currentHeading, distanceIncrement
        );
        
        sim.currentLat = newPosition[0];
        sim.currentLon = newPosition[1];
        sim.distanceRemaining -= distanceIncrement;
        
        // Gérer les phases de vol
        if (sim.isClimbing) {
            sim.currentAltitude += CLIMB_RATE * UPDATE_INTERVAL_SECONDS;
            sim.currentSpeed = Math.min(sim.currentSpeed + 5, CRUISE_SPEED); // Accélération progressive
            
            if (sim.currentAltitude >= CRUISE_ALTITUDE) {
                sim.isClimbing = false;
                sim.isCruising = true;
                sim.currentAltitude = CRUISE_ALTITUDE;
                sim.currentSpeed = CRUISE_SPEED;
                log.info("Avion {} en croisière", sim.aircraftId);
            }
        } else if (sim.isCruising) {
            // Vérifier si on doit commencer la descente (à 50km de l'arrivée)
            if (sim.distanceRemaining <= 50.0) {
                sim.isCruising = false;
                sim.isDescending = true;
                log.info("Avion {} commence la descente", sim.aircraftId);
            }
        } else if (sim.isDescending) {
            sim.currentAltitude -= DESCENT_RATE * UPDATE_INTERVAL_SECONDS;
            sim.currentSpeed = Math.max(sim.currentSpeed - 3, 200.0); // Ralentissement progressif
            
            if (sim.currentAltitude <= 0 || sim.distanceRemaining <= 0.5) {
                // Atterrissage
                sim.currentAltitude = 0.0;
                sim.currentSpeed = 0.0;
                sim.currentLat = sim.arrivalAirport.getLatitude();
                sim.currentLon = sim.arrivalAirport.getLongitude();
                
                // Terminer le vol
                completeFlight(sim);
                return;
            }
        }
        
        // Mettre à jour l'avion dans la base de données
        aircraft.setPositionLat(sim.currentLat);
        aircraft.setPositionLon(sim.currentLon);
        aircraft.setAltitude(sim.currentAltitude);
        aircraft.setSpeed(sim.currentSpeed);
        aircraft.setHeading(sim.currentHeading);
        aircraft.setAirSpeed(sim.currentSpeed);
        aircraft.setVerticalSpeed(sim.isClimbing ? CLIMB_RATE : (sim.isDescending ? -DESCENT_RATE : 0.0));
        aircraftRepository.save(aircraft);
        
        // Envoyer la mise à jour via WebSocket
        realtimeUpdateService.sendAircraftUpdate(aircraft);
        
        // Envoyer une mise à jour spécifique pour le vol
        Map<String, Object> flightUpdate = new HashMap<>();
        flightUpdate.put("type", "flight_update");
        flightUpdate.put("flightId", sim.flightId);
        flightUpdate.put("aircraftId", sim.aircraftId);
        flightUpdate.put("latitude", sim.currentLat);
        flightUpdate.put("longitude", sim.currentLon);
        flightUpdate.put("altitude", sim.currentAltitude);
        flightUpdate.put("speed", sim.currentSpeed);
        flightUpdate.put("heading", sim.currentHeading);
        flightUpdate.put("distanceRemaining", sim.distanceRemaining);
        flightUpdate.put("estimatedArrival", sim.estimatedArrival);
        flightUpdate.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/flight/" + sim.flightId, flightUpdate);
        messagingTemplate.convertAndSend("/topic/aircraft/" + sim.aircraftId, flightUpdate);
    }
    
    /**
     * Calcule la nouvelle position basée sur la position actuelle, le cap et la distance
     */
    private double[] calculateNewPosition(double lat, double lon, double bearing, double distanceKm) {
        final double R = 6371.0; // Rayon de la Terre en km
        
        double lat1Rad = Math.toRadians(lat);
        double lon1Rad = Math.toRadians(lon);
        double bearingRad = Math.toRadians(bearing);
        
        double lat2Rad = Math.asin(
            Math.sin(lat1Rad) * Math.cos(distanceKm / R) +
            Math.cos(lat1Rad) * Math.sin(distanceKm / R) * Math.cos(bearingRad)
        );
        
        double lon2Rad = lon1Rad + Math.atan2(
            Math.sin(bearingRad) * Math.sin(distanceKm / R) * Math.cos(lat1Rad),
            Math.cos(distanceKm / R) - Math.sin(lat1Rad) * Math.sin(lat2Rad)
        );
        
        return new double[]{
            Math.toDegrees(lat2Rad),
            Math.toDegrees(lon2Rad)
        };
    }
    
    /**
     * Termine le vol et nettoie la simulation
     */
    private void completeFlight(FlightSimulation sim) {
        log.info("Vol {} terminé - Avion atterri", sim.flightId);
        
        Optional<Flight> flightOpt = flightRepository.findById(sim.flightId);
        Optional<Aircraft> aircraftOpt = aircraftRepository.findById(sim.aircraftId);
        Optional<Airport> arrivalOpt = airportRepository.findById(sim.arrivalAirport.getId());
        
        if (flightOpt.isPresent() && aircraftOpt.isPresent() && arrivalOpt.isPresent()) {
            Flight flight = flightOpt.get();
            Aircraft aircraft = aircraftOpt.get();
            Airport arrival = arrivalOpt.get();
            
            flight.setFlightStatus(FlightStatus.TERMINE);
            flight.setActualArrival(LocalDateTime.now());
            flightRepository.save(flight);
            
            aircraft.setStatus(AircraftStatus.AU_SOL);
            aircraft.setAirport(arrival);
            aircraft.setPositionLat(arrival.getLatitude());
            aircraft.setPositionLon(arrival.getLongitude());
            aircraft.setAltitude(0.0);
            aircraft.setSpeed(0.0);
            aircraftRepository.save(aircraft);
            
            // Envoyer notification de fin de vol
            Map<String, Object> completionUpdate = new HashMap<>();
            completionUpdate.put("type", "flight_completed");
            completionUpdate.put("flightId", sim.flightId);
            completionUpdate.put("aircraftId", sim.aircraftId);
            completionUpdate.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/flight/" + sim.flightId, completionUpdate);
            messagingTemplate.convertAndSend("/topic/aircraft/" + sim.aircraftId, completionUpdate);
        }
        
        activeSimulations.remove(sim.aircraftId);
    }
    
    /**
     * Génère un numéro de vol unique
     */
    private String generateFlightNumber(Aircraft aircraft) {
        String prefix = "AT"; // Royal Air Maroc
        String registration = aircraft.getRegistration().replace("-", "");
        String suffix = String.valueOf(System.currentTimeMillis() % 10000);
        return prefix + registration.substring(Math.max(0, registration.length() - 3)) + suffix;
    }
    
    /**
     * Récupère les informations d'un vol en cours
     */
    public Map<String, Object> getFlightStatus(Long flightId) {
        Optional<Flight> flightOpt = flightRepository.findById(flightId);
        if (flightOpt.isEmpty()) {
            return null;
        }
        
        Flight flight = flightOpt.get();
        Map<String, Object> status = new HashMap<>();
        status.put("flightId", flight.getId());
        status.put("flightNumber", flight.getFlightNumber());
        status.put("status", flight.getFlightStatus());
        status.put("departureAirport", flight.getDepartureAirport().getCodeIATA());
        status.put("arrivalAirport", flight.getArrivalAirport().getCodeIATA());
        status.put("actualDeparture", flight.getActualDeparture());
        status.put("estimatedArrival", flight.getEstimatedArrival());
        status.put("actualArrival", flight.getActualArrival());
        
        // Si le vol est en cours, ajouter les infos de simulation
        if (flight.getAircraft() != null) {
            Long aircraftId = flight.getAircraft().getId();
            if (activeSimulations.containsKey(aircraftId)) {
                FlightSimulation sim = activeSimulations.get(aircraftId);
                status.put("currentLatitude", sim.currentLat);
                status.put("currentLongitude", sim.currentLon);
                status.put("currentAltitude", sim.currentAltitude);
                status.put("currentSpeed", sim.currentSpeed);
                status.put("currentHeading", sim.currentHeading);
                status.put("distanceRemaining", sim.distanceRemaining);
            }
        }
        
        return status;
    }
    
    /**
     * Vérifie si un vol est en cours de simulation
     */
    public boolean isFlightInProgress(Long aircraftId) {
        return activeSimulations.containsKey(aircraftId);
    }
}

