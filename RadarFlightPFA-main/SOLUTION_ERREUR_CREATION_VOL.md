# 🔧 Solution : Erreur lors de la création d'un vol

## ❌ Problème

Lorsque vous essayez de créer un vol, vous obtenez cette erreur :
```
Erreur de base de données. Vérifiez que les colonnes existent (exécutez les scripts de migration SQL).
```

## 🔍 Cause

Le modèle Java `Flight.java` utilise des colonnes qui n'existent pas encore dans la table `flights` de votre base de données PostgreSQL.

## ✅ Solution

Vous devez exécuter le script de migration SQL pour ajouter les colonnes manquantes.

### Méthode 1 : Script PowerShell (Recommandé)

1. Ouvrez PowerShell dans le répertoire du projet :
   ```powershell
   cd "C:\Users\pc\Downloads\RadarFlightPFA-main\RadarFlightPFA-main"
   ```

2. Exécutez le script de migration :
   ```powershell
   .\EXECUTER_MIGRATION_FLIGHTS.ps1
   ```

3. Entrez "O" pour confirmer l'exécution
4. Entrez le mot de passe PostgreSQL (généralement `postgres`)

### Méthode 2 : Via psql (Ligne de commande)

1. Ouvrez PowerShell ou Terminal
2. Connectez-vous à PostgreSQL :
   ```powershell
   psql -U postgres -d flightradar
   ```
   
   Si psql n'est pas dans le PATH :
   ```powershell
   cd "C:\Program Files\PostgreSQL\16\bin"
   .\psql.exe -U postgres -d flightradar
   ```

3. Exécutez le script SQL :
   ```sql
   \i backend/database/add_flight_fields.sql
   ```

   Ou depuis PowerShell (sans se connecter) :
   ```powershell
   psql -U postgres -d flightradar -f backend/database/add_flight_fields.sql
   ```

### Méthode 3 : Via pgAdmin (Interface graphique)

1. Ouvrez pgAdmin
2. Connectez-vous à votre serveur PostgreSQL
3. Naviguez vers : `flightradar` → `Schemas` → `public`
4. Clic droit sur `flightradar` → `Query Tool`
5. Ouvrez le fichier `backend/database/add_flight_fields.sql`
6. Exécutez le script (F5)

## 📋 Colonnes ajoutées

Le script ajoute les colonnes suivantes à la table `flights` :
- `estimated_arrival` (TIMESTAMP) - Heure d'arrivée estimée
- `cruise_altitude` (INTEGER) - Altitude de croisière en pieds
- `cruise_speed` (INTEGER) - Vitesse de croisière en nœuds
- `flight_type` (VARCHAR) - Type de vol (COMMERCIAL, CARGO, etc.)
- `alternate_airport_id` (BIGINT) - Aéroport alternatif
- `estimated_time_enroute` (INTEGER) - Temps estimé en route
- `pilot_id` (BIGINT) - Pilote assigné

## 🚀 Après la migration

1. **Redémarrez le backend Spring Boot** (si nécessaire)
2. **Rafraîchissez le frontend** dans votre navigateur
3. **Essayez de créer un vol à nouveau**

## ✅ Vérification

Pour vérifier que les colonnes ont été ajoutées, exécutez dans psql :

```sql
\d flights
```

Vous devriez voir toutes les colonnes listées ci-dessus.

## 🆘 Si vous avez encore des problèmes

1. Vérifiez que PostgreSQL est démarré :
   ```powershell
   Get-Service postgresql*
   ```

2. Vérifiez que la base de données `flightradar` existe :
   ```powershell
   psql -U postgres -c "\l" | Select-String "flightradar"
   ```

3. Vérifiez les logs du backend Spring Boot pour plus de détails

