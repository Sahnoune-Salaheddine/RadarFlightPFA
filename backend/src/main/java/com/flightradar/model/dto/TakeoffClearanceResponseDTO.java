package com.flightradar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour la réponse d'autorisation de décollage
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TakeoffClearanceResponseDTO {
    private ClearanceStatus status; // APPROVED, DENIED, PENDING
    private String message;
    private String detailedExplanation;
    private LocalDateTime timestamp;
    private List<ClearanceCheckDTO> checks;
    
    public enum ClearanceStatus {
        APPROVED,   // Autorisation accordée
        DENIED,     // Autorisation refusée
        PENDING     // En attente
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClearanceCheckDTO {
        private String checkName; // "Traffic", "Weather", "Runway", "Distance"
        private Boolean passed;
        private String message;
    }
}

