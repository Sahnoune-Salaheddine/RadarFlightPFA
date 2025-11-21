-- Script SQL pour assigner un aéroport à un utilisateur CENTRE_RADAR

-- 1. Vérifier les utilisateurs CENTRE_RADAR sans aéroport
SELECT id, username, role, airport_id, pilot_id 
FROM users 
WHERE role = 'CENTRE_RADAR' AND (airport_id IS NULL OR airport_id = 0);

-- 2. Lister les aéroports disponibles
SELECT id, name, code_iata, city 
FROM airports 
ORDER BY id;

-- 3. Assigner un aéroport à un utilisateur CENTRE_RADAR
-- Remplacez 'username_radar' par le username de l'utilisateur
-- Remplacez 1 par l'ID de l'aéroport souhaité

-- Exemple : Assigner l'aéroport ID 1 (Casablanca) à l'utilisateur 'radar1'
UPDATE users 
SET airport_id = 1 
WHERE username = 'radar1' AND role = 'CENTRE_RADAR';

-- Ou assigner l'aéroport ID 2 (Marrakech) à l'utilisateur 'radar2'
-- UPDATE users 
-- SET airport_id = 2 
-- WHERE username = 'radar2' AND role = 'CENTRE_RADAR';

-- 4. Vérifier l'assignation
SELECT u.id, u.username, u.role, u.airport_id, a.name as airport_name, a.code_iata
FROM users u
LEFT JOIN airports a ON u.airport_id = a.id
WHERE u.role = 'CENTRE_RADAR';

