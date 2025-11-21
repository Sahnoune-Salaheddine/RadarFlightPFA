-- =====================================================
-- DONNÉES D'INITIALISATION - FLIGHT RADAR
-- Script SQL pour initialiser les données de test
-- =====================================================

-- Note: Ce script est optionnel car DataInitializer.java
-- initialise automatiquement les données au démarrage

-- =====================================================
-- UTILISATEURS
-- =====================================================
-- Les mots de passe sont hashés avec BCrypt
-- admin123, pilote123, radar123

-- =====================================================
-- AÉROPORTS
-- =====================================================
INSERT INTO airports (name, city, code_iata, latitude, longitude) VALUES
('Aéroport Mohammed V', 'Casablanca', 'CMN', 33.367500, -7.589800),
('Aéroport Rabat-Salé', 'Rabat', 'RBA', 34.051500, -6.751500),
('Aéroport Marrakech-Ménara', 'Marrakech', 'RAK', 31.606900, -8.036300),
('Aéroport Tanger-Ibn Battouta', 'Tanger', 'TNG', 35.726900, -5.916900)
ON CONFLICT (code_iata) DO NOTHING;

-- =====================================================
-- PISTES
-- =====================================================
-- Casablanca (CMN) - 2 pistes
INSERT INTO runways (name, orientation, length_meters, width_meters, airport_id)
SELECT '17/35', 170.0, 3700, 45, id FROM airports WHERE code_iata = 'CMN'
ON CONFLICT DO NOTHING;

INSERT INTO runways (name, orientation, length_meters, width_meters, airport_id)
SELECT '09/27', 90.0, 3200, 45, id FROM airports WHERE code_iata = 'CMN'
ON CONFLICT DO NOTHING;

-- Rabat (RBA) - 2 pistes
INSERT INTO runways (name, orientation, length_meters, width_meters, airport_id)
SELECT '03/21', 30.0, 3500, 45, id FROM airports WHERE code_iata = 'RBA'
ON CONFLICT DO NOTHING;

INSERT INTO runways (name, orientation, length_meters, width_meters, airport_id)
SELECT '12/30', 120.0, 3000, 45, id FROM airports WHERE code_iata = 'RBA'
ON CONFLICT DO NOTHING;

-- Marrakech (RAK) - 2 pistes
INSERT INTO runways (name, orientation, length_meters, width_meters, airport_id)
SELECT '10/28', 100.0, 4500, 45, id FROM airports WHERE code_iata = 'RAK'
ON CONFLICT DO NOTHING;

INSERT INTO runways (name, orientation, length_meters, width_meters, airport_id)
SELECT '05/23', 50.0, 3000, 45, id FROM airports WHERE code_iata = 'RAK'
ON CONFLICT DO NOTHING;

-- Tanger (TNG) - 2 pistes
INSERT INTO runways (name, orientation, length_meters, width_meters, airport_id)
SELECT '10/28', 100.0, 3500, 45, id FROM airports WHERE code_iata = 'TNG'
ON CONFLICT DO NOTHING;

INSERT INTO runways (name, orientation, length_meters, width_meters, airport_id)
SELECT '17/35', 170.0, 3200, 45, id FROM airports WHERE code_iata = 'TNG'
ON CONFLICT DO NOTHING;

-- =====================================================
-- NOTES
-- =====================================================
-- Les autres données (pilotes, avions, centres radar, etc.)
-- sont initialisées automatiquement par DataInitializer.java
-- au démarrage de l'application Spring Boot

