-- =====================================================
-- Script SQL pour Assigner un Avion au Pilote
-- =====================================================
-- Ce script assigne un avion au pilote connecté
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

-- 2. Assigner un avion au pilote (pour tous les pilotes sans avion)
DO $$
DECLARE
    v_pilot_record RECORD;
    v_aircraft_id BIGINT;
    v_airport_id BIGINT;
BEGIN
    -- Pour chaque pilote sans avion
    FOR v_pilot_record IN 
        SELECT p.id as pilot_id, u.username, p.name as pilot_name
        FROM pilots p
        JOIN users u ON p.user_id = u.id
        WHERE u.role = 'PILOTE'
          AND NOT EXISTS (
              SELECT 1 FROM aircraft a WHERE a.pilot_id = p.id
          )
    LOOP
        -- Chercher un avion disponible (sans pilote)
        SELECT id INTO v_aircraft_id
        FROM aircraft
        WHERE pilot_id IS NULL
        LIMIT 1;
        
        -- Si aucun avion disponible, créer un nouvel avion
        IF v_aircraft_id IS NULL THEN
            -- Récupérer l'aéroport par défaut (CMN) ou le premier disponible
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
                'CN-' || UPPER(SUBSTRING(v_pilot_record.username, 1, 3)) || '01',
                'A320',
                'AU_SOL',
                v_airport_id,
                v_pilot_record.pilot_id,
                v_pilot_record.username,
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
            
            RAISE NOTICE 'Avion créé et assigné: % pour pilote %', v_aircraft_id, v_pilot_record.username;
        ELSE
            -- Assigner l'avion existant au pilote
            UPDATE aircraft
            SET 
                pilot_id = v_pilot_record.pilot_id,
                username_pilote = v_pilot_record.username
            WHERE id = v_aircraft_id;
            
            RAISE NOTICE 'Avion existant assigné: % pour pilote %', v_aircraft_id, v_pilot_record.username;
        END IF;
    END LOOP;
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
WHERE u.role = 'PILOTE'
ORDER BY u.username;

-- 4. Afficher un résumé
SELECT 
    COUNT(DISTINCT u.id) as total_pilotes,
    COUNT(DISTINCT a.id) as total_avions_assignes,
    COUNT(DISTINCT u.id) - COUNT(DISTINCT a.id) as pilotes_sans_avion
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE';

