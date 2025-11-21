-- =====================================================
-- Vérification et Correction des Doublons
-- Table: radar_centers
-- =====================================================
-- Ce script vérifie s'il y a plusieurs centres radar
-- pour le même aéroport et propose une solution
-- =====================================================

-- 1. Vérifier les doublons
SELECT '=== DOUBLONS DANS radar_centers ===' as info;

SELECT 
    airport_id,
    COUNT(*) as nombre_centres,
    STRING_AGG(id::text, ', ') as ids_centres,
    STRING_AGG(name, ', ') as noms_centres
FROM radar_centers
WHERE airport_id IS NOT NULL
GROUP BY airport_id
HAVING COUNT(*) > 1;

-- 2. Voir tous les centres radar
SELECT '=== TOUS LES CENTRES RADAR ===' as info;

SELECT 
    id,
    name,
    code,
    airport_id,
    (SELECT name FROM airports WHERE id = radar_centers.airport_id) as nom_aeroport
FROM radar_centers
ORDER BY airport_id, id;

-- 3. Solution : Garder seulement le premier centre radar par aéroport
-- (Décommentez si vous voulez supprimer les doublons)

-- DO $$
-- DECLARE
--     duplicate_record RECORD;
-- BEGIN
--     FOR duplicate_record IN 
--         SELECT airport_id, MIN(id) as keep_id
--         FROM radar_centers
--         WHERE airport_id IS NOT NULL
--         GROUP BY airport_id
--         HAVING COUNT(*) > 1
--     LOOP
--         -- Supprimer les doublons sauf le premier
--         DELETE FROM radar_centers
--         WHERE airport_id = duplicate_record.airport_id
--         AND id != duplicate_record.keep_id;
--         
--         RAISE NOTICE 'Doublons supprimés pour aéroport_id: %, gardé: %', 
--             duplicate_record.airport_id, duplicate_record.keep_id;
--     END LOOP;
-- END $$;

-- 4. Vérification après correction (si appliquée)
SELECT '=== VÉRIFICATION FINALE ===' as info;

SELECT 
    airport_id,
    COUNT(*) as nombre_centres
FROM radar_centers
WHERE airport_id IS NOT NULL
GROUP BY airport_id
HAVING COUNT(*) > 1;

-- Si cette requête retourne 0 lignes, il n'y a plus de doublons

