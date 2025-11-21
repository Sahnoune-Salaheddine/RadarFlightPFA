-- =====================================================
-- Script SQL - Créer et Assigner Avions aux Pilotes
-- =====================================================
-- Ce script crée les avions manquants et les assigne aux pilotes
-- selon la logique métier : 2 avions par aéroport, 1 pilote = 1 avion
-- =====================================================

-- =====================================================
-- ÉTAPE 1: Vérifier l'état actuel
-- =====================================================

SELECT '=== ÉTAT ACTUEL ===' as info;

-- Compter les avions par aéroport
SELECT 
    a.code_iata,
    COUNT(ac.id) as nombre_avions,
    COUNT(CASE WHEN ac.pilot_id IS NULL THEN 1 END) as avions_sans_pilote
FROM airports a
LEFT JOIN aircraft ac ON ac.airport_id = a.id
GROUP BY a.id, a.code_iata
ORDER BY a.code_iata;

-- =====================================================
-- ÉTAPE 2: Créer les avions manquants (2 par aéroport)
-- =====================================================

DO $$
DECLARE
    airport_record RECORD;
    aircraft_count INTEGER;
    aircraft_counter INTEGER;
    new_aircraft_id BIGINT;
BEGIN
    -- Pour chaque aéroport
    FOR airport_record IN SELECT id, code_iata FROM airports ORDER BY id
    LOOP
        -- Compter les avions existants
        SELECT COUNT(*) INTO aircraft_count
        FROM aircraft
        WHERE airport_id = airport_record.id;
        
        -- Si moins de 2 avions, créer les manquants
        IF aircraft_count < 2 THEN
            aircraft_counter := aircraft_count + 1;
            
            WHILE aircraft_counter <= 2 LOOP
                -- Créer l'avion
                INSERT INTO aircraft (
                    registration,
                    model,
                    capacity,
                    status,
                    airport_id,
                    position_lat,
                    position_lon,
                    altitude,
                    speed,
                    heading,
                    transponder_code
                )
                VALUES (
                    'CN-' || airport_record.code_iata || LPAD(aircraft_counter::TEXT, 2, '0'),
                    CASE WHEN aircraft_counter = 1 THEN 'A320' ELSE 'B737' END,
                    CASE WHEN aircraft_counter = 1 THEN 180 ELSE 150 END,
                    'AU_SOL',
                    airport_record.id,
                    (SELECT latitude FROM airports WHERE id = airport_record.id),
                    (SELECT longitude FROM airports WHERE id = airport_record.id),
                    0.0,
                    0.0,
                    0.0,
                    '1200'
                )
                RETURNING id INTO new_aircraft_id;
                
                RAISE NOTICE 'Avion % cree pour l''aeroport %', 
                    'CN-' || airport_record.code_iata || LPAD(aircraft_counter::TEXT, 2, '0'),
                    airport_record.code_iata;
                
                aircraft_counter := aircraft_counter + 1;
            END LOOP;
        END IF;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 3: Assigner les aéroports aux pilotes
-- =====================================================

DO $$
DECLARE
    pilot_record RECORD;
    airport_id_var BIGINT;
BEGIN
    -- Pour chaque pilote sans aéroport
    FOR pilot_record IN 
        SELECT p.id, p.user_id, u.username, p.airport_id
        FROM pilots p
        JOIN users u ON p.user_id = u.id
        WHERE u.role = 'PILOTE'
        AND (p.airport_id IS NULL OR p.airport_id = 0)
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
            -- Par défaut, assigner le premier aéroport
            SELECT id INTO airport_id_var FROM airports ORDER BY id LIMIT 1;
        END IF;
        
        IF airport_id_var IS NOT NULL THEN
            UPDATE pilots SET airport_id = airport_id_var WHERE id = pilot_record.id;
            RAISE NOTICE 'Aeroport assigne au pilote % (username: %)', 
                pilot_record.id, pilot_record.username;
        END IF;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 4: Assigner les avions aux pilotes
-- =====================================================

DO $$
DECLARE
    airport_record RECORD;
    pilot_record RECORD;
    aircraft_record RECORD;
    aircraft_id_var BIGINT;
    aircraft_airport_id_var BIGINT;
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
                
                RAISE NOTICE 'Pilote % (username: %) -> Avion % (aeroport: %)', 
                    pilot_record.id, pilot_record.username, aircraft_id_var, airport_record.code_iata;
            ELSE
                RAISE NOTICE 'AUCUN AVION DISPONIBLE pour le pilote % (username: %) a l''aeroport %', 
                    pilot_record.id, pilot_record.username, airport_record.code_iata;
            END IF;
            
            pilot_counter := pilot_counter + 1;
        END LOOP;
    END LOOP;
    
    -- ÉTAPE 4B: Pour les pilotes qui n'ont toujours pas d'avion
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
        SELECT id, airport_id INTO aircraft_id_var, aircraft_airport_id_var
        FROM aircraft
        WHERE pilot_id IS NULL
        ORDER BY id
        LIMIT 1;
        
        IF aircraft_id_var IS NOT NULL THEN
            -- Assigner l'avion au pilote
            UPDATE aircraft 
            SET pilot_id = pilot_record.id
            WHERE id = aircraft_id_var;
            
            -- Mettre à jour l'aéroport du pilote
            IF aircraft_airport_id_var IS NOT NULL THEN
                UPDATE pilots 
                SET airport_id = aircraft_airport_id_var
                WHERE id = pilot_record.id;
            END IF;
            
            RAISE NOTICE 'Pilote % (username: %) -> Avion % (assignation globale)', 
                pilot_record.id, pilot_record.username, aircraft_id_var;
        END IF;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 5: Vérification finale
-- =====================================================

SELECT '=== VÉRIFICATION FINALE ===' as info;

-- Vérifier les avions par aéroport
SELECT 
    a.code_iata,
    COUNT(ac.id) as nombre_avions,
    COUNT(CASE WHEN ac.pilot_id IS NOT NULL THEN 1 END) as avions_assignes
FROM airports a
LEFT JOIN aircraft ac ON ac.airport_id = a.id
GROUP BY a.id, a.code_iata
ORDER BY a.code_iata;

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

SELECT '=== TERMINÉ ===' as info;

