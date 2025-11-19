package com.flightradar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la demande d'autorisation de décollage
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TakeoffClearanceRequestDTO {
    private Long aircraftId;
    private Long pilotId;
    private String pilotUsername;
    private Long departureAirportId;
    private Long destinationAirportId;
    private String flightNumber;
}

