package com.flightradar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité représentant un avion
 * Correspond à la table "aircraft" dans la base de données
 */
@Entity
@Table(name = "aircraft")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aircraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String model; // Ex: A320, A330
    
    @Column(nullable = false, unique = true, length = 20)
    private String registration; // Ex: CN-ABC
    
    @Column(name = "numero_vol", length = 10)
    private String numeroVol; // Numéro de vol (ex: AT1001)
    
    @Column(name = "type_avion", length = 50)
    private String typeAvion; // Type d'avion (ex: A320, A330)
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AircraftStatus status;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airport_id")
    private Airport airport;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pilot_id")
    private Pilot pilot;
    
    @Column(name = "position_lat", columnDefinition = "DECIMAL(10,8)")
    private Double positionLat;
    
    @Column(name = "position_lon", columnDefinition = "DECIMAL(11,8)")
    private Double positionLon;
    
    @Column(columnDefinition = "DECIMAL(10,2)")
    private Double altitude = 0.0; // en mètres
    
    @Column(columnDefinition = "DECIMAL(8,2)")
    private Double speed = 0.0; // en km/h
    
    @Column(columnDefinition = "DECIMAL(5,2)")
    private Double heading = 0.0; // en degrés (0-360)
    
    // Champs ADS-B supplémentaires
    @Column(name = "air_speed", columnDefinition = "DECIMAL(8,2)")
    private Double airSpeed = 0.0; // vitesse air en km/h
    
    @Column(name = "vertical_speed", columnDefinition = "DECIMAL(8,2)")
    private Double verticalSpeed = 0.0; // taux montée/descente en m/s
    
    @Column(name = "transponder_code", length = 4)
    private String transponderCode; // Code transpondeur (ex: 1200, 7500, 7600, 7700)
    
    @Column(name = "username_pilote", length = 50)
    private String usernamePilote; // Username du pilote (alternative à pilot_id)
    
    // Trajectoires (JSON)
    @Column(name = "trajectoire_prevue", columnDefinition = "TEXT")
    private String trajectoirePrévue; // JSON array de positions
    
    @Column(name = "trajectoire_reelle", columnDefinition = "TEXT")
    private String trajectoireRéelle; // JSON array de positions
    
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
    
    // Relations
    @OneToMany(mappedBy = "aircraft", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Flight> flights;
    
    @PreUpdate
    @PrePersist
    protected void updateTimestamp() {
        lastUpdate = LocalDateTime.now();
    }
}

