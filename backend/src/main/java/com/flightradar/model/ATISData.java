package com.flightradar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant les données ATIS (Automatic Terminal Information Service)
 * Correspond à la table "atis_data" dans la base de données
 */
@Entity
@Table(name = "atis_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ATISData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airport_id", nullable = false)
    private Airport airport;
    
    @Column(columnDefinition = "DECIMAL(8,2)")
    private Double vent; // Vitesse du vent en km/h
    
    @Column(columnDefinition = "DECIMAL(8,2)")
    private Double visibilité; // Visibilité en km
    
    @Column(columnDefinition = "DECIMAL(8,2)")
    private Double pression; // Pression en hPa
    
    @Column(length = 100)
    private String turbulence; // Niveau de turbulence
    
    @Column(columnDefinition = "DECIMAL(5,2)")
    private Double temperature; // Température en °C
    
    @Column(length = 200)
    private String conditions; // Conditions météo (ex: "Clear", "Rain", "Fog")
    
    @Column(name = "piste_en_service", length = 20)
    private String pisteEnService; // Piste active (ex: "17/35")
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

