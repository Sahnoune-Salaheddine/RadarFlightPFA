package com.flightradar.service;

import com.flightradar.model.Aircraft;
import com.flightradar.model.AircraftStatus;
import com.flightradar.model.Airport;
import com.flightradar.model.dto.LiveAircraft;
import com.flightradar.repository.AircraftRepository;
import com.flightradar.repository.AirportRepository;
import com.flightradar.repository.PilotRepository;
import com.flightradar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Service pour gérer les avions
 * - Gestion des avions en base de données
 * - Intégration avec OpenSky Network pour données live
 * - Simulation du mouvement
 * - Mise à jour des positions
 * - Gestion des statuts
 */
@Service
public class AircraftService {
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private AirportRepository airportRepository;
    
    @Autowired
    private PilotRepository pilotRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OpenSkyService openSkyService;
    
    private final Random random = new Random();
    
    /**
     * Met à jour la position d'un avion
     */
    public Aircraft updatePosition(Long aircraftId, Double latitude, Double longitude,
                                  Double altitude, Double speed, Double heading) {
        Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
        if (aircraftOpt.isPresent()) {
            Aircraft aircraft = aircraftOpt.get();
            aircraft.setPositionLat(latitude);
            aircraft.setPositionLon(longitude);
            aircraft.setAltitude(altitude);
            aircraft.setSpeed(speed);
            aircraft.setHeading(heading);
            aircraft.setLastUpdate(LocalDateTime.now());
            return aircraftRepository.save(aircraft);
        }
        return null;
    }
    
    /**
     * Récupère tous les avions (base de données + OpenSky live)
     * Combine les avions simulés avec les données OpenSky
     */
    public List<Aircraft> getAllAircraft() {
        return aircraftRepository.findAll();
    }
    
    /**
     * Récupère tous les avions live depuis OpenSky Network
     * 
     * @return Liste des avions en temps réel
     */
    public List<LiveAircraft> getAllLiveAircraft() {
        return openSkyService.getLiveAircraft();
    }
    
    /**
     * Récupère un avion par ID
     */
    public Optional<Aircraft> getAircraftById(Long id) {
        return aircraftRepository.findById(id);
    }
    
    /**
     * Récupère les avions d'un aéroport
     */
    public List<Aircraft> getAircraftByAirport(Long airportId) {
        return aircraftRepository.findByAirportId(airportId);
    }
    
    /**
     * Récupère les avions en vol
     */
    public List<Aircraft> getAircraftInFlight() {
        return aircraftRepository.findByStatus(AircraftStatus.EN_VOL);
    }
    
    /**
     * Simule le mouvement des avions en vol
     * Exécuté toutes les 5 secondes
     */
    @Scheduled(fixedRate = 5000)
    public void simulateAircraftMovement() {
        List<Aircraft> aircraftList = aircraftRepository.findAll();
        
        for (Aircraft aircraft : aircraftList) {
            if (aircraft.getStatus() == AircraftStatus.EN_VOL && 
                aircraft.getPositionLat() != null && 
                aircraft.getPositionLon() != null) {
                
                // Calculer le déplacement
                double speedKmh = aircraft.getSpeed();
                double speedMs = speedKmh / 3.6;
                double timeHours = 5.0 / 3600.0; // 5 secondes en heures
                double distanceKm = speedKmh * timeHours;
                
                // Calculer la nouvelle position (approximation simple)
                double latChange = distanceKm * Math.cos(Math.toRadians(aircraft.getHeading())) / 111.0;
                double lonChange = distanceKm * Math.sin(Math.toRadians(aircraft.getHeading())) / 
                    (111.0 * Math.cos(Math.toRadians(aircraft.getPositionLat())));
                
                aircraft.setPositionLat(aircraft.getPositionLat() + latChange);
                aircraft.setPositionLon(aircraft.getPositionLon() + lonChange);
                
                // Petite variation aléatoire de direction (±1 degré)
                double headingChange = (random.nextDouble() - 0.5) * 2.0;
                double newHeading = aircraft.getHeading() + headingChange;
                if (newHeading < 0) newHeading += 360;
                if (newHeading >= 360) newHeading -= 360;
                aircraft.setHeading(newHeading);
                
                aircraft.setLastUpdate(LocalDateTime.now());
                aircraftRepository.save(aircraft);
            }
        }
    }
    
    /**
     * Change le statut d'un avion
     */
    public Aircraft changeStatus(Long aircraftId, AircraftStatus newStatus) {
        Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
        if (aircraftOpt.isPresent()) {
            Aircraft aircraft = aircraftOpt.get();
            aircraft.setStatus(newStatus);
            aircraft.setLastUpdate(LocalDateTime.now());
            return aircraftRepository.save(aircraft);
        }
        return null;
    }
    
    /**
     * Démarre un vol (décolage)
     */
    public Aircraft startFlight(Long aircraftId, Long destinationAirportId) {
        Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
        Optional<Airport> destinationOpt = airportRepository.findById(destinationAirportId);
        
        if (aircraftOpt.isPresent() && destinationOpt.isPresent()) {
            Aircraft aircraft = aircraftOpt.get();
            Airport destination = destinationOpt.get();
            
            // Calculer la direction initiale vers la destination
            double heading = calculateHeading(
                aircraft.getPositionLat(), aircraft.getPositionLon(),
                destination.getLatitude(), destination.getLongitude()
            );
            
            aircraft.setHeading(heading);
            aircraft.setStatus(AircraftStatus.DECOLLAGE);
            aircraft.setAltitude(100.0); // Altitude initiale
            aircraft.setSpeed(200.0); // Vitesse de décollage
            aircraft.setLastUpdate(LocalDateTime.now());
            
            return aircraftRepository.save(aircraft);
        }
        return null;
    }
    
    /**
     * Récupère l'avion d'un pilote par son username
     */
    public Optional<Aircraft> getAircraftByPilotUsername(String username) {
        // Trouver le User par username
        Optional<com.flightradar.model.User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        
        // Trouver le Pilot par userId
        Optional<com.flightradar.model.Pilot> pilotOpt = pilotRepository.findByUserId(userOpt.get().getId());
        if (pilotOpt.isEmpty()) {
            return Optional.empty();
        }
        
        // Trouver l'avion par pilotId (relation OneToOne)
        Optional<Aircraft> aircraftOpt = aircraftRepository.findByPilotId(pilotOpt.get().getId());
        return aircraftOpt;
    }
    
    /**
     * Calcule le cap (heading) entre deux points GPS
     */
    private double calculateHeading(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        
        double y = Math.sin(dLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - 
                  Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);
        
        double heading = Math.toDegrees(Math.atan2(y, x));
        return (heading + 360) % 360;
    }
}
