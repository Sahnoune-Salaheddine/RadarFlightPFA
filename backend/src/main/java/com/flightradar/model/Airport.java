package com.flightradar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entité représentant un aéroport
 * Correspond à la table "airports" dans la base de données
 */
@Entity
@Table(name = "airports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 100)
    private String city;
    
    @Column(name = "code_iata", nullable = false, unique = true, length = 3)
    private String codeIATA; // Ex: CMN, RBA, RAK, TNG
    
    @Column(nullable = false, columnDefinition = "DECIMAL(10,8)")
    private Double latitude;
    
    @Column(nullable = false, columnDefinition = "DECIMAL(11,8)")
    private Double longitude;
    
    // Relations
    @OneToMany(mappedBy = "airport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Runway> runways;
    
    @OneToMany(mappedBy = "airport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Aircraft> aircraft;
    
    @OneToOne(mappedBy = "airport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private RadarCenter radarCenter;
    
    @OneToMany(mappedBy = "airport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<WeatherData> weatherData;
    
    @OneToMany(mappedBy = "departureAirport", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Flight> departureFlights;
    
    @OneToMany(mappedBy = "arrivalAirport", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Flight> arrivalFlights;
}

