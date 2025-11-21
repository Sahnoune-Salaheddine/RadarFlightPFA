package com.flightradar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    
    // Lien vers aéroport si rôle = CENTRE_RADAR
    @Column(name = "airport_id")
    private Long airportId;
    
    // Lien vers pilote si rôle = PILOTE
    @Column(name = "pilot_id")
    private Long pilotId;
}

