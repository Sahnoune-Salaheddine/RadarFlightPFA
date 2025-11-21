# 🔧 Correction de la Longueur de flight_number

## ❌ Problème

L'erreur suivante se produit lors de la création d'un vol :
```
valeur trop longue pour le type character varying(10)
```

La colonne `flight_number` dans la base de données est limitée à **10 caractères**, mais le script génère des numéros de vol plus longs.

## ✅ Solution

### Option 1 : Exécuter le Script PowerShell (Recommandé)

```powershell
powershell -ExecutionPolicy Bypass -File FIX_FLIGHT_NUMBER_SIMPLE.ps1
```

Vous devrez entrer le mot de passe PostgreSQL lorsque demandé.

### Option 2 : Exécuter Manuellement avec psql

```powershell
psql -U postgres -d flightradar -c "ALTER TABLE flights ALTER COLUMN flight_number TYPE VARCHAR(20);"
```

### Option 3 : Exécuter le Script SQL Directement

```powershell
psql -U postgres -d flightradar -f backend/database/fix_flight_number_length.sql
```

## 📋 Vérification

Après l'exécution, vérifiez que la modification a été appliquée :

```sql
SELECT column_name, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'flights' 
  AND column_name = 'flight_number';
```

**Résultat attendu :** `character_maximum_length = 20`

## 🔄 Modifications Apportées

1. **Modèle Java** (`Flight.java`) : 
   - `length = 10` → `length = 20`

2. **Script de Test** (`TEST_CREATION_VOL.ps1`) :
   - Format du numéro de vol : `TEST` + 4 chiffres aléatoires (ex: `TEST1234`)
   - Plus court et plus réaliste

## ⚠️ Important

**Redémarrez le backend Spring Boot** après avoir modifié la base de données pour que Hibernate prenne en compte le changement.

## 🧪 Test

Après avoir appliqué la correction, relancez le test :

```powershell
powershell -ExecutionPolicy Bypass -File TEST_CREATION_VOL.ps1
```

Le vol devrait maintenant être créé avec succès ! ✈️

