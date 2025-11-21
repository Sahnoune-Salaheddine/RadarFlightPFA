-- =====================================================
-- Script SQL - Assigner un Avion à TOUS les Pilotes
-- =====================================================
-- Ce script assigne automatiquement un avion à chaque pilote
-- qui n'en a pas encore
-- =====================================================

-- Afficher l'état actuel
SELECT '=== ETAT ACTUEL ===' as info;

SELECT 
    u.username,
    p.id as pilot_id,
    p.name as pilot_name,
    COUNT(a.id) as nombre_avions
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE'
GROUP BY u.username, p.id, p.name;

-- =====================================================
-- ÉTAPE 1: Assigner un avion existant à chaque pilote sans avion
-- =====================================================
DO $$
DECLARE
    pilot_record RECORD;
    aircraft_record RECORD;
BEGIN
    -- Pour chaque pilote sans avion
    FOR pilot_record IN 
        SELECT p.id as pilot_id, u.username
        FROM pilots p
        JOIN users u ON p.user_id = u.id
        WHERE u.role = 'PILOTE'
        AND NOT EXISTS (
            SELECT 1 FROM aircraft a WHERE a.pilot_id = p.id
        )
    LOOP
        -- Trouver un avion disponible (sans pilote)
        SELECT id INTO aircraft_record
        FROM aircraft
        WHERE pilot_id IS NULL
        LIMIT 1;
        
        -- Si un avion est disponible, l'assigner
        IF aircraft_record.id IS NOT NULL THEN
            UPDATE aircraft
            SET 
                pilot_id = pilot_record.pilot_id,
                username_pilote = pilot_record.username
            WHERE id = aircraft_record.id;
            
            RAISE NOTICE 'Avion % assigne au pilote %', aircraft_record.id, pilot_record.username;
        END IF;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 2: Créer des avions pour les pilotes qui n'en ont toujours pas
-- =====================================================
DO $$
DECLARE
    pilot_record RECORD;
    airport_id_var BIGINT;
    aircraft_counter INTEGER := 1;
BEGIN
    -- Récupérer l'ID de l'aéroport CMN (Casablanca)
    SELECT id INTO airport_id_var FROM airports WHERE code_iata = 'CMN' LIMIT 1;
    
    -- Si aucun aéroport n'existe, utiliser le premier disponible
    IF airport_id_var IS NULL THEN
        SELECT id INTO airport_id_var FROM airports LIMIT 1;
    END IF;
    
    -- Pour chaque pilote sans avion
    FOR pilot_record IN 
        SELECT p.id as pilot_id, u.username, p.name as pilot_name
        FROM pilots p
        JOIN users u ON p.user_id = u.id
        WHERE u.role = 'PILOTE'
        AND NOT EXISTS (
            SELECT 1 FROM aircraft a WHERE a.pilot_id = p.id
        )
    LOOP
        -- Créer un nouvel avion pour ce pilote
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
        ) VALUES (
            'CN-' || LPAD(aircraft_counter::TEXT, 3, '0'),
            'A320',
            'AU_SOL',
            airport_id_var,
            pilot_record.pilot_id,
            pilot_record.username,
            33.367500,
            -7.589800,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            '1200',
            CURRENT_TIMESTAMP
        )
        ON CONFLICT (registration) DO NOTHING;
        
        RAISE NOTICE 'Nouvel avion CN-% cree et assigne au pilote %', 
            LPAD(aircraft_counter::TEXT, 3, '0'), pilot_record.username;
        
        aircraft_counter := aircraft_counter + 1;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 3: Vérification finale
-- =====================================================
SELECT '=== RESULTAT FINAL ===' as info;

SELECT 
    u.username,
    p.name as pilot_name,
    a.registration as aircraft_registration,
    a.model as aircraft_model,
    a.status as aircraft_status
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE'
ORDER BY u.username;

-- Compter les pilotes avec et sans avion
SELECT 
    'Pilotes avec avion: ' || COUNT(DISTINCT p.id) FILTER (WHERE a.id IS NOT NULL) as avec_avion,
    'Pilotes sans avion: ' || COUNT(DISTINCT p.id) FILTER (WHERE a.id IS NULL) as sans_avion
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE';

SELECT '=== ASSIGNATION TERMINEE ===' as info;

