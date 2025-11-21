# 🔌 Guide de Connexion à la Base de Données

## 📋 Informations de Connexion

D'après la configuration dans `backend/src/main/resources/application.properties` :

```
Host: localhost
Port: 5432
Base de données: flightradar
Utilisateur: postgres
Mot de passe: postgres
```

---

## 🔧 Méthode 1 : pgAdmin (Interface Graphique) - Recommandé

### Installation
1. Télécharger pgAdmin depuis : https://www.pgadmin.org/download/
2. Installer pgAdmin
3. Lancer pgAdmin

### Connexion
1. **Clic droit sur "Servers"** → **Create** → **Server...**
2. Dans l'onglet **General** :
   - **Name** : `FlightRadar Local` (ou un nom de votre choix)
3. Dans l'onglet **Connection** :
   - **Host name/address** : `localhost`
   - **Port** : `5432`
   - **Maintenance database** : `postgres` (ou `flightradar` si elle existe déjà)
   - **Username** : `postgres`
   - **Password** : `postgres`
   - ✅ Cocher **Save password** (optionnel)
4. Cliquer sur **Save**

### Utilisation
- Une fois connecté, vous verrez la base de données `flightradar` dans l'arborescence
- Vous pouvez :
  - Exécuter des requêtes SQL
  - Voir les tables et leurs données
  - Modifier les données
  - Exécuter les scripts SQL du projet

### Exécuter un Script SQL
1. Clic droit sur la base `flightradar`
2. **Query Tool**
3. Ouvrir un fichier SQL (ex: `ASSIGNER_AVION_RAPIDE.sql`)
4. Copier-coller le contenu
5. Cliquer sur **Execute** (F5)

---

## 💻 Méthode 2 : psql (Ligne de Commande)

### Vérifier que psql est installé
```bash
psql --version
```

Si psql n'est pas trouvé, ajouter PostgreSQL au PATH ou utiliser le chemin complet.

### Connexion
```bash
psql -h localhost -p 5432 -U postgres -d flightradar
```

**Ou plus simple :**
```bash
psql -U postgres -d flightradar
```

Vous serez invité à entrer le mot de passe : `postgres`

### Commandes Utiles dans psql

```sql
-- Lister toutes les bases de données
\l

-- Se connecter à une autre base
\c flightradar

-- Lister toutes les tables
\dt

-- Voir la structure d'une table
\d flights

-- Voir toutes les colonnes d'une table
\d+ flights

-- Exécuter un fichier SQL
\i chemin/vers/fichier.sql

-- Quitter psql
\q
```

### Exécuter un Script SQL
```bash
psql -U postgres -d flightradar -f ASSIGNER_AVION_RAPIDE.sql
```

---

## 🚀 Méthode 3 : PowerShell Script (Windows)

### Script de Connexion Rapide

Créez un fichier `CONNECTER_BD.ps1` :

```powershell
# Configuration
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_USER = "postgres"
$DB_NAME = "flightradar"

Write-Host "Connexion à la base de données..." -ForegroundColor Cyan
Write-Host "Host: $DB_HOST" -ForegroundColor Gray
Write-Host "Port: $DB_PORT" -ForegroundColor Gray
Write-Host "Database: $DB_NAME" -ForegroundColor Gray
Write-Host "User: $DB_USER" -ForegroundColor Gray
Write-Host ""

# Connexion interactive
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME
```

**Exécution :**
```powershell
.\CONNECTER_BD.ps1
```

---

## 🔍 Méthode 4 : Vérifier la Connexion

### Test de Connexion Rapide

```bash
# Test simple
psql -U postgres -d flightradar -c "SELECT version();"
```

### Vérifier que la Base Existe

```bash
psql -U postgres -c "\l" | grep flightradar
```

### Créer la Base de Données (si elle n'existe pas)

```bash
psql -U postgres -c "CREATE DATABASE flightradar;"
```

---

## 🛠️ Méthode 5 : Via Spring Boot (Automatique)

Le backend Spring Boot se connecte automatiquement à la base de données au démarrage.

### Vérifier la Connexion
1. Démarrer le backend :
   ```bash
   cd backend
   mvn spring-boot:run
   ```
2. Si la connexion réussit, vous verrez dans les logs :
   ```
   HikariPool-1 - Starting...
   HikariPool-1 - Start completed.
   ```
3. Si la connexion échoue, vous verrez une erreur :
   ```
   Connection refused
   ```

### Configuration dans application.properties

Le fichier `backend/src/main/resources/application.properties` contient :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/flightradar
spring.datasource.username=postgres
spring.datasource.password=postgres
```

**Si votre mot de passe PostgreSQL est différent**, modifiez cette ligne :
```properties
spring.datasource.password=VOTRE_MOT_DE_PASSE
```

---

## 📝 Requêtes SQL Utiles

### Vérifier les Tables

```sql
-- Lister toutes les tables
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public';

-- Compter les enregistrements par table
SELECT 
    'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'pilots', COUNT(*) FROM pilots
UNION ALL
SELECT 'aircraft', COUNT(*) FROM aircraft
UNION ALL
SELECT 'flights', COUNT(*) FROM flights
UNION ALL
SELECT 'airports', COUNT(*) FROM airports;
```

### Vérifier les Pilotes et leurs Avions

```sql
SELECT 
    u.username,
    p.name as pilote_name,
    a.registration,
    a.model,
    a.status
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE';
```

### Vérifier les Vols

```sql
SELECT 
    f.flight_number,
    f.airline,
    f.flight_status,
    dep.code_iata as departure,
    arr.code_iata as arrival,
    a.registration as aircraft
FROM flights f
LEFT JOIN airports dep ON f.departure_airport_id = dep.id
LEFT JOIN airports arr ON f.arrival_airport_id = arr.id
LEFT JOIN aircraft a ON f.aircraft_id = a.id
ORDER BY f.created_at DESC;
```

---

## ⚠️ Problèmes Courants

### 1. Erreur "Connection refused"
**Cause** : PostgreSQL n'est pas démarré

**Solution** :
- Windows : Vérifier que le service PostgreSQL est démarré
  ```powershell
  Get-Service -Name postgresql*
  ```
- Démarrer le service :
  ```powershell
  Start-Service postgresql-x64-14  # Remplacer par votre version
  ```

### 2. Erreur "password authentication failed"
**Cause** : Mot de passe incorrect

**Solution** :
- Vérifier le mot de passe dans `application.properties`
- Réinitialiser le mot de passe PostgreSQL si nécessaire

### 3. Erreur "database does not exist"
**Cause** : La base `flightradar` n'existe pas

**Solution** :
```sql
CREATE DATABASE flightradar;
```

### 4. Erreur "psql: command not found"
**Cause** : PostgreSQL n'est pas dans le PATH

**Solution** :
- Ajouter PostgreSQL au PATH Windows
- Ou utiliser le chemin complet : `C:\Program Files\PostgreSQL\14\bin\psql.exe`

---

## 🔐 Sécurité

### Changer le Mot de Passe PostgreSQL

```sql
-- Se connecter en tant que superutilisateur
psql -U postgres

-- Changer le mot de passe
ALTER USER postgres WITH PASSWORD 'nouveau_mot_de_passe';
```

**Puis mettre à jour** `application.properties` :
```properties
spring.datasource.password=nouveau_mot_de_passe
```

---

## 📚 Ressources

- **Documentation PostgreSQL** : https://www.postgresql.org/docs/
- **pgAdmin Documentation** : https://www.pgadmin.org/docs/
- **psql Documentation** : https://www.postgresql.org/docs/current/app-psql.html

---

## ✅ Checklist de Connexion

- [ ] PostgreSQL est installé
- [ ] PostgreSQL est démarré (service actif)
- [ ] La base de données `flightradar` existe
- [ ] Les identifiants sont corrects (postgres/postgres)
- [ ] Le port 5432 est accessible
- [ ] La connexion fonctionne (test avec psql ou pgAdmin)

---

**Dernière mise à jour** : 2025-01-27

