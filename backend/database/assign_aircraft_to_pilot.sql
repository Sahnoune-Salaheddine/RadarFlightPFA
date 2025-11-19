-- =====================================================
-- Script pour assigner un avion au pilote
-- =====================================================
-- Ce script vérifie et assigne un avion au pilote "pilote_cmn1"
-- =====================================================

-- 1. Vérifier que l'utilisateur pilote existe
SELECT id, username, role FROM users WHERE username = 'pilote_cmn1';

-- 2. Vérifier que le pilote existe
SELECT p.id, p.name, p.license, p.user_id, u.username 
FROM pilots p 
LEFT JOIN users u ON p.user_id = u.id 
WHERE u.username = 'pilote_cmn1';

-- 3. Vérifier les avions existants
SELECT id, registration, model, status, pilot_id, username_pilote, airport_id 
FROM aircraft;

-- 4. Assigner un avion au pilote (si le pilote existe mais n'a pas d'avion)
-- Option A : Si un avion existe déjà sans pilote, l'assigner
UPDATE aircraft 
SET pilot_id = (
    SELECT p.id 
    FROM pilots p 
    JOIN users u ON p.user_id = u.id 
    WHERE u.username = 'pilote_cmn1' 
    LIMIT 1
),
username_pilote = 'pilote_cmn1'
WHERE id = (
    SELECT id FROM aircraft 
    WHERE pilot_id IS NULL 
    LIMIT 1
);

-- Option B : Créer un nouvel avion et l'assigner au pilote
-- (Exécuter seulement si aucun avion n'existe)
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
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM aircraft WHERE registration = 'CN-ABC'
);

-- 5. Vérifier le résultat
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

