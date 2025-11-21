-- =====================================================
-- Script de Vérification et Correction Complète
-- Table: flights
-- =====================================================
-- Ce script vérifie et corrige TOUTES les colonnes nécessaires
-- pour que le modèle Flight.java fonctionne correctement
-- =====================================================

-- =====================================================
-- ÉTAPE 1: Vérifier la structure actuelle
-- =====================================================
SELECT '=== STRUCTURE ACTUELLE DE LA TABLE flights ===' as info;

SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'flights'
ORDER BY ordinal_position;

-- =====================================================
-- ÉTAPE 2: Ajouter la colonne 'airline' si elle n'existe pas
-- =====================================================
-- Cette colonne est utilisée dans Flight.java mais peut être absente
-- du schéma initial (schema.sql) bien qu'elle soit dans recreate_database.sql

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'airline'
    ) THEN
        ALTER TABLE flights ADD COLUMN airline VARCHAR(100);
        RAISE NOTICE 'Colonne airline ajoutée';
    ELSE
        RAISE NOTICE 'Colonne airline existe déjà';
    END IF;
END $$;

-- =====================================================
-- ÉTAPE 3: Ajouter toutes les colonnes de la migration
-- =====================================================

-- estimated_arrival
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'estimated_arrival'
    ) THEN
        ALTER TABLE flights ADD COLUMN estimated_arrival TIMESTAMP;
        RAISE NOTICE 'Colonne estimated_arrival ajoutée';
    ELSE
        RAISE NOTICE 'Colonne estimated_arrival existe déjà';
    END IF;
END $$;

-- cruise_altitude
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'cruise_altitude'
    ) THEN
        ALTER TABLE flights ADD COLUMN cruise_altitude INTEGER;
        RAISE NOTICE 'Colonne cruise_altitude ajoutée';
    ELSE
        RAISE NOTICE 'Colonne cruise_altitude existe déjà';
    END IF;
END $$;

-- cruise_speed
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'cruise_speed'
    ) THEN
        ALTER TABLE flights ADD COLUMN cruise_speed INTEGER;
        RAISE NOTICE 'Colonne cruise_speed ajoutée';
    ELSE
        RAISE NOTICE 'Colonne cruise_speed existe déjà';
    END IF;
END $$;

-- flight_type
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'flight_type'
    ) THEN
        ALTER TABLE flights ADD COLUMN flight_type VARCHAR(20) 
            CHECK (flight_type IN ('COMMERCIAL', 'CARGO', 'PRIVATE', 'MILITARY', 'TRAINING'));
        RAISE NOTICE 'Colonne flight_type ajoutée';
    ELSE
        RAISE NOTICE 'Colonne flight_type existe déjà';
    END IF;
END $$;

-- alternate_airport_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'alternate_airport_id'
    ) THEN
        ALTER TABLE flights ADD COLUMN alternate_airport_id BIGINT;
        RAISE NOTICE 'Colonne alternate_airport_id ajoutée';
    ELSE
        RAISE NOTICE 'Colonne alternate_airport_id existe déjà';
    END IF;
END $$;

-- estimated_time_enroute
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'estimated_time_enroute'
    ) THEN
        ALTER TABLE flights ADD COLUMN estimated_time_enroute INTEGER;
        RAISE NOTICE 'Colonne estimated_time_enroute ajoutée';
    ELSE
        RAISE NOTICE 'Colonne estimated_time_enroute existe déjà';
    END IF;
END $$;

-- pilot_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' AND column_name = 'pilot_id'
    ) THEN
        ALTER TABLE flights ADD COLUMN pilot_id BIGINT;
        RAISE NOTICE 'Colonne pilot_id ajoutée';
    ELSE
        RAISE NOTICE 'Colonne pilot_id existe déjà';
    END IF;
END $$;

-- =====================================================
-- ÉTAPE 4: Vérifier et corriger la longueur de flight_number
-- =====================================================
-- Le modèle Flight.java utilise length = 20 mais schema.sql utilise VARCHAR(10)
-- Il faut agrandir la colonne si nécessaire

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'flights' 
        AND column_name = 'flight_number' 
        AND character_maximum_length < 20
    ) THEN
        ALTER TABLE flights ALTER COLUMN flight_number TYPE VARCHAR(20);
        RAISE NOTICE 'Colonne flight_number agrandie à VARCHAR(20)';
    ELSE
        RAISE NOTICE 'Colonne flight_number a déjà la bonne taille';
    END IF;
END $$;

-- =====================================================
-- ÉTAPE 5: Ajouter les contraintes de clés étrangères
-- =====================================================

-- Contrainte pour alternate_airport_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_flights_alternate_airport'
    ) THEN
        ALTER TABLE flights
        ADD CONSTRAINT fk_flights_alternate_airport 
        FOREIGN KEY (alternate_airport_id) REFERENCES airports(id) ON DELETE SET NULL;
        RAISE NOTICE 'Contrainte fk_flights_alternate_airport ajoutée';
    ELSE
        RAISE NOTICE 'Contrainte fk_flights_alternate_airport existe déjà';
    END IF;
END $$;

-- Contrainte pour pilot_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_flights_pilot'
    ) THEN
        ALTER TABLE flights
        ADD CONSTRAINT fk_flights_pilot 
        FOREIGN KEY (pilot_id) REFERENCES pilots(id) ON DELETE SET NULL;
        RAISE NOTICE 'Contrainte fk_flights_pilot ajoutée';
    ELSE
        RAISE NOTICE 'Contrainte fk_flights_pilot existe déjà';
    END IF;
END $$;

-- =====================================================
-- ÉTAPE 6: Créer les index pour améliorer les performances
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_flights_pilot_id ON flights(pilot_id);
CREATE INDEX IF NOT EXISTS idx_flights_alternate_airport_id ON flights(alternate_airport_id);
CREATE INDEX IF NOT EXISTS idx_flights_flight_type ON flights(flight_type);
CREATE INDEX IF NOT EXISTS idx_flights_flight_status ON flights(flight_status);

-- =====================================================
-- ÉTAPE 7: Vérifier la contrainte CHECK sur flight_status
-- =====================================================
-- S'assurer que la contrainte CHECK inclut 'RETARDE'

DO $$
BEGIN
    -- Vérifier si la contrainte existe et si elle inclut RETARDE
    IF EXISTS (
        SELECT 1 FROM information_schema.check_constraints cc
        JOIN information_schema.constraint_column_usage ccu 
            ON cc.constraint_name = ccu.constraint_name
        WHERE ccu.table_name = 'flights' 
        AND ccu.column_name = 'flight_status'
        AND cc.check_clause NOT LIKE '%RETARDE%'
    ) THEN
        -- Supprimer l'ancienne contrainte
        ALTER TABLE flights DROP CONSTRAINT IF EXISTS flights_flight_status_check;
        -- Ajouter la nouvelle contrainte avec RETARDE
        ALTER TABLE flights ADD CONSTRAINT flights_flight_status_check 
            CHECK (flight_status IN ('PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE', 'RETARDE'));
        RAISE NOTICE 'Contrainte CHECK sur flight_status mise à jour pour inclure RETARDE';
    ELSE
        RAISE NOTICE 'Contrainte CHECK sur flight_status est correcte';
    END IF;
END $$;

-- =====================================================
-- ÉTAPE 8: Vérification finale
-- =====================================================
SELECT '=== STRUCTURE FINALE DE LA TABLE flights ===' as info;

SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'flights'
ORDER BY ordinal_position;

-- =====================================================
-- ÉTAPE 9: Vérifier les contraintes
-- =====================================================
SELECT '=== CONTRAINTES DE LA TABLE flights ===' as info;

SELECT 
    conname as constraint_name,
    contype as constraint_type,
    pg_get_constraintdef(oid) as constraint_definition
FROM pg_constraint
WHERE conrelid = 'flights'::regclass
ORDER BY conname;

-- =====================================================
-- ÉTAPE 10: Vérifier les index
-- =====================================================
SELECT '=== INDEX DE LA TABLE flights ===' as info;

SELECT 
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'flights'
ORDER BY indexname;

-- =====================================================
-- RÉSUMÉ
-- =====================================================
SELECT '=== RÉSUMÉ ===' as info;
SELECT 
    COUNT(*) as total_columns,
    COUNT(CASE WHEN column_name = 'airline' THEN 1 END) as has_airline,
    COUNT(CASE WHEN column_name = 'estimated_arrival' THEN 1 END) as has_estimated_arrival,
    COUNT(CASE WHEN column_name = 'cruise_altitude' THEN 1 END) as has_cruise_altitude,
    COUNT(CASE WHEN column_name = 'cruise_speed' THEN 1 END) as has_cruise_speed,
    COUNT(CASE WHEN column_name = 'flight_type' THEN 1 END) as has_flight_type,
    COUNT(CASE WHEN column_name = 'alternate_airport_id' THEN 1 END) as has_alternate_airport_id,
    COUNT(CASE WHEN column_name = 'estimated_time_enroute' THEN 1 END) as has_estimated_time_enroute,
    COUNT(CASE WHEN column_name = 'pilot_id' THEN 1 END) as has_pilot_id,
    COUNT(CASE WHEN column_name = 'flight_status' THEN 1 END) as has_flight_status
FROM information_schema.columns
WHERE table_name = 'flights';

