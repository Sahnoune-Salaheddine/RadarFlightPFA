package com.flightradar.service;

import com.flightradar.model.dto.LiveAircraft;
import com.flightradar.model.dto.OpenSkyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
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
@Slf4j
public class OpenSkyService {
    
    private static final String OPENSKY_API_URL = "https://opensky-network.org/api/states/all";
    
    // Limite de l'API OpenSky : ~10 requêtes/minute pour les utilisateurs anonymes
    // On utilise 60 secondes (1 minute) pour être sûr de ne pas dépasser la limite
    private static final long UPDATE_INTERVAL_MS = 60000; // 60 secondes au lieu de 5
    
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
     * Dernière mise à jour réussie
     */
    private LocalDateTime lastSuccessfulUpdate;
    
    /**
     * Nombre d'erreurs consécutives
     */
    private int consecutiveErrors = 0;
    
    /**
     * Désactivé temporairement si trop d'erreurs
     */
    private boolean temporarilyDisabled = false;
    
    /**
     * Récupère les données live depuis l'API OpenSky Network
     * 
     * @return Liste des avions en temps réel normalisés
     */
    public List<LiveAircraft> fetchLiveData() {
        // Si temporairement désactivé, retourner le cache
        if (temporarilyDisabled) {
            log.debug("OpenSky temporairement désactivé, utilisation du cache");
            return new ArrayList<>(liveAircraftCache);
        }
        
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
                
                // Succès : réinitialiser le compteur d'erreurs
                consecutiveErrors = 0;
                lastSuccessfulUpdate = LocalDateTime.now();
                temporarilyDisabled = false;
                
            }
            
        } catch (HttpClientErrorException e) {
            // Gestion spécifique des erreurs HTTP
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("⚠️  OpenSky API : Trop de requêtes (429). Augmentation de l'intervalle de mise à jour.");
                consecutiveErrors++;
                
                // Désactiver temporairement après 3 erreurs consécutives
                if (consecutiveErrors >= 3) {
                    temporarilyDisabled = true;
                    log.warn("🚫 OpenSky désactivé temporairement après {} erreurs consécutives", consecutiveErrors);
                }
            } else {
                log.error("Erreur HTTP lors de l'appel à l'API OpenSky: {} - {}", e.getStatusCode(), e.getMessage());
                consecutiveErrors++;
            }
            
            // En cas d'erreur, retourner le cache existant si disponible
            return new ArrayList<>(liveAircraftCache);
            
        } catch (RestClientException e) {
            log.error("Erreur lors de l'appel à l'API OpenSky: {}", e.getMessage());
            consecutiveErrors++;
            // En cas d'erreur, retourner le cache existant si disponible
            return new ArrayList<>(liveAircraftCache);
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la récupération des données OpenSky: {}", e.getMessage(), e);
            consecutiveErrors++;
            return new ArrayList<>(liveAircraftCache);
        }
        
        return aircraftList;
    }
    
    /**
     * Met à jour le cache des avions en temps réel
     * Exécuté automatiquement toutes les 60 secondes (1 minute)
     * Limite OpenSky : ~10 requêtes/minute pour utilisateurs anonymes
     */
    @Scheduled(fixedRate = UPDATE_INTERVAL_MS)
    public void updateLiveAircraftCache() {
        try {
            List<LiveAircraft> newData = fetchLiveData();
            
            // Ne mettre à jour que si on a de nouvelles données
            if (!newData.isEmpty() || liveAircraftCache.isEmpty()) {
                liveAircraftCache.clear();
                liveAircraftCache.addAll(newData);
                log.info("✅ Cache OpenSky mis à jour: {} avions", newData.size());
            } else {
                log.debug("Cache OpenSky conservé: {} avions (pas de nouvelles données)", liveAircraftCache.size());
            }
            
            // Réactiver après 10 minutes si désactivé
            if (temporarilyDisabled && lastSuccessfulUpdate != null) {
                if (lastSuccessfulUpdate.isBefore(LocalDateTime.now().minusMinutes(10))) {
                    temporarilyDisabled = false;
                    consecutiveErrors = 0;
                    log.info("🔄 Réactivation d'OpenSky après période de pause");
                }
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du cache OpenSky: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Récupère la liste des avions en temps réel depuis le cache
     * 
     * @return Liste des avions live normalisés
     */
    public List<LiveAircraft> getLiveAircraft() {
        // Si le cache est vide et qu'on n'est pas désactivé, essayer une fois
        if (liveAircraftCache.isEmpty() && !temporarilyDisabled) {
            log.debug("Cache vide, tentative de récupération immédiate");
            List<LiveAircraft> freshData = fetchLiveData();
            if (!freshData.isEmpty()) {
                liveAircraftCache.addAll(freshData);
                return freshData;
            }
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

