-- Script SQL pour assigner automatiquement les aéroports aux utilisateurs CENTRE_RADAR
-- Basé sur le code IATA dans le username

-- Assigner Casablanca (CMN) à radar_cmn
UPDATE users 
SET airport_id = 1 
WHERE username = 'radar_cmn' AND role = 'CENTRE_RADAR';

-- Assigner Rabat (RBA) à radar_rba
UPDATE users 
SET airport_id = 2 
WHERE username = 'radar_rba' AND role = 'CENTRE_RADAR';

-- Assigner Marrakech (RAK) à radar_rak
UPDATE users 
SET airport_id = 3 
WHERE username = 'radar_rak' AND role = 'CENTRE_RADAR';

-- Assigner Tanger (TNG) à radar_tng
UPDATE users 
SET airport_id = 4 
WHERE username = 'radar_tng' AND role = 'CENTRE_RADAR';

-- Vérifier les assignations
SELECT 
    u.id, 
    u.username, 
    u.role, 
    u.airport_id, 
    a.name as airport_name, 
    a.code_iata,
    a.city
FROM users u
LEFT JOIN airports a ON u.airport_id = a.id
WHERE u.role = 'CENTRE_RADAR'
ORDER BY u.id;

