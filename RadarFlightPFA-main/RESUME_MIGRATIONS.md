# 📋 Résumé des Migrations SQL

## ✅ État Actuel (Vérifié)

D'après la vérification automatique :
- ✅ `flight_number` : VARCHAR(20) (corrigé)
- ✅ Toutes les colonnes existent :
  - `cruise_altitude`
  - `cruise_speed`
  - `flight_type`
  - `alternate_airport_id`
  - `estimated_time_enroute`
  - `pilot_id`
- ✅ Table `activity_logs` existe

## 🔧 Scripts de Migration Disponibles

### 1. `add_flight_fields.sql`
Ajoute les colonnes suivantes à la table `flights` :
- `cruise_altitude` (INTEGER)
- `cruise_speed` (INTEGER)
- `flight_type` (VARCHAR(20) avec CHECK)
- `alternate_airport_id` (BIGINT)
- `estimated_time_enroute` (INTEGER)
- `pilot_id` (BIGINT)

**Contraintes ajoutées :**
- Clé étrangère `fk_flights_alternate_airport`
- Clé étrangère `fk_flights_pilot`
- Index pour les performances

### 2. `add_activity_logs_table.sql`
Crée la table `activity_logs` pour le journal d'activité système.

### 3. `fix_flight_number_length.sql`
Modifie `flight_number` de VARCHAR(10) à VARCHAR(20).

## 🚀 Commandes pour Exécuter les Migrations

### Option 1 : Script Automatique (Recommandé)

```powershell
# Vérification et exécution automatique
powershell -ExecutionPolicy Bypass -File EXECUTER_TOUTES_MIGRATIONS.ps1

# Ou forcer l'exécution (si certaines ont échoué)
powershell -ExecutionPolicy Bypass -File EXECUTER_MIGRATIONS_FORCE.ps1
```

### Option 2 : Exécution Manuelle

```powershell
# 1. Corriger flight_number
psql -U postgres -d flightradar -c "ALTER TABLE flights ALTER COLUMN flight_number TYPE VARCHAR(20);"

# 2. Ajouter les colonnes
psql -U postgres -d flightradar -f backend/database/add_flight_fields.sql

# 3. Créer activity_logs
psql -U postgres -d flightradar -f backend/database/add_activity_logs_table.sql
```

### Option 3 : Diagnostic Détaillé

```powershell
# Voir l'état exact de la base de données
psql -U postgres -d flightradar -f DIAGNOSTIC_DETAILLE.sql
```

## 🔍 Vérification

### Vérifier que toutes les colonnes existent :

```sql
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'flights' 
  AND column_name IN (
    'cruise_altitude', 
    'cruise_speed', 
    'flight_type', 
    'alternate_airport_id', 
    'estimated_time_enroute', 
    'pilot_id'
  );
```

**Résultat attendu :** 6 lignes

### Vérifier la longueur de flight_number :

```sql
SELECT character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'flights' 
  AND column_name = 'flight_number';
```

**Résultat attendu :** `20`

### Vérifier que activity_logs existe :

```sql
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_name = 'activity_logs'
);
```

**Résultat attendu :** `t` (true)

## ⚠️ Si une Erreur Persiste

1. **Vérifier les logs du backend** pour voir l'erreur exacte
2. **Exécuter le diagnostic détaillé** : `DIAGNOSTIC_DETAILLE.sql`
3. **Vérifier que le backend est redémarré** après les migrations
4. **Vérifier les données** : s'assurer qu'il y a au moins 1 avion et 2 aéroports

## 📝 Notes

- Les scripts utilisent `IF NOT EXISTS` donc ils peuvent être exécutés plusieurs fois sans problème
- Après chaque modification de la base, **redémarrer le backend Spring Boot**
- Les migrations sont idempotentes (sûres à réexécuter)

