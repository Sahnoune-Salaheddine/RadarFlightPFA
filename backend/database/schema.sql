-- =====================================================
-- SCHÉMA DE BASE DE DONNÉES - FLIGHT RADAR
-- Base de données PostgreSQL
-- =====================================================

-- Suppression des tables si elles existent (pour réinitialisation)
DROP TABLE IF EXISTS communications CASCADE;
DROP TABLE IF EXISTS weather_data CASCADE;
DROP TABLE IF EXISTS flights CASCADE;
DROP TABLE IF EXISTS aircraft CASCADE;
DROP TABLE IF EXISTS pilots CASCADE;
DROP TABLE IF EXISTS runways CASCADE;
DROP TABLE IF EXISTS radar_centers CASCADE;
DROP TABLE IF EXISTS airports CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =====================================================
-- TABLE: users
-- =====================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'PILOTE', 'CENTRE_RADAR')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TABLE: airports
-- =====================================================
CREATE TABLE airports (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    code_iata VARCHAR(3) UNIQUE NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TABLE: runways
-- =====================================================
CREATE TABLE runways (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(10) NOT NULL,
    orientation DECIMAL(5, 2) NOT NULL CHECK (orientation >= 0 AND orientation < 360),
    length_meters INTEGER NOT NULL,
    width_meters INTEGER NOT NULL,
    airport_id BIGINT NOT NULL,
    FOREIGN KEY (airport_id) REFERENCES airports(id) ON DELETE CASCADE,
    UNIQUE(airport_id, name)
);

-- =====================================================
-- TABLE: pilots
-- =====================================================
CREATE TABLE pilots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    license VARCHAR(50) UNIQUE NOT NULL,
    experience_years INTEGER NOT NULL DEFAULT 0,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TABLE: radar_centers
-- =====================================================
CREATE TABLE radar_centers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    frequency DECIMAL(6, 2) NOT NULL,
    airport_id BIGINT NOT NULL,
    user_id BIGINT,
    FOREIGN KEY (airport_id) REFERENCES airports(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE(airport_id)
);

-- =====================================================
-- TABLE: aircraft
-- =====================================================
CREATE TABLE aircraft (
    id BIGSERIAL PRIMARY KEY,
    model VARCHAR(50) NOT NULL,
    registration VARCHAR(20) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('AU_SOL', 'DECOLLAGE', 'EN_VOL', 'ATTERRISSAGE', 'EN_ATTENTE')),
    airport_id BIGINT,
    pilot_id BIGINT,
    position_lat DECIMAL(10, 8),
    position_lon DECIMAL(11, 8),
    altitude DECIMAL(10, 2) DEFAULT 0,
    speed DECIMAL(8, 2) DEFAULT 0,
    heading DECIMAL(5, 2) DEFAULT 0 CHECK (heading >= 0 AND heading < 360),
    last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (airport_id) REFERENCES airports(id) ON DELETE SET NULL,
    FOREIGN KEY (pilot_id) REFERENCES pilots(id) ON DELETE SET NULL
);

-- =====================================================
-- TABLE: flights
-- =====================================================
CREATE TABLE flights (
    id BIGSERIAL PRIMARY KEY,
    flight_number VARCHAR(10) UNIQUE NOT NULL,
    aircraft_id BIGINT NOT NULL,
    departure_airport_id BIGINT NOT NULL,
    arrival_airport_id BIGINT NOT NULL,
    flight_status VARCHAR(20) NOT NULL CHECK (flight_status IN ('PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE', 'RETARDE')),
    scheduled_departure TIMESTAMP,
    scheduled_arrival TIMESTAMP,
    actual_departure TIMESTAMP,
    actual_arrival TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (aircraft_id) REFERENCES aircraft(id) ON DELETE CASCADE,
    FOREIGN KEY (departure_airport_id) REFERENCES airports(id) ON DELETE RESTRICT,
    FOREIGN KEY (arrival_airport_id) REFERENCES airports(id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLE: weather_data
-- =====================================================
CREATE TABLE weather_data (
    id BIGSERIAL PRIMARY KEY,
    airport_id BIGINT NOT NULL,
    wind_speed DECIMAL(6, 2) NOT NULL,
    wind_direction DECIMAL(5, 2) NOT NULL CHECK (wind_direction >= 0 AND wind_direction < 360),
    visibility DECIMAL(6, 2) NOT NULL,
    temperature DECIMAL(5, 2) NOT NULL,
    humidity INTEGER NOT NULL CHECK (humidity >= 0 AND humidity <= 100),
    pressure DECIMAL(7, 2) NOT NULL,
    conditions VARCHAR(50) NOT NULL,
    crosswind DECIMAL(6, 2) DEFAULT 0,
    alert BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (airport_id) REFERENCES airports(id) ON DELETE CASCADE
);

-- =====================================================
-- TABLE: communications
-- =====================================================
CREATE TABLE communications (
    id BIGSERIAL PRIMARY KEY,
    sender_type VARCHAR(20) NOT NULL CHECK (sender_type IN ('RADAR', 'AIRCRAFT', 'AIRPORT')),
    sender_id BIGINT NOT NULL,
    receiver_type VARCHAR(20) NOT NULL CHECK (receiver_type IN ('RADAR', 'AIRCRAFT', 'AIRPORT')),
    receiver_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    frequency DECIMAL(6, 2) NOT NULL DEFAULT 121.5,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INDEXES pour améliorer les performances
-- =====================================================
CREATE INDEX idx_aircraft_airport ON aircraft(airport_id);
CREATE INDEX idx_aircraft_pilot ON aircraft(pilot_id);
CREATE INDEX idx_aircraft_status ON aircraft(status);
CREATE INDEX idx_flights_aircraft ON flights(aircraft_id);
CREATE INDEX idx_flights_departure ON flights(departure_airport_id);
CREATE INDEX idx_flights_arrival ON flights(arrival_airport_id);
CREATE INDEX idx_flights_status ON flights(flight_status);
CREATE INDEX idx_weather_airport ON weather_data(airport_id);
CREATE INDEX idx_weather_timestamp ON weather_data(timestamp DESC);
CREATE INDEX idx_communications_timestamp ON communications(timestamp DESC);
CREATE INDEX idx_communications_sender ON communications(sender_type, sender_id);
CREATE INDEX idx_communications_receiver ON communications(receiver_type, receiver_id);
CREATE INDEX idx_runways_airport ON runways(airport_id);
CREATE INDEX idx_radar_airport ON radar_centers(airport_id);

-- =====================================================
-- VUES UTILES
-- =====================================================

-- Vue pour les avions en vol avec leurs informations
CREATE OR REPLACE VIEW aircraft_in_flight AS
SELECT 
    a.id,
    a.registration,
    a.model,
    a.status,
    a.position_lat,
    a.position_lon,
    a.altitude,
    a.speed,
    a.heading,
    p.name AS pilot_name,
    p.license AS pilot_license,
    ap.name AS airport_name,
    ap.code_iata AS airport_code
FROM aircraft a
LEFT JOIN pilots p ON a.pilot_id = p.id
LEFT JOIN airports ap ON a.airport_id = ap.id
WHERE a.status IN ('EN_VOL', 'ATTERRISSAGE', 'DECOLLAGE');

-- Vue pour les alertes météo actives
CREATE OR REPLACE VIEW active_weather_alerts AS
SELECT 
    w.id,
    w.airport_id,
    a.name AS airport_name,
    a.code_iata,
    w.wind_speed,
    w.visibility,
    w.conditions,
    w.alert,
    w.timestamp
FROM weather_data w
JOIN airports a ON w.airport_id = a.id
WHERE w.alert = TRUE
ORDER BY w.timestamp DESC;

-- =====================================================
-- FONCTIONS UTILES
-- =====================================================

-- Fonction pour calculer le vent de travers
CREATE OR REPLACE FUNCTION calculate_crosswind(
    wind_speed DECIMAL,
    wind_direction DECIMAL,
    runway_orientation DECIMAL
) RETURNS DECIMAL AS $$
DECLARE
    angle_diff DECIMAL;
    crosswind DECIMAL;
BEGIN
    -- Calculer la différence d'angle
    angle_diff := ABS(wind_direction - runway_orientation);
    
    -- Normaliser entre 0 et 180
    IF angle_diff > 180 THEN
        angle_diff := 360 - angle_diff;
    END IF;
    
    -- Calculer le vent de travers (composante perpendiculaire)
    crosswind := wind_speed * SIN(RADIANS(angle_diff));
    
    RETURN ABS(crosswind);
END;
$$ LANGUAGE plpgsql;

-- Fonction pour déterminer si les conditions météo sont sûres pour atterrir
CREATE OR REPLACE FUNCTION is_safe_to_land(
    p_visibility DECIMAL,
    p_wind_speed DECIMAL,
    p_crosswind DECIMAL,
    p_conditions VARCHAR
) RETURNS BOOLEAN AS $$
BEGIN
    -- Conditions minimales pour atterrir
    IF p_visibility < 1.0 THEN
        RETURN FALSE;
    END IF;
    
    IF p_wind_speed > 50.0 THEN
        RETURN FALSE;
    END IF;
    
    IF p_crosswind > 15.0 THEN
        RETURN FALSE;
    END IF;
    
    IF p_conditions IN ('Thunderstorm', 'Heavy Rain', 'Fog', 'Blizzard') THEN
        RETURN FALSE;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- COMMENTAIRES SUR LES TABLES
-- =====================================================
COMMENT ON TABLE airports IS 'Aéroports du système (Casablanca, Rabat, Marrakech, Tanger)';
COMMENT ON TABLE runways IS 'Pistes d''atterrissage des aéroports';
COMMENT ON TABLE aircraft IS 'Avions du système avec leur position et statut';
COMMENT ON TABLE pilots IS 'Pilotes assignés aux avions';
COMMENT ON TABLE radar_centers IS 'Centres radar de contrôle aérien';
COMMENT ON TABLE flights IS 'Vols planifiés et en cours';
COMMENT ON TABLE weather_data IS 'Données météorologiques en temps réel';
COMMENT ON TABLE communications IS 'Communications VHF entre radar, avions et aéroports';
COMMENT ON TABLE users IS 'Utilisateurs du système (admin, pilotes, centres radar)';

