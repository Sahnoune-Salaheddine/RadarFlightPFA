package com.flightradar.service;

import com.flightradar.model.*;
import com.flightradar.repository.AircraftRepository;
import com.flightradar.repository.AirportRepository;
import com.flightradar.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les vols
 * - Création de vols
 * - Suivi des statuts
 * - Gestion des horaires
 */
@Service
public class FlightService {
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private AirportRepository airportRepository;
    
    /**
     * Crée un nouveau vol
     */
    public Flight createFlight(String flightNumber, Long aircraftId, 
                              Long departureAirportId, Long arrivalAirportId) {
        Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
        Optional<Airport> departureOpt = airportRepository.findById(departureAirportId);
        Optional<Airport> arrivalOpt = airportRepository.findById(arrivalAirportId);
        
        if (aircraftOpt.isPresent() && departureOpt.isPresent() && arrivalOpt.isPresent()) {
            Flight flight = new Flight();
            flight.setFlightNumber(flightNumber);
            flight.setAircraft(aircraftOpt.get());
            flight.setDepartureAirport(departureOpt.get());
            flight.setArrivalAirport(arrivalOpt.get());
            flight.setFlightStatus(FlightStatus.PLANIFIE);
            flight.setScheduledDeparture(LocalDateTime.now().plusHours(1));
            flight.setScheduledArrival(LocalDateTime.now().plusHours(2));
            
            return flightRepository.save(flight);
        }
        return null;
    }
    
    /**
     * Démarre un vol
     */
    public Flight startFlight(Long flightId) {
        Optional<Flight> flightOpt = flightRepository.findById(flightId);
        if (flightOpt.isPresent()) {
            Flight flight = flightOpt.get();
            flight.setFlightStatus(FlightStatus.EN_COURS);
            flight.setActualDeparture(LocalDateTime.now());
            
            // Mettre à jour le statut de l'avion
            Aircraft aircraft = flight.getAircraft();
            aircraft.setStatus(AircraftStatus.EN_VOL);
            aircraftRepository.save(aircraft);
            
            return flightRepository.save(flight);
        }
        return null;
    }
    
    /**
     * Termine un vol
     */
    public Flight completeFlight(Long flightId) {
        Optional<Flight> flightOpt = flightRepository.findById(flightId);
        if (flightOpt.isPresent()) {
            Flight flight = flightOpt.get();
            flight.setFlightStatus(FlightStatus.TERMINE);
            flight.setActualArrival(LocalDateTime.now());
            
            // Mettre à jour le statut de l'avion
            Aircraft aircraft = flight.getAircraft();
            aircraft.setStatus(AircraftStatus.AU_SOL);
            aircraft.setAirport(flight.getArrivalAirport());
            aircraft.setPositionLat(flight.getArrivalAirport().getLatitude());
            aircraft.setPositionLon(flight.getArrivalAirport().getLongitude());
            aircraft.setAltitude(0.0);
            aircraft.setSpeed(0.0);
            aircraftRepository.save(aircraft);
            
            return flightRepository.save(flight);
        }
        return null;
    }
    
    /**
     * Récupère tous les vols
     */
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }
    
    /**
     * Récupère un vol par ID
     */
    public Optional<Flight> getFlightById(Long id) {
        return flightRepository.findById(id);
    }
    
    /**
     * Récupère les vols d'un avion
     */
    public List<Flight> getFlightsByAircraft(Long aircraftId) {
        return flightRepository.findByAircraftId(aircraftId);
    }
    
    /**
     * Récupère les vols en cours
     */
    public List<Flight> getActiveFlights() {
        return flightRepository.findByFlightStatus(FlightStatus.EN_COURS);
    }
}

