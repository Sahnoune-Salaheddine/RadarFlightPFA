-- Script pour augmenter la longueur de la colonne flight_number
-- De VARCHAR(10) à VARCHAR(20) pour permettre des numéros de vol plus longs

ALTER TABLE flights 
ALTER COLUMN flight_number TYPE VARCHAR(20);

-- Vérification
SELECT column_name, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'flights' 
  AND column_name = 'flight_number';

