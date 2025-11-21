-- =====================================================
-- Script SQL - Vérifier et Nettoyer les Doublons de Pilotes
-- =====================================================
-- Ce script vérifie et nettoie les doublons de pilotes
-- en gardant le premier pilote pour chaque user_id
-- =====================================================

-- =====================================================
-- ÉTAPE 1: Vérifier les doublons
-- =====================================================

SELECT '=== VÉRIFICATION DES DOUBLONS ===' as info;

SELECT 
    u.id as user_id,
    u.username,
    COUNT(p.id) as nombre_pilotes
FROM users u
JOIN pilots p ON p.user_id = u.id
WHERE u.role = 'PILOTE'
GROUP BY u.id, u.username
HAVING COUNT(p.id) > 1
ORDER BY u.username;

-- =====================================================
-- ÉTAPE 2: Afficher tous les doublons
-- =====================================================

SELECT 
    '=== DOUBLONS DÉTECTÉS ===' as info;

SELECT 
    p.id as pilot_id,
    p.user_id,
    u.username,
    p.name as pilot_name,
    p.license,
    p.airport_id,
    a.registration as aircraft_registration
FROM pilots p
JOIN users u ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE'
AND p.user_id IN (
    SELECT user_id
    FROM pilots
    GROUP BY user_id
    HAVING COUNT(*) > 1
)
ORDER BY p.user_id, p.id;

-- =====================================================
-- ÉTAPE 3: Supprimer les doublons (garder le premier)
-- =====================================================

DO $$
DECLARE
    user_record RECORD;
    pilot_to_keep_id BIGINT;
    pilots_to_delete_ids BIGINT[];
BEGIN
    SELECT '=== NETTOYAGE DES DOUBLONS ===' as info;
    
    -- Pour chaque user_id avec des doublons
    FOR user_record IN 
        SELECT user_id, COUNT(*) as count
        FROM pilots
        GROUP BY user_id
        HAVING COUNT(*) > 1
    LOOP
        -- Garder le premier pilote (celui avec le plus petit ID)
        SELECT id INTO pilot_to_keep_id
        FROM pilots
        WHERE user_id = user_record.user_id
        ORDER BY id
        LIMIT 1;
        
        -- Désassigner les avions des autres pilotes
        UPDATE aircraft 
        SET pilot_id = NULL
        WHERE pilot_id IN (
            SELECT id FROM pilots 
            WHERE user_id = user_record.user_id 
            AND id != pilot_to_keep_id
        );
        
        -- Supprimer les autres pilotes
        DELETE FROM pilots
        WHERE user_id = user_record.user_id
        AND id != pilot_to_keep_id;
        
        RAISE NOTICE 'Doublons supprimes pour user_id % (pilote garde: %)', 
            user_record.user_id, pilot_to_keep_id;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 4: Ajouter la contrainte unique si elle n'existe pas
-- =====================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'pilots_user_id_unique'
    ) THEN
        ALTER TABLE pilots
        ADD CONSTRAINT pilots_user_id_unique UNIQUE (user_id);
        RAISE NOTICE 'Contrainte unique ajoutee sur user_id';
    ELSE
        RAISE NOTICE 'La contrainte unique existe deja';
    END IF;
END $$;

-- =====================================================
-- ÉTAPE 5: Vérification finale
-- =====================================================

SELECT '=== VÉRIFICATION FINALE ===' as info;

SELECT 
    u.id as user_id,
    u.username,
    COUNT(p.id) as nombre_pilotes,
    CASE 
        WHEN COUNT(p.id) = 0 THEN 'ERREUR - PAS DE PILOTE'
        WHEN COUNT(p.id) > 1 THEN 'ERREUR - DOUBLONS'
        ELSE 'OK'
    END as statut
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
WHERE u.role = 'PILOTE'
GROUP BY u.id, u.username
ORDER BY u.username;

SELECT '=== TERMINÉ ===' as info;

