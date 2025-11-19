-- Script RAPIDE pour corriger la liaison Pilote ⇄ Avion
-- Exécuter dans PostgreSQL : psql -U postgres -d flightradar -f CORRIGER_PILOTE_AVION_RAPIDE.sql

-- 1. S'assurer que l'utilisateur pilote_cmn1 existe
INSERT INTO users (username, password, role)
SELECT 'pilote_cmn1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'PILOTE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'pilote_cmn1')
ON CONFLICT (username) DO NOTHING;

-- 2. Créer le pilote s'il n'existe pas
INSERT INTO pilots (name, license, experience_years, user_id)
SELECT 'Pilote CMN1', 'CMN1', 5, id
FROM users 
WHERE username = 'pilote_cmn1'
  AND NOT EXISTS (SELECT 1 FROM pilots WHERE user_id = users.id);

-- 3. S'assurer qu'un aéroport existe
INSERT INTO airports (name, city, code_iata, latitude, longitude)
SELECT 'Aéroport Mohammed V', 'Casablanca', 'CMN', 33.3675, -7.5898
WHERE NOT EXISTS (SELECT 1 FROM airports WHERE code_iata = 'CMN');

INSERT INTO airports (name, city, code_iata, latitude, longitude)
SELECT 'Aéroport Rabat-Salé', 'Rabat', 'RBA', 34.0515, -6.7515
WHERE NOT EXISTS (SELECT 1 FROM airports WHERE code_iata = 'RBA');

-- 4. Créer ou mettre à jour l'avion pour pilote_cmn1
INSERT INTO aircraft (registration, model, status, airport_id, position_lat, position_lon, altitude, speed, heading, last_update, pilot_id, username_pilote)
SELECT 
    'CN-AT01',
    'A320',
    'AU_SOL',
    (SELECT id FROM airports WHERE code_iata = 'CMN' LIMIT 1),
    33.3675,
    -7.5898,
    0.0,
    0.0,
    0.0,
    NOW(),
    (SELECT id FROM pilots WHERE user_id = (SELECT id FROM users WHERE username = 'pilote_cmn1')),
    'pilote_cmn1'
ON CONFLICT (registration) 
DO UPDATE SET
    pilot_id = (SELECT id FROM pilots WHERE user_id = (SELECT id FROM users WHERE username = 'pilote_cmn1')),
    username_pilote = 'pilote_cmn1',
    airport_id = (SELECT id FROM airports WHERE code_iata = 'CMN' LIMIT 1),
    position_lat = 33.3675,
    position_lon = -7.5898;

-- 5. Créer un vol pour l'avion
INSERT INTO flights (flight_number, aircraft_id, departure_airport_id, arrival_airport_id, flight_status, scheduled_departure, scheduled_arrival, created_at, airline)
SELECT 
    'AT1001',
    (SELECT id FROM aircraft WHERE registration = 'CN-AT01'),
    (SELECT id FROM airports WHERE code_iata = 'CMN'),
    (SELECT id FROM airports WHERE code_iata = 'RBA'),
    'PLANIFIE',
    NOW() + INTERVAL '1 hour',
    NOW() + INTERVAL '2 hours',
    NOW(),
    'Royal Air Maroc'
ON CONFLICT (flight_number) 
DO UPDATE SET
    aircraft_id = (SELECT id FROM aircraft WHERE registration = 'CN-AT01'),
    departure_airport_id = (SELECT id FROM airports WHERE code_iata = 'CMN'),
    arrival_airport_id = (SELECT id FROM airports WHERE code_iata = 'RBA'),
    airline = 'Royal Air Maroc';

-- 6. Vérification
SELECT 
    '✅ Vérification' as status,
    u.username,
    a.registration,
    a.model,
    a.status,
    f.flight_number,
    f.airline
FROM users u
JOIN pilots p ON p.user_id = u.id
JOIN aircraft a ON a.pilot_id = p.id
LEFT JOIN flights f ON f.aircraft_id = a.id
WHERE u.username = 'pilote_cmn1';

