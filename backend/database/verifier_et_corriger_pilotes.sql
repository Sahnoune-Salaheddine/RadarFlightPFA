-- Script pour vérifier et corriger la liaison Pilote ⇄ Avion
-- Exécuter ce script dans PostgreSQL pour vérifier/corriger les données

-- =====================================================
-- 1. VÉRIFIER LES DONNÉES EXISTANTES
-- =====================================================

-- Vérifier les utilisateurs pilotes
SELECT id, username, role FROM users WHERE role = 'PILOTE';

-- Vérifier les pilotes
SELECT p.id, p.name, p.license, p.user_id, u.username 
FROM pilots p 
LEFT JOIN users u ON p.user_id = u.id;

-- Vérifier les avions et leurs pilotes
SELECT a.id, a.registration, a.model, a.status, a.pilot_id, p.name as pilot_name, u.username
FROM aircraft a
LEFT JOIN pilots p ON a.pilot_id = p.id
LEFT JOIN users u ON p.user_id = u.id;

-- =====================================================
-- 2. CORRIGER LA LIAISON PILOTE ⇄ AVION
-- =====================================================

-- Si le pilote pilote_cmn1 existe mais n'a pas d'avion assigné
-- Assigner le premier avion disponible à pilote_cmn1

-- Étape 1 : Trouver l'ID du pilote pour pilote_cmn1
DO $$
DECLARE
    v_pilot_id INTEGER;
    v_aircraft_id INTEGER;
    v_user_id INTEGER;
BEGIN
    -- Trouver l'ID de l'utilisateur pilote_cmn1
    SELECT id INTO v_user_id FROM users WHERE username = 'pilote_cmn1';
    
    IF v_user_id IS NULL THEN
        RAISE NOTICE 'Utilisateur pilote_cmn1 non trouvé';
    ELSE
        -- Trouver l'ID du pilote
        SELECT id INTO v_pilot_id FROM pilots WHERE user_id = v_user_id;
        
        IF v_pilot_id IS NULL THEN
            RAISE NOTICE 'Pilote non trouvé pour pilote_cmn1';
        ELSE
            -- Trouver un avion sans pilote ou réassigner
            SELECT id INTO v_aircraft_id 
            FROM aircraft 
            WHERE pilot_id IS NULL OR pilot_id = v_pilot_id
            LIMIT 1;
            
            IF v_aircraft_id IS NULL THEN
                -- Créer un nouvel avion si aucun n'existe
                INSERT INTO aircraft (registration, model, status, airport_id, position_lat, position_lon, altitude, speed, heading, last_update, pilot_id)
                SELECT 
                    'CN-AT01',
                    'A320',
                    'AU_SOL',
                    (SELECT id FROM airports WHERE code_iata = 'CMN' LIMIT 1),
                    (SELECT latitude FROM airports WHERE code_iata = 'CMN' LIMIT 1),
                    (SELECT longitude FROM airports WHERE code_iata = 'CMN' LIMIT 1),
                    0.0,
                    0.0,
                    0.0,
                    NOW(),
                    v_pilot_id
                RETURNING id INTO v_aircraft_id;
                
                RAISE NOTICE 'Nouvel avion créé avec ID: %', v_aircraft_id;
            ELSE
                -- Assigner le pilote à l'avion
                UPDATE aircraft 
                SET pilot_id = v_pilot_id,
                    username_pilote = 'pilote_cmn1'
                WHERE id = v_aircraft_id;
                
                RAISE NOTICE 'Avion ID % assigné au pilote ID %', v_aircraft_id, v_pilot_id;
            END IF;
        END IF;
    END IF;
END $$;

-- =====================================================
-- 3. VÉRIFIER APRÈS CORRECTION
-- =====================================================

-- Vérifier que pilote_cmn1 a maintenant un avion
SELECT 
    u.username,
    p.name as pilot_name,
    a.registration,
    a.model,
    a.status
FROM users u
JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.username = 'pilote_cmn1';

-- =====================================================
-- 4. CRÉER UN VOL POUR L'AVION (OPTIONNEL)
-- =====================================================

-- Créer un vol pour l'avion du pilote_cmn1
DO $$
DECLARE
    v_aircraft_id INTEGER;
    v_departure_airport_id INTEGER;
    v_arrival_airport_id INTEGER;
BEGIN
    -- Trouver l'avion du pilote_cmn1
    SELECT a.id, a.airport_id INTO v_aircraft_id, v_departure_airport_id
    FROM aircraft a
    JOIN pilots p ON a.pilot_id = p.id
    JOIN users u ON p.user_id = u.id
    WHERE u.username = 'pilote_cmn1'
    LIMIT 1;
    
    IF v_aircraft_id IS NOT NULL THEN
        -- Trouver un aéroport de destination (Rabat)
        SELECT id INTO v_arrival_airport_id 
        FROM airports 
        WHERE code_iata = 'RBA' 
        LIMIT 1;
        
        IF v_arrival_airport_id IS NOT NULL THEN
            -- Créer un vol
            INSERT INTO flights (flight_number, aircraft_id, departure_airport_id, arrival_airport_id, flight_status, scheduled_departure, scheduled_arrival, created_at)
            VALUES (
                'AT1001',
                v_aircraft_id,
                v_departure_airport_id,
                v_arrival_airport_id,
                'PLANIFIE',
                NOW() + INTERVAL '1 hour',
                NOW() + INTERVAL '2 hours',
                NOW()
            )
            ON CONFLICT (flight_number) DO NOTHING;
            
            RAISE NOTICE 'Vol créé pour l''avion ID %', v_aircraft_id;
        END IF;
    END IF;
END $$;

-- =====================================================
-- 5. VÉRIFICATION FINALE
-- =====================================================

-- Vérifier toutes les données pour pilote_cmn1
SELECT 
    u.username,
    u.role,
    p.name as pilot_name,
    p.license,
    a.id as aircraft_id,
    a.registration,
    a.model,
    a.status as aircraft_status,
    a.username_pilote,
    f.flight_number,
    f.flight_status,
    dep.name as departure_airport,
    arr.name as arrival_airport
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
LEFT JOIN flights f ON f.aircraft_id = a.id AND f.flight_status != 'TERMINE'
LEFT JOIN airports dep ON f.departure_airport_id = dep.id
LEFT JOIN airports arr ON f.arrival_airport_id = arr.id
WHERE u.username = 'pilote_cmn1';

