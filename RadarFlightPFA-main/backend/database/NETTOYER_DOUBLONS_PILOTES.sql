-- =====================================================
-- Script SQL - Nettoyer les Doublons de Pilotes
-- =====================================================
-- Ce script identifie et supprime les doublons de pilotes
-- qui ont le meme user_id
-- =====================================================

-- Afficher l'état actuel
SELECT '=== ETAT ACTUEL ===' as info;

SELECT 
    user_id,
    COUNT(*) as nombre_pilotes,
    STRING_AGG(id::TEXT, ', ') as pilot_ids
FROM pilots
WHERE user_id IS NOT NULL
GROUP BY user_id
HAVING COUNT(*) > 1;

-- =====================================================
-- ÉTAPE 1: Identifier les doublons
-- =====================================================
SELECT '=== DOUBLONS IDENTIFIES ===' as info;

SELECT 
    p.id,
    p.name,
    p.license,
    p.user_id,
    u.username
FROM pilots p
JOIN users u ON p.user_id = u.id
WHERE p.user_id IN (
    SELECT user_id
    FROM pilots
    WHERE user_id IS NOT NULL
    GROUP BY user_id
    HAVING COUNT(*) > 1
)
ORDER BY p.user_id, p.id;

-- =====================================================
-- ÉTAPE 2: Garder le premier pilote et supprimer les doublons
-- =====================================================
DO $$
DECLARE
    duplicate_record RECORD;
    first_pilot_id BIGINT;
BEGIN
    -- Pour chaque user_id avec des doublons
    FOR duplicate_record IN 
        SELECT user_id, MIN(id) as first_id
        FROM pilots
        WHERE user_id IS NOT NULL
        GROUP BY user_id
        HAVING COUNT(*) > 1
    LOOP
        first_pilot_id := duplicate_record.first_id;
        
        -- Supprimer tous les pilotes sauf le premier
        DELETE FROM pilots
        WHERE user_id = duplicate_record.user_id
        AND id != first_pilot_id;
        
        RAISE NOTICE 'Doublons supprimes pour user_id %, pilote % conserve', 
            duplicate_record.user_id, first_pilot_id;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 3: Vérifier qu'il n'y a plus de doublons
-- =====================================================
SELECT '=== VERIFICATION FINALE ===' as info;

SELECT 
    user_id,
    COUNT(*) as nombre_pilotes
FROM pilots
WHERE user_id IS NOT NULL
GROUP BY user_id
HAVING COUNT(*) > 1;

-- Si cette requête ne retourne aucun résultat, c'est bon !

-- Afficher tous les pilotes restants
SELECT 
    p.id,
    p.name,
    p.license,
    u.username,
    COUNT(a.id) as nombre_avions
FROM pilots p
LEFT JOIN users u ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
GROUP BY p.id, p.name, p.license, u.username
ORDER BY u.username;

-- =====================================================
-- ÉTAPE 4: Ajouter une contrainte unique sur user_id
-- =====================================================
DO $$
BEGIN
    -- Supprimer la contrainte si elle existe déjà
    ALTER TABLE pilots DROP CONSTRAINT IF EXISTS pilots_user_id_unique;
    
    -- Ajouter la contrainte unique
    ALTER TABLE pilots ADD CONSTRAINT pilots_user_id_unique UNIQUE (user_id);
    
    RAISE NOTICE 'Contrainte unique ajoutee sur user_id';
EXCEPTION
    WHEN duplicate_object THEN
        RAISE NOTICE 'Contrainte unique deja presente';
END $$;

SELECT '=== NETTOYAGE TERMINE ===' as info;

