package com.flightradar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 20)
    private String flightNumber; // Ex: "AT1001", "TEST20251120153820"
    
    @Column(length = 100)
    private String airline; // Compagnie aérienne (ex: "Royal Air Maroc", "Air France")
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id", nullable = false)
    @JsonIgnore
    private Aircraft aircraft;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departure_airport_id", nullable = false)
    private Airport departureAirport;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "arrival_airport_id", nullable = false)
    private Airport arrivalAirport;
    
    @Column(name = "flight_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private com.flightradar.model.FlightStatus flightStatus;
    
    @Column(name = "scheduled_departure")
    private LocalDateTime scheduledDeparture;
    
    @Column(name = "scheduled_arrival")
    private LocalDateTime scheduledArrival;
    
    @Column(name = "actual_departure")
    private LocalDateTime actualDeparture;
    
    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;
    
    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival; // ETA calculé au décollage
    
    @Column(name = "cruise_altitude")
    private Integer cruiseAltitude; // Altitude de croisière en pieds
    
    @Column(name = "cruise_speed")
    private Integer cruiseSpeed; // Vitesse de croisière en nœuds
    
    @Column(name = "flight_type", length = 20)
    @Enumerated(EnumType.STRING)
    private FlightType flightType; // Type de vol
    
    @Column(name = "alternate_airport_id")
    private Long alternateAirportId; // Aéroport alternatif (optionnel)
    
    @Column(name = "estimated_time_enroute")
    private Integer estimatedTimeEnroute; // ETE en minutes
    
    @Column(name = "pilot_id")
    private Long pilotId; // Pilote assigné directement au vol
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * Type de vol
     */
    public enum FlightType {
        COMMERCIAL,
        CARGO,
        PRIVATE,
        MILITARY,
        TRAINING
    }
}

