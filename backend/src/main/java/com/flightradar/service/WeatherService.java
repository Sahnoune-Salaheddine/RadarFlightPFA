package com.flightradar.service;

import com.flightradar.model.Airport;
import com.flightradar.model.Runway;
import com.flightradar.model.WeatherData;
import com.flightradar.repository.AirportRepository;
import com.flightradar.repository.RunwayRepository;
import com.flightradar.repository.WeatherDataRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service pour gérer les données météorologiques
 * - Récupération depuis Open-Meteo API (gratuite, remplace OpenWeather)
 * - Calcul du vent de travers
 * - Détection d'alertes météo
 */
@Service
public class WeatherService {
    
    private static final String OPEN_METEO_API_URL = "https://api.open-meteo.com/v1/forecast";
    
    private final WebClient webClient;
    private final WeatherDataRepository weatherDataRepository;
    private final AirportRepository airportRepository;
    private final RunwayRepository runwayRepository;
    
    public WeatherService(WeatherDataRepository weatherDataRepository,
                         AirportRepository airportRepository,
                         RunwayRepository runwayRepository) {
        this.weatherDataRepository = weatherDataRepository;
        this.airportRepository = airportRepository;
        this.runwayRepository = runwayRepository;
        this.webClient = WebClient.builder().build();
    }
    
    /**
     * Récupère les données météo depuis l'API Open-Meteo
     */
    public WeatherData fetchWeatherFromAPI(Airport airport) {
        try {
            // Construire l'URL Open-Meteo
            String url = String.format("%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,wind_speed_10m,wind_direction_10m,visibility",
                OPEN_METEO_API_URL, airport.getLatitude(), airport.getLongitude());
            
            Map<String, Object> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            if (response != null) {
                return parseOpenMeteoResponse(response, airport);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des données météo Open-Meteo: " + e.getMessage());
        }
        
        return createDefaultWeatherData(airport);
    }
    
    /**
     * Parse la réponse de l'API Open-Meteo
     * Structure de réponse Open-Meteo :
     * {
     *   "current": {
     *     "temperature_2m": 20.5,
     *     "wind_speed_10m": 15.2,
     *     "wind_direction_10m": 180.0,
     *     "visibility": 10.0
     *   }
     * }
     */
    @SuppressWarnings("unchecked")
    private WeatherData parseOpenMeteoResponse(Map<String, Object> response, Airport airport) {
        Map<String, Object> current = (Map<String, Object>) response.get("current");
        
        if (current == null) {
            return createDefaultWeatherData(airport);
        }
        
        // Extraire les données de current
        Double temperature = getDoubleValue(current.get("temperature_2m"));
        Double windSpeed = getDoubleValue(current.get("wind_speed_10m")); // Déjà en km/h
        Double windDirection = getDoubleValue(current.get("wind_direction_10m")); // En degrés
        Double visibility = getDoubleValue(current.get("visibility")); // En km
        
        // Open-Meteo ne fournit pas directement humidity et pressure dans current
        // On utilise des valeurs par défaut ou on peut les récupérer via d'autres paramètres
        // Pour l'instant, on met null pour humidity et une valeur par défaut pour pressure
        Integer humidity = null; // Open-Meteo ne fournit pas humidity dans current
        Double pressure = 1013.25; // Valeur par défaut (pression standard au niveau de la mer)
        
        // Déterminer les conditions météo basées sur la visibilité et la température
        String conditions = determineConditions(visibility, temperature, windSpeed);
        
        WeatherData weatherData = new WeatherData();
        weatherData.setAirport(airport);
        weatherData.setTemperature(temperature != null ? temperature : 20.0);
        weatherData.setPressure(pressure);
        weatherData.setHumidity(humidity); // Peut être null
        weatherData.setWindSpeed(windSpeed != null ? windSpeed : 0.0);
        weatherData.setWindDirection(windDirection != null ? windDirection : 0.0);
        weatherData.setVisibility(visibility != null ? visibility : 10.0);
        weatherData.setConditions(conditions);
        weatherData.setTimestamp(LocalDateTime.now());
        
        // Calculer le vent de travers pour chaque piste
        calculateCrosswindForRunways(weatherData, airport);
        
        // Détecter les alertes
        detectWeatherAlerts(weatherData);
        
        return weatherData;
    }
    
    /**
     * Détermine les conditions météo basées sur les données disponibles
     */
    private String determineConditions(Double visibility, Double temperature, Double windSpeed) {
        if (visibility == null) {
            return "Clear";
        }
        
        // Déterminer les conditions selon la visibilité
        if (visibility < 1.0) {
            return "Fog";
        } else if (visibility < 3.0) {
            return "Mist";
        } else if (windSpeed != null && windSpeed > 50.0) {
            return "Strong Wind";
        } else if (temperature != null && temperature < 0.0) {
            return "Freezing";
        } else {
            return "Clear";
        }
    }
    
    /**
     * Helper pour extraire une valeur Double de manière sécurisée
     */
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
    
    /**
     * Calcule le vent de travers pour toutes les pistes de l'aéroport
     * et stocke le maximum
     */
    private void calculateCrosswindForRunways(WeatherData weatherData, Airport airport) {
        List<Runway> runways = runwayRepository.findByAirportId(airport.getId());
        
        double maxCrosswind = 0.0;
        for (Runway runway : runways) {
            double crosswind = calculateCrosswind(
                weatherData.getWindSpeed(),
                weatherData.getWindDirection(),
                runway.getOrientation()
            );
            maxCrosswind = Math.max(maxCrosswind, crosswind);
        }
        
        weatherData.setCrosswind(maxCrosswind);
    }
    
    /**
     * Calcule le vent de travers pour une piste donnée
     * @param windSpeed Vitesse du vent en km/h
     * @param windDirection Direction du vent en degrés (0-360)
     * @param runwayOrientation Orientation de la piste en degrés (0-360)
     * @return Vent de travers en km/h
     */
    public double calculateCrosswind(double windSpeed, double windDirection, double runwayOrientation) {
        // Calculer la différence d'angle
        double angleDiff = Math.abs(windDirection - runwayOrientation);
        
        // Normaliser entre 0 et 180
        if (angleDiff > 180) {
            angleDiff = 360 - angleDiff;
        }
        
        // Calculer le vent de travers (composante perpendiculaire)
        double crosswind = windSpeed * Math.sin(Math.toRadians(angleDiff));
        
        return Math.abs(crosswind);
    }
    
    /**
     * Détecte si les conditions météo nécessitent une alerte
     */
    private void detectWeatherAlerts(WeatherData weatherData) {
        boolean alert = false;
        
        // Visibilité trop faible
        if (weatherData.getVisibility() < 1.0) {
            alert = true;
        }
        
        // Vent trop fort
        if (weatherData.getWindSpeed() > 50.0) {
            alert = true;
        }
        
        // Vent de travers trop fort
        if (weatherData.getCrosswind() > 15.0) {
            alert = true;
        }
        
        // Conditions météo dangereuses
        String conditions = weatherData.getConditions().toUpperCase();
        if (conditions.contains("THUNDERSTORM") || 
            conditions.contains("HEAVY RAIN") || 
            conditions.contains("FOG") ||
            conditions.contains("BLIZZARD") ||
            conditions.contains("STRONG WIND") ||
            conditions.contains("FREEZING")) {
            alert = true;
        }
        
        weatherData.setAlert(alert);
    }
    
    /**
     * Détermine si les conditions sont sûres pour atterrir
     */
    public boolean isSafeToLand(WeatherData weatherData, Runway runway) {
        if (weatherData.getVisibility() < 1.0) {
            return false;
        }
        
        if (weatherData.getWindSpeed() > 50.0) {
            return false;
        }
        
        double crosswind = calculateCrosswind(
            weatherData.getWindSpeed(),
            weatherData.getWindDirection(),
            runway.getOrientation()
        );
        
        if (crosswind > 15.0) {
            return false;
        }
        
        String conditions = weatherData.getConditions().toUpperCase();
        if (conditions.contains("THUNDERSTORM") || 
            conditions.contains("HEAVY RAIN") || 
            conditions.contains("FOG") ||
            conditions.contains("STRONG WIND") ||
            conditions.contains("FREEZING")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Crée des données météo par défaut si l'API n'est pas disponible
     */
    private WeatherData createDefaultWeatherData(Airport airport) {
        WeatherData weatherData = new WeatherData();
        weatherData.setAirport(airport);
        weatherData.setTemperature(20.0);
        weatherData.setPressure(1013.25);
        weatherData.setHumidity(60);
        weatherData.setWindSpeed(10.0);
        weatherData.setWindDirection(180.0);
        weatherData.setVisibility(10.0);
        weatherData.setConditions("Clear");
        weatherData.setCrosswind(5.0);
        weatherData.setAlert(false);
        weatherData.setTimestamp(LocalDateTime.now());
        return weatherData;
    }
    
    /**
     * Met à jour les données météo pour tous les aéroports
     * Exécuté toutes les 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void updateAllWeatherData() {
        List<Airport> airports = airportRepository.findAll();
        for (Airport airport : airports) {
            WeatherData weatherData = fetchWeatherFromAPI(airport);
            weatherDataRepository.save(weatherData);
        }
    }
    
    /**
     * Récupère les données météo actuelles d'un aéroport
     */
    public Optional<WeatherData> getCurrentWeather(Long airportId) {
        return weatherDataRepository.findFirstByAirportIdOrderByTimestampDesc(airportId);
    }
    
    /**
     * Récupère toutes les alertes météo actives
     */
    public List<WeatherData> getWeatherAlerts() {
        return weatherDataRepository.findByAlertTrue();
    }
}
