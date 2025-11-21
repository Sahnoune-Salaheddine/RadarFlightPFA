-- =====================================================
-- Script de vérification des migrations
-- Exécutez ce script pour vérifier que les migrations ont été appliquées
-- =====================================================

-- Vérifier les colonnes de la table flights
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'flights' 
  AND column_name IN (
    'cruise_altitude', 
    'cruise_speed', 
    'flight_type', 
    'alternate_airport_id', 
    'estimated_time_enroute', 
    'pilot_id'
  )
ORDER BY column_name;

-- Vérifier les contraintes
SELECT 
    conname AS constraint_name,
    contype AS constraint_type,
    pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint
WHERE conrelid = 'flights'::regclass
  AND conname IN ('fk_flights_alternate_airport', 'fk_flights_pilot');

-- Vérifier la table activity_logs
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT FROM information_schema.tables 
            WHERE table_name = 'activity_logs'
        ) THEN '✅ Table activity_logs existe'
        ELSE '❌ Table activity_logs n''existe PAS'
    END AS status_activity_logs;

-- Compter les colonnes manquantes
SELECT 
    COUNT(*) AS colonnes_manquantes
FROM (
    SELECT 'cruise_altitude' AS col
    UNION SELECT 'cruise_speed'
    UNION SELECT 'flight_type'
    UNION SELECT 'alternate_airport_id'
    UNION SELECT 'estimated_time_enroute'
    UNION SELECT 'pilot_id'
) AS required_cols
WHERE NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'flights' 
    AND column_name = required_cols.col
);

