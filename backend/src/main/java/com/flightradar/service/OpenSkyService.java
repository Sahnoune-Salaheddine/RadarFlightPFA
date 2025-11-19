package com.flightradar.service;

import com.flightradar.model.dto.LiveAircraft;
import com.flightradar.model.dto.OpenSkyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service pour récupérer et gérer les données en temps réel depuis OpenSky Network
 * 
 * API OpenSky : https://opensky-network.org/api/states/all
 * Documentation : https://openskynetwork.github.io/opensky-api/rest.html#all-state-vectors
 */
@Service
public class OpenSkyService {
    
    private static final String OPENSKY_API_URL = "https://opensky-network.org/api/states/all";
    
    @Autowired
    private OpenSkyMapper openSkyMapper;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Cache en mémoire des avions en temps réel
     * Utilise CopyOnWriteArrayList pour thread-safety lors des mises à jour périodiques
     */
    private final List<LiveAircraft> liveAircraftCache = new CopyOnWriteArrayList<>();
    
    /**
     * Récupère les données live depuis l'API OpenSky Network
     * 
     * @return Liste des avions en temps réel normalisés
     */
    public List<LiveAircraft> fetchLiveData() {
        List<LiveAircraft> aircraftList = new ArrayList<>();
        
        try {
            // Appel à l'API OpenSky
            ResponseEntity<OpenSkyResponse> response = restTemplate.getForEntity(
                OPENSKY_API_URL,
                OpenSkyResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                OpenSkyResponse openSkyResponse = response.getBody();
                
                if (openSkyResponse != null && openSkyResponse.getStates() != null) {
                    // Mapper chaque état vers un LiveAircraft
                    for (List<Object> state : openSkyResponse.getStates()) {
                        LiveAircraft aircraft = openSkyMapper.mapStateToLiveAircraft(state);
                        if (aircraft != null && aircraft.getLatitude() != null && aircraft.getLongitude() != null) {
                            // Filtrer les avions sans position valide
                            aircraftList.add(aircraft);
                        }
                    }
                }
            }
            
        } catch (RestClientException e) {
            System.err.println("Erreur lors de l'appel à l'API OpenSky: " + e.getMessage());
            // En cas d'erreur, retourner le cache existant si disponible
            return new ArrayList<>(liveAircraftCache);
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de la récupération des données OpenSky: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(liveAircraftCache);
        }
        
        return aircraftList;
    }
    
    /**
     * Met à jour le cache des avions en temps réel
     * Exécuté automatiquement toutes les 5 secondes
     */
    @Scheduled(fixedRate = 5000)
    public void updateLiveAircraftCache() {
        try {
            List<LiveAircraft> newData = fetchLiveData();
            liveAircraftCache.clear();
            liveAircraftCache.addAll(newData);
            System.out.println("Cache OpenSky mis à jour: " + newData.size() + " avions");
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du cache OpenSky: " + e.getMessage());
        }
    }
    
    /**
     * Récupère la liste des avions en temps réel depuis le cache
     * 
     * @return Liste des avions live normalisés
     */
    public List<LiveAircraft> getLiveAircraft() {
        // Si le cache est vide, essayer de récupérer les données immédiatement
        if (liveAircraftCache.isEmpty()) {
            List<LiveAircraft> freshData = fetchLiveData();
            liveAircraftCache.addAll(freshData);
            return freshData;
        }
        
        return new ArrayList<>(liveAircraftCache);
    }
    
    /**
     * Récupère un avion spécifique par son ICAO24
     * 
     * @param icao24 Identifiant ICAO24 de l'avion
     * @return LiveAircraft correspondant ou null
     */
    public LiveAircraft getLiveAircraftByIcao24(String icao24) {
        return liveAircraftCache.stream()
            .filter(aircraft -> icao24 != null && icao24.equalsIgnoreCase(aircraft.getIcao24()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Filtre les avions par pays d'origine
     * 
     * @param countryCode Code pays (ex: "Morocco", "France")
     * @return Liste filtrée
     */
    public List<LiveAircraft> getLiveAircraftByCountry(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return getLiveAircraft();
        }
        
        return liveAircraftCache.stream()
            .filter(aircraft -> countryCode.equalsIgnoreCase(aircraft.getOriginCountry()))
            .toList();
    }
    
    /**
     * Filtre les avions par statut radar
     * 
     * @param radarStatus Statut recherché (ok, warning, danger)
     * @return Liste filtrée
     */
    public List<LiveAircraft> getLiveAircraftByRadarStatus(String radarStatus) {
        if (radarStatus == null || radarStatus.isEmpty()) {
            return getLiveAircraft();
        }
        
        return liveAircraftCache.stream()
            .filter(aircraft -> radarStatus.equalsIgnoreCase(aircraft.getRadarStatus()))
            .toList();
    }
}

