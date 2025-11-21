package com.flightradar.config;

import com.flightradar.model.*;
import com.flightradar.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Initialise les données de base au démarrage de l'application
 * Utilise les NOUVELLES entités (Airport, Aircraft, Pilot, etc.)
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private AirportRepository airportRepository;
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private PilotRepository pilotRepository;
    
    @Autowired
    private RadarCenterRepository radarCenterRepository;
    
    @Autowired
    private RunwayRepository runwayRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Initialiser seulement si la base est vide
        if (airportRepository.count() == 0) {
            initializeData();
        }
    }
    
    private void initializeData() {
        // Créer l'utilisateur admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        
        // Créer les aéroports marocains
        Airport casablanca = createAirport("Aéroport Mohammed V", "Casablanca", "CMN", 33.3675, -7.5898);
        Airport rabat = createAirport("Aéroport Rabat-Salé", "Rabat", "RBA", 34.0515, -6.7515);
        Airport marrakech = createAirport("Aéroport Marrakech-Ménara", "Marrakech", "RAK", 31.6069, -8.0363);
        Airport tanger = createAirport("Aéroport Tanger-Ibn Battouta", "Tanger", "TNG", 35.7269, -5.9169);
        
        // Créer les pistes pour chaque aéroport
        createRunways(casablanca);
        createRunways(rabat);
        createRunways(marrakech);
        createRunways(tanger);
        
        // Créer les utilisateurs pour les centres radar
        User userRadarCMN = createUser("radar_cmn", "radar123", Role.CENTRE_RADAR);
        User userRadarRBA = createUser("radar_rba", "radar123", Role.CENTRE_RADAR);
        User userRadarRAK = createUser("radar_rak", "radar123", Role.CENTRE_RADAR);
        User userRadarTNG = createUser("radar_tng", "radar123", Role.CENTRE_RADAR);
        
        // Créer les centres radar
        RadarCenter radarCMN = createRadarCenter("Centre Radar Casablanca", "CMN_RADAR", 121.5, casablanca, userRadarCMN);
        RadarCenter radarRBA = createRadarCenter("Centre Radar Rabat", "RBA_RADAR", 121.5, rabat, userRadarRBA);
        RadarCenter radarRAK = createRadarCenter("Centre Radar Marrakech", "RAK_RADAR", 121.5, marrakech, userRadarRAK);
        RadarCenter radarTNG = createRadarCenter("Centre Radar Tanger", "TNG_RADAR", 121.5, tanger, userRadarTNG);
        
        // Créer les utilisateurs pour les pilotes
        User userPiloteCMN1 = createUser("pilote_cmn1", "pilote123", Role.PILOTE);
        User userPiloteCMN2 = createUser("pilote_cmn2", "pilote123", Role.PILOTE);
        User userPiloteRBA1 = createUser("pilote_rba1", "pilote123", Role.PILOTE);
        User userPiloteRBA2 = createUser("pilote_rba2", "pilote123", Role.PILOTE);
        User userPiloteRAK1 = createUser("pilote_rak1", "pilote123", Role.PILOTE);
        User userPiloteRAK2 = createUser("pilote_rak2", "pilote123", Role.PILOTE);
        User userPiloteTNG1 = createUser("pilote_tng1", "pilote123", Role.PILOTE);
        User userPiloteTNG2 = createUser("pilote_tng2", "pilote123", Role.PILOTE);
        
        // Créer 2 avions par aéroport avec leurs pilotes
        createAircraftAndPilot("CN-AT01", "A320", casablanca, rabat, "Pilote", "CMN1", userPiloteCMN1);
        createAircraftAndPilot("CN-AT02", "A330", casablanca, marrakech, "Pilote", "CMN2", userPiloteCMN2);
        
        createAircraftAndPilot("CN-AT03", "A320", rabat, casablanca, "Pilote", "RBA1", userPiloteRBA1);
        createAircraftAndPilot("CN-AT04", "A330", rabat, tanger, "Pilote", "RBA2", userPiloteRBA2);
        
        createAircraftAndPilot("CN-AT05", "A320", marrakech, casablanca, "Pilote", "RAK1", userPiloteRAK1);
        createAircraftAndPilot("CN-AT06", "A330", marrakech, rabat, "Pilote", "RAK2", userPiloteRAK2);
        
        createAircraftAndPilot("CN-AT07", "A320", tanger, rabat, "Pilote", "TNG1", userPiloteTNG1);
        createAircraftAndPilot("CN-AT08", "A330", tanger, casablanca, "Pilote", "TNG2", userPiloteTNG2);
    }
    
    private User createUser(String username, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return userRepository.save(user);
    }
    
    private Airport createAirport(String name, String city, String codeIATA, Double lat, Double lon) {
        Airport airport = new Airport();
        airport.setName(name);
        airport.setCity(city);
        airport.setCodeIATA(codeIATA);
        airport.setLatitude(lat);
        airport.setLongitude(lon);
        return airportRepository.save(airport);
    }
    
    private void createRunways(Airport airport) {
        // Créer 2 pistes par aéroport
        Runway runway1 = new Runway();
        runway1.setName("09/27");
        runway1.setOrientation(90.0);
        runway1.setLengthMeters(3500);
        runway1.setWidthMeters(45);
        runway1.setAirport(airport);
        runwayRepository.save(runway1);
        
        Runway runway2 = new Runway();
        runway2.setName("17/35");
        runway2.setOrientation(170.0);
        runway2.setLengthMeters(3200);
        runway2.setWidthMeters(45);
        runway2.setAirport(airport);
        runwayRepository.save(runway2);
    }
    
    private RadarCenter createRadarCenter(String name, String code, Double frequency, Airport airport, User user) {
        RadarCenter radar = new RadarCenter();
        radar.setName(name);
        radar.setCode(code);
        radar.setFrequency(frequency);
        radar.setAirport(airport);
        radar.setUser(user);
        return radarCenterRepository.save(radar);
    }
    
    private void createAircraftAndPilot(String registration, String model, Airport airport, 
                                       Airport destination, String pilotName,
                                       String license, User user) {
        Aircraft aircraft = new Aircraft();
        aircraft.setRegistration(registration);
        aircraft.setModel(model);
        aircraft.setStatus(AircraftStatus.AU_SOL);
        aircraft.setAirport(airport);
        aircraft.setPositionLat(airport.getLatitude());
        aircraft.setPositionLon(airport.getLongitude());
        aircraft.setAltitude(0.0);
        aircraft.setSpeed(0.0);
        aircraft.setHeading(0.0);
        aircraft.setLastUpdate(LocalDateTime.now());
        // Définir username_pilote pour faciliter la recherche
        aircraft.setUsernamePilote(user.getUsername());
        aircraft = aircraftRepository.save(aircraft);
        
        Pilot pilot = new Pilot();
        pilot.setName(pilotName);
        pilot.setLicense(license);
        pilot.setExperienceYears(5);
        pilot.setUser(user);
        pilot = pilotRepository.save(pilot);
        
        // Lier le pilote à l'avion
        aircraft.setPilot(pilot);
        aircraft.setUsernamePilote(user.getUsername()); // S'assurer que username_pilote est défini
        aircraftRepository.save(aircraft);
    }
}
