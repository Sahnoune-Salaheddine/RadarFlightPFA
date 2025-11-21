package com.flightradar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un log d'activité système
 * Correspond à la table "activity_logs" dans la base de données
 */
@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "username")
    private String username;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "entity_type", length = 50)
    private String entityType; // FLIGHT, AIRCRAFT, USER, WEATHER, etc.
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private LogSeverity severity; // INFO, WARNING, ERROR, CRITICAL
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (severity == null) {
            severity = LogSeverity.INFO;
        }
    }
    
    /**
     * Type d'activité loggée
     */
    public enum ActivityType {
        LOGIN,
        LOGOUT,
        FLIGHT_CREATED,
        FLIGHT_UPDATED,
        FLIGHT_CANCELLED,
        AIRCRAFT_UPDATED,
        WEATHER_ALERT,
        RADAR_ALERT,
        USER_CREATED,
        USER_UPDATED,
        USER_DELETED,
        SYSTEM_ERROR,
        DATA_EXPORT,
        REPORT_GENERATED
    }
    
    /**
     * Niveau de sévérité du log
     */
    public enum LogSeverity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }
}

