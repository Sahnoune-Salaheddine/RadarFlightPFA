package com.flightradar.service;

import com.flightradar.model.Aircraft;
import com.flightradar.model.AircraftStatus;
import com.flightradar.model.RadarCenter;
import com.flightradar.repository.AircraftRepository;
import com.flightradar.repository.RadarCenterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour détecter les conflits de trajectoire entre avions
 * - Détection automatique de risques de collision
 * - Génération d'alertes automatiques
 * - Envoi de messages VHF automatiques aux pilotes
 */
@Service
public class ConflictDetectionService {
    
    // Distance minimale de sécurité entre avions (en kilomètres)
    private static final double MIN_SAFE_DISTANCE_KM = 5.0;
    
    // Distance minimale verticale (en mètres)
    private static final double MIN_SAFE_ALTITUDE_DIFF_M = 300.0;
    
    // Distance critique (en kilomètres) - alerte immédiate
    private static final double CRITICAL_DISTANCE_KM = 2.0;
    
    @Autowired
    private AircraftRepository aircraftRepository;
    
    @Autowired
    private RadarService radarService;
    
    @Autowired
    private RadarCenterRepository radarCenterRepository;
    
    /**
     * Détecte les conflits potentiels entre tous les avions en vol
     * Exécuté toutes les 5 secondes
     */
    @Scheduled(fixedRate = 5000)
    public void detectConflicts() {
        List<Aircraft> aircraftInFlight = aircraftRepository.findByStatus(AircraftStatus.EN_VOL);
        
        // Comparer chaque paire d'avions
        for (int i = 0; i < aircraftInFlight.size(); i++) {
            for (int j = i + 1; j < aircraftInFlight.size(); j++) {
                Aircraft aircraft1 = aircraftInFlight.get(i);
                Aircraft aircraft2 = aircraftInFlight.get(j);
                
                if (aircraft1.getPositionLat() != null && aircraft1.getPositionLon() != null &&
                    aircraft2.getPositionLat() != null && aircraft2.getPositionLon() != null) {
                    
                    ConflictInfo conflict = checkConflict(aircraft1, aircraft2);
                    
                    if (conflict != null && conflict.isConflict()) {
                        handleConflict(aircraft1, aircraft2, conflict);
                    }
                }
            }
        }
    }
    
    /**
     * Vérifie s'il y a un conflit entre deux avions
     */
    public ConflictInfo checkConflict(Aircraft aircraft1, Aircraft aircraft2) {
        double distance = calculateDistance(
            aircraft1.getPositionLat(), aircraft1.getPositionLon(),
            aircraft2.getPositionLat(), aircraft2.getPositionLon()
        );
        
        double altitudeDiff = Math.abs(
            (aircraft1.getAltitude() != null ? aircraft1.getAltitude() : 0) -
            (aircraft2.getAltitude() != null ? aircraft2.getAltitude() : 0)
        );
        
        // Calculer la vitesse de rapprochement
        double closingSpeed = calculateClosingSpeed(aircraft1, aircraft2);
        
        // Calculer le temps jusqu'à la collision potentielle
        double timeToConflict = distance > 0 ? distance / closingSpeed : Double.MAX_VALUE;
        
        boolean isConflict = false;
        ConflictSeverity severity = ConflictSeverity.LOW;
        
        // Vérifier la distance horizontale
        if (distance < CRITICAL_DISTANCE_KM && altitudeDiff < MIN_SAFE_ALTITUDE_DIFF_M) {
            isConflict = true;
            severity = ConflictSeverity.CRITICAL;
        } else if (distance < MIN_SAFE_DISTANCE_KM && altitudeDiff < MIN_SAFE_ALTITUDE_DIFF_M) {
            isConflict = true;
            severity = ConflictSeverity.HIGH;
        } else if (distance < MIN_SAFE_DISTANCE_KM * 1.5 && altitudeDiff < MIN_SAFE_ALTITUDE_DIFF_M * 1.5) {
            isConflict = true;
            severity = ConflictSeverity.MEDIUM;
        }
        
        // Vérifier si les trajectoires convergent
        if (isConflict && timeToConflict < 60 && timeToConflict > 0) { // Moins d'1 minute
            severity = ConflictSeverity.CRITICAL;
        }
        
        return new ConflictInfo(isConflict, distance, altitudeDiff, closingSpeed, timeToConflict, severity);
    }
    
    /**
     * Calcule la distance entre deux points GPS (formule de Haversine)
     * @return Distance en kilomètres
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en kilomètres
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Calcule la vitesse de rapprochement entre deux avions
     * @return Vitesse en km/h
     */
    private double calculateClosingSpeed(Aircraft aircraft1, Aircraft aircraft2) {
        // Approximation simple : vitesse relative
        double speed1 = aircraft1.getSpeed() != null ? aircraft1.getSpeed() : 0;
        double speed2 = aircraft2.getSpeed() != null ? aircraft2.getSpeed() : 0;
        
        // Calculer l'angle entre les deux trajectoires
        double heading1 = aircraft1.getHeading() != null ? aircraft1.getHeading() : 0;
        double heading2 = aircraft2.getHeading() != null ? aircraft2.getHeading() : 0;
        
        double headingDiff = Math.abs(heading1 - heading2);
        if (headingDiff > 180) {
            headingDiff = 360 - headingDiff;
        }
        
        // Si les avions volent dans des directions opposées, vitesse de rapprochement = somme
        // Sinon, approximation
        if (headingDiff > 90) {
            return speed1 + speed2;
        } else {
            return Math.abs(speed1 - speed2);
        }
    }
    
    /**
     * Gère un conflit détecté en envoyant des alertes
     */
    private void handleConflict(Aircraft aircraft1, Aircraft aircraft2, ConflictInfo conflict) {
        // Trouver le centre radar le plus proche
        RadarCenter radarCenter = findNearestRadarCenter(aircraft1);
        
        if (radarCenter == null) {
            return;
        }
        
        // Générer des messages d'alerte
        String message1 = generateAlertMessage(aircraft2, conflict);
        String message2 = generateAlertMessage(aircraft1, conflict);
        
        // Envoyer les messages VHF
        if (conflict.getSeverity() == ConflictSeverity.CRITICAL) {
            radarService.sendMessageToAircraft(
                radarCenter.getId(),
                aircraft1.getId(),
                "⚠️ ALERTE CRITIQUE: " + message1
            );
            radarService.sendMessageToAircraft(
                radarCenter.getId(),
                aircraft2.getId(),
                "⚠️ ALERTE CRITIQUE: " + message2
            );
        } else if (conflict.getSeverity() == ConflictSeverity.HIGH) {
            radarService.sendMessageToAircraft(
                radarCenter.getId(),
                aircraft1.getId(),
                "⚠️ ALERTE: " + message1
            );
            radarService.sendMessageToAircraft(
                radarCenter.getId(),
                aircraft2.getId(),
                "⚠️ ALERTE: " + message2
            );
        } else {
            radarService.sendMessageToAircraft(
                radarCenter.getId(),
                aircraft1.getId(),
                "ℹ️ AVIS: " + message1
            );
            radarService.sendMessageToAircraft(
                radarCenter.getId(),
                aircraft2.getId(),
                "ℹ️ AVIS: " + message2
            );
        }
    }
    
    /**
     * Génère un message d'alerte pour un pilote
     */
    private String generateAlertMessage(Aircraft otherAircraft, ConflictInfo conflict) {
        StringBuilder message = new StringBuilder();
        
        message.append("Trafic proche: ").append(otherAircraft.getRegistration());
        message.append(" - Distance: ").append(String.format("%.1f", conflict.getDistance())).append(" km");
        message.append(" - Altitude diff: ").append(String.format("%.0f", conflict.getAltitudeDiff())).append(" m");
        
        if (conflict.getTimeToConflict() < Double.MAX_VALUE && conflict.getTimeToConflict() > 0) {
            message.append(" - Temps estimé: ").append(String.format("%.0f", conflict.getTimeToConflict())).append(" s");
        }
        
        if (conflict.getSeverity() == ConflictSeverity.CRITICAL) {
            message.append(" - ACTION REQUISE: Ajustez altitude ou vitesse immédiatement");
        } else if (conflict.getSeverity() == ConflictSeverity.HIGH) {
            message.append(" - Surveillez attentivement");
        }
        
        return message.toString();
    }
    
    /**
     * Trouve le centre radar le plus proche d'un avion
     */
    private RadarCenter findNearestRadarCenter(Aircraft aircraft) {
        List<RadarCenter> radarCenters = radarCenterRepository.findAll();
        
        if (radarCenters.isEmpty() || aircraft.getPositionLat() == null || aircraft.getPositionLon() == null) {
            return null;
        }
        
        RadarCenter nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (RadarCenter radar : radarCenters) {
            if (radar.getAirport() != null && radar.getAirport().getLatitude() != null) {
                double distance = calculateDistance(
                    aircraft.getPositionLat(), aircraft.getPositionLon(),
                    radar.getAirport().getLatitude(), radar.getAirport().getLongitude()
                );
                
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = radar;
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * Récupère tous les conflits actifs
     */
    public List<ConflictAlert> getActiveConflicts() {
        List<ConflictAlert> alerts = new ArrayList<>();
        List<Aircraft> aircraftInFlight = aircraftRepository.findByStatus(AircraftStatus.EN_VOL);
        
        for (int i = 0; i < aircraftInFlight.size(); i++) {
            for (int j = i + 1; j < aircraftInFlight.size(); j++) {
                Aircraft aircraft1 = aircraftInFlight.get(i);
                Aircraft aircraft2 = aircraftInFlight.get(j);
                
                if (aircraft1.getPositionLat() != null && aircraft1.getPositionLon() != null &&
                    aircraft2.getPositionLat() != null && aircraft2.getPositionLon() != null) {
                    
                    ConflictInfo conflict = checkConflict(aircraft1, aircraft2);
                    
                    if (conflict != null && conflict.isConflict()) {
                        alerts.add(new ConflictAlert(aircraft1, aircraft2, conflict));
                    }
                }
            }
        }
        
        return alerts;
    }
    
    /**
     * Classe interne pour stocker les informations de conflit
     */
    public static class ConflictInfo {
        private final boolean conflict;
        private final double distance;
        private final double altitudeDiff;
        private final double closingSpeed;
        private final double timeToConflict;
        private final ConflictSeverity severity;
        
        public ConflictInfo(boolean conflict, double distance, double altitudeDiff, 
                          double closingSpeed, double timeToConflict, ConflictSeverity severity) {
            this.conflict = conflict;
            this.distance = distance;
            this.altitudeDiff = altitudeDiff;
            this.closingSpeed = closingSpeed;
            this.timeToConflict = timeToConflict;
            this.severity = severity;
        }
        
        public boolean isConflict() { return conflict; }
        public double getDistance() { return distance; }
        public double getAltitudeDiff() { return altitudeDiff; }
        public double getClosingSpeed() { return closingSpeed; }
        public double getTimeToConflict() { return timeToConflict; }
        public ConflictSeverity getSeverity() { return severity; }
    }
    
    /**
     * Classe pour représenter une alerte de conflit
     */
    public static class ConflictAlert {
        private final Aircraft aircraft1;
        private final Aircraft aircraft2;
        private final ConflictInfo conflictInfo;
        private final LocalDateTime timestamp;
        
        public ConflictAlert(Aircraft aircraft1, Aircraft aircraft2, ConflictInfo conflictInfo) {
            this.aircraft1 = aircraft1;
            this.aircraft2 = aircraft2;
            this.conflictInfo = conflictInfo;
            this.timestamp = LocalDateTime.now();
        }
        
        public Aircraft getAircraft1() { return aircraft1; }
        public Aircraft getAircraft2() { return aircraft2; }
        public ConflictInfo getConflictInfo() { return conflictInfo; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    /**
     * Enum pour la sévérité des conflits
     */
    public enum ConflictSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}

