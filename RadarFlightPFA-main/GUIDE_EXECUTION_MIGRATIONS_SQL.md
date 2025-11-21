# Guide d'Exécution des Scripts SQL de Migration

## 📋 Scripts à Exécuter

Deux scripts SQL doivent être exécutés pour ajouter les nouvelles fonctionnalités :

1. **`backend/database/add_flight_fields.sql`** - Ajoute les nouvelles colonnes à la table `flights`
2. **`backend/database/add_activity_logs_table.sql`** - Crée la table `activity_logs` pour la journalisation

---

## 🚀 Méthode 1 : Via psql (Ligne de commande) - RECOMMANDÉ

### Étape 1 : Ouvrir PowerShell ou Terminal

### Étape 2 : Se connecter à PostgreSQL

```powershell
# Si PostgreSQL est dans le PATH
psql -U postgres -d flightradar

# Si PostgreSQL n'est pas dans le PATH
cd "C:\Program Files\PostgreSQL\16\bin"
.\psql.exe -U postgres -d flightradar
```

**Remarque :** Vous serez invité à entrer le mot de passe PostgreSQL (généralement `postgres`)

### Étape 3 : Exécuter les scripts

**Option A : Depuis psql (une fois connecté)**

```sql
-- Exécuter le script 1 : Ajouter les colonnes à flights
\i backend/database/add_flight_fields.sql

-- Exécuter le script 2 : Créer la table activity_logs
\i backend/database/add_activity_logs_table.sql
```

**Option B : Depuis PowerShell (sans se connecter)**

```powershell
# Naviguer vers le répertoire du projet
cd C:\Users\pc\Downloads\RadarFlightPFA-main\RadarFlightPFA-main

# Exécuter le script 1
psql -U postgres -d flightradar -f backend/database/add_flight_fields.sql

# Exécuter le script 2
psql -U postgres -d flightradar -f backend/database/add_activity_logs_table.sql
```

**Si PostgreSQL n'est pas dans le PATH :**

```powershell
# Naviguer vers le répertoire du projet
cd C:\Users\pc\Downloads\RadarFlightPFA-main\RadarFlightPFA-main

# Exécuter le script 1
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d flightradar -f backend/database/add_flight_fields.sql

# Exécuter le script 2
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d flightradar -f backend/database/add_activity_logs_table.sql
```

---

## 🖥️ Méthode 2 : Via pgAdmin (Interface Graphique)

### Étape 1 : Ouvrir pgAdmin

1. Lancer **pgAdmin** (généralement installé avec PostgreSQL)
2. Se connecter au serveur PostgreSQL (mot de passe requis)

### Étape 2 : Sélectionner la base de données

1. Dans le panneau de gauche, développer **Servers**
2. Développer votre serveur PostgreSQL
3. Développer **Databases**
4. Clic droit sur **flightradar** → **Query Tool**

### Étape 3 : Exécuter les scripts

1. Ouvrir le fichier `backend/database/add_flight_fields.sql`
2. Copier tout le contenu
3. Coller dans l'éditeur de requête de pgAdmin
4. Cliquer sur **Execute** (ou F5)
5. Répéter pour `backend/database/add_activity_logs_table.sql`

---

## 🔧 Méthode 3 : Via Spring Boot (Automatique)

Si vous avez configuré `spring.jpa.hibernate.ddl-auto=update` dans `application.properties`, Hibernate peut créer automatiquement les colonnes manquantes, mais **pas les contraintes et index**.

**Recommandation :** Exécuter quand même les scripts SQL pour garantir que tout est correct.

---

## ✅ Vérification que les Scripts ont Réussi

### Vérifier les colonnes de la table flights

```sql
-- Se connecter à PostgreSQL
psql -U postgres -d flightradar

-- Vérifier les colonnes
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'flights' 
  AND column_name IN ('cruise_altitude', 'cruise_speed', 'flight_type', 
                      'alternate_airport_id', 'estimated_time_enroute', 'pilot_id')
ORDER BY column_name;
```

**Résultat attendu :** 6 lignes avec les nouvelles colonnes

### Vérifier la table activity_logs

```sql
-- Vérifier que la table existe
SELECT EXISTS (
   SELECT FROM information_schema.tables 
   WHERE table_name = 'activity_logs'
);

-- Vérifier les colonnes
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'activity_logs'
ORDER BY ordinal_position;
```

**Résultat attendu :** `true` et une liste de colonnes

---

## 🐛 Résolution des Problèmes

### Problème 1 : "psql: command not found" ou "psql n'est pas reconnu"

**Solution :** Ajouter PostgreSQL au PATH

```powershell
# Temporaire (session actuelle)
$env:Path += ";C:\Program Files\PostgreSQL\16\bin"

# Permanent : Ajouter au PATH système
# Win + X → Système → Paramètres système avancés → Variables d'environnement
# Ajouter : C:\Program Files\PostgreSQL\16\bin
```

### Problème 2 : "password authentication failed"

**Solution :** Vérifier le mot de passe dans `application.properties` et utiliser le même

```powershell
# Spécifier le mot de passe dans la commande (moins sécurisé mais pratique pour les tests)
$env:PGPASSWORD="postgres"
psql -U postgres -d flightradar -f backend/database/add_flight_fields.sql
```

### Problème 3 : "database flightradar does not exist"

**Solution :** Créer la base de données d'abord

```sql
psql -U postgres
CREATE DATABASE flightradar;
\q
```

### Problème 4 : "relation flights does not exist"

**Solution :** La table flights n'existe pas encore. Lancer d'abord l'application Spring Boot pour créer les tables de base, puis exécuter les scripts de migration.

### Problème 5 : "constraint already exists"

**Solution :** C'est normal si vous exécutez le script plusieurs fois. Les scripts utilisent `IF NOT EXISTS` pour éviter les erreurs.

---

## 📝 Commandes Rapides (Copier-Coller)

### Pour PowerShell (si PostgreSQL est dans le PATH)

```powershell
# Naviguer vers le projet
cd C:\Users\pc\Downloads\RadarFlightPFA-main\RadarFlightPFA-main

# Script 1 : Ajouter colonnes à flights
psql -U postgres -d flightradar -f backend/database/add_flight_fields.sql

# Script 2 : Créer table activity_logs
psql -U postgres -d flightradar -f backend/database/add_activity_logs_table.sql
```

### Pour PowerShell (si PostgreSQL n'est PAS dans le PATH)

```powershell
# Naviguer vers le projet
cd C:\Users\pc\Downloads\RadarFlightPFA-main\RadarFlightPFA-main

# Ajouter PostgreSQL au PATH temporairement
$env:Path += ";C:\Program Files\PostgreSQL\16\bin"

# Script 1
psql -U postgres -d flightradar -f backend/database/add_flight_fields.sql

# Script 2
psql -U postgres -d flightradar -f backend/database/add_activity_logs_table.sql
```

### Pour Command Prompt (cmd)

```cmd
cd C:\Users\pc\Downloads\RadarFlightPFA-main\RadarFlightPFA-main
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d flightradar -f backend/database/add_flight_fields.sql
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d flightradar -f backend/database/add_activity_logs_table.sql
```

---

## 🎯 Après l'Exécution

1. ✅ **Vérifier** que les scripts ont réussi (voir section "Vérification" ci-dessus)
2. ✅ **Redémarrer** le backend Spring Boot
3. ✅ **Tester** la création d'un vol depuis l'interface Admin

---

## 📞 Besoin d'Aide ?

Si vous rencontrez des erreurs :

1. **Vérifiez les logs** du backend Spring Boot
2. **Vérifiez la console** du navigateur (F12)
3. **Vérifiez** que PostgreSQL est démarré : `Get-Service -Name "*postgres*"`
4. **Vérifiez** la connexion : `Test-NetConnection -ComputerName localhost -Port 5432`

Les messages d'erreur vous indiqueront exactement ce qui ne va pas.

