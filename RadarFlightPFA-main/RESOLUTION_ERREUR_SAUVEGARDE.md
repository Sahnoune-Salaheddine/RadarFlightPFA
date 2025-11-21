# 🚨 RÉSOLUTION IMMÉDIATE - Erreur lors de la sauvegarde

## ❌ ERREUR ACTUELLE

```
Erreur lors de la sauvegarde
Status: 400
Type: RUNTIME_ERROR
```

## 🔍 ÉTAPE 1 : DIAGNOSTIQUER L'ERREUR EXACTE

### Option A : Script PowerShell (Recommandé)

```powershell
.\EXECUTER_DIAGNOSTIC.ps1
```

Ce script va vous montrer :
- ✅ Quelles colonnes existent
- ❌ Quelles colonnes manquent
- ⚠️ Les contraintes incorrectes
- 📊 Un résumé complet

### Option B : Manuellement

```powershell
psql -U postgres -d flightradar -f DIAGNOSTIC_COMPLET_ERREUR.sql
```

---

## ✅ ÉTAPE 2 : CORRIGER LA BASE DE DONNÉES

Une fois le diagnostic effectué, exécutez la correction :

### Option A : Script PowerShell (Recommandé)

```powershell
.\CORRIGER_FLIGHTS_MAINTENANT.ps1
```

### Option B : Manuellement

```powershell
psql -U postgres -d flightradar -f backend\database\CORRIGER_FLIGHTS_FORCE.sql
```

### Option C : Via pgAdmin

1. Ouvrir pgAdmin
2. Se connecter à PostgreSQL
3. Clic droit sur `flightradar` → Query Tool
4. Ouvrir `backend\database\CORRIGER_FLIGHTS_FORCE.sql`
5. Exécuter (F5)

---

## 🔄 ÉTAPE 3 : REDÉMARRER LE BACKEND

**⚠️ IMPORTANT :** Le backend DOIT être redémarré après la correction SQL.

1. **Arrêter le backend** (Ctrl+C dans la console Spring Boot)
2. **Redémarrer** :
   ```bash
   cd backend
   mvn spring-boot:run
   ```

---

## 🧪 ÉTAPE 4 : TESTER LA CRÉATION

1. **Rafraîchir le frontend** (F5)
2. **Se connecter** en tant qu'admin
3. **Aller dans** "Gestion des Vols"
4. **Créer un nouveau vol** avec ces données minimales :
   - Numéro de vol : `TEST001`
   - Compagnie : `Royal Air Maroc`
   - Avion : Sélectionner un avion
   - Départ : Sélectionner un aéroport
   - Arrivée : Sélectionner un autre aéroport
   - STD : Date/heure future
   - STA : Date/heure future (après STD)
   - Type : Commercial
5. **Cliquer sur "Créer"**

---

## 🔍 SI L'ERREUR PERSISTE

### 1. Vérifier les logs du backend

Dans la console Spring Boot, cherchez les lignes qui contiennent :
- `❌ ERREUR`
- `Erreur lors de la sauvegarde`
- `Transaction silently rolled back`
- `column ... does not exist`

**Copiez l'erreur SQL complète** - elle indiquera la colonne exacte qui manque.

### 2. Vérifier la console du navigateur (F12)

Ouvrez la console (F12) et regardez :
- L'onglet **Network** → Requête `/api/flight/manage` → Response
- L'onglet **Console** → Messages d'erreur

### 3. Vérifier directement dans PostgreSQL

```sql
-- Se connecter
psql -U postgres -d flightradar

-- Voir la structure exacte
\d flights

-- Compter les colonnes
SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'flights';
-- Devrait être 19 colonnes
```

### 4. Tester une insertion SQL directe

```sql
-- Vérifier les données existantes
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

---

## 📋 CHECKLIST COMPLÈTE

- [ ] Diagnostic exécuté (`EXECUTER_DIAGNOSTIC.ps1`)
- [ ] Correction SQL exécutée (`CORRIGER_FLIGHTS_MAINTENANT.ps1`)
- [ ] Vérification : 19 colonnes dans la table `flights`
- [ ] Backend Spring Boot redémarré
- [ ] Frontend rafraîchi (F5)
- [ ] Test de création de vol effectué
- [ ] Logs backend vérifiés (si erreur persiste)
- [ ] Console navigateur vérifiée (F12)

---

## 🆘 SOLUTION D'URGENCE (Si rien ne fonctionne)

Exécutez ce script SQL qui **FORCE** tout :

```sql
-- Se connecter à PostgreSQL
psql -U postgres -d flightradar

-- Script de correction forcée
\i backend/database/CORRIGER_FLIGHTS_FORCE.sql
```

OU copiez-collez directement dans pgAdmin :

```sql
-- Supprimer les contraintes
ALTER TABLE flights DROP CONSTRAINT IF EXISTS flights_flight_status_check;
ALTER TABLE flights DROP CONSTRAINT IF EXISTS flights_flight_type_check;
ALTER TABLE flights DROP CONSTRAINT IF EXISTS fk_flights_alternate_airport;
ALTER TABLE flights DROP CONSTRAINT IF EXISTS fk_flights_pilot;

-- Ajouter TOUTES les colonnes
ALTER TABLE flights ADD COLUMN IF NOT EXISTS airline VARCHAR(100);
ALTER TABLE flights ADD COLUMN IF NOT EXISTS estimated_arrival TIMESTAMP;
ALTER TABLE flights ADD COLUMN IF NOT EXISTS cruise_altitude INTEGER;
ALTER TABLE flights ADD COLUMN IF NOT EXISTS cruise_speed INTEGER;
ALTER TABLE flights ADD COLUMN IF NOT EXISTS flight_type VARCHAR(20);
ALTER TABLE flights ADD COLUMN IF NOT EXISTS alternate_airport_id BIGINT;
ALTER TABLE flights ADD COLUMN IF NOT EXISTS estimated_time_enroute INTEGER;
ALTER TABLE flights ADD COLUMN IF NOT EXISTS pilot_id BIGINT;

-- Corriger flight_number
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

## 📞 BESOIN D'AIDE ?

Si l'erreur persiste après toutes ces étapes :

1. **Copiez l'erreur SQL complète** des logs backend
2. **Copiez le résultat du diagnostic** (`EXECUTER_DIAGNOSTIC.ps1`)
3. **Vérifiez** que le backend a bien été redémarré

Ces informations permettront d'identifier précisément le problème.

---

**Date :** 2025-01-XX  
**Statut :** ✅ Scripts de diagnostic et correction prêts

