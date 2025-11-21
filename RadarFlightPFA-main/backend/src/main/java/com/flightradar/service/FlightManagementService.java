package com.flightradar.service;

import com.flightradar.model.*;
import com.flightradar.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service pour la gestion complète des vols (CRUD)
 */
@Service
@Slf4j
public class FlightManagementService {
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private AirportRepository airportRepository;
    
    @Autowired
    private PilotRepository pilotRepository;
    
    @Autowired
    private WeatherService weatherService;
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    /**
     * Crée un nouveau vol
     */
    @Transactional(rollbackFor = {IllegalArgumentException.class, org.springframework.dao.DataIntegrityViolationException.class})
    public Flight createFlight(Map<String, Object> flightData, String username) {
        try {
            log.info("=== DÉBUT CRÉATION VOL ===");
            log.info("Données reçues: {}", flightData);
            
            Flight flight = new Flight();
            
            // Champs obligatoires
            String flightNumber = (String) flightData.get("flightNumber");
            log.info("flightNumber extrait: {}", flightNumber);
            if (flightNumber == null || flightNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Le numéro de vol est obligatoire");
            }
            
            // Vérifier l'unicité du numéro de vol
            Optional<Flight> existingFlight = flightRepository.findByFlightNumber(flightNumber);
            if (existingFlight.isPresent()) {
                throw new IllegalArgumentException("Un vol avec le numéro " + flightNumber + " existe déjà");
            }
            
            flight.setFlightNumber(flightNumber);
            flight.setAirline((String) flightData.get("airline"));
            log.info("Numéro de vol et compagnie définis");
            
            // Avion
            Object aircraftIdObj = flightData.get("aircraftId");
            log.info("aircraftId reçu: {} (type: {})", aircraftIdObj, aircraftIdObj != null ? aircraftIdObj.getClass().getName() : "null");
            
            if (aircraftIdObj == null) {
                throw new IllegalArgumentException("L'ID de l'avion est obligatoire");
            }
            
            Long aircraftId;
            try {
                if (aircraftIdObj instanceof Number) {
                    aircraftId = ((Number) aircraftIdObj).longValue();
                } else {
                    aircraftId = Long.valueOf(aircraftIdObj.toString());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Format d'ID d'avion invalide: " + aircraftIdObj, e);
            }
            
            log.info("Recherche avion avec ID: {}", aircraftId);
            Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
            if (aircraftOpt.isEmpty()) {
                log.error("Avion non trouvé avec ID: {}", aircraftId);
                throw new IllegalArgumentException("Avion non trouvé avec l'ID: " + aircraftId);
            }
            flight.setAircraft(aircraftOpt.get());
            log.info("Avion trouvé: {}", aircraftOpt.get().getRegistration());
            
            // Aéroports
            Object depAirportIdObj = flightData.get("departureAirportId");
            Object arrAirportIdObj = flightData.get("arrivalAirportId");
            
            log.info("departureAirportId reçu: {}", depAirportIdObj);
            log.info("arrivalAirportId reçu: {}", arrAirportIdObj);
            
            if (depAirportIdObj == null || arrAirportIdObj == null) {
                throw new IllegalArgumentException("Les IDs d'aéroport de départ et d'arrivée sont obligatoires");
            }
            
            Long departureAirportId;
            Long arrivalAirportId;
            try {
                if (depAirportIdObj instanceof Number) {
                    departureAirportId = ((Number) depAirportIdObj).longValue();
                } else {
                    departureAirportId = Long.valueOf(depAirportIdObj.toString());
                }
                
                if (arrAirportIdObj instanceof Number) {
                    arrivalAirportId = ((Number) arrAirportIdObj).longValue();
                } else {
                    arrivalAirportId = Long.valueOf(arrAirportIdObj.toString());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Format d'ID d'aéroport invalide", e);
            }
            
            log.info("Recherche aéroports: départ={}, arrivée={}", departureAirportId, arrivalAirportId);
            Optional<Airport> depAirportOpt = airportRepository.findById(departureAirportId);
            Optional<Airport> arrAirportOpt = airportRepository.findById(arrivalAirportId);
            
            if (depAirportOpt.isEmpty()) {
                log.error("Aéroport de départ non trouvé avec ID: {}", departureAirportId);
                throw new IllegalArgumentException("Aéroport de départ non trouvé avec l'ID: " + departureAirportId);
            }
            if (arrAirportOpt.isEmpty()) {
                log.error("Aéroport d'arrivée non trouvé avec ID: {}", arrivalAirportId);
                throw new IllegalArgumentException("Aéroport d'arrivée non trouvé avec l'ID: " + arrivalAirportId);
            }
            
            flight.setDepartureAirport(depAirportOpt.get());
            flight.setArrivalAirport(arrAirportOpt.get());
            log.info("Aéroports définis: {} -> {}", depAirportOpt.get().getCodeIATA(), arrAirportOpt.get().getCodeIATA());
            
            // Aéroport alternatif (optionnel)
            if (flightData.get("alternateAirportId") != null && !flightData.get("alternateAirportId").toString().trim().isEmpty()) {
                try {
                    Long alternateId = Long.valueOf(flightData.get("alternateAirportId").toString());
                    // Vérifier que l'aéroport alternatif existe
                    Optional<Airport> altAirportOpt = airportRepository.findById(alternateId);
                    if (altAirportOpt.isPresent()) {
                        flight.setAlternateAirportId(alternateId);
                        log.info("Aéroport alternatif assigné: {}", alternateId);
                    } else {
                        log.warn("Aéroport alternatif non trouvé avec ID: {}, le vol sera créé sans aéroport alternatif", alternateId);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Format d'ID d'aéroport alternatif invalide: {}, le vol sera créé sans aéroport alternatif", flightData.get("alternateAirportId"));
                }
            }
            
            // Dates
            if (flightData.get("scheduledDeparture") != null) {
                try {
                    String depStr = flightData.get("scheduledDeparture").toString();
                    // Gérer le format datetime-local (YYYY-MM-DDTHH:mm) ou ISO (YYYY-MM-DDTHH:mm:ss)
                    if (depStr.contains("T")) {
                        // Format datetime-local sans secondes
                        if (depStr.length() == 16) {
                            // YYYY-MM-DDTHH:mm -> ajouter :00 pour les secondes
                            depStr = depStr + ":00";
                        }
                        flight.setScheduledDeparture(LocalDateTime.parse(depStr));
                    } else {
                        throw new IllegalArgumentException("Format de date de départ invalide (doit contenir 'T'): " + depStr);
                    }
                } catch (Exception e) {
                    log.error("Erreur parsing scheduledDeparture: {}", flightData.get("scheduledDeparture"), e);
                    throw new IllegalArgumentException("Format de date de départ invalide: " + flightData.get("scheduledDeparture") + " - " + e.getMessage(), e);
                }
            }
            if (flightData.get("scheduledArrival") != null) {
                try {
                    String arrStr = flightData.get("scheduledArrival").toString();
                    // Gérer le format datetime-local (YYYY-MM-DDTHH:mm) ou ISO (YYYY-MM-DDTHH:mm:ss)
                    if (arrStr.contains("T")) {
                        // Format datetime-local sans secondes
                        if (arrStr.length() == 16) {
                            // YYYY-MM-DDTHH:mm -> ajouter :00 pour les secondes
                            arrStr = arrStr + ":00";
                        }
                        flight.setScheduledArrival(LocalDateTime.parse(arrStr));
                    } else {
                        throw new IllegalArgumentException("Format de date d'arrivée invalide (doit contenir 'T'): " + arrStr);
                    }
                } catch (Exception e) {
                    log.error("Erreur parsing scheduledArrival: {}", flightData.get("scheduledArrival"), e);
                    throw new IllegalArgumentException("Format de date d'arrivée invalide: " + flightData.get("scheduledArrival") + " - " + e.getMessage(), e);
                }
            }
            
            // Calculer ETE si dates disponibles
            if (flight.getScheduledDeparture() != null && flight.getScheduledArrival() != null) {
                Duration duration = Duration.between(flight.getScheduledDeparture(), flight.getScheduledArrival());
                flight.setEstimatedTimeEnroute((int) duration.toMinutes());
            }
            
            // Altitude et vitesse
            if (flightData.get("cruiseAltitude") != null && !flightData.get("cruiseAltitude").toString().trim().isEmpty()) {
                try {
                    Integer altitude = Integer.valueOf(flightData.get("cruiseAltitude").toString());
                    if (altitude > 0 && altitude <= 50000) { // Validation: altitude entre 0 et 50000 pieds
                        flight.setCruiseAltitude(altitude);
                    } else {
                        log.warn("Altitude de croisière invalide: {}, valeur ignorée", altitude);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Format d'altitude de croisière invalide: {}", flightData.get("cruiseAltitude"));
                }
            }
            if (flightData.get("cruiseSpeed") != null && !flightData.get("cruiseSpeed").toString().trim().isEmpty()) {
                try {
                    Integer speed = Integer.valueOf(flightData.get("cruiseSpeed").toString());
                    if (speed > 0 && speed <= 1000) { // Validation: vitesse entre 0 et 1000 nœuds
                        flight.setCruiseSpeed(speed);
                    } else {
                        log.warn("Vitesse de croisière invalide: {}, valeur ignorée", speed);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Format de vitesse de croisière invalide: {}", flightData.get("cruiseSpeed"));
                }
            }
            
            // Type de vol
            if (flightData.get("flightType") != null) {
                try {
                    flight.setFlightType(Flight.FlightType.valueOf(flightData.get("flightType").toString().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    flight.setFlightType(Flight.FlightType.COMMERCIAL); // Par défaut
                }
            } else {
                flight.setFlightType(Flight.FlightType.COMMERCIAL);
            }
            
            // Pilote assigné
            if (flightData.get("pilotId") != null && !flightData.get("pilotId").toString().trim().isEmpty()) {
                try {
                    Long pilotId = Long.valueOf(flightData.get("pilotId").toString());
                    // Vérifier que le pilote existe
                    Optional<Pilot> pilotOpt = pilotRepository.findById(pilotId);
                    if (pilotOpt.isPresent()) {
                        flight.setPilotId(pilotId);
                        log.info("Pilote assigné: {}", pilotId);
                    } else {
                        log.warn("Pilote non trouvé avec ID: {}, le vol sera créé sans pilote assigné", pilotId);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Format d'ID de pilote invalide: {}, le vol sera créé sans pilote assigné", flightData.get("pilotId"));
                }
            }
            
            // Statut initial
            if (flightData.get("flightStatus") != null) {
                try {
                    flight.setFlightStatus(FlightStatus.valueOf(flightData.get("flightStatus").toString().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    flight.setFlightStatus(FlightStatus.PLANIFIE);
                }
            } else {
                flight.setFlightStatus(FlightStatus.PLANIFIE);
            }
            
            log.info("Tentative de sauvegarde du vol...");
            log.info("Données du vol avant sauvegarde:");
            log.info("  - flightNumber: {}", flight.getFlightNumber());
            log.info("  - airline: {}", flight.getAirline());
            log.info("  - aircraft: {}", flight.getAircraft() != null ? flight.getAircraft().getId() : "null");
            log.info("  - departureAirport: {}", flight.getDepartureAirport() != null ? flight.getDepartureAirport().getId() : "null");
            log.info("  - arrivalAirport: {}", flight.getArrivalAirport() != null ? flight.getArrivalAirport().getId() : "null");
            log.info("  - flightStatus: {}", flight.getFlightStatus());
            log.info("  - flightType: {}", flight.getFlightType());
            log.info("  - cruiseAltitude: {}", flight.getCruiseAltitude());
            log.info("  - cruiseSpeed: {}", flight.getCruiseSpeed());
            log.info("  - pilotId: {}", flight.getPilotId());
            
            Flight savedFlight;
            try {
                savedFlight = flightRepository.save(flight);
                log.info("✅ Vol sauvegardé avec succès. ID: {}", savedFlight.getId());
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.error("❌ ERREUR D'INTÉGRITÉ LORS DE LA SAUVEGARDE", e);
                log.error("Message: {}", e.getMessage());
                if (e.getCause() != null) {
                    log.error("Cause: {}", e.getCause().getClass().getName());
                    log.error("Message de la cause: {}", e.getCause().getMessage());
                }
                // Relancer comme DataIntegrityViolationException pour que le contrôleur la gère
                throw e;
            } catch (Exception e) {
                log.error("❌ ERREUR INATTENDUE LORS DE LA SAUVEGARDE", e);
                log.error("Type d'exception: {}", e.getClass().getName());
                log.error("Message d'erreur: {}", e.getMessage());
                log.error("Cause: {}", e.getCause());
                if (e.getCause() != null) {
                    log.error("Type de la cause: {}", e.getCause().getClass().getName());
                    log.error("Message de la cause: {}", e.getCause().getMessage());
                }
                // Stack trace complet
                log.error("Stack trace:", e);
                throw new RuntimeException("Erreur lors de la sauvegarde du vol: " + e.getMessage(), e);
            }
            
            log.info("Vol créé avec succès: {} (ID: {}) par {}", savedFlight.getFlightNumber(), savedFlight.getId(), username);
            
            // Journaliser l'action dans une transaction séparée pour éviter les rollbacks
            // DÉSACTIVÉ TEMPORAIREMENT pour diagnostiquer le problème
            // try {
            //     logActivity(username, ActivityLog.ActivityType.FLIGHT_CREATED, 
            //         "Création du vol " + savedFlight.getFlightNumber(), 
            //         "FLIGHT", savedFlight.getId(), ActivityLog.LogSeverity.INFO);
            // } catch (Exception e) {
            //     // Ne pas faire échouer la création du vol si la journalisation échoue
            //     log.warn("Erreur lors de la journalisation (non bloquante)", e);
            // }
            
            return savedFlight;
            
        } catch (IllegalArgumentException e) {
            log.error("Erreur de validation lors de la création du vol: {}", e.getMessage());
            throw e; // Relancer les exceptions de validation telles quelles
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la création du vol", e);
            throw new RuntimeException("Erreur lors de la création du vol: " + e.getMessage(), e);
        }
    }
    
    /**
     * Met à jour un vol (uniquement s'il n'est pas en vol)
     */
    @Transactional(rollbackFor = Exception.class)
    public Flight updateFlight(Long flightId, Map<String, Object> flightData, String username) {
            Optional<Flight> flightOpt = flightRepository.findById(flightId);
            if (flightOpt.isEmpty()) {
                throw new IllegalArgumentException("Vol non trouvé");
            }
            
            Flight flight = flightOpt.get();
            
            // Vérifier que le vol n'est pas en cours
            if (flight.getFlightStatus() == FlightStatus.EN_COURS) {
                throw new IllegalStateException("Impossible de modifier un vol en cours");
            }
            
            // Mettre à jour les champs modifiables
            if (flightData.get("flightNumber") != null) {
                flight.setFlightNumber((String) flightData.get("flightNumber"));
            }
            if (flightData.get("airline") != null) {
                flight.setAirline((String) flightData.get("airline"));
            }
            
            if (flightData.get("aircraftId") != null) {
                Long aircraftId = Long.valueOf(flightData.get("aircraftId").toString());
                Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
                if (aircraftOpt.isPresent()) {
                    flight.setAircraft(aircraftOpt.get());
                }
            }
            
            if (flightData.get("departureAirportId") != null) {
                Long airportId = Long.valueOf(flightData.get("departureAirportId").toString());
                airportRepository.findById(airportId).ifPresent(flight::setDepartureAirport);
            }
            
            if (flightData.get("arrivalAirportId") != null) {
                Long airportId = Long.valueOf(flightData.get("arrivalAirportId").toString());
                airportRepository.findById(airportId).ifPresent(flight::setArrivalAirport);
            }
            
            if (flightData.get("alternateAirportId") != null) {
                flight.setAlternateAirportId(Long.valueOf(flightData.get("alternateAirportId").toString()));
            } else if (flightData.containsKey("alternateAirportId") && flightData.get("alternateAirportId") == null) {
                flight.setAlternateAirportId(null);
            }
            
            if (flightData.get("scheduledDeparture") != null) {
                flight.setScheduledDeparture(LocalDateTime.parse(flightData.get("scheduledDeparture").toString()));
            }
            if (flightData.get("scheduledArrival") != null) {
                flight.setScheduledArrival(LocalDateTime.parse(flightData.get("scheduledArrival").toString()));
            }
            
            // Recalculer ETE
            if (flight.getScheduledDeparture() != null && flight.getScheduledArrival() != null) {
                Duration duration = Duration.between(flight.getScheduledDeparture(), flight.getScheduledArrival());
                flight.setEstimatedTimeEnroute((int) duration.toMinutes());
            }
            
            if (flightData.get("cruiseAltitude") != null) {
                flight.setCruiseAltitude(Integer.valueOf(flightData.get("cruiseAltitude").toString()));
            }
            if (flightData.get("cruiseSpeed") != null) {
                flight.setCruiseSpeed(Integer.valueOf(flightData.get("cruiseSpeed").toString()));
            }
            
            if (flightData.get("flightType") != null) {
                try {
                    flight.setFlightType(Flight.FlightType.valueOf(flightData.get("flightType").toString().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Ignorer si type invalide
                }
            }
            
            if (flightData.get("pilotId") != null) {
                flight.setPilotId(Long.valueOf(flightData.get("pilotId").toString()));
            }
            
            Flight updatedFlight = flightRepository.save(flight);
            
            log.info("Vol modifié: {} par {}", updatedFlight.getFlightNumber(), username);
            
            // Journaliser l'action dans une transaction séparée
            try {
                logActivity(username, ActivityLog.ActivityType.FLIGHT_UPDATED, 
                    "Modification du vol " + updatedFlight.getFlightNumber(), 
                    "FLIGHT", updatedFlight.getId(), ActivityLog.LogSeverity.INFO);
            } catch (Exception e) {
                log.warn("Erreur lors de la journalisation (non bloquante)", e);
            }
            
            return updatedFlight;
    }
    
    /**
     * Supprime un vol (uniquement s'il n'est pas en vol)
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFlight(Long flightId, String username) {
            Optional<Flight> flightOpt = flightRepository.findById(flightId);
            if (flightOpt.isEmpty()) {
                throw new IllegalArgumentException("Vol non trouvé");
            }
            
            Flight flight = flightOpt.get();
            
            // Vérifier que le vol n'est pas en cours
            if (flight.getFlightStatus() == FlightStatus.EN_COURS) {
                throw new IllegalStateException("Impossible de supprimer un vol en cours");
            }
            
            String flightNumber = flight.getFlightNumber();
            flightRepository.delete(flight);
            
            log.info("Vol supprimé: {} par {}", flightNumber, username);
            
            // Journaliser l'action dans une transaction séparée
            try {
                logActivity(username, ActivityLog.ActivityType.FLIGHT_CANCELLED, 
                    "Suppression du vol " + flightNumber, 
                    "FLIGHT", flightId, ActivityLog.LogSeverity.WARNING);
            } catch (Exception e) {
                log.warn("Erreur lors de la journalisation (non bloquante)", e);
            }
    }
    
    /**
     * Récupère les vols assignés à un pilote
     */
    public List<Flight> getFlightsByPilot(Long pilotId) {
        return flightRepository.findByPilotId(pilotId);
    }
    
    /**
     * Récupère un vol avec toutes ses informations (météo, etc.)
     */
    public Map<String, Object> getFlightDetails(Long flightId) {
        Optional<Flight> flightOpt = flightRepository.findById(flightId);
        if (flightOpt.isEmpty()) {
            return null;
        }
        
        Flight flight = flightOpt.get();
        Map<String, Object> details = new HashMap<>();
        
        // Informations de base
        details.put("id", flight.getId());
        details.put("flightNumber", flight.getFlightNumber());
        details.put("airline", flight.getAirline());
        details.put("flightStatus", flight.getFlightStatus());
        details.put("flightType", flight.getFlightType());
        details.put("scheduledDeparture", flight.getScheduledDeparture());
        details.put("scheduledArrival", flight.getScheduledArrival());
        details.put("actualDeparture", flight.getActualDeparture());
        details.put("actualArrival", flight.getActualArrival());
        details.put("estimatedArrival", flight.getEstimatedArrival());
        details.put("cruiseAltitude", flight.getCruiseAltitude());
        details.put("cruiseSpeed", flight.getCruiseSpeed());
        details.put("estimatedTimeEnroute", flight.getEstimatedTimeEnroute());
        
        // Avion
        if (flight.getAircraft() != null) {
            Map<String, Object> aircraft = new HashMap<>();
            aircraft.put("id", flight.getAircraft().getId());
            aircraft.put("registration", flight.getAircraft().getRegistration());
            aircraft.put("model", flight.getAircraft().getModel());
            details.put("aircraft", aircraft);
        }
        
        // Aéroport de départ
        if (flight.getDepartureAirport() != null) {
            Map<String, Object> depAirport = new HashMap<>();
            depAirport.put("id", flight.getDepartureAirport().getId());
            depAirport.put("name", flight.getDepartureAirport().getName());
            depAirport.put("codeIATA", flight.getDepartureAirport().getCodeIATA());
            depAirport.put("latitude", flight.getDepartureAirport().getLatitude());
            depAirport.put("longitude", flight.getDepartureAirport().getLongitude());
            
            // Météo pour l'aéroport de départ
            weatherService.getCurrentWeather(flight.getDepartureAirport().getId())
                .ifPresent(weather -> {
                    Map<String, Object> weatherData = new HashMap<>();
                    weatherData.put("temperature", weather.getTemperature());
                    weatherData.put("windSpeed", weather.getWindSpeed());
                    weatherData.put("windDirection", weather.getWindDirection());
                    weatherData.put("visibility", weather.getVisibility());
                    weatherData.put("conditions", weather.getConditions());
                    weatherData.put("alert", weather.getAlert());
                    depAirport.put("weather", weatherData);
                });
            
            details.put("departureAirport", depAirport);
        }
        
        // Aéroport d'arrivée
        if (flight.getArrivalAirport() != null) {
            Map<String, Object> arrAirport = new HashMap<>();
            arrAirport.put("id", flight.getArrivalAirport().getId());
            arrAirport.put("name", flight.getArrivalAirport().getName());
            arrAirport.put("codeIATA", flight.getArrivalAirport().getCodeIATA());
            arrAirport.put("latitude", flight.getArrivalAirport().getLatitude());
            arrAirport.put("longitude", flight.getArrivalAirport().getLongitude());
            
            // Météo pour l'aéroport d'arrivée
            weatherService.getCurrentWeather(flight.getArrivalAirport().getId())
                .ifPresent(weather -> {
                    Map<String, Object> weatherData = new HashMap<>();
                    weatherData.put("temperature", weather.getTemperature());
                    weatherData.put("windSpeed", weather.getWindSpeed());
                    weatherData.put("windDirection", weather.getWindDirection());
                    weatherData.put("visibility", weather.getVisibility());
                    weatherData.put("conditions", weather.getConditions());
                    weatherData.put("alert", weather.getAlert());
                    arrAirport.put("weather", weatherData);
                });
            
            details.put("arrivalAirport", arrAirport);
        }
        
        // Aéroport alternatif
        if (flight.getAlternateAirportId() != null) {
            airportRepository.findById(flight.getAlternateAirportId()).ifPresent(altAirport -> {
                Map<String, Object> alt = new HashMap<>();
                alt.put("id", altAirport.getId());
                alt.put("name", altAirport.getName());
                alt.put("codeIATA", altAirport.getCodeIATA());
                details.put("alternateAirport", alt);
            });
        }
        
        // Pilote
        if (flight.getPilotId() != null) {
            pilotRepository.findById(flight.getPilotId()).ifPresent(pilot -> {
                Map<String, Object> pilotData = new HashMap<>();
                pilotData.put("id", pilot.getId());
                pilotData.put("firstName", pilot.getFirstName());
                pilotData.put("lastName", pilot.getLastName());
                pilotData.put("license", pilot.getLicense());
                details.put("pilot", pilotData);
            });
        }
        
        return details;
    }
    
    /**
     * Journalise une activité dans une transaction séparée pour éviter les rollbacks
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    private void logActivity(String username, ActivityLog.ActivityType activityType, 
                            String description, String entityType, Long entityId, 
                            ActivityLog.LogSeverity severity) {
        try {
            ActivityLog activityLog = new ActivityLog();
            activityLog.setUsername(username);
            activityLog.setActivityType(activityType);
            activityLog.setDescription(description);
            activityLog.setEntityType(entityType);
            activityLog.setEntityId(entityId);
            activityLog.setSeverity(severity);
            activityLog.setTimestamp(LocalDateTime.now());
            activityLogRepository.save(activityLog);
        } catch (Exception e) {
            // Si la table activity_logs n'existe pas encore, on ignore l'erreur
            log.warn("Impossible de journaliser l'activité (table activity_logs peut-être absente): {}", e.getMessage());
        }
    }
}

