-- =====================================================
-- Script RAPIDE pour Assigner un Avion au Pilote
-- =====================================================
-- Exécutez ce script dans pgAdmin ou psql
-- =====================================================

-- 1. Vérifier l'état actuel
SELECT '=== ÉTAT ACTUEL ===' as info;

SELECT u.username, p.name, a.registration 
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE';

-- 2. Assigner un avion existant au pilote pilote_cmn1 (ou créer un nouveau)
DO $$
DECLARE
    v_pilot_id BIGINT;
    v_aircraft_id BIGINT;
    v_airport_id BIGINT;
    v_username TEXT := 'pilote_cmn1';
BEGIN
    -- Trouver l'ID du pilote
    SELECT p.id INTO v_pilot_id
    FROM pilots p
    JOIN users u ON p.user_id = u.id
    WHERE u.username = v_username
    LIMIT 1;
    
    IF v_pilot_id IS NULL THEN
        RAISE NOTICE 'Pilote % non trouvé', v_username;
        RETURN;
    END IF;
    
    -- Chercher un avion disponible (sans pilote)
    SELECT id INTO v_aircraft_id
    FROM aircraft
    WHERE pilot_id IS NULL
    LIMIT 1;
    
    -- Si aucun avion disponible, créer un nouvel avion
    IF v_aircraft_id IS NULL THEN
        -- Récupérer l'aéroport par défaut (CMN)
        SELECT id INTO v_airport_id
        FROM airports
        WHERE code_iata = 'CMN'
        LIMIT 1;
        
        IF v_airport_id IS NULL THEN
            SELECT id INTO v_airport_id
            FROM airports
            LIMIT 1;
        END IF;
        
        -- Créer un nouvel avion
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
        VALUES (
            'CN-ABC',
            'A320',
            'AU_SOL',
            v_airport_id,
            v_pilot_id,
            v_username,
            COALESCE((SELECT latitude FROM airports WHERE id = v_airport_id), 33.367500),
            COALESCE((SELECT longitude FROM airports WHERE id = v_airport_id), -7.589800),
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            '1200',
            NOW()
        )
        RETURNING id INTO v_aircraft_id;
        
        RAISE NOTICE 'Avion créé et assigné: % pour pilote %', v_aircraft_id, v_username;
    ELSE
        -- Assigner l'avion existant au pilote
        UPDATE aircraft
        SET 
            pilot_id = v_pilot_id,
            username_pilote = v_username
        WHERE id = v_aircraft_id;
        
        RAISE NOTICE 'Avion existant assigné: % pour pilote %', v_aircraft_id, v_username;
    END IF;
END $$;

-- 3. Vérifier le résultat
SELECT '=== RÉSULTAT ===' as info;

SELECT 
    u.username as pilote_username,
    p.name as pilote_name,
    a.id as aircraft_id,
    a.registration,
    a.model,
    a.status,
    a.username_pilote
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.username = 'pilote_cmn1';

