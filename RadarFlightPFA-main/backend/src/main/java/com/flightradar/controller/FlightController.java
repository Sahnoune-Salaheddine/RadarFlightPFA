package com.flightradar.controller;

import com.flightradar.model.Flight;
import com.flightradar.model.User;
import com.flightradar.model.Aircraft;
import com.flightradar.model.Role;
import com.flightradar.model.Pilot;
import com.flightradar.repository.*;
import com.flightradar.service.FlightSimulationService;
import com.flightradar.service.FlightManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur pour gérer les vols et la simulation
 */
@RestController
@RequestMapping("/api/flight")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
public class FlightController {
    
    @Autowired
    private FlightSimulationService flightSimulationService;
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PilotRepository pilotRepository;
    
    @Autowired
    private FlightManagementService flightManagementService;
    
    
    /**
     * POST /api/flight/simulate-takeoff
     * Démarre la simulation d'un vol après autorisation de décollage
     * 
     * Body: {
     *   "aircraftId": 1,
     *   "departureAirportId": 1,
     *   "arrivalAirportId": 2
     * }
     */
    @PostMapping("/simulate-takeoff")
    @PreAuthorize("hasAnyRole('PILOTE', 'ADMIN')")
    public ResponseEntity<?> simulateTakeoff(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long aircraftId = Long.valueOf(request.get("aircraftId").toString());
            Long departureAirportId = Long.valueOf(request.get("departureAirportId").toString());
            Long arrivalAirportId = Long.valueOf(request.get("arrivalAirportId").toString());
            
            // Vérifier les permissions
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur non trouvé"));
            }
            
            User user = userOpt.get();
            
            // Si c'est un pilote, vérifier qu'il est autorisé pour cet avion
            if (user.getRole() == Role.PILOTE) {
                if (aircraftId == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "ID d'avion invalide"));
                }
                Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
                if (aircraftOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Avion non trouvé"));
                }
                
                Aircraft aircraft = aircraftOpt.get();
                if (aircraft.getPilot() == null || 
                    !aircraft.getPilot().getUser().getId().equals(user.getId())) {
                    return ResponseEntity.status(403).body(Map.of(
                        "error", "Vous n'êtes pas autorisé à piloter cet avion"
                    ));
                }
            }
            
            // Vérifier qu'il n'y a pas déjà une simulation en cours
            if (flightSimulationService.isFlightInProgress(aircraftId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Un vol est déjà en cours pour cet avion"
                ));
            }
            
            // Démarrer la simulation
            Flight flight = flightSimulationService.startFlightSimulation(
                aircraftId, departureAirportId, arrivalAirportId
            );
            
            if (flight == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Impossible de démarrer la simulation"
                ));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("flightId", flight.getId());
            response.put("flightNumber", flight.getFlightNumber());
            response.put("estimatedArrival", flight.getEstimatedArrival());
            response.put("message", "Simulation de vol démarrée avec succès");
            
            log.info("Simulation démarrée par {} pour avion {}", username, aircraftId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du démarrage de la simulation", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erreur lors du démarrage de la simulation: " + e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/flight/{flightId}
     * Récupère les informations d'un vol en temps réel
     */
    @GetMapping("/{flightId}")
    @PreAuthorize("hasAnyRole('PILOTE', 'CENTRE_RADAR', 'ADMIN')")
    public ResponseEntity<?> getFlightStatus(@PathVariable Long flightId) {
        try {
            Map<String, Object> status = flightSimulationService.getFlightStatus(flightId);
            
            if (status == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du statut du vol", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erreur lors de la récupération du statut du vol"
            ));
        }
    }
    
    /**
     * GET /api/flight
     * Récupère tous les vols (pour le dashboard admin)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CENTRE_RADAR')")
    public ResponseEntity<List<Map<String, Object>>> getAllFlights() {
        try {
            List<Flight> flights = flightRepository.findAll();
            
            List<Map<String, Object>> flightsData = flights.stream()
                .map(flight -> {
                    Map<String, Object> flightData = new HashMap<>();
                    flightData.put("id", flight.getId());
                    flightData.put("flightNumber", flight.getFlightNumber());
                    flightData.put("airline", flight.getAirline());
                    flightData.put("aircraftRegistration", 
                        flight.getAircraft() != null ? flight.getAircraft().getRegistration() : "N/A");
                    flightData.put("departureAirport", 
                        flight.getDepartureAirport() != null ? 
                        flight.getDepartureAirport().getName() + " (" + flight.getDepartureAirport().getCodeIATA() + ")" : 
                        "N/A");
                    flightData.put("arrivalAirport", 
                        flight.getArrivalAirport() != null ? 
                        flight.getArrivalAirport().getName() + " (" + flight.getArrivalAirport().getCodeIATA() + ")" : 
                        "N/A");
                    flightData.put("status", flight.getFlightStatus());
                    flightData.put("scheduledDeparture", flight.getScheduledDeparture());
                    flightData.put("actualDeparture", flight.getActualDeparture());
                    flightData.put("estimatedArrival", flight.getEstimatedArrival());
                    flightData.put("scheduledArrival", flight.getScheduledArrival());
                    flightData.put("actualArrival", flight.getActualArrival());
                    return flightData;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(flightsData);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des vols", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * POST /api/flight/manage
     * Crée un nouveau vol (ADMIN uniquement)
     */
    @PostMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createFlight(
            @RequestBody Map<String, Object> flightData,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("=== TENTATIVE DE CRÉATION DE VOL ===");
            log.info("Utilisateur: {}", username);
            log.info("Données reçues: {}", flightData);
            log.info("flightNumber: {}", flightData.get("flightNumber"));
            log.info("airline: {}", flightData.get("airline"));
            log.info("aircraftId: {}", flightData.get("aircraftId"));
            log.info("departureAirportId: {}", flightData.get("departureAirportId"));
            log.info("arrivalAirportId: {}", flightData.get("arrivalAirportId"));
            log.info("scheduledDeparture: {}", flightData.get("scheduledDeparture"));
            log.info("scheduledArrival: {}", flightData.get("scheduledArrival"));
            
            Flight flight = flightManagementService.createFlight(flightData, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("flight", flight);
            response.put("message", "Vol créé avec succès");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Erreur de validation lors de la création du vol: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "type", "VALIDATION_ERROR"
            ));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("❌ ERREUR D'INTÉGRITÉ DES DONNÉES", e);
            log.error("Message complet: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getClass().getName());
                log.error("Message de la cause: {}", e.getCause().getMessage());
            }
            
            String errorMessage = "Erreur de contrainte de base de données";
            String details = e.getMessage() != null ? e.getMessage() : "";
            
            if (details.contains("flight_number") || details.contains("duplicate key")) {
                errorMessage = "Un vol avec ce numéro existe déjà";
            } else if (details.contains("foreign key") || details.contains("violates foreign key")) {
                errorMessage = "Référence invalide (avion, aéroport ou pilote non trouvé)";
            } else if (details.contains("column") && details.contains("does not exist")) {
                errorMessage = "Colonnes manquantes dans la base de données. Veuillez exécuter les scripts de migration SQL.";
            } else if (details.contains("null value in column") && details.contains("violates not-null constraint")) {
                errorMessage = "Champ obligatoire manquant";
            }
            
            return ResponseEntity.badRequest().body(Map.of(
                "error", errorMessage,
                "type", "DATA_INTEGRITY_ERROR",
                "details", details
            ));
        } catch (RuntimeException e) {
            log.error("❌ ERREUR RUNTIME", e);
            log.error("Message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getClass().getName());
                log.error("Message de la cause: {}", e.getCause().getMessage());
                if (e.getCause().getCause() != null) {
                    log.error("Cause de la cause: {}", e.getCause().getCause().getClass().getName());
                    log.error("Message de la cause de la cause: {}", e.getCause().getCause().getMessage());
                }
            }
            // Stack trace complet pour diagnostic
            log.error("Stack trace complet:", e);
            
            String errorMessage = e.getMessage();
            String details = "";
            
            // Extraire l'erreur SQL réelle si disponible
            Throwable cause = e.getCause();
            while (cause != null) {
                String causeMessage = cause.getMessage();
                if (causeMessage != null) {
                    details += causeMessage + " | ";
                    // Si c'est une erreur SQL, la mettre en avant
                    if (causeMessage.contains("column") || causeMessage.contains("does not exist") || 
                        causeMessage.contains("violates") || causeMessage.contains("constraint")) {
                        errorMessage = causeMessage;
                    }
                }
                cause = cause.getCause();
            }
            
            if (errorMessage != null && errorMessage.contains("Transaction silently rolled back")) {
                errorMessage = "Erreur de base de données. Détails: " + (details.isEmpty() ? e.getMessage() : details);
            }
            
            return ResponseEntity.badRequest().body(Map.of(
                "error", errorMessage != null ? errorMessage : "Erreur lors de la création du vol",
                "type", "RUNTIME_ERROR",
                "details", details.isEmpty() ? (e.getCause() != null ? e.getCause().getMessage() : "") : details
            ));
        } catch (Exception e) {
            log.error("❌ ERREUR INATTENDUE", e);
            log.error("Type: {}", e.getClass().getName());
            log.error("Message: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur lors de la création du vol: " + (e.getMessage() != null ? e.getMessage() : "Erreur inconnue"),
                "type", "UNKNOWN_ERROR"
            ));
        }
    }
    
    /**
     * PUT /api/flight/manage/{flightId}
     * Met à jour un vol (ADMIN uniquement, uniquement si pas en vol)
     */
    @PutMapping("/manage/{flightId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateFlight(
            @PathVariable Long flightId,
            @RequestBody Map<String, Object> flightData,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Flight flight = flightManagementService.updateFlight(flightId, flightData, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("flight", flight);
            response.put("message", "Vol modifié avec succès");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la modification du vol", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur lors de la modification du vol: " + e.getMessage()
            ));
        }
    }
    
    /**
     * DELETE /api/flight/manage/{flightId}
     * Supprime un vol (ADMIN uniquement, uniquement si pas en vol)
     */
    @DeleteMapping("/manage/{flightId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFlight(
            @PathVariable Long flightId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            flightManagementService.deleteFlight(flightId, username);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vol supprimé avec succès"
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du vol", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur lors de la suppression du vol: " + e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/flight/manage/{flightId}/details
     * Récupère les détails complets d'un vol avec météo
     */
    @GetMapping("/manage/{flightId}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'PILOTE', 'CENTRE_RADAR')")
    public ResponseEntity<?> getFlightDetails(@PathVariable Long flightId) {
        try {
            Map<String, Object> details = flightManagementService.getFlightDetails(flightId);
            
            if (details == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(details);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des détails du vol", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erreur lors de la récupération des détails du vol"
            ));
        }
    }
    
    /**
     * GET /api/flight/pilot/{pilotId}
     * Récupère les vols assignés à un pilote
     */
    @GetMapping("/pilot/{pilotId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PILOTE')")
    public ResponseEntity<?> getFlightsByPilot(
            @PathVariable Long pilotId,
            Authentication authentication) {
        try {
            // Vérifier que le pilote peut voir ses propres vols
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur non trouvé"));
            }
            
            User user = userOpt.get();
            
            // Si c'est un pilote, vérifier qu'il demande ses propres vols
            if (user.getRole() == Role.PILOTE && user.getPilotId() != null && !user.getPilotId().equals(pilotId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Vous ne pouvez voir que vos propres vols"
                ));
            }
            
            List<Flight> flights = flightManagementService.getFlightsByPilot(pilotId);
            
            List<Map<String, Object>> flightsData = flights.stream()
                .map(flight -> {
                    Map<String, Object> flightData = new HashMap<>();
                    flightData.put("id", flight.getId());
                    flightData.put("flightNumber", flight.getFlightNumber());
                    flightData.put("airline", flight.getAirline());
                    flightData.put("departureAirport", 
                        flight.getDepartureAirport() != null ? 
                        flight.getDepartureAirport().getName() + " (" + flight.getDepartureAirport().getCodeIATA() + ")" : 
                        "N/A");
                    flightData.put("arrivalAirport", 
                        flight.getArrivalAirport() != null ? 
                        flight.getArrivalAirport().getName() + " (" + flight.getArrivalAirport().getCodeIATA() + ")" : 
                        "N/A");
                    flightData.put("status", flight.getFlightStatus());
                    flightData.put("scheduledDeparture", flight.getScheduledDeparture());
                    flightData.put("scheduledArrival", flight.getScheduledArrival());
                    flightData.put("cruiseAltitude", flight.getCruiseAltitude());
                    flightData.put("cruiseSpeed", flight.getCruiseSpeed());
                    flightData.put("estimatedTimeEnroute", flight.getEstimatedTimeEnroute());
                    return flightData;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of("flights", flightsData));
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des vols du pilote", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erreur lors de la récupération des vols"
            ));
        }
    }
    
    /**
     * GET /api/flight/pilot/username/{username}
     * Récupère les vols assignés à un pilote par son username
     */
    @GetMapping("/pilot/username/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PILOTE')")
    public ResponseEntity<?> getFlightsByPilotUsername(
            @PathVariable String username,
            Authentication authentication) {
        try {
            // Vérifier que le pilote peut voir ses propres vols
            String currentUsername = null;
            Object principal = authentication.getPrincipal();
            
            // Extraire le username du principal
            if (principal instanceof User) {
                currentUsername = ((User) principal).getUsername();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                currentUsername = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            } else {
                currentUsername = authentication.getName();
            }
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur non trouvé"));
            }
            
            User user = userOpt.get();
            
            // Si c'est un pilote, vérifier qu'il demande ses propres vols
            if (user.getRole() == Role.PILOTE && currentUsername != null && !currentUsername.equals(username)) {
                log.warn("Tentative d'accès non autorisé: {} essaie d'accéder aux vols de {}", currentUsername, username);
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Vous ne pouvez voir que vos propres vols"
                ));
            }
            
            // Si currentUsername est null, permettre l'accès (peut être un admin)
            if (currentUsername == null && user.getRole() == Role.PILOTE) {
                log.warn("Username non trouvé dans l'authentification pour la vérification des vols");
            }
            
            // Trouver le pilote
            Optional<Pilot> pilotOpt = pilotRepository.findByUserId(user.getId());
            if (pilotOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of("flights", List.of()));
            }
            
            Pilot pilot = pilotOpt.get();
            List<Flight> flights = flightManagementService.getFlightsByPilot(pilot.getId());
            
            // Récupérer les détails complets avec météo
            List<Map<String, Object>> flightsData = flights.stream()
                .map(flight -> {
                    Map<String, Object> flightDetails = flightManagementService.getFlightDetails(flight.getId());
                    return flightDetails != null ? flightDetails : new HashMap<String, Object>();
                })
                .filter(flight -> !flight.isEmpty())
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of("flights", flightsData));
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des vols du pilote", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erreur lors de la récupération des vols"
            ));
        }
    }
}
