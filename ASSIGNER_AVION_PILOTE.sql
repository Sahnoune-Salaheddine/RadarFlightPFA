-- =====================================================
-- Script SQL Simple pour Assigner un Avion au Pilote
-- =====================================================
-- Exécutez ce script dans pgAdmin ou psql
-- =====================================================

-- 1. Vérifier l'état actuel
SELECT '=== ÉTAT ACTUEL ===' as info;

SELECT 'Utilisateurs pilotes:' as info;
SELECT id, username, role FROM users WHERE role = 'PILOTE';

SELECT 'Pilotes:' as info;
SELECT p.id, p.name, p.license, u.username 
FROM pilots p 
LEFT JOIN users u ON p.user_id = u.id;

SELECT 'Avions:' as info;
SELECT id, registration, model, status, pilot_id, username_pilote 
FROM aircraft;

-- 2. Assigner un avion au pilote pilote_cmn1
SELECT '=== ASSIGNATION ===' as info;

-- Option 1 : Si un avion existe déjà, l'assigner au pilote
UPDATE aircraft 
SET 
    pilot_id = (SELECT p.id FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1' LIMIT 1),
    username_pilote = 'pilote_cmn1'
WHERE id = (
    SELECT id FROM aircraft 
    WHERE (pilot_id IS NULL OR username_pilote IS NULL)
    LIMIT 1
);

-- Option 2 : Si aucun avion n'existe, créer un nouvel avion
INSERT INTO aircraft (
    registration, 
    model, 
    status, 
    airport_id, 
    pilot_id,
    username_pilote,
    position_lat, 
    position_lon, 
    altitude, 
    speed, 
    heading,
    air_speed,
    vertical_speed,
    transponder_code,
    last_update
)
SELECT 
    'CN-ABC',
    'A320',
    'AU_SOL',
    (SELECT id FROM airports WHERE code_iata = 'CMN' LIMIT 1),
    (SELECT p.id FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1' LIMIT 1),
    'pilote_cmn1',
    33.367500,
    -7.589800,
    0.0,
    0.0,
    0.0,
    0.0,
    0.0,
    '1200',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM aircraft 
    WHERE registration = 'CN-ABC'
)
AND EXISTS (
    SELECT 1 FROM pilots p 
    JOIN users u ON p.user_id = u.id 
    WHERE u.username = 'pilote_cmn1'
);

-- 3. Vérifier le résultat
SELECT '=== RÉSULTAT ===' as info;

SELECT 
    a.id as aircraft_id,
    a.registration,
    a.model,
    a.status,
    a.username_pilote,
    p.id as pilot_id,
    p.name as pilot_name,
    u.username
FROM aircraft a
LEFT JOIN pilots p ON a.pilot_id = p.id
LEFT JOIN users u ON p.user_id = u.id
WHERE u.username = 'pilote_cmn1' OR a.username_pilote = 'pilote_cmn1';

