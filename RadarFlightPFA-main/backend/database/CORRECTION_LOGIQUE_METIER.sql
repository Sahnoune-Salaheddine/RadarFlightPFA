-- =====================================================
-- CORRECTION - Logique Métier
-- =====================================================
-- Ce script corrige les données pour respecter la logique métier :
-- - Exactement 2 avions par aéroport
-- - Chaque avion assigné à un pilote
-- - Nettoyage des avions en trop
-- =====================================================

-- =====================================================
-- ÉTAPE 1: Nettoyer les assignations incorrectes
-- =====================================================

-- Désassigner tous les avions des pilotes
UPDATE aircraft SET pilot_id = NULL;

-- =====================================================
-- ÉTAPE 2: Supprimer les avions en trop (garder seulement 2 par aéroport)
-- =====================================================

DO $$
DECLARE
    airport_record RECORD;
    aircraft_to_keep RECORD;
    aircraft_count INTEGER;
    aircraft_ids_to_delete BIGINT[];
BEGIN
    -- Pour chaque aéroport
    FOR airport_record IN SELECT id, code_iata FROM airports ORDER BY id
    LOOP
        -- Compter les avions de cet aéroport
        SELECT COUNT(*) INTO aircraft_count
        FROM aircraft
        WHERE airport_id = airport_record.id;
        
        -- Si plus de 2 avions, supprimer les en trop
        IF aircraft_count > 2 THEN
            -- Garder les 2 premiers avions (par ID)
            -- Supprimer les autres
            DELETE FROM aircraft
            WHERE airport_id = airport_record.id
            AND id NOT IN (
                SELECT id FROM aircraft
                WHERE airport_id = airport_record.id
                ORDER BY id
                LIMIT 2
            );
            
            RAISE NOTICE 'Aeroport % : % avions supprimes', 
                airport_record.code_iata, aircraft_count - 2;
        END IF;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 3: S'assurer qu'il y a exactement 2 avions par aéroport
-- =====================================================

DO $$
DECLARE
    airport_record RECORD;
    aircraft_count INTEGER;
    aircraft_counter INTEGER;
BEGIN
    -- Pour chaque aéroport
    FOR airport_record IN SELECT id, code_iata FROM airports ORDER BY id
    LOOP
        -- Compter les avions
        SELECT COUNT(*) INTO aircraft_count
        FROM aircraft
        WHERE airport_id = airport_record.id;
        
        -- Si moins de 2 avions, créer les manquants
        IF aircraft_count < 2 THEN
            aircraft_counter := aircraft_count + 1;
            
            WHILE aircraft_counter <= 2 LOOP
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
                ON CONFLICT (registration) DO NOTHING;
                
                aircraft_counter := aircraft_counter + 1;
            END LOOP;
            
            RAISE NOTICE 'Aeroport % : % avions crees', 
                airport_record.code_iata, 2 - aircraft_count;
        END IF;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 4: Assigner les pilotes aux avions (1 pilote = 1 avion)
-- =====================================================

DO $$
DECLARE
    airport_record RECORD;
    aircraft_record RECORD;
    pilot_id_var BIGINT;
    pilot_counter INTEGER;
BEGIN
    -- Pour chaque aéroport
    FOR airport_record IN SELECT id, code_iata FROM airports ORDER BY id
    LOOP
        pilot_counter := 1;
        
        -- Récupérer les 2 avions de cet aéroport (par ordre d'ID)
        FOR aircraft_record IN 
            SELECT id FROM aircraft 
            WHERE airport_id = airport_record.id 
            ORDER BY id 
            LIMIT 2
        LOOP
            -- Trouver ou créer le pilote correspondant
            SELECT id INTO pilot_id_var
            FROM pilots
            WHERE license = airport_record.code_iata || 'P' || pilot_counter
            LIMIT 1;
            
            -- Si le pilote n'existe pas, le créer
            IF pilot_id_var IS NULL THEN
                INSERT INTO pilots (
                    name,
                    license,
                    experience_years,
                    first_name,
                    last_name,
                    airport_id
                )
                VALUES (
                    'Pilote ' || airport_record.code_iata || ' ' || pilot_counter,
                    airport_record.code_iata || 'P' || pilot_counter,
                    5 + pilot_counter,
                    'Prenom',
                    'Nom ' || pilot_counter,
                    airport_record.id
                )
                RETURNING id INTO pilot_id_var;
            END IF;
            
            -- Assigner l'avion au pilote
            UPDATE aircraft 
            SET pilot_id = pilot_id_var
            WHERE id = aircraft_record.id;
            
            -- Mettre à jour l'aéroport du pilote si nécessaire
            UPDATE pilots 
            SET airport_id = airport_record.id
            WHERE id = pilot_id_var;
            
            pilot_counter := pilot_counter + 1;
        END LOOP;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 5: Vérification finale
-- =====================================================

SELECT '=== VERIFICATION FINALE ===' as info;

-- Compter les avions par aéroport (devrait être 2 pour chacun)
SELECT 
    a.code_iata,
    COUNT(ac.id) as nombre_avions,
    CASE 
        WHEN COUNT(ac.id) = 2 THEN 'OK'
        ELSE 'ERREUR'
    END as statut
FROM airports a
LEFT JOIN aircraft ac ON ac.airport_id = a.id
GROUP BY a.id, a.code_iata
ORDER BY a.code_iata;

-- Compter les pilotes par aéroport (devrait être 2 pour chacun)
SELECT 
    a.code_iata,
    COUNT(p.id) as nombre_pilotes,
    CASE 
        WHEN COUNT(p.id) = 2 THEN 'OK'
        ELSE 'ERREUR'
    END as statut
FROM airports a
LEFT JOIN pilots p ON p.airport_id = a.id
GROUP BY a.id, a.code_iata
ORDER BY a.code_iata;

-- Vérifier l'assignation avion-pilote (tous les avions doivent avoir un pilote)
SELECT 
    a.code_iata as aeroport,
    ac.registration as avion,
    p.name as pilote,
    p.license as licence,
    CASE 
        WHEN p.id IS NOT NULL THEN 'OK'
        ELSE 'ERREUR - Pas de pilote'
    END as statut
FROM airports a
JOIN aircraft ac ON ac.airport_id = a.id
LEFT JOIN pilots p ON p.id = ac.pilot_id
ORDER BY a.code_iata, ac.registration;

-- Résumé global
SELECT 
    'Total aeroports: ' || COUNT(DISTINCT a.id) as resume,
    'Total avions: ' || COUNT(DISTINCT ac.id) as total_avions,
    'Total pilotes: ' || COUNT(DISTINCT p.id) as total_pilotes,
    'Avions avec pilote: ' || COUNT(DISTINCT CASE WHEN ac.pilot_id IS NOT NULL THEN ac.id END) as avions_assignes
FROM airports a
LEFT JOIN aircraft ac ON ac.airport_id = a.id
LEFT JOIN pilots p ON p.airport_id = a.id;

SELECT '=== CORRECTION TERMINEE ===' as info;

