# 🔍 Diagnostic Complet - Erreur de Transaction

## ❌ Erreur Actuelle

```
Transaction silently rolled back because it has been marked as rollback-only
```

## 🔧 Étape 1 : Vérifier que les Colonnes Existent

**Exécutez ce script SQL pour vérifier :**

```sql
-- Se connecter à PostgreSQL
psql -U postgres -d flightradar

-- Vérifier les colonnes
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

**Si le résultat est vide ou incomplet**, exécutez les scripts de migration :

```powershell
cd C:\Users\pc\Downloads\RadarFlightPFA-main\RadarFlightPFA-main
psql -U postgres -d flightradar -f backend/database/add_flight_fields.sql
psql -U postgres -d flightradar -f backend/database/add_activity_logs_table.sql
```

## 🔧 Étape 2 : Vérifier les Logs du Backend

**Regardez les logs Spring Boot** pour voir l'erreur exacte. Cherchez :

- `❌ ERREUR LORS DE LA SAUVEGARDE`
- `❌ ERREUR D'INTÉGRITÉ DES DONNÉES`
- `Message de la cause:`

Ces messages vous indiqueront la cause exacte.

## 🔧 Étape 3 : Vérifier la Console du Navigateur

**Ouvrez la console (F12)** et regardez :

- `=== DONNÉES ENVOYÉES AU SERVEUR ===`
- `Message d'erreur final:`

## 🐛 Causes Possibles et Solutions

### Cause 1 : Colonnes Manquantes

**Symptôme :** Erreur mentionnant "column does not exist"

**Solution :**
```powershell
psql -U postgres -d flightradar -f backend/database/add_flight_fields.sql
```

### Cause 2 : Contrainte Violée

**Symptôme :** "duplicate key" ou "foreign key constraint"

**Solution :** 
- Vérifier que le numéro de vol est unique
- Vérifier que les IDs d'avion/aéroport/pilote existent

### Cause 3 : Format de Date Invalide

**Symptôme :** "Invalid date format"

**Solution :** Vérifier que les dates sont au format `YYYY-MM-DDTHH:mm`

### Cause 4 : Table activity_logs Manquante

**Symptôme :** Erreur lors de la journalisation

**Solution :**
```powershell
psql -U postgres -d flightradar -f backend/database/add_activity_logs_table.sql
```

## 📋 Checklist de Diagnostic

- [ ] Les colonnes existent dans la table `flights`
- [ ] La table `activity_logs` existe
- [ ] Les logs du backend montrent l'erreur exacte
- [ ] Les données envoyées sont correctes (console navigateur)
- [ ] Les IDs d'avion/aéroport/pilote sont valides

## 🚀 Solution Rapide

**Exécutez ce script PowerShell complet :**

```powershell
cd C:\Users\pc\Downloads\RadarFlightPFA-main\RadarFlightPFA-main

# Vérifier les colonnes
psql -U postgres -d flightradar -c "SELECT column_name FROM information_schema.columns WHERE table_name = 'flights' AND column_name IN ('cruise_altitude', 'cruise_speed', 'flight_type', 'alternate_airport_id', 'estimated_time_enroute', 'pilot_id');"

# Si colonnes manquantes, exécuter les migrations
psql -U postgres -d flightradar -f backend/database/add_flight_fields.sql
psql -U postgres -d flightradar -f backend/database/add_activity_logs_table.sql

# Redémarrer le backend
# Puis tester à nouveau
```

