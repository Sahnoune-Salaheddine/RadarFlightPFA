-- Script de vérification complète de la base de données pour la gestion des vols
-- Exécutez ce script pour vérifier que tout est en place

-- 1. Vérifier la structure de la table flights
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'flights'
ORDER BY ordinal_position;

-- 2. Vérifier spécifiquement les colonnes critiques
SELECT 
    column_name,
    character_maximum_length,
    data_type
FROM information_schema.columns
WHERE table_name = 'flights' 
  AND column_name IN (
    'flight_number',
    'cruise_altitude',
    'cruise_speed',
    'flight_type',
    'alternate_airport_id',
    'estimated_time_enroute',
    'pilot_id',
    'flight_status'
  )
ORDER BY column_name;

-- 3. Vérifier les contraintes de clé étrangère
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_name = 'flights'
ORDER BY tc.constraint_name;

-- 4. Vérifier les index
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'flights'
ORDER BY indexname;

-- 5. Vérifier que la table activity_logs existe
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_name = 'activity_logs'
) AS activity_logs_exists;

-- 6. Vérifier les valeurs possibles pour flight_status (si contrainte CHECK existe)
SELECT
    conname AS constraint_name,
    pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint
WHERE conrelid = 'flights'::regclass
    AND contype = 'c'
    AND conname LIKE '%flight_status%';

-- 7. Vérifier les valeurs possibles pour flight_type (si contrainte CHECK existe)
SELECT
    conname AS constraint_name,
    pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint
WHERE conrelid = 'flights'::regclass
    AND contype = 'c'
    AND conname LIKE '%flight_type%';

-- 8. Compter les vols existants
SELECT COUNT(*) AS total_flights FROM flights;

-- 9. Afficher les 5 derniers vols créés
SELECT 
    id,
    flight_number,
    flight_status,
    flight_type,
    cruise_altitude,
    cruise_speed,
    pilot_id,
    created_at
FROM flights
ORDER BY created_at DESC
LIMIT 5;

