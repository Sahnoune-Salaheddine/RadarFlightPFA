package com.flightradar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour le Dashboard Pilote complet
 * Contient toutes les informations nécessaires pour afficher le dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PilotDashboardDTO {
    
    // ========== 1. Informations générales du vol ==========
    private String flightNumber;
    private String airline;
    private String aircraftType;
    private String departureAirport;
    private String arrivalAirport;
    private String route; // "CMN → RAK"
    
    // ========== 2. Position & mouvement (ADS-B) ==========
    private Double latitude;
    private Double longitude;
    private Double altitude; // en mètres
    private Double altitudeFeet; // en pieds
    private Double groundSpeed; // vitesse sol en km/h
    private Double airSpeed; // vitesse air en km/h
    private Double heading; // cap en degrés (0-360)
    private Double verticalSpeed; // taux montée/descente en m/s
    
    // ========== 3. Statut du vol ==========
    private String flightStatus; // "Décollé", "En vol", "Atterrissage", "Au sol"
    private LocalDateTime actualDeparture;
    private LocalDateTime actualArrival;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;
    private Long delayMinutes; // retard en minutes
    private String gate; // porte
    private String runway; // piste
    
    // ========== 4. Météo du vol ==========
    private WeatherInfoDTO weather;
    
    // ========== 5. Communications et contrôle aérien (ATC) ==========
    private String lastATCMessage;
    private List<String> currentInstructions;
    private String radarCenterName;
    private List<ATCMessageDTO> atcHistory;
    
    // ========== 6. Sécurité / Suivi ADS-B ==========
    private String transponderCode;
    private List<PositionDTO> trajectory; // trajectoire temps réel
    private List<AlertDTO> alerts;
    private String riskLevel; // "LOW", "MEDIUM", "HIGH"
    
    // ========== 7. KPIs ==========
    private KPIsDTO kpis;
    
    // Classes internes pour les sous-objets
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherInfoDTO {
        private Double windSpeed;
        private Double windDirection;
        private Double visibility;
        private String precipitation;
        private String turbulence;
        private Double temperature;
        private Double pressure;
        private List<String> weatherAlerts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ATCMessageDTO {
        private LocalDateTime timestamp;
        private String message;
        private String sender; // "ATC" ou "PILOT"
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionDTO {
        private Double latitude;
        private Double longitude;
        private Double altitude;
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertDTO {
        private String type; // "TECHNICAL", "WEATHER", "TRAFFIC"
        private String severity; // "LOW", "MEDIUM", "HIGH", "CRITICAL"
        private String message;
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KPIsDTO {
        // KPIs Temps Réel
        private Double remainingDistance; // km
        private LocalDateTime estimatedArrival; // ETA
        private Double estimatedFuelConsumption; // litres
        private Double fuelLevel; // pourcentage
        private Double averageSpeed; // km/h
        private Boolean stableAltitude; // oui/non
        private Boolean turbulenceDetected;
        
        // KPIs Radar / Sécurité
        private Integer weatherSeverity; // 0-100%
        private Integer trajectoryRiskIndex; // 0-100
        private Integer trafficDensity30km; // nombre d'avions dans 30km
        private Integer aircraftHealthScore; // 0-100 (santé capteurs, transpondeur, moteur)
    }
}

