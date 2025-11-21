# 🔍 Diagnostic des Erreurs de Création de Vol

## ❌ Erreurs Courantes et Solutions

### 1. Erreur : "valeur trop longue pour le type character varying(10)"

**Cause :** La colonne `flight_number` est limitée à 10 caractères dans la base de données.

**Solution :**
```sql
ALTER TABLE flights ALTER COLUMN flight_number TYPE VARCHAR(20);
```

**Script :** Exécutez `FIX_FLIGHT_NUMBER_SIMPLE.ps1`

---

### 2. Erreur : "No enum constant" ou "Invalid value for enum"

**Cause :** Le script envoie des valeurs incorrectes pour les enums.

**Valeurs valides :**

#### FlightStatus (statut du vol) :
- `PLANIFIE` ✅
- `EN_COURS`
- `TERMINE`
- `ANNULE`
- `RETARDE`

❌ **Ne pas utiliser :** `SCHEDULED`, `IN_FLIGHT`, `COMPLETED`, etc.

#### FlightType (type de vol) :
- `COMMERCIAL` ✅
- `CARGO`
- `PRIVATE`
- `MILITARY`
- `TRAINING`

**Solution :** Utiliser `flightStatus = "PLANIFIE"` au lieu de `status = "SCHEDULED"`

---

### 3. Erreur : "column does not exist"

**Cause :** Les migrations SQL n'ont pas été exécutées.

**Solution :**
1. Exécutez `add_flight_fields.sql`
2. Exécutez `add_activity_logs_table.sql`
3. Redémarrez le backend

**Script :** `EXECUTER_MIGRATIONS_SIMPLE.ps1`

---

### 4. Erreur : "foreign key constraint" ou "violates foreign key"

**Cause :** Les IDs fournis (avion, aéroport, pilote) n'existent pas dans la base.

**Solution :**
- Vérifiez que les IDs existent :
  ```sql
  SELECT id FROM aircraft LIMIT 5;
  SELECT id FROM airports LIMIT 5;
  SELECT id FROM pilots LIMIT 5;
  ```
- Utilisez des IDs valides dans le script de test

---

### 5. Erreur : "duplicate key value" ou "flight_number already exists"

**Cause :** Un vol avec le même numéro existe déjà.

**Solution :** Le script génère maintenant des numéros uniques (`TEST` + 4 chiffres aléatoires)

---

### 6. Erreur : "null value in column violates not-null constraint"

**Cause :** Un champ obligatoire n'est pas fourni.

**Champs obligatoires :**
- `flightNumber` ✅
- `aircraftId` ✅
- `departureAirportId` ✅
- `arrivalAirportId` ✅
- `flightStatus` ✅ (défaut : `PLANIFIE`)

**Champs optionnels :**
- `pilotId`
- `alternateAirportId`
- `cruiseAltitude`
- `cruiseSpeed`
- `flightType` (défaut : `COMMERCIAL`)

---

## ✅ Checklist Avant de Tester

- [ ] Backend Spring Boot démarré
- [ ] Migrations SQL exécutées (`add_flight_fields.sql`)
- [ ] Colonne `flight_number` modifiée en VARCHAR(20)
- [ ] Table `activity_logs` créée
- [ ] Identifiants ADMIN corrects dans le script
- [ ] IDs valides (avion, aéroports) existent dans la base

---

## 🧪 Test Complet

```powershell
# 1. Corriger la longueur de flight_number
powershell -ExecutionPolicy Bypass -File FIX_FLIGHT_NUMBER_SIMPLE.ps1

# 2. Redémarrer le backend
# (Ctrl+C puis mvn spring-boot:run)

# 3. Lancer le test
powershell -ExecutionPolicy Bypass -File TEST_CREATION_VOL.ps1
```

---

## 📋 Format des Données Attendues

```json
{
  "flightNumber": "TEST1234",
  "airline": "Test Airlines",
  "aircraftId": 1,
  "departureAirportId": 1,
  "arrivalAirportId": 2,
  "scheduledDeparture": "2025-11-20T17:38:20",
  "scheduledArrival": "2025-11-20T19:38:20",
  "cruiseAltitude": 35000,
  "cruiseSpeed": 450,
  "flightType": "COMMERCIAL",
  "flightStatus": "PLANIFIE",
  "pilotId": null  // optionnel
}
```

---

## 🔍 Vérification dans la Base de Données

```sql
-- Vérifier la structure de la table
\d flights

-- Vérifier les colonnes
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'flights'
ORDER BY ordinal_position;

-- Vérifier les vols créés
SELECT id, flight_number, flight_status, flight_type, cruise_altitude, cruise_speed
FROM flights
ORDER BY created_at DESC
LIMIT 5;
```

