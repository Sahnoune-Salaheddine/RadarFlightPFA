package com.flightradar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité représentant un pilote
 * Correspond à la table "pilots" dans la base de données
 */
@Entity
@Table(name = "pilots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pilot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 50)
    private String license; // Numéro de licence
    
    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears = 0;
    
    @Column(name = "first_name", length = 100)
    private String firstName; // Prénom
    
    @Column(name = "last_name", length = 100)
    private String lastName; // Nom de famille
    
    @Column(name = "assigned_aircraft_id")
    private Long assignedAircraftId; // Avion assigné (déprécié, utiliser la relation JPA)
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnore
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airport_id")
    private Airport airport; // Aéroport d'affectation du pilote
    
    @OneToOne(mappedBy = "pilot", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Aircraft aircraft; // Relation OneToOne : 1 pilote = 1 avion
}

