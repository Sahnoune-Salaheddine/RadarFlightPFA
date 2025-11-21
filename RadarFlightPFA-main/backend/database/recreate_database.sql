-- =====================================================
-- SCRIPT DE RÉINITIALISATION COMPLÈTE DE LA BASE DE DONNÉES
-- Supprime toutes les tables et les recrée
-- =====================================================
-- Usage: psql -U postgres -d flightradar -f recreate_database.sql
-- =====================================================

-- Désactiver temporairement les contraintes de clés étrangères
SET session_replication_role = 'replica';

-- =====================================================
-- SUPPRESSION DE TOUTES LES TABLES
-- =====================================================

-- Supprimer toutes les tables dans l'ordre inverse des dépendances
DROP TABLE IF EXISTS communications CASCADE;
DROP TABLE IF EXISTS atc_messages CASCADE;
DROP TABLE IF EXISTS atis_data CASCADE;
DROP TABLE IF EXISTS weather_data CASCADE;
DROP TABLE IF EXISTS flights CASCADE;
DROP TABLE IF EXISTS aircraft CASCADE;
DROP TABLE IF EXISTS pilots CASCADE;
DROP TABLE IF EXISTS radar_centers CASCADE;
DROP TABLE IF EXISTS runways CASCADE;
DROP TABLE IF EXISTS airports CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Supprimer les tables supplémentaires qui pourraient exister
DROP TABLE IF EXISTS airmet CASCADE;
DROP TABLE IF EXISTS alert_rules CASCADE;
DROP TABLE IF EXISTS alerts CASCADE;
DROP TABLE IF EXISTS api_health_checks CASCADE;
DROP TABLE IF EXISTS flight_states CASCADE;
DROP TABLE IF EXISTS metar_decoded CASCADE;
DROP TABLE IF EXISTS metar_raw CASCADE;
DROP TABLE IF EXISTS pirep CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS sigmet CASCADE;
DROP TABLE IF EXISTS taf_decoded CASCADE;
DROP TABLE IF EXISTS taf_raw CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;

-- Réactiver les contraintes
SET session_replication_role = 'origin';

-- =====================================================
-- CRÉATION DES TABLES (schéma complet)
-- =====================================================

-- =====================================================
-- TABLE: users
-- =====================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'PILOTE', 'CENTRE_RADAR')),
    airport_id BIGINT,
    pilot_id BIGINT,
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
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    assigned_aircraft_id BIGINT,
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
    numero_vol VARCHAR(10),
    type_avion VARCHAR(50),
    status VARCHAR(20) NOT NULL CHECK (status IN ('AU_SOL', 'DECOLLAGE', 'EN_VOL', 'ATTERRISSAGE', 'EN_ATTENTE')),
    airport_id BIGINT,
    pilot_id BIGINT,
    username_pilote VARCHAR(50),
    position_lat DECIMAL(10, 8),
    position_lon DECIMAL(11, 8),
    altitude DECIMAL(10, 2) DEFAULT 0,
    speed DECIMAL(8, 2) DEFAULT 0,
    heading DECIMAL(5, 2) DEFAULT 0 CHECK (heading >= 0 AND heading < 360),
    air_speed DECIMAL(8, 2),
    vertical_speed DECIMAL(8, 2),
    transponder_code VARCHAR(10),
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
    airline VARCHAR(100),
    aircraft_id BIGINT NOT NULL,
    departure_airport_id BIGINT NOT NULL,
    arrival_airport_id BIGINT NOT NULL,
    flight_status VARCHAR(20) NOT NULL CHECK (flight_status IN ('PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE')),
    scheduled_departure TIMESTAMP,
    scheduled_arrival TIMESTAMP,
    actual_departure TIMESTAMP,
    actual_arrival TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (aircraft_id) REFERENCES aircraft(id) ON DELETE CASCADE,
    FOREIGN KEY (departure_airport_id) REFERENCES airports(id) ON DELETE CASCADE,
    FOREIGN KEY (arrival_airport_id) REFERENCES airports(id) ON DELETE CASCADE
);

-- =====================================================
-- TABLE: weather_data
-- =====================================================
CREATE TABLE weather_data (
    id BIGSERIAL PRIMARY KEY,
    airport_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    temperature DECIMAL(5, 2),
    wind_speed DECIMAL(6, 2),
    wind_direction DECIMAL(5, 2),
    visibility DECIMAL(6, 2),
    pressure DECIMAL(7, 2),
    conditions VARCHAR(100),
    crosswind DECIMAL(6, 2),
    alert BOOLEAN DEFAULT FALSE,
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
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TABLE: atc_messages
-- =====================================================
CREATE TABLE atc_messages (
    id BIGSERIAL PRIMARY KEY,
    aircraft_id BIGINT NOT NULL,
    radar_center_id BIGINT NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (aircraft_id) REFERENCES aircraft(id) ON DELETE CASCADE,
    FOREIGN KEY (radar_center_id) REFERENCES radar_centers(id) ON DELETE CASCADE
);

-- =====================================================
-- TABLE: atis_data
-- =====================================================
CREATE TABLE atis_data (
    id BIGSERIAL PRIMARY KEY,
    airport_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    temperature DECIMAL(5, 2),
    pression DECIMAL(8, 2),
    vent DECIMAL(8, 2),
    visibilité DECIMAL(8, 2),
    FOREIGN KEY (airport_id) REFERENCES airports(id) ON DELETE CASCADE
);

-- =====================================================
-- INDEXES POUR PERFORMANCE
-- =====================================================

CREATE INDEX idx_aircraft_pilot_id ON aircraft(pilot_id);
CREATE INDEX idx_aircraft_airport_id ON aircraft(airport_id);
CREATE INDEX idx_aircraft_status ON aircraft(status);
CREATE INDEX idx_flights_aircraft_id ON flights(aircraft_id);
CREATE INDEX idx_flights_status ON flights(flight_status);
CREATE INDEX idx_weather_airport_id ON weather_data(airport_id);
CREATE INDEX idx_weather_timestamp ON weather_data(timestamp);
CREATE INDEX idx_communications_timestamp ON communications(timestamp);
CREATE INDEX idx_atc_messages_aircraft_id ON atc_messages(aircraft_id);

-- =====================================================
-- COMMENTAIRES
-- =====================================================

COMMENT ON TABLE users IS 'Utilisateurs du système (ADMIN, PILOTE, CENTRE_RADAR)';
COMMENT ON TABLE airports IS 'Aéroports marocains';
COMMENT ON TABLE runways IS 'Pistes d''atterrissage';
COMMENT ON TABLE pilots IS 'Pilotes assignés aux avions';
COMMENT ON TABLE radar_centers IS 'Centres radar par aéroport';
COMMENT ON TABLE aircraft IS 'Avions avec positions GPS';
COMMENT ON TABLE flights IS 'Vols planifiés et en cours';
COMMENT ON TABLE weather_data IS 'Données météorologiques par aéroport';
COMMENT ON TABLE communications IS 'Communications VHF entre radar, avions et aéroports';
COMMENT ON TABLE atc_messages IS 'Messages ATC (Air Traffic Control)';
COMMENT ON TABLE atis_data IS 'Données ATIS (Automatic Terminal Information Service)';

-- =====================================================
-- FIN DU SCRIPT
-- =====================================================

