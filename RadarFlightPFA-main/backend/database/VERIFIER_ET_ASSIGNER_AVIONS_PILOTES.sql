-- =====================================================
-- Script SQL - Vérifier et Assigner Avions aux Pilotes
-- =====================================================
-- Ce script vérifie et assigne correctement les avions aux pilotes
-- selon la logique métier : 1 pilote = 1 avion
-- =====================================================

-- =====================================================
-- ÉTAPE 1: État actuel
-- =====================================================

SELECT '=== ÉTAT ACTUEL ===' as info;

-- Vérifier les pilotes et leurs avions
SELECT 
    u.username,
    p.id as pilot_id,
    p.name as pilot_name,
    p.license,
    p.airport_id,
    a.id as aircraft_id,
    a.registration as aircraft_registration,
    a.model as aircraft_model,
    a.airport_id as aircraft_airport_id,
    CASE 
        WHEN a.id IS NULL THEN 'PAS D''AVION'
        WHEN a.pilot_id IS NULL THEN 'AVION NON ASSIGNE'
        WHEN a.pilot_id != p.id THEN 'AVION ASSIGNE A UN AUTRE PILOTE'
        ELSE 'OK'
    END as statut
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE'
ORDER BY u.username;

-- =====================================================
-- ÉTAPE 2: Assigner les avions aux pilotes
-- =====================================================

DO $$
DECLARE
    pilot_record RECORD;
    aircraft_record RECORD;
    airport_record RECORD;
    aircraft_id_var BIGINT;
    pilot_counter INTEGER;
    airport_id_var BIGINT;
BEGIN
    -- ÉTAPE 1: Assigner d'abord les aéroports aux pilotes qui n'en ont pas
    -- En se basant sur le username (pilote_cmn1 -> CMN, etc.)
    FOR pilot_record IN 
        SELECT p.id, p.user_id, u.username, p.airport_id
        FROM pilots p
        JOIN users u ON p.user_id = u.id
        WHERE u.role = 'PILOTE'
        AND (p.airport_id IS NULL OR p.airport_id = 0)
    LOOP
        -- Extraire le code aéroport du username (ex: pilote_cmn1 -> CMN)
        IF pilot_record.username LIKE 'pilote_cmn%' THEN
            SELECT id INTO airport_id_var FROM airports WHERE code_iata = 'CMN';
        ELSIF pilot_record.username LIKE 'pilote_rba%' THEN
            SELECT id INTO airport_id_var FROM airports WHERE code_iata = 'RBA';
        ELSIF pilot_record.username LIKE 'pilote_rak%' THEN
            SELECT id INTO airport_id_var FROM airports WHERE code_iata = 'RAK';
        ELSIF pilot_record.username LIKE 'pilote_tng%' THEN
            SELECT id INTO airport_id_var FROM airports WHERE code_iata = 'TNG';
        ELSE
            -- Par défaut, assigner le premier aéroport
            SELECT id INTO airport_id_var FROM airports ORDER BY id LIMIT 1;
        END IF;
        
        IF airport_id_var IS NOT NULL THEN
            UPDATE pilots SET airport_id = airport_id_var WHERE id = pilot_record.id;
            RAISE NOTICE 'Aeroport % assigne au pilote % (username: %)', 
                airport_id_var, pilot_record.id, pilot_record.username;
        END IF;
    END LOOP;
    
    -- ÉTAPE 2: Assigner les avions aux pilotes
    -- Pour chaque aéroport
    FOR airport_record IN SELECT id, code_iata FROM airports ORDER BY id
    LOOP
        pilot_counter := 1;
        
        -- Récupérer les pilotes de cet aéroport (ou sans aéroport)
        FOR pilot_record IN 
            SELECT p.id, p.user_id, u.username, p.airport_id
            FROM pilots p
            JOIN users u ON p.user_id = u.id
            WHERE (p.airport_id = airport_record.id OR p.airport_id IS NULL)
            AND u.role = 'PILOTE'
            ORDER BY 
                CASE WHEN p.airport_id = airport_record.id THEN 0 ELSE 1 END,
                p.id
            LIMIT 2
        LOOP
            -- Si le pilote n'a pas d'aéroport, lui assigner
            IF pilot_record.airport_id IS NULL OR pilot_record.airport_id = 0 THEN
                UPDATE pilots SET airport_id = airport_record.id WHERE id = pilot_record.id;
            END IF;
            
            -- Récupérer un avion disponible de cet aéroport
            SELECT id INTO aircraft_id_var
            FROM aircraft
            WHERE airport_id = airport_record.id
            AND (pilot_id IS NULL OR pilot_id = pilot_record.id)
            ORDER BY 
                CASE WHEN pilot_id = pilot_record.id THEN 0 ELSE 1 END,
                id
            LIMIT 1;
            
            IF aircraft_id_var IS NOT NULL THEN
                -- Assigner l'avion au pilote
                UPDATE aircraft 
                SET pilot_id = pilot_record.id
                WHERE id = aircraft_id_var;
                
                -- Mettre à jour l'aéroport du pilote
                UPDATE pilots 
                SET airport_id = airport_record.id
                WHERE id = pilot_record.id;
                
                RAISE NOTICE 'Pilote % (username: %) -> Avion % (aeroport: %)', 
                    pilot_record.id, pilot_record.username, aircraft_id_var, airport_record.code_iata;
            ELSE
                RAISE NOTICE 'AUCUN AVION DISPONIBLE pour le pilote % (username: %) a l''aeroport %', 
                    pilot_record.id, pilot_record.username, airport_record.code_iata;
            END IF;
            
            pilot_counter := pilot_counter + 1;
        END LOOP;
    END LOOP;
    
    -- ÉTAPE 3: Pour les pilotes qui n'ont toujours pas d'avion, chercher dans tous les aéroports
    FOR pilot_record IN 
        SELECT p.id, p.user_id, u.username, p.airport_id
        FROM pilots p
        JOIN users u ON p.user_id = u.id
        WHERE u.role = 'PILOTE'
        AND NOT EXISTS (
            SELECT 1 FROM aircraft WHERE pilot_id = p.id
        )
    LOOP
        -- Chercher un avion disponible dans n'importe quel aéroport
        SELECT id INTO aircraft_id_var
        FROM aircraft
        WHERE pilot_id IS NULL
        ORDER BY id
        LIMIT 1;
        
        IF aircraft_id_var IS NOT NULL THEN
            -- Récupérer l'aéroport de l'avion
            SELECT airport_id INTO airport_id_var
            FROM aircraft
            WHERE id = aircraft_id_var;
            
            -- Assigner l'avion au pilote
            UPDATE aircraft 
            SET pilot_id = pilot_record.id
            WHERE id = aircraft_id_var;
            
            -- Mettre à jour l'aéroport du pilote
            IF airport_id_var IS NOT NULL THEN
                UPDATE pilots 
                SET airport_id = airport_id_var
                WHERE id = pilot_record.id;
            END IF;
            
            RAISE NOTICE 'Pilote % (username: %) -> Avion % (assignation globale)', 
                pilot_record.id, pilot_record.username, aircraft_id_var;
        END IF;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 3: Vérification finale
-- =====================================================

SELECT '=== VÉRIFICATION FINALE ===' as info;

-- Vérifier que tous les pilotes ont un avion
SELECT 
    u.username,
    p.name as pilot_name,
    p.license,
    a.registration as aircraft_registration,
    a.model as aircraft_model,
    CASE 
        WHEN a.id IS NULL THEN 'ERREUR - PAS D''AVION'
        WHEN a.pilot_id IS NULL THEN 'ERREUR - AVION NON ASSIGNE'
        WHEN a.pilot_id != p.id THEN 'ERREUR - AVION ASSIGNE A UN AUTRE PILOTE'
        ELSE 'OK'
    END as statut
FROM users u
JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE'
ORDER BY u.username;

-- Résumé
SELECT 
    '=== RÉSUMÉ ===' as info,
    COUNT(DISTINCT u.id) as total_pilotes,
    COUNT(DISTINCT a.id) as pilotes_avec_avion,
    COUNT(DISTINCT u.id) - COUNT(DISTINCT a.id) as pilotes_sans_avion
FROM users u
JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE';

SELECT '=== TERMINÉ ===' as info;

