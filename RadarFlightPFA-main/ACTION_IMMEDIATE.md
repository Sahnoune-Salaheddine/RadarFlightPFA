# 🚨 ACTION IMMÉDIATE - Corriger l'erreur de création de vol

## ❌ ERREUR ACTUELLE

```
Erreur de base de données. Vérifiez que les colonnes existent (exécutez les scripts de migration SQL).
Status: 400
Type: RUNTIME_ERROR
```

## ✅ SOLUTION RAPIDE (2 minutes)

### ÉTAPE 1 : Exécuter le script de correction

**Option A : Script PowerShell automatique**
```powershell
.\CORRIGER_FLIGHTS_MAINTENANT.ps1
```

**Option B : Manuellement via psql**
```powershell
psql -U postgres -d flightradar -f backend\database\CORRIGER_FLIGHTS_FORCE.sql
```

**Option C : Via pgAdmin**
1. Ouvrir pgAdmin
2. Se connecter à PostgreSQL
3. Clic droit sur la base `flightradar` → Query Tool
4. Ouvrir le fichier `backend\database\CORRIGER_FLIGHTS_FORCE.sql`
5. Exécuter (F5)

### ÉTAPE 2 : Vérifier que les colonnes existent

```powershell
psql -U postgres -d flightradar -f backend\database\VERIFIER_COLONNES_FLIGHTS.sql
```

**Vous devriez voir 19 colonnes au total, incluant :**
- ✅ `airline`
- ✅ `estimated_arrival`
- ✅ `cruise_altitude`
- ✅ `cruise_speed`
- ✅ `flight_type`
- ✅ `alternate_airport_id`
- ✅ `estimated_time_enroute`
- ✅ `pilot_id`

### ÉTAPE 3 : Redémarrer le backend Spring Boot

**Important :** Le backend DOIT être redémarré après la modification de la base de données.

```bash
# Arrêter le backend (Ctrl+C)
# Puis redémarrer :
cd backend
mvn spring-boot:run
```

### ÉTAPE 4 : Tester la création d'un vol

1. Rafraîchir le frontend (F5)
2. Se connecter en tant qu'admin
3. Aller dans "Gestion des Vols"
4. Cliquer sur "+ Nouveau Vol"
5. Remplir le formulaire avec tous les champs :
   
   **Champs obligatoires (*) :**
   - Numéro de vol / Callsign * : `TEST001`
   - Compagnie aérienne * : `Royal Air Maroc`
   - Avion * : Sélectionner un avion
   - Aéroport de départ * : Sélectionner un aéroport
   - Aéroport d'arrivée * : Sélectionner un aéroport
   - STD (Heure départ prévue) * : Date/heure future
   - STA (Heure arrivée prévue) * : Date/heure future (après STD)
   - Type de vol * : Commercial
   
   **Champs optionnels :**
   - Pilote assigné : (optionnel)
   - Aéroport alternatif : (optionnel)
   - Altitude de croisière (pieds) : ex. `35000` (optionnel)
   - Vitesse de croisière (nœuds) : ex. `450` (optionnel)
   - Statut initial : Planifié ou Retardé (par défaut : Planifié)
   
6. Cliquer sur "Créer"

---

## 🔍 SI L'ERREUR PERSISTE

### 1. Vérifier les logs du backend

Dans la console Spring Boot, cherchez les lignes qui commencent par :
- `❌ ERREUR`
- `Erreur lors de la sauvegarde`
- `Transaction silently rolled back`

**Copiez l'erreur SQL complète** (elle indiquera la colonne exacte qui manque).

### 2. Vérifier la structure exacte de la table

```sql
-- Se connecter à PostgreSQL
psql -U postgres -d flightradar

-- Voir la structure
\d flights

-- Ou avec une requête
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'flights' 
ORDER BY ordinal_position;
```

### 3. Tester une insertion SQL directe

```sql
-- Vérifier que les données existent
SELECT id FROM aircraft LIMIT 1;
SELECT id FROM airports LIMIT 2;

-- Tester l'insertion
INSERT INTO flights (
    flight_number, airline, aircraft_id, 
    departure_airport_id, arrival_airport_id, 
    flight_status, flight_type
) VALUES (
    'TEST001', 'Royal Air Maroc', 1, 1, 2, 'PLANIFIE', 'COMMERCIAL'
);
```

**Si cette insertion échoue**, l'erreur SQL vous dira exactement ce qui manque.

### 4. Vérifier les contraintes CHECK

```sql
-- Vérifier la contrainte sur flight_status
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conrelid = 'flights'::regclass 
AND conname LIKE '%flight_status%';

-- Si RETARDE n'est pas dans la liste, exécuter :
ALTER TABLE flights DROP CONSTRAINT IF EXISTS flights_flight_status_check;
ALTER TABLE flights ADD CONSTRAINT flights_flight_status_check 
    CHECK (flight_status IN ('PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE', 'RETARDE'));
```

---

## 📋 CHECKLIST RAPIDE

- [ ] Script `CORRIGER_FLIGHTS_FORCE.sql` exécuté
- [ ] Vérification des colonnes effectuée (19 colonnes au total)
- [ ] Backend Spring Boot redémarré
- [ ] Frontend rafraîchi
- [ ] Test de création de vol effectué

---

## 🆘 EN CAS D'URGENCE

Si rien ne fonctionne, exécutez ce script SQL qui **FORCE** tout :

```sql
-- Supprimer toutes les contraintes
ALTER TABLE flights DROP CONSTRAINT IF EXISTS flights_flight_status_check;
ALTER TABLE flights DROP CONSTRAINT IF EXISTS flights_flight_type_check;
ALTER TABLE flights DROP CONSTRAINT IF EXISTS fk_flights_alternate_airport;
ALTER TABLE flights DROP CONSTRAINT IF EXISTS fk_flights_pilot;

-- Ajouter TOUTES les colonnes (sans erreur si elles existent)
ALTER TABLE flights ADD COLUMN IF NOT EXISTS airline VARCHAR(100);
ALTER TABLE flights ADD COLUMN IF NOT EXISTS estimated_arrival TIMESTAMP;
ALTER TABLE flights ADD COLUMN IF NOT EXISTS cruise_altitude INTEGER;
ALTER TABLE flights ADD COLUMN IF NOT EXISTS cruise_speed INTEGER;
ALTER TABLE flights ADD COLUMN IF NOT EXISTS flight_type VARCHAR(20);
ALTER TABLE flights ADD COLUMN IF NOT EXISTS alternate_airport_id BIGINT;
ALTER TABLE flights ADD COLUMN IF NOT EXISTS estimated_time_enroute INTEGER;
ALTER TABLE flights ADD COLUMN IF NOT EXISTS pilot_id BIGINT;

-- Corriger la longueur de flight_number
ALTER TABLE flights ALTER COLUMN flight_number TYPE VARCHAR(20);

-- Recréer les contraintes
ALTER TABLE flights ADD CONSTRAINT flights_flight_status_check 
    CHECK (flight_status IN ('PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE', 'RETARDE'));
ALTER TABLE flights ADD CONSTRAINT flights_flight_type_check 
    CHECK (flight_type IS NULL OR flight_type IN ('COMMERCIAL', 'CARGO', 'PRIVATE', 'MILITARY', 'TRAINING'));
ALTER TABLE flights ADD CONSTRAINT fk_flights_alternate_airport 
    FOREIGN KEY (alternate_airport_id) REFERENCES airports(id) ON DELETE SET NULL;
ALTER TABLE flights ADD CONSTRAINT fk_flights_pilot 
    FOREIGN KEY (pilot_id) REFERENCES pilots(id) ON DELETE SET NULL;
```

---

**Date :** 2025-01-XX
**Statut :** ✅ Scripts prêts, action requise

