-- =====================================================
-- Migration: Ajout de champs au modèle Flight
-- =====================================================

-- Ajouter les nouveaux champs à la table flights
ALTER TABLE flights 
ADD COLUMN IF NOT EXISTS estimated_arrival TIMESTAMP,
ADD COLUMN IF NOT EXISTS cruise_altitude INTEGER,
ADD COLUMN IF NOT EXISTS cruise_speed INTEGER,
ADD COLUMN IF NOT EXISTS flight_type VARCHAR(20) CHECK (flight_type IN ('COMMERCIAL', 'CARGO', 'PRIVATE', 'MILITARY', 'TRAINING')),
ADD COLUMN IF NOT EXISTS alternate_airport_id BIGINT,
ADD COLUMN IF NOT EXISTS estimated_time_enroute INTEGER,
ADD COLUMN IF NOT EXISTS pilot_id BIGINT;

-- Ajouter la contrainte de clé étrangère pour alternate_airport_id (si elle n'existe pas)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_flights_alternate_airport'
    ) THEN
        ALTER TABLE flights
        ADD CONSTRAINT fk_flights_alternate_airport 
        FOREIGN KEY (alternate_airport_id) REFERENCES airports(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Ajouter la contrainte de clé étrangère pour pilot_id (si elle n'existe pas)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_flights_pilot'
    ) THEN
        ALTER TABLE flights
        ADD CONSTRAINT fk_flights_pilot 
        FOREIGN KEY (pilot_id) REFERENCES pilots(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_flights_pilot_id ON flights(pilot_id);
CREATE INDEX IF NOT EXISTS idx_flights_alternate_airport_id ON flights(alternate_airport_id);
CREATE INDEX IF NOT EXISTS idx_flights_flight_type ON flights(flight_type);

COMMENT ON COLUMN flights.estimated_arrival IS 'Heure d''arrivée estimée (ETA)';
COMMENT ON COLUMN flights.cruise_altitude IS 'Altitude de croisière en pieds';
COMMENT ON COLUMN flights.cruise_speed IS 'Vitesse de croisière en nœuds';
COMMENT ON COLUMN flights.flight_type IS 'Type de vol (COMMERCIAL, CARGO, PRIVATE, MILITARY, TRAINING)';
COMMENT ON COLUMN flights.alternate_airport_id IS 'Aéroport alternatif (optionnel)';
COMMENT ON COLUMN flights.estimated_time_enroute IS 'Temps estimé en route en minutes';
COMMENT ON COLUMN flights.pilot_id IS 'Pilote assigné directement au vol';

