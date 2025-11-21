-- =====================================================
-- Script SQL - Corriger la Relation Pilot ↔ Aircraft
-- =====================================================
-- Ce script :
-- 1. Nettoie les doublons (plusieurs avions pour 1 pilote)
-- 2. Nettoie les doublons (plusieurs pilotes pour 1 avion)
-- 3. Ajoute une contrainte unique sur pilot_id dans aircraft
-- 4. Vérifie que la relation est correcte (1 pilote = 1 avion)
-- =====================================================

-- =====================================================
-- ÉTAPE 1: Vérifier l'état actuel
-- =====================================================

SELECT '=== ÉTAT ACTUEL ===' as info;

-- Vérifier les pilotes avec plusieurs avions
SELECT 
    p.id as pilot_id,
    u.username,
    COUNT(a.id) as nombre_avions
FROM pilots p
JOIN users u ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE'
GROUP BY p.id, u.username
HAVING COUNT(a.id) > 1
ORDER BY u.username;

-- Vérifier les avions avec plusieurs pilotes (ne devrait pas exister)
SELECT 
    a.id as aircraft_id,
    a.registration,
    COUNT(DISTINCT a.pilot_id) as nombre_pilotes
FROM aircraft a
WHERE a.pilot_id IS NOT NULL
GROUP BY a.id, a.registration
HAVING COUNT(DISTINCT a.pilot_id) > 1;

-- =====================================================
-- ÉTAPE 2: Nettoyer les doublons (plusieurs avions pour 1 pilote)
-- =====================================================

DO $$
DECLARE
    pilot_record RECORD;
    aircraft_to_keep_id BIGINT;
BEGIN
    -- Pour chaque pilote avec plusieurs avions
    FOR pilot_record IN 
        SELECT p.id, u.username, COUNT(a.id) as count
        FROM pilots p
        JOIN users u ON p.user_id = u.id
        JOIN aircraft a ON a.pilot_id = p.id
        WHERE u.role = 'PILOTE'
        GROUP BY p.id, u.username
        HAVING COUNT(a.id) > 1
    LOOP
        -- Garder le premier avion (celui avec le plus petit ID)
        SELECT id INTO aircraft_to_keep_id
        FROM aircraft
        WHERE pilot_id = pilot_record.id
        ORDER BY id
        LIMIT 1;
        
        -- Désassigner les autres avions
        UPDATE aircraft 
        SET pilot_id = NULL
        WHERE pilot_id = pilot_record.id
        AND id != aircraft_to_keep_id;
        
        RAISE NOTICE 'Pilote % (username: %) - Avion garde: %, autres desassignes', 
            pilot_record.id, pilot_record.username, aircraft_to_keep_id;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 3: Nettoyer les doublons (plusieurs pilotes pour 1 avion)
-- =====================================================

-- Cette situation ne devrait pas exister car pilot_id est une colonne simple
-- Mais on vérifie quand même et on garde le premier pilote
DO $$
DECLARE
    aircraft_record RECORD;
    pilot_to_keep_id BIGINT;
BEGIN
    -- Trouver les avions qui ont un pilot_id qui correspond à plusieurs pilotes
    -- (Cela ne devrait pas arriver, mais on vérifie)
    FOR aircraft_record IN 
        SELECT a.id, a.registration, a.pilot_id
        FROM aircraft a
        WHERE a.pilot_id IS NOT NULL
        AND NOT EXISTS (
            SELECT 1 FROM pilots p WHERE p.id = a.pilot_id
        )
    LOOP
        -- Si le pilot_id ne correspond à aucun pilote, désassigner l'avion
        UPDATE aircraft 
        SET pilot_id = NULL
        WHERE id = aircraft_record.id;
        
        RAISE NOTICE 'Avion % (registration: %) - pilot_id % invalide, desassigne', 
            aircraft_record.id, aircraft_record.registration, aircraft_record.pilot_id;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 4: Ajouter la contrainte unique sur pilot_id
-- =====================================================

DO $$
BEGIN
    -- Supprimer la contrainte si elle existe déjà
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'aircraft_pilot_id_unique'
    ) THEN
        ALTER TABLE aircraft DROP CONSTRAINT aircraft_pilot_id_unique;
        RAISE NOTICE 'Ancienne contrainte unique supprimee';
    END IF;
    
    -- Ajouter la contrainte unique sur pilot_id
    ALTER TABLE aircraft
    ADD CONSTRAINT aircraft_pilot_id_unique UNIQUE (pilot_id);
    
    RAISE NOTICE 'Contrainte unique ajoutee sur pilot_id dans aircraft';
END $$;

-- =====================================================
-- ÉTAPE 5: Vérification finale
-- =====================================================

SELECT '=== VÉRIFICATION FINALE ===' as info;

-- Vérifier que tous les pilotes ont exactement 1 avion
SELECT 
    u.username,
    p.id as pilot_id,
    a.id as aircraft_id,
    a.registration,
    CASE 
        WHEN a.id IS NULL THEN 'ERREUR - PAS D''AVION'
        WHEN COUNT(*) OVER (PARTITION BY p.id) > 1 THEN 'ERREUR - PLUSIEURS AVIONS'
        ELSE 'OK'
    END as statut
FROM users u
JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE'
ORDER BY u.username;

-- Résumé
WITH pilot_aircraft_counts AS (
    SELECT 
        p.id as pilot_id,
        COUNT(a.id) as aircraft_count
    FROM users u
    JOIN pilots p ON p.user_id = u.id
    LEFT JOIN aircraft a ON a.pilot_id = p.id
    WHERE u.role = 'PILOTE'
    GROUP BY p.id
)
SELECT 
    '=== RÉSUMÉ ===' as info,
    (SELECT COUNT(*) FROM users WHERE role = 'PILOTE') as total_pilotes,
    (SELECT COUNT(*) FROM pilot_aircraft_counts WHERE aircraft_count = 1) as pilotes_avec_avion,
    (SELECT COUNT(*) FROM pilot_aircraft_counts WHERE aircraft_count = 0) as pilotes_sans_avion,
    (SELECT COUNT(*) FROM pilot_aircraft_counts WHERE aircraft_count > 1) as pilotes_avec_doublons;

-- Vérifier la contrainte unique
SELECT 
    '=== VÉRIFICATION CONTRAINTE UNIQUE ===' as info,
    conname as nom_contrainte,
    contype as type_contrainte
FROM pg_constraint
WHERE conrelid = 'aircraft'::regclass
AND conname = 'aircraft_pilot_id_unique';

SELECT '=== TERMINÉ ===' as info;

