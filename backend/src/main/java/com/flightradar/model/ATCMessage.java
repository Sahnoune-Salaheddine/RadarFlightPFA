package com.flightradar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un message ATC (Air Traffic Control)
 * Correspond à la table "atc_messages" dans la base de données
 */
@Entity
@Table(name = "atc_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ATCMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "aircraft_id", nullable = false)
    private Long aircraftId;
    
    @Column(name = "radar_id", nullable = false)
    private Long radarId;
    
    @Column(name = "pilot_id")
    private Long pilotId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ATCMessageType type; // AUTORISATION, INSTRUCTION, ALERTE
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    /**
     * Type de message ATC
     */
    public enum ATCMessageType {
        AUTORISATION_DECOLLAGE,
        AUTORISATION_ATTERRISSAGE,
        INSTRUCTION_ALTITUDE,
        INSTRUCTION_CAP,
        INSTRUCTION_VITESSE,
        ALERTE,
        INFORMATION
    }
}

