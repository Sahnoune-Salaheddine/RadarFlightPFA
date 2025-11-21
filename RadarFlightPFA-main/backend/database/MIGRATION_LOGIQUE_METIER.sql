-- =====================================================
-- MIGRATION - Logique Métier Complète
-- =====================================================
-- Ce script ajoute les colonnes manquantes et initialise
-- les données selon la logique métier :
-- - 4 aéroports
-- - 8 avions (2 par aéroport)
-- - 8 pilotes (1 par avion, 2 par aéroport)
-- - 4 radars (1 par aéroport)
-- =====================================================

-- =====================================================
-- ÉTAPE 1: Ajouter les colonnes manquantes
-- =====================================================

-- Aéroports : Ajouter code_icao et country
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'airports' AND column_name = 'code_icao'
    ) THEN
        ALTER TABLE airports ADD COLUMN code_icao VARCHAR(4) UNIQUE;
        RAISE NOTICE 'Colonne code_icao ajoutee a airports';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'airports' AND column_name = 'country'
    ) THEN
        ALTER TABLE airports ADD COLUMN country VARCHAR(100);
        RAISE NOTICE 'Colonne country ajoutee a airports';
    END IF;
END $$;

-- Avions : Ajouter capacity
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'aircraft' AND column_name = 'capacity'
    ) THEN
        ALTER TABLE aircraft ADD COLUMN capacity INTEGER NOT NULL DEFAULT 150;
        RAISE NOTICE 'Colonne capacity ajoutee a aircraft';
    END IF;
END $$;

-- Pilotes : Ajouter airport_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'pilots' AND column_name = 'airport_id'
    ) THEN
        ALTER TABLE pilots ADD COLUMN airport_id BIGINT;
        ALTER TABLE pilots ADD CONSTRAINT fk_pilots_airport 
            FOREIGN KEY (airport_id) REFERENCES airports(id) ON DELETE SET NULL;
        RAISE NOTICE 'Colonne airport_id ajoutee a pilots';
    END IF;
END $$;

-- Radar : Ajouter status et range
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'radar_centers' AND column_name = 'status'
    ) THEN
        ALTER TABLE radar_centers ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIF';
        RAISE NOTICE 'Colonne status ajoutee a radar_centers';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'radar_centers' AND column_name = 'range'
    ) THEN
        ALTER TABLE radar_centers ADD COLUMN range DECIMAL(8,2) NOT NULL DEFAULT 200.0;
        RAISE NOTICE 'Colonne range ajoutee a radar_centers';
    END IF;
END $$;

-- =====================================================
-- ÉTAPE 2: Mettre à jour les aéroports existants
-- =====================================================

-- Mettre à jour les codes ICAO et pays pour les aéroports existants
UPDATE airports SET code_icao = 'GMMN', country = 'Maroc' WHERE code_iata = 'CMN';
UPDATE airports SET code_icao = 'GMME', country = 'Maroc' WHERE code_iata = 'RBA';
UPDATE airports SET code_icao = 'GMMX', country = 'Maroc' WHERE code_iata = 'RAK';
UPDATE airports SET code_icao = 'GMTT', country = 'Maroc' WHERE code_iata = 'TNG';

-- S'assurer qu'il y a exactement 4 aéroports
DO $$
DECLARE
    airport_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO airport_count FROM airports;
    
    IF airport_count < 4 THEN
        -- Créer les aéroports manquants
        INSERT INTO airports (name, city, code_iata, code_icao, country, latitude, longitude)
        VALUES 
            ('Aéroport Mohammed V', 'Casablanca', 'CMN', 'GMMN', 'Maroc', 33.367500, -7.589800),
            ('Aéroport Rabat-Salé', 'Rabat', 'RBA', 'GMME', 'Maroc', 34.051500, -6.751500),
            ('Aéroport Marrakech-Ménara', 'Marrakech', 'RAK', 'GMMX', 'Maroc', 31.606900, -8.036300),
            ('Aéroport Tanger-Ibn Battouta', 'Tanger', 'TNG', 'GMTT', 'Maroc', 35.726900, -5.916900)
        ON CONFLICT (code_iata) DO NOTHING;
        
        RAISE NOTICE 'Aeroports crees ou mis a jour';
    END IF;
END $$;

-- =====================================================
-- ÉTAPE 3: Créer/Mettre à jour les avions (2 par aéroport)
-- =====================================================

DO $$
DECLARE
    airport_record RECORD;
    aircraft_counter INTEGER;
    pilot_record RECORD;
BEGIN
    -- Pour chaque aéroport
    FOR airport_record IN SELECT id, code_iata FROM airports ORDER BY id
    LOOP
        aircraft_counter := 1;
        
        -- Créer 2 avions par aéroport
        FOR i IN 1..2 LOOP
            -- Vérifier si l'avion existe déjà
            IF NOT EXISTS (
                SELECT 1 FROM aircraft 
                WHERE registration = 'CN-' || airport_record.code_iata || LPAD(aircraft_counter::TEXT, 2, '0')
            ) THEN
                INSERT INTO aircraft (
                    registration,
                    model,
                    capacity,
                    status,
                    airport_id,
                    position_lat,
                    position_lon,
                    altitude,
                    speed,
                    heading,
                    transponder_code
                )
                VALUES (
                    'CN-' || airport_record.code_iata || LPAD(aircraft_counter::TEXT, 2, '0'),
                    CASE WHEN aircraft_counter = 1 THEN 'A320' ELSE 'B737' END,
                    CASE WHEN aircraft_counter = 1 THEN 180 ELSE 150 END,
                    'AU_SOL',
                    airport_record.id,
                    (SELECT latitude FROM airports WHERE id = airport_record.id),
                    (SELECT longitude FROM airports WHERE id = airport_record.id),
                    0.0,
                    0.0,
                    0.0,
                    '1200'
                );
            END IF;
            
            aircraft_counter := aircraft_counter + 1;
        END LOOP;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 4: Créer/Mettre à jour les pilotes (2 par aéroport, 1 par avion)
-- =====================================================

DO $$
DECLARE
    airport_record RECORD;
    aircraft_record RECORD;
    pilot_counter INTEGER;
    pilot_id_var BIGINT;
BEGIN
    -- Pour chaque aéroport
    FOR airport_record IN SELECT id, code_iata FROM airports ORDER BY id
    LOOP
        pilot_counter := 1;
        
        -- Récupérer les 2 avions de cet aéroport
        FOR aircraft_record IN 
            SELECT id FROM aircraft 
            WHERE airport_id = airport_record.id 
            ORDER BY id 
            LIMIT 2
        LOOP
            -- Vérifier si le pilote existe déjà
            SELECT id INTO pilot_id_var
            FROM pilots
            WHERE license = airport_record.code_iata || 'P' || pilot_counter
            LIMIT 1;
            
            IF pilot_id_var IS NULL THEN
                -- Créer le pilote
                INSERT INTO pilots (
                    name,
                    license,
                    experience_years,
                    first_name,
                    last_name,
                    airport_id
                )
                VALUES (
                    'Pilote ' || airport_record.code_iata || ' ' || pilot_counter,
                    airport_record.code_iata || 'P' || pilot_counter,
                    5 + pilot_counter,
                    'Prenom',
                    'Nom ' || pilot_counter,
                    airport_record.id
                )
                RETURNING id INTO pilot_id_var;
            ELSE
                -- Mettre à jour l'aéroport du pilote
                UPDATE pilots SET airport_id = airport_record.id WHERE id = pilot_id_var;
            END IF;
            
            -- Assigner l'avion au pilote
            UPDATE aircraft 
            SET pilot_id = pilot_id_var
            WHERE id = aircraft_record.id;
            
            pilot_counter := pilot_counter + 1;
        END LOOP;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 5: Créer/Mettre à jour les radars (1 par aéroport)
-- =====================================================

DO $$
DECLARE
    airport_record RECORD;
    radar_id_var BIGINT;
BEGIN
    -- Pour chaque aéroport
    FOR airport_record IN SELECT id, code_iata FROM airports ORDER BY id
    LOOP
        -- Vérifier si le radar existe déjà
        SELECT id INTO radar_id_var
        FROM radar_centers
        WHERE airport_id = airport_record.id
        LIMIT 1;
        
        IF radar_id_var IS NULL THEN
            -- Créer le radar
            INSERT INTO radar_centers (
                name,
                code,
                frequency,
                status,
                range,
                airport_id
            )
            VALUES (
                'Centre Radar ' || airport_record.code_iata,
                airport_record.code_iata || '_RADAR',
                121.5 + (SELECT id FROM airports WHERE id = airport_record.id),
                'ACTIF',
                200.0,
                airport_record.id
            );
        ELSE
            -- Mettre à jour le statut et la portée si nécessaire
            UPDATE radar_centers 
            SET status = COALESCE(status, 'ACTIF'),
                range = COALESCE(range, 200.0)
            WHERE id = radar_id_var;
        END IF;
    END LOOP;
END $$;

-- =====================================================
-- ÉTAPE 6: Vérification finale
-- =====================================================

SELECT '=== VERIFICATION FINALE ===' as info;

-- Compter les aéroports
SELECT 'Aeroports: ' || COUNT(*) as total FROM airports;

-- Compter les avions par aéroport
SELECT 
    a.code_iata,
    COUNT(ac.id) as nombre_avions
FROM airports a
LEFT JOIN aircraft ac ON ac.airport_id = a.id
GROUP BY a.id, a.code_iata
ORDER BY a.code_iata;

-- Compter les pilotes par aéroport
SELECT 
    a.code_iata,
    COUNT(p.id) as nombre_pilotes
FROM airports a
LEFT JOIN pilots p ON p.airport_id = a.id
GROUP BY a.id, a.code_iata
ORDER BY a.code_iata;

-- Vérifier les radars
SELECT 
    a.code_iata,
    rc.name as radar_name,
    rc.status as radar_status
FROM airports a
LEFT JOIN radar_centers rc ON rc.airport_id = a.id
ORDER BY a.code_iata;

-- Vérifier l'assignation avion-pilote
SELECT 
    a.code_iata as aeroport,
    ac.registration as avion,
    p.name as pilote,
    p.license as licence
FROM airports a
JOIN aircraft ac ON ac.airport_id = a.id
LEFT JOIN pilots p ON p.id = ac.pilot_id
ORDER BY a.code_iata, ac.registration;

SELECT '=== MIGRATION TERMINEE ===' as info;

