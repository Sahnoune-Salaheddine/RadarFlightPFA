package com.flightradar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "runways")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Runway {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 10)
    private String name; // Ex: "09/27"
    
    @Column(nullable = false, columnDefinition = "DECIMAL(5,2)")
    private Double orientation; // Orientation en degrés (0-360)
    
    @Column(nullable = false)
    private Integer lengthMeters; // Longueur de la piste en mètres
    
    @Column(nullable = false)
    private Integer widthMeters; // Largeur de la piste en mètres
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airport_id", nullable = false)
    @JsonIgnore
    private Airport airport;
    
    // Méthode utilitaire pour obtenir l'orientation opposée
    public Double getOppositeOrientation() {
        return (orientation + 180) % 360;
    }
}

