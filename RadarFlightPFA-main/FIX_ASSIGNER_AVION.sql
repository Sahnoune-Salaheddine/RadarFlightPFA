-- =====================================================
-- FIX RAPIDE : Assigner un Avion au Pilote
-- =====================================================
-- Copiez-collez ce script dans pgAdmin et exécutez-le
-- =====================================================

-- Assigner un avion existant au pilote pilote_cmn1
UPDATE aircraft 
SET 
    pilot_id = (SELECT p.id FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1' LIMIT 1),
    username_pilote = 'pilote_cmn1'
WHERE id = (SELECT id FROM aircraft WHERE pilot_id IS NULL LIMIT 1);

-- Si aucun avion n'existe, créer un nouvel avion
INSERT INTO aircraft (
    registration, model, status, airport_id, pilot_id, username_pilote,
    position_lat, position_lon, altitude, speed, heading,
    air_speed, vertical_speed, transponder_code, last_update
)
SELECT 
    'CN-ABC', 'A320', 'AU_SOL',
    (SELECT id FROM airports WHERE code_iata = 'CMN' LIMIT 1),
    (SELECT p.id FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1' LIMIT 1),
    'pilote_cmn1',
    33.367500, -7.589800, 0.0, 0.0, 0.0, 0.0, 0.0, '1200', NOW()
WHERE NOT EXISTS (SELECT 1 FROM aircraft WHERE registration = 'CN-ABC')
AND EXISTS (SELECT 1 FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1');

-- Vérifier le résultat
SELECT 
    a.registration,
    a.model,
    a.status,
    u.username as pilote_username,
    a.username_pilote
FROM aircraft a
LEFT JOIN pilots p ON a.pilot_id = p.id
LEFT JOIN users u ON p.user_id = u.id
WHERE u.username = 'pilote_cmn1' OR a.username_pilote = 'pilote_cmn1';

