package com.flightradar.service;

import com.flightradar.model.Aircraft;
import com.flightradar.model.AircraftStatus;
import com.flightradar.model.Communication;
import com.flightradar.model.ReceiverType;
import com.flightradar.model.RadarCenter;
import com.flightradar.model.SenderType;
import com.flightradar.model.WeatherData;
import com.flightradar.repository.AircraftRepository;
import com.flightradar.repository.CommunicationRepository;
import com.flightradar.repository.RadarCenterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les communications radar
 * - Envoi de messages depuis le radar
 * - Réception de messages
 * - Gestion des communications VHF
 * - Vérification de piste avant décollage
 * - Autorisation/défense de décollage selon météo
 */
@Service
public class RadarService {
    
    @Autowired
    private CommunicationRepository communicationRepository;
    
    @Autowired
    private RadarCenterRepository radarCenterRepository;
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private WeatherService weatherService;
    
    /**
     * Envoie un message depuis un centre radar vers un avion
     */
    public Communication sendMessageToAircraft(Long radarCenterId, Long aircraftId, String message) {
        Communication communication = new Communication();
        communication.setSenderType(SenderType.RADAR);
        communication.setSenderId(radarCenterId);
        communication.setReceiverType(ReceiverType.AIRCRAFT);
        communication.setReceiverId(aircraftId);
        communication.setMessage(message);
        communication.setFrequency(121.5); // Fréquence VHF standard
        communication.setTimestamp(LocalDateTime.now());
        
        return communicationRepository.save(communication);
    }
    
    /**
     * Envoie un message depuis un centre radar vers un aéroport
     */
    public Communication sendMessageToAirport(Long radarCenterId, Long airportId, String message) {
        Communication communication = new Communication();
        communication.setSenderType(SenderType.RADAR);
        communication.setSenderId(radarCenterId);
        communication.setReceiverType(ReceiverType.AIRPORT);
        communication.setReceiverId(airportId);
        communication.setMessage(message);
        communication.setFrequency(121.5);
        communication.setTimestamp(LocalDateTime.now());
        
        return communicationRepository.save(communication);
    }
    
    /**
     * Reçoit un message d'un avion vers le radar
     */
    public Communication receiveMessageFromAircraft(Long radarCenterId, Long aircraftId, String message) {
        Communication communication = new Communication();
        communication.setSenderType(SenderType.AIRCRAFT);
        communication.setSenderId(aircraftId);
        communication.setReceiverType(ReceiverType.RADAR);
        communication.setReceiverId(radarCenterId);
        communication.setMessage(message);
        communication.setFrequency(121.5);
        communication.setTimestamp(LocalDateTime.now());
        
        return communicationRepository.save(communication);
    }
    
    /**
     * Récupère tous les messages d'un centre radar
     */
    public List<Communication> getRadarMessages(Long radarCenterId) {
        return communicationRepository.findBySenderTypeAndSenderIdOrReceiverTypeAndReceiverId(
            SenderType.RADAR, radarCenterId, 
            ReceiverType.RADAR, radarCenterId
        );
    }
    
    /**
     * Récupère les communications d'un avion
     */
    public List<Communication> getAircraftCommunications(Long aircraftId) {
        return communicationRepository.findBySenderTypeAndSenderIdOrReceiverTypeAndReceiverId(
            SenderType.AIRCRAFT, aircraftId, 
            ReceiverType.AIRCRAFT, aircraftId
        );
    }
    
    /**
     * Récupère un centre radar par ID
     */
    public Optional<RadarCenter> getRadarCenterById(Long id) {
        return radarCenterRepository.findById(id);
    }
    
    /**
     * Récupère un centre radar par aéroport
     */
    public Optional<RadarCenter> getRadarCenterByAirport(Long airportId) {
        return radarCenterRepository.findByAirportId(airportId);
    }
    
    /**
     * Vérifie si la piste est libre pour le décollage
     * @param airportId ID de l'aéroport
     * @return true si la piste est libre
     */
    public boolean isRunwayClear(Long airportId) {
        List<Aircraft> aircraftAtAirport = aircraftRepository.findByAirportId(airportId);
        
        // Vérifier s'il y a des avions en décollage ou en atterrissage
        for (Aircraft aircraft : aircraftAtAirport) {
            if (aircraft.getStatus() == AircraftStatus.DECOLLAGE || 
                aircraft.getStatus() == AircraftStatus.ATTERRISSAGE) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Vérifie si les conditions météo permettent le décollage
     * @param airportId ID de l'aéroport
     * @return true si les conditions sont favorables
     */
    public boolean isWeatherSuitableForTakeoff(Long airportId) {
        Optional<WeatherData> weatherOpt = weatherService.getCurrentWeather(airportId);
        
        if (weatherOpt.isEmpty()) {
            return true; // Si pas de données météo, on autorise par défaut
        }
        
        WeatherData weather = weatherOpt.get();
        
        // Vérifier les conditions critiques
        if (weather.getAlert()) {
            return false;
        }
        
        // Visibilité minimale de 1 km
        if (weather.getVisibility() < 1.0) {
            return false;
        }
        
        // Vent de travers maximum 15 km/h
        if (weather.getCrosswind() > 15.0) {
            return false;
        }
        
        // Vent maximum 50 km/h
        if (weather.getWindSpeed() > 50.0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Demande d'autorisation de décollage
     * Vérifie la piste et la météo, puis envoie un message VHF
     * @param radarCenterId ID du centre radar
     * @param aircraftId ID de l'avion
     * @return Message d'autorisation ou de refus
     */
    public Communication requestTakeoffClearance(Long radarCenterId, Long aircraftId) {
        Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
        Optional<RadarCenter> radarOpt = radarCenterRepository.findById(radarCenterId);
        
        if (aircraftOpt.isEmpty() || radarOpt.isEmpty()) {
            return null;
        }
        
        Aircraft aircraft = aircraftOpt.get();
        
        if (aircraft.getAirport() == null) {
            return null;
        }
        
        Long airportId = aircraft.getAirport().getId();
        boolean runwayClear = isRunwayClear(airportId);
        boolean weatherSuitable = isWeatherSuitableForTakeoff(airportId);
        
        String message;
        if (runwayClear && weatherSuitable) {
            // Autorisation accordée
            message = String.format(
                "✅ AUTORISATION DÉCOLLAGE accordée pour %s. Piste libre, conditions météo favorables. Vous pouvez décoller.",
                aircraft.getRegistration()
            );
            
            // Changer le statut de l'avion
            aircraft.setStatus(AircraftStatus.DECOLLAGE);
            aircraftRepository.save(aircraft);
        } else {
            // Autorisation refusée
            StringBuilder reason = new StringBuilder();
            if (!runwayClear) {
                reason.append("Piste occupée. ");
            }
            if (!weatherSuitable) {
                reason.append("Conditions météo défavorables. ");
            }
            
            message = String.format(
                "❌ AUTORISATION DÉCOLLAGE refusée pour %s. %sAttendez instructions.",
                aircraft.getRegistration(),
                reason.toString()
            );
        }
        
        return sendMessageToAircraft(radarCenterId, aircraftId, message);
    }
    
    /**
     * Demande d'autorisation d'atterrissage
     * Vérifie la piste et la météo, puis envoie un message VHF
     * @param radarCenterId ID du centre radar
     * @param aircraftId ID de l'avion
     * @return Message d'autorisation ou de refus
     */
    public Communication requestLandingClearance(Long radarCenterId, Long aircraftId) {
        Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
        Optional<RadarCenter> radarOpt = radarCenterRepository.findById(radarCenterId);
        
        if (aircraftOpt.isEmpty() || radarOpt.isEmpty()) {
            return null;
        }
        
        Aircraft aircraft = aircraftOpt.get();
        
        if (aircraft.getAirport() == null) {
            return null;
        }
        
        Long airportId = aircraft.getAirport().getId();
        boolean runwayClear = isRunwayClear(airportId);
        boolean weatherSuitable = isWeatherSuitableForTakeoff(airportId); // Même logique
        
        String message;
        if (runwayClear && weatherSuitable) {
            // Autorisation accordée
            message = String.format(
                "✅ AUTORISATION ATTERRISSAGE accordée pour %s. Piste libre, conditions météo favorables. Vous pouvez atterrir.",
                aircraft.getRegistration()
            );
            
            // Changer le statut de l'avion
            aircraft.setStatus(AircraftStatus.ATTERRISSAGE);
            aircraftRepository.save(aircraft);
        } else {
            // Autorisation refusée
            StringBuilder reason = new StringBuilder();
            if (!runwayClear) {
                reason.append("Piste occupée. ");
            }
            if (!weatherSuitable) {
                reason.append("Conditions météo défavorables. ");
            }
            
            message = String.format(
                "❌ AUTORISATION ATTERRISSAGE refusée pour %s. %sMaintenez votre altitude et attendez instructions.",
                aircraft.getRegistration(),
                reason.toString()
            );
        }
        
        return sendMessageToAircraft(radarCenterId, aircraftId, message);
    }
}
