-- =====================================================
-- Script de Vérification Rapide - Table flights
-- =====================================================
-- Exécutez ce script pour voir exactement quelles colonnes manquent
-- =====================================================

SELECT '=== COLONNES ACTUELLES ===' as info;

SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'flights'
ORDER BY ordinal_position;

SELECT '' as separator;

SELECT '=== COLONNES MANQUANTES ===' as info;

-- Liste des colonnes attendues
WITH expected_columns AS (
    SELECT unnest(ARRAY[
        'id', 'flight_number', 'airline', 'aircraft_id', 
        'departure_airport_id', 'arrival_airport_id', 'flight_status',
        'scheduled_departure', 'scheduled_arrival', 'actual_departure', 
        'actual_arrival', 'estimated_arrival', 'cruise_altitude', 
        'cruise_speed', 'flight_type', 'alternate_airport_id', 
        'estimated_time_enroute', 'pilot_id', 'created_at'
    ]) AS col_name
),
existing_columns AS (
    SELECT column_name
    FROM information_schema.columns
    WHERE table_name = 'flights'
)
SELECT ec.col_name as colonne_manquante
FROM expected_columns ec
LEFT JOIN existing_columns ex ON ec.col_name = ex.column_name
WHERE ex.column_name IS NULL
ORDER BY ec.col_name;

SELECT '' as separator;

SELECT '=== RÉSUMÉ ===' as info;

SELECT 
    COUNT(*) as total_colonnes,
    COUNT(CASE WHEN column_name = 'airline' THEN 1 END) as a_airline,
    COUNT(CASE WHEN column_name = 'estimated_arrival' THEN 1 END) as a_estimated_arrival,
    COUNT(CASE WHEN column_name = 'cruise_altitude' THEN 1 END) as a_cruise_altitude,
    COUNT(CASE WHEN column_name = 'cruise_speed' THEN 1 END) as a_cruise_speed,
    COUNT(CASE WHEN column_name = 'flight_type' THEN 1 END) as a_flight_type,
    COUNT(CASE WHEN column_name = 'alternate_airport_id' THEN 1 END) as a_alternate_airport_id,
    COUNT(CASE WHEN column_name = 'estimated_time_enroute' THEN 1 END) as a_estimated_time_enroute,
    COUNT(CASE WHEN column_name = 'pilot_id' THEN 1 END) as a_pilot_id
FROM information_schema.columns
WHERE table_name = 'flights';

