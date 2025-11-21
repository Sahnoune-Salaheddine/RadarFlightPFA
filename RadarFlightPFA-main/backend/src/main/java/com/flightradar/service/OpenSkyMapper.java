package com.flightradar.service;

import com.flightradar.model.dto.LiveAircraft;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper pour transformer les données brutes d'OpenSky Network en objets LiveAircraft normalisés
 * 
 * Documentation OpenSky states[] :
 * 0: icao24 (String)
 * 1: callsign (String ou null)
 * 2: originCountry (String)
 * 3: timePosition (Long ou null)
 * 4: lastContact (Long)
 * 5: longitude (Double ou null)
 * 6: latitude (Double ou null)
 * 7: baroAltitude (Double ou null) - altitude barométrique en mètres
 * 8: onGround (Boolean)
 * 9: velocity (Double ou null) - vitesse horizontale en m/s
 * 10: trueTrack (Double ou null) - cap en degrés
 * 11: verticalRate (Double ou null) - taux vertical en m/s
 * 12: sensors (List<Integer> ou null)
 * 13: geoAltitude (Double ou null) - altitude géométrique en mètres
 * 14: squawk (String ou null)
 * 15: spi (Boolean)
 * 16: positionSource (Integer)
 */
@Component
public class OpenSkyMapper {
    
    /**
     * Map statique pour enrichir les données avec le modèle d'avion
     * Clé : ICAO24 (hexadécimal), Valeur : Modèle d'avion
     * 
     * Note : En production, cette map pourrait être chargée depuis une base de données
     * ou une API externe (ex: Aviation Edge, Aircraft Database)
     */
    private static final Map<String, String> AIRCRAFT_MODEL_MAP = new HashMap<>();
    
    static {
        // Exemples de mapping ICAO24 -> Modèle
        // En production, charger depuis une source externe
        AIRCRAFT_MODEL_MAP.put("abc123", "A320");
        AIRCRAFT_MODEL_MAP.put("def456", "B737");
        AIRCRAFT_MODEL_MAP.put("ghi789", "A330");
        // Ajouter plus de mappings selon les besoins
    }
    
    /**
     * Transforme un tableau d'état OpenSky en objet LiveAircraft normalisé
     * 
     * @param stateArray Tableau d'objets représentant un état d'avion depuis OpenSky
     * @return LiveAircraft normalisé avec statut et radarStatus calculés
     */
    public LiveAircraft mapStateToLiveAircraft(List<Object> stateArray) {
        if (stateArray == null || stateArray.size() < 17) {
            return null;
        }
        
        LiveAircraft aircraft = new LiveAircraft();
        
        try {
            // Extraction des champs de base
            aircraft.setIcao24(getStringValue(stateArray.get(0)));
            aircraft.setCallsign(getStringValue(stateArray.get(1)));
            aircraft.setOriginCountry(getStringValue(stateArray.get(2)));
            aircraft.setLastContact(getLongValue(stateArray.get(4)));
            
            // Position
            aircraft.setLongitude(getDoubleValue(stateArray.get(5)));
            aircraft.setLatitude(getDoubleValue(stateArray.get(6)));
            
            // Altitude : priorité à baroAltitude, sinon geoAltitude
            Double baroAltitude = getDoubleValue(stateArray.get(7));
            Double geoAltitude = getDoubleValue(stateArray.get(13));
            aircraft.setAltitude(baroAltitude != null ? baroAltitude : geoAltitude);
            
            // Vitesse : conversion de m/s vers km/h
            Double velocityMs = getDoubleValue(stateArray.get(9));
            aircraft.setVelocity(velocityMs != null ? velocityMs * 3.6 : 0.0);
            
            // Taux vertical (m/s)
            aircraft.setVerticalRate(getDoubleValue(stateArray.get(11)));
            
            // Enrichissement avec le modèle d'avion
            String model = AIRCRAFT_MODEL_MAP.getOrDefault(
                aircraft.getIcao24() != null ? aircraft.getIcao24().toLowerCase() : "",
                "Unknown"
            );
            aircraft.setModel(model);
            
            // Calcul automatique du statut
            aircraft.setStatus(calculateStatus(aircraft));
            
            // Calcul du statut radar
            aircraft.setRadarStatus(calculateRadarStatus(aircraft));
            
        } catch (Exception e) {
            // Log l'erreur et retourne null si le mapping échoue
            System.err.println("Erreur lors du mapping OpenSky: " + e.getMessage());
            return null;
        }
        
        return aircraft;
    }
    
    /**
     * Calcule le statut de vol automatiquement selon les règles métier
     * 
     * @param aircraft Avion avec données de base
     * @return Statut calculé : on-ground, climbing, descending, cruising, landing, takeoff
     */
    private String calculateStatus(LiveAircraft aircraft) {
        if (aircraft.getVelocity() == null || aircraft.getVelocity() < 10) {
            return "on-ground";
        }
        
        Double verticalRate = aircraft.getVerticalRate();
        Double altitude = aircraft.getAltitude();
        
        if (verticalRate == null) {
            verticalRate = 0.0;
        }
        if (altitude == null) {
            altitude = 0.0;
        }
        
        // Règles de calcul du statut
        if (verticalRate > 2) {
            return "climbing";
        }
        
        if (verticalRate < -2) {
            // Si en descente et basse altitude → landing
            if (altitude < 2000) {
                return "landing";
            }
            return "descending";
        }
        
        // Si haute altitude → cruising
        if (altitude > 8000) {
            return "cruising";
        }
        
        // Si basse altitude et montée → takeoff
        if (altitude < 2000 && verticalRate > 0) {
            return "takeoff";
        }
        
        // Par défaut : en vol
        return "cruising";
    }
    
    /**
     * Calcule le statut radar personnalisé
     * 
     * @param aircraft Avion avec données de base
     * @return Statut radar : ok, warning, danger
     */
    private String calculateRadarStatus(LiveAircraft aircraft) {
        Double altitude = aircraft.getAltitude();
        Double verticalRate = aircraft.getVerticalRate();
        
        if (altitude == null) {
            altitude = 0.0;
        }
        if (verticalRate == null) {
            verticalRate = 0.0;
        }
        
        // Danger : altitude très basse
        if (altitude < 100 && altitude > 0) {
            return "danger";
        }
        
        // Warning : taux vertical élevé (manœuvre brusque)
        if (Math.abs(verticalRate) > 20) {
            return "warning";
        }
        
        // OK : conditions normales
        return "ok";
    }
    
    /**
     * Helpers pour extraction sécurisée des valeurs
     */
    private String getStringValue(Object obj) {
        if (obj == null) {
            return null;
        }
        String str = obj.toString().trim();
        return str.isEmpty() ? null : str;
    }
    
    private Double getDoubleValue(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            if (obj instanceof Number) {
                return ((Number) obj).doubleValue();
            }
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Long getLongValue(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            if (obj instanceof Number) {
                return ((Number) obj).longValue();
            }
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

