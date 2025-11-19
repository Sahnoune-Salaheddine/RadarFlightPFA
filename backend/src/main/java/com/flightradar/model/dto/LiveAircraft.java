package com.flightradar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les données d'avion en temps réel provenant d'OpenSky Network
 * Ne correspond pas à une entité JPA mais à une représentation des données live
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveAircraft {
    
    /**
     * Identifiant unique ICAO 24-bit (hexadécimal)
     */
    @JsonProperty("icao24")
    private String icao24;
    
    /**
     * Indicatif d'appel (callsign)
     */
    @JsonProperty("callsign")
    private String callsign;
    
    /**
     * Pays d'origine
     */
    @JsonProperty("originCountry")
    private String originCountry;
    
    /**
     * Longitude en degrés décimaux
     */
    @JsonProperty("longitude")
    private Double longitude;
    
    /**
     * Latitude en degrés décimaux
     */
    @JsonProperty("latitude")
    private Double latitude;
    
    /**
     * Altitude en mètres
     */
    @JsonProperty("altitude")
    private Double altitude;
    
    /**
     * Vitesse en km/h
     */
    @JsonProperty("velocity")
    private Double velocity;
    
    /**
     * Taux de montée/descente en m/s (positif = montée, négatif = descente)
     */
    @JsonProperty("verticalRate")
    private Double verticalRate;
    
    /**
     * Modèle d'avion (enrichi via mapping)
     */
    @JsonProperty("model")
    private String model;
    
    /**
     * Statut de vol calculé automatiquement
     * Valeurs possibles : on-ground, climbing, descending, cruising, landing, takeoff
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * Statut radar personnalisé
     * Valeurs possibles : ok, warning, danger
     */
    @JsonProperty("radarStatus")
    private String radarStatus;
    
    /**
     * Timestamp Unix du dernier contact (secondes)
     */
    @JsonProperty("lastContact")
    private Long lastContact;
}

