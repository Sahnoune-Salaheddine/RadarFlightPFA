package com.flightradar.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO pour la réponse de l'API OpenSky Network
 * Structure : {"time": 1234567890, "states": [[state1], [state2], ...]}
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenSkyResponse {
    
    /**
     * Timestamp Unix de la requête
     */
    @JsonProperty("time")
    private Long time;
    
    /**
     * Liste des états d'avions
     * Chaque état est un tableau d'objets selon la documentation OpenSky
     */
    @JsonProperty("states")
    private List<List<Object>> states;
}

