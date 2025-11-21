package com.flightradar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant une communication VHF
 * Correspond à la table "communications" dans la base de données
 * Utilise un pattern polymorphe avec sender_type/receiver_type
 */
@Entity
@Table(name = "communications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Communication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sender_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SenderType senderType; // RADAR, AIRCRAFT, AIRPORT
    
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    
    @Column(name = "receiver_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReceiverType receiverType; // RADAR, AIRCRAFT, AIRPORT
    
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(nullable = false, columnDefinition = "DECIMAL(6,2)")
    private Double frequency = 121.5; // Fréquence VHF en MHz
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    // Relations optionnelles pour faciliter les requêtes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    @JsonIgnore
    private RadarCenter radarCenter; // Si sender_type = RADAR
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
