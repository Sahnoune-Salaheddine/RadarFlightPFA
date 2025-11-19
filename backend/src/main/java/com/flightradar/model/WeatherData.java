package com.flightradar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant les données météorologiques
 * Correspond à la table "weather_data" dans la base de données
 */
@Entity
@Table(name = "weather_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airport_id", nullable = false)
    private Airport airport;
    
    @Column(name = "wind_speed", nullable = false, columnDefinition = "DECIMAL(6,2)")
    private Double windSpeed; // en km/h
    
    @Column(name = "wind_direction", nullable = false, columnDefinition = "DECIMAL(5,2)")
    private Double windDirection; // en degrés (0-360)
    
    @Column(nullable = false, columnDefinition = "DECIMAL(6,2)")
    private Double visibility; // en km
    
    @Column(nullable = false, columnDefinition = "DECIMAL(5,2)")
    private Double temperature; // en Celsius
    
    @Column(nullable = false)
    private Integer humidity; // en pourcentage (0-100)
    
    @Column(nullable = false, columnDefinition = "DECIMAL(7,2)")
    private Double pressure; // en hPa
    
    @Column(nullable = false, length = 50)
    private String conditions; // Ex: Clear, Rain, Fog, etc.
    
    @Column(columnDefinition = "DECIMAL(6,2)")
    private Double crosswind = 0.0; // Vent de travers calculé
    
    @Column(nullable = false)
    private Boolean alert = false; // Alerte météo active
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

