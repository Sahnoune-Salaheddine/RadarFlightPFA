# 🐘 GUIDE POSTGRESQL - Installation et Démarrage

## 🔍 VÉRIFICATION DE L'INSTALLATION

### Méthode 1 : Vérifier si PostgreSQL est installé

**PowerShell** :
```powershell
# Vérifier la version
psql --version

# Vérifier les services PostgreSQL
Get-Service -Name "*postgres*"
```

**Si installé** : Vous verrez la version (ex: `psql (PostgreSQL) 15.x`)  
**Si non installé** : Erreur `'psql' is not recognized`

---

### Méthode 2 : Vérifier le port 5432

```powershell
Test-NetConnection -ComputerName localhost -Port 5432
```

**Si démarré** : `TcpTestSucceeded : True`  
**Si non démarré** : `TcpTestSucceeded : False`

---

## 📥 INSTALLATION (si non installé)

### Option 1 : Installer PostgreSQL (Recommandé)

1. **Télécharger** : https://www.postgresql.org/download/windows/
2. **Installer** avec l'installateur officiel
3. **Noter** :
   - Mot de passe du superutilisateur `postgres`
   - Port (par défaut : 5432)
   - Répertoire d'installation

### Option 2 : Installer via Chocolatey (si installé)

```powershell
choco install postgresql
```

### Option 3 : Utiliser Docker (alternative)

```powershell
docker run --name postgres-flightradar -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=flightradar -p 5432:5432 -d postgres:15
```

---

## 🚀 DÉMARRAGE DE POSTGRESQL

### Méthode 1 : Service Windows (Recommandé)

**Vérifier le nom du service** :
```powershell
Get-Service -Name "*postgres*"
```

**Démarrer le service** :
```powershell
# Remplacer "postgresql-x64-15" par le nom réel de votre service
Start-Service -Name "postgresql-x64-15"

# Ou avec net start
net start postgresql-x64-15
```

**Vérifier le statut** :
```powershell
Get-Service -Name "*postgres*" | Select-Object Name, Status
```

**Attendu** : `Status : Running`

---

### Méthode 2 : Services Windows (Interface graphique)

1. Appuyer sur `Win + R`
2. Taper `services.msc` et appuyer sur Entrée
3. Chercher "PostgreSQL" dans la liste
4. Clic droit → Démarrer

---

### Méthode 3 : Ligne de commande PostgreSQL

Si PostgreSQL est installé mais le service n'est pas configuré :

```powershell
# Trouver le répertoire d'installation (généralement)
cd "C:\Program Files\PostgreSQL\15\bin"

# Initialiser la base de données (si première fois)
.\initdb.exe -D "C:\Program Files\PostgreSQL\15\data"

# Démarrer PostgreSQL
.\pg_ctl.exe -D "C:\Program Files\PostgreSQL\15\data" start
```

---

## ✅ VÉRIFICATION QUE POSTGRESQL FONCTIONNE

### Test 1 : Connexion

```powershell
# Se connecter à PostgreSQL
psql -U postgres

# Si demandé, entrer le mot de passe
# Vous devriez voir : postgres=#
```

### Test 2 : Créer la base de données

```powershell
# Depuis PowerShell
psql -U postgres -c "CREATE DATABASE flightradar;"

# Ou se connecter et créer manuellement
psql -U postgres
CREATE DATABASE flightradar;
\q
```

### Test 3 : Vérifier la connexion depuis l'application

Une fois PostgreSQL démarré, relancer :
```powershell
cd backend
mvn spring-boot:run
```

**Attendu** : Plus d'erreur `Connection refused`

---

## 🔧 CONFIGURATION application.properties

Vérifiez que `backend/src/main/resources/application.properties` contient :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/flightradar
spring.datasource.username=postgres
spring.datasource.password=VOTRE_MOT_DE_PASSE
```

**Remplacez** `VOTRE_MOT_DE_PASSE` par le mot de passe que vous avez défini lors de l'installation.

---

## 🆘 DÉPANNAGE

### Problème : Service introuvable

**Solution** : PostgreSQL n'est peut-être pas installé comme service.

**Alternative** : Utiliser Docker :
```powershell
docker run --name postgres-flightradar -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=flightradar -p 5432:5432 -d postgres:15
```

### Problème : Port 5432 déjà utilisé

**Solution 1** : Trouver quel processus utilise le port
```powershell
netstat -ano | findstr :5432
```

**Solution 2** : Changer le port dans `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/flightradar
```

### Problème : Mot de passe oublié

**Solution** : Réinitialiser le mot de passe dans `pg_hba.conf` :
1. Trouver le fichier : `C:\Program Files\PostgreSQL\15\data\pg_hba.conf`
2. Modifier la ligne `host all all 127.0.0.1/32 md5` en `trust`
3. Redémarrer PostgreSQL
4. Se connecter sans mot de passe et changer le mot de passe
5. Remettre `md5` dans `pg_hba.conf`

---

## 📋 CHECKLIST RAPIDE

- [ ] PostgreSQL installé ? (`psql --version`)
- [ ] Service démarré ? (`Get-Service "*postgres*"`)
- [ ] Port 5432 accessible ? (`Test-NetConnection localhost -Port 5432`)
- [ ] Base de données `flightradar` créée ?
- [ ] `application.properties` configuré avec le bon mot de passe ?
- [ ] Application Spring Boot démarre sans erreur ?

---

## 🎯 SOLUTION RAPIDE (Docker)

Si vous avez Docker installé, c'est la solution la plus rapide :

```powershell
# Démarrer PostgreSQL dans Docker
docker run --name postgres-flightradar `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_DB=flightradar `
  -p 5432:5432 `
  -d postgres:15

# Vérifier qu'il tourne
docker ps

# Créer la base de données (si nécessaire)
docker exec -it postgres-flightradar psql -U postgres -c "CREATE DATABASE flightradar;"
```

Puis dans `application.properties` :
```properties
spring.datasource.password=postgres
```

---

**Date** : 2026  
**Statut** : Guide complet pour Windows

