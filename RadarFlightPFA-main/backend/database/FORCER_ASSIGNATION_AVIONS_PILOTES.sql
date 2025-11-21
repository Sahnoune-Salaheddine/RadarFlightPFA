-- =====================================================
-- Script SQL - Forcer l'Assignation Avions aux Pilotes
-- =====================================================
-- Ce script force l'assignation correcte des avions aux pilotes
-- en désassignant d'abord tous les avions, puis en réassignant
-- =====================================================

-- =====================================================
-- ÉTAPE 1: État actuel des avions
-- =====================================================

SELECT '=== ÉTAT ACTUEL DES AVIONS ===' as info;

SELECT 
    a.id,
    a.registration,
    a.model,
    a.airport_id,
    ap.code_iata as aeroport,
    a.pilot_id,
    p.name as pilote_name,
    u.username as pilote_username
FROM aircraft a
LEFT JOIN airports ap ON ap.id = a.airport_id
LEFT JOIN pilots p ON p.id = a.pilot_id
LEFT JOIN users u ON u.id = p.user_id
ORDER BY a.airport_id, a.id;

-- =====================================================
-- ÉTAPE 2: Désassigner tous les avions
-- =====================================================

UPDATE aircraft SET pilot_id = NULL;

SELECT '=== TOUS LES AVIONS DÉSASSIGNÉS ===' as info;

-- =====================================================
-- ÉTAPE 3: Assigner les aéroports aux pilotes
-- =====================================================

DO $$
DECLARE
    pilot_record RECORD;
    airport_id_var BIGINT;
BEGIN
    FOR pilot_record IN 
        SELECT p.id, p.user_id, u.username, p.airport_id
        FROM pilots p
        JOIN users u ON p.user_id = u.id
        WHERE u.role = 'PILOTE'
    LOOP
        -- Extraire le code aéroport du username
        IF pilot_record.username LIKE 'pilote_cmn%' THEN
            SELECT id INTO airport_id_var FROM airports WHERE code_iata = 'CMN';
        ELSIF pilot_record.username LIKE 'pilote_rba%' THEN
            SELECT id INTO airport_id_var FROM airports WHERE code_iata = 'RBA';
        ELSIF pilot_record.username LIKE 'pilote_rak%' THEN
            SELECT id INTO airport_id_var FROM airports WHERE code_iata = 'RAK';
        ELSIF pilot_record.username LIKE 'pilote_tng%' THEN
            SELECT id INTO airport_id_var FROM airports WHERE code_iata = 'TNG';
        ELSE
            SELECT id INTO airport_id_var FROM airports ORDER BY id LIMIT 1;
        END IF;
        
        IF airport_id_var IS NOT NULL THEN
            UPDATE pilots SET airport_id = airport_id_var WHERE id = pilot_record.id;
            RAISE NOTICE 'Aeroport % assigne au pilote % (username: %)', 
                airport_id_var, pilot_record.id, pilot_record.username;
        END IF;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 4: Assigner les avions aux pilotes (1 pilote = 1 avion)
-- =====================================================

DO $$
DECLARE
    airport_record RECORD;
    pilot_record RECORD;
    aircraft_record RECORD;
    aircraft_id_var BIGINT;
    pilot_counter INTEGER;
BEGIN
    -- Pour chaque aéroport
    FOR airport_record IN SELECT id, code_iata FROM airports ORDER BY id
    LOOP
        pilot_counter := 1;
        
        -- Récupérer les 2 premiers pilotes de cet aéroport
        FOR pilot_record IN 
            SELECT p.id, p.user_id, u.username
            FROM pilots p
            JOIN users u ON p.user_id = u.id
            WHERE p.airport_id = airport_record.id
            AND u.role = 'PILOTE'
            ORDER BY p.id
            LIMIT 2
        LOOP
            -- Récupérer le premier avion disponible de cet aéroport (sans pilote)
            SELECT id INTO aircraft_id_var
            FROM aircraft
            WHERE airport_id = airport_record.id
            AND pilot_id IS NULL
            ORDER BY id
            LIMIT 1;
            
            IF aircraft_id_var IS NOT NULL THEN
                -- Assigner l'avion au pilote
                UPDATE aircraft 
                SET pilot_id = pilot_record.id
                WHERE id = aircraft_id_var;
                
                RAISE NOTICE 'Pilote % (username: %) -> Avion % (aeroport: %)', 
                    pilot_record.id, pilot_record.username, aircraft_id_var, airport_record.code_iata;
            ELSE
                RAISE NOTICE 'AUCUN AVION DISPONIBLE pour le pilote % (username: %) a l''aeroport %', 
                    pilot_record.id, pilot_record.username, airport_record.code_iata;
            END IF;
            
            pilot_counter := pilot_counter + 1;
        END LOOP;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 5: Vérification finale
-- =====================================================

SELECT '=== VÉRIFICATION FINALE ===' as info;

-- Vérifier que tous les pilotes ont un avion
SELECT 
    u.username,
    p.name as pilot_name,
    p.license,
    a.registration as aircraft_registration,
    a.model as aircraft_model,
    ap.code_iata as aeroport,
    CASE 
        WHEN a.id IS NULL THEN 'ERREUR - PAS D''AVION'
        WHEN a.pilot_id IS NULL THEN 'ERREUR - AVION NON ASSIGNE'
        WHEN a.pilot_id != p.id THEN 'ERREUR - AVION ASSIGNE A UN AUTRE PILOTE'
        ELSE 'OK'
    END as statut
FROM users u
JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
LEFT JOIN airports ap ON ap.id = p.airport_id
WHERE u.role = 'PILOTE'
ORDER BY u.username;

-- Résumé
SELECT 
    '=== RÉSUMÉ ===' as info,
    COUNT(DISTINCT u.id) as total_pilotes,
    COUNT(DISTINCT a.id) as pilotes_avec_avion,
    COUNT(DISTINCT CASE WHEN a.id IS NULL THEN u.id END) as pilotes_sans_avion
FROM users u
JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE';

-- Détail des assignations
SELECT 
    '=== DÉTAIL DES ASSIGNATIONS ===' as info;

SELECT 
    u.username as pilote,
    a.registration as avion,
    a.model as modele,
    ap.code_iata as aeroport
FROM users u
JOIN pilots p ON p.user_id = u.id
JOIN aircraft a ON a.pilot_id = p.id
JOIN airports ap ON ap.id = a.airport_id
WHERE u.role = 'PILOTE'
ORDER BY u.username;

SELECT '=== TERMINÉ ===' as info;

