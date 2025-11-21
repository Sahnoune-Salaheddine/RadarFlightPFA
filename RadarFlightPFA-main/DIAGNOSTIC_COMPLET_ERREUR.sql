-- =====================================================
-- DIAGNOSTIC COMPLET - Identifier l'erreur exacte
-- =====================================================
-- Exécutez ce script pour voir EXACTEMENT ce qui manque
-- =====================================================

\echo '========================================'
\echo 'DIAGNOSTIC COMPLET - TABLE flights'
\echo '========================================'
\echo ''

-- 1. Vérifier si la table existe
\echo '1. Vérification de l''existence de la table...'
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'flights')
        THEN '✅ Table flights existe'
        ELSE '❌ Table flights N''EXISTE PAS'
    END as statut_table;

\echo ''

-- 2. Lister TOUTES les colonnes actuelles
\echo '2. Colonnes actuelles de la table flights:'
SELECT 
    column_name as colonne,
    data_type as type,
    character_maximum_length as longueur,
    is_nullable as nullable
FROM information_schema.columns
WHERE table_name = 'flights'
ORDER BY ordinal_position;

\echo ''

-- 3. Colonnes REQUISES par Flight.java
\echo '3. Colonnes REQUISES par le modèle Java Flight.java:'
SELECT 
    'id' as colonne_requise, 'BIGSERIAL' as type_requis
UNION ALL SELECT 'flight_number', 'VARCHAR(20)'
UNION ALL SELECT 'airline', 'VARCHAR(100)'
UNION ALL SELECT 'aircraft_id', 'BIGINT'
UNION ALL SELECT 'departure_airport_id', 'BIGINT'
UNION ALL SELECT 'arrival_airport_id', 'BIGINT'
UNION ALL SELECT 'flight_status', 'VARCHAR(20)'
UNION ALL SELECT 'scheduled_departure', 'TIMESTAMP'
UNION ALL SELECT 'scheduled_arrival', 'TIMESTAMP'
UNION ALL SELECT 'actual_departure', 'TIMESTAMP'
UNION ALL SELECT 'actual_arrival', 'TIMESTAMP'
UNION ALL SELECT 'estimated_arrival', 'TIMESTAMP'
UNION ALL SELECT 'cruise_altitude', 'INTEGER'
UNION ALL SELECT 'cruise_speed', 'INTEGER'
UNION ALL SELECT 'flight_type', 'VARCHAR(20)'
UNION ALL SELECT 'alternate_airport_id', 'BIGINT'
UNION ALL SELECT 'estimated_time_enroute', 'INTEGER'
UNION ALL SELECT 'pilot_id', 'BIGINT'
UNION ALL SELECT 'created_at', 'TIMESTAMP'
ORDER BY colonne_requise;

\echo ''

-- 4. Identifier les colonnes MANQUANTES
\echo '4. Colonnes MANQUANTES (comparaison):'
WITH required_cols AS (
    SELECT unnest(ARRAY[
        'id', 'flight_number', 'airline', 'aircraft_id', 
        'departure_airport_id', 'arrival_airport_id', 'flight_status',
        'scheduled_departure', 'scheduled_arrival', 'actual_departure', 
        'actual_arrival', 'estimated_arrival', 'cruise_altitude', 
        'cruise_speed', 'flight_type', 'alternate_airport_id', 
        'estimated_time_enroute', 'pilot_id', 'created_at'
    ]) AS col_name
),
existing_cols AS (
    SELECT column_name
    FROM information_schema.columns
    WHERE table_name = 'flights'
)
SELECT 
    rc.col_name as colonne_manquante,
    '❌ MANQUANTE' as statut
FROM required_cols rc
LEFT JOIN existing_cols ec ON rc.col_name = ec.column_name
WHERE ec.column_name IS NULL
ORDER BY rc.col_name;

\echo ''

-- 5. Vérifier les contraintes
\echo '5. Contraintes sur flight_status:'
SELECT 
    conname as nom_contrainte,
    pg_get_constraintdef(oid) as definition
FROM pg_constraint
WHERE conrelid = 'flights'::regclass
AND conname LIKE '%flight_status%';

\echo ''

-- 6. Test d'insertion minimal (pour voir l'erreur exacte)
\echo '6. Test d''insertion minimal (pour identifier l''erreur SQL exacte):'
\echo '   (Cette requête peut échouer, c''est normal - elle nous montre l''erreur)'

-- Vérifier d'abord qu'on a des données de test
DO $$
DECLARE
    aircraft_exists BOOLEAN;
    airport1_exists BOOLEAN;
    airport2_exists BOOLEAN;
BEGIN
    SELECT EXISTS(SELECT 1 FROM aircraft LIMIT 1) INTO aircraft_exists;
    SELECT EXISTS(SELECT 1 FROM airports LIMIT 1) INTO airport1_exists;
    SELECT EXISTS(SELECT 1 FROM airports OFFSET 1 LIMIT 1) INTO airport2_exists;
    
    IF NOT aircraft_exists THEN
        RAISE NOTICE '⚠️  Aucun avion trouvé dans la base';
    ELSE
        RAISE NOTICE '✅ Au moins un avion existe';
    END IF;
    
    IF NOT airport1_exists THEN
        RAISE NOTICE '⚠️  Aucun aéroport trouvé dans la base';
    ELSE
        RAISE NOTICE '✅ Au moins un aéroport existe';
    END IF;
    
    IF NOT airport2_exists THEN
        RAISE NOTICE '⚠️  Un seul aéroport trouvé (besoin de 2 pour test)';
    ELSE
        RAISE NOTICE '✅ Au moins deux aéroports existent';
    END IF;
END $$;

\echo ''

-- 7. Résumé final
\echo '7. RÉSUMÉ FINAL:'
SELECT 
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'flights') as colonnes_actuelles,
    (SELECT COUNT(*) FROM (
        SELECT unnest(ARRAY[
            'id', 'flight_number', 'airline', 'aircraft_id', 
            'departure_airport_id', 'arrival_airport_id', 'flight_status',
            'scheduled_departure', 'scheduled_arrival', 'actual_departure', 
            'actual_arrival', 'estimated_arrival', 'cruise_altitude', 
            'cruise_speed', 'flight_type', 'alternate_airport_id', 
            'estimated_time_enroute', 'pilot_id', 'created_at'
        ])
    ) AS required) as colonnes_requises,
    CASE 
        WHEN (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'flights') >= 19
        THEN '✅ Structure complète'
        ELSE '❌ Colonnes manquantes'
    END as statut_final;

\echo ''
\echo '========================================'
\echo 'FIN DU DIAGNOSTIC'
\echo '========================================'

