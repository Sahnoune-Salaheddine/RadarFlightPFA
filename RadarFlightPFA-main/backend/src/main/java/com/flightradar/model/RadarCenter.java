package com.flightradar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entité représentant un centre radar
 * Correspond à la table "radar_centers" dans la base de données
 */
@Entity
@Table(name = "radar_centers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RadarCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 20)
    private String code; // Ex: CMN_RADAR
    
    @Column(nullable = false, columnDefinition = "DECIMAL(6,2)")
    private Double frequency; // Fréquence VHF en MHz
    
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private RadarStatus status; // Statut du radar (peut être null si migration incomplète)
    
    @Column(columnDefinition = "DECIMAL(8,2)")
    private Double range; // Portée en kilomètres (peut être null si migration incomplète)
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airport_id", nullable = false, unique = true)
    private Airport airport;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    
    @OneToMany(mappedBy = "radarCenter", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Communication> communications;
}

