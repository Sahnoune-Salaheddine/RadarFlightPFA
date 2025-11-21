-- Script de diagnostic detaille de la table flights
-- Executez ce script pour voir exactement l'etat de la base de données

-- 1. Structure complete de la table flights
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    numeric_precision,
    numeric_scale,
    is_nullable,
    column_default,
    ordinal_position
FROM information_schema.columns
WHERE table_name = 'flights'
ORDER BY ordinal_position;

-- 2. Verifier toutes les colonnes requises par le code Java
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'flights' AND column_name = 'flight_number'
        ) THEN 'OK'
        ELSE 'MANQUANT'
    END AS flight_number,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'flights' AND column_name = 'cruise_altitude'
        ) THEN 'OK'
        ELSE 'MANQUANT'
    END AS cruise_altitude,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'flights' AND column_name = 'cruise_speed'
        ) THEN 'OK'
        ELSE 'MANQUANT'
    END AS cruise_speed,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'flights' AND column_name = 'flight_type'
        ) THEN 'OK'
        ELSE 'MANQUANT'
    END AS flight_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'flights' AND column_name = 'alternate_airport_id'
        ) THEN 'OK'
        ELSE 'MANQUANT'
    END AS alternate_airport_id,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'flights' AND column_name = 'estimated_time_enroute'
        ) THEN 'OK'
        ELSE 'MANQUANT'
    END AS estimated_time_enroute,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'flights' AND column_name = 'pilot_id'
        ) THEN 'OK'
        ELSE 'MANQUANT'
    END AS pilot_id,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'flights' AND column_name = 'flight_status'
        ) THEN 'OK'
        ELSE 'MANQUANT'
    END AS flight_status;

-- 3. Verifier les contraintes CHECK pour flight_type
SELECT
    conname AS constraint_name,
    pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint
WHERE conrelid = 'flights'::regclass
    AND contype = 'c'
    AND (conname LIKE '%flight_type%' OR conname LIKE '%flight_status%');

-- 4. Verifier les contraintes de cles etrangeres
SELECT
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_name = 'flights'
ORDER BY tc.constraint_name;

-- 5. Verifier la longueur exacte de flight_number
SELECT 
    column_name,
    character_maximum_length,
    CASE 
        WHEN character_maximum_length >= 20 THEN 'OK (>= 20)'
        ELSE 'ERREUR (< 20)'
    END AS status
FROM information_schema.columns
WHERE table_name = 'flights' 
  AND column_name = 'flight_number';

-- 6. Verifier que la table activity_logs existe et a la bonne structure
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'activity_logs'
ORDER BY ordinal_position;

-- 7. Tester une insertion (ne sera pas commitée)
BEGIN;
-- Cette requête va échouer si les colonnes n'existent pas
SELECT 
    'flight_number' AS test_column,
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'flight_number'
    ) THEN 'EXISTE' ELSE 'MANQUANT' END AS status
UNION ALL
SELECT 'cruise_altitude', 
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'cruise_altitude'
    ) THEN 'EXISTE' ELSE 'MANQUANT' END
UNION ALL
SELECT 'cruise_speed',
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'cruise_speed'
    ) THEN 'EXISTE' ELSE 'MANQUANT' END
UNION ALL
SELECT 'flight_type',
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'flight_type'
    ) THEN 'EXISTE' ELSE 'MANQUANT' END
UNION ALL
SELECT 'alternate_airport_id',
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'alternate_airport_id'
    ) THEN 'EXISTE' ELSE 'MANQUANT' END
UNION ALL
SELECT 'estimated_time_enroute',
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'estimated_time_enroute'
    ) THEN 'EXISTE' ELSE 'MANQUANT' END
UNION ALL
SELECT 'pilot_id',
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'pilot_id'
    ) THEN 'EXISTE' ELSE 'MANQUANT' END;
ROLLBACK;

