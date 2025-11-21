# 📥 INSTALLATION POSTGRESQL - Guide Complet

## 🔍 RÉSULTAT DE LA VÉRIFICATION

✅ **PostgreSQL n'est PAS installé** sur votre machine.

**Preuve** :
- ❌ `psql --version` : Commande introuvable
- ❌ Aucun service PostgreSQL trouvé
- ❌ Port 5432 non accessible

---

## 🎯 SOLUTION RECOMMANDÉE : Docker (Plus Simple)

### Si vous avez Docker installé

**Avantages** :
- ✅ Installation en 1 commande
- ✅ Pas de configuration complexe
- ✅ Facile à démarrer/arrêter
- ✅ Pas de conflit avec d'autres installations

**Commandes** :
```powershell
# Démarrer PostgreSQL dans Docker
docker run --name postgres-flightradar `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_DB=flightradar `
  -p 5432:5432 `
  -d postgres:15

# Vérifier qu'il tourne
docker ps

# Voir les logs
docker logs postgres-flightradar
```

**Puis** : Mettre à jour `application.properties` :
```properties
spring.datasource.password=postgres
```

---

## 📦 INSTALLATION POSTGRESQL NATIVE (Alternative)

### Étape 1 : Télécharger

1. Aller sur : https://www.postgresql.org/download/windows/
2. Cliquer sur "Download the installer"
3. Choisir la version (recommandé : PostgreSQL 15 ou 16)
4. Télécharger l'installateur Windows

### Étape 2 : Installer

1. **Lancer l'installateur** téléchargé
2. **Suivre l'assistant** :
   - Répertoire d'installation : Par défaut (`C:\Program Files\PostgreSQL\15`)
   - Composants : Tout cocher (PostgreSQL Server, pgAdmin 4, Command Line Tools)
   - Répertoire de données : Par défaut
   - **MOT DE PASSE** : Choisir un mot de passe (ex: `postgres`) ⚠️ **À NOTER**
   - Port : 5432 (par défaut)
   - Locale : Par défaut
   - Pré-installation : Laisser par défaut

3. **Finir l'installation**

### Étape 3 : Vérifier l'installation

```powershell
# Ajouter PostgreSQL au PATH (si nécessaire)
$env:Path += ";C:\Program Files\PostgreSQL\15\bin"

# Vérifier
psql --version
```

### Étape 4 : Démarrer le service

```powershell
# Trouver le nom du service
Get-Service -Name "*postgres*"

# Démarrer (remplacer par le nom réel)
Start-Service -Name "postgresql-x64-15"
```

### Étape 5 : Créer la base de données

```powershell
# Se connecter
psql -U postgres

# Créer la base de données
CREATE DATABASE flightradar;

# Quitter
\q
```

### Étape 6 : Configurer application.properties

Éditer `backend/src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/flightradar
spring.datasource.username=postgres
spring.datasource.password=VOTRE_MOT_DE_PASSE_ICI
```

---

## 🚀 SOLUTION RAPIDE : Script PowerShell

J'ai créé un script pour vous aider. Exécutez-le :

```powershell
.\INSTALL_POSTGRESQL.ps1
```

---

## ✅ VÉRIFICATION FINALE

Après installation, vérifiez :

```powershell
# 1. PostgreSQL accessible
psql --version

# 2. Service démarré
Get-Service -Name "*postgres*" | Select-Object Status

# 3. Port accessible
Test-NetConnection -ComputerName localhost -Port 5432

# 4. Base de données créée
psql -U postgres -c "\l" | Select-String "flightradar"
```

---

## 🆘 DÉPANNAGE

### Problème : Docker non installé

**Solution** : Installer Docker Desktop
- Télécharger : https://www.docker.com/products/docker-desktop/
- Installer et redémarrer

### Problème : Port 5432 déjà utilisé

**Solution** : Trouver et arrêter le processus
```powershell
netstat -ano | findstr :5432
taskkill /PID <PID> /F
```

### Problème : Mot de passe oublié

**Solution** : Réinitialiser via pgAdmin ou réinstaller

---

**Date** : 2026  
**Recommandation** : Utiliser Docker si disponible (plus simple)

