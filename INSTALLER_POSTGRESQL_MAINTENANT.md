# 📥 INSTALLER POSTGRESQL MAINTENANT

## 🔍 VOTRE SITUATION

- ❌ PostgreSQL : **NON installé**
- ❌ Docker : **NON installé**

---

## 🎯 SOLUTION : Installer PostgreSQL

### ÉTAPE 1 : Télécharger PostgreSQL

1. **Ouvrir votre navigateur**
2. **Aller sur** : https://www.postgresql.org/download/windows/
3. **Cliquer sur** : "Download the installer"
4. **Choisir** : PostgreSQL 15 ou 16 (version récente)
5. **Télécharger** l'installateur Windows (fichier .exe)

---

### ÉTAPE 2 : Installer PostgreSQL

1. **Double-cliquer** sur le fichier téléchargé
2. **Suivre l'assistant d'installation** :
   - ✅ Cliquer "Next" sur l'écran d'accueil
   - ✅ Choisir le répertoire d'installation (par défaut : `C:\Program Files\PostgreSQL\15`)
   - ✅ Sélectionner les composants : **Tout cocher** (PostgreSQL Server, pgAdmin 4, Command Line Tools)
   - ✅ Choisir le répertoire de données (par défaut)
   - ⚠️ **MOT DE PASSE** : Entrer `postgres` (ou un autre, **À NOTER**)
   - ✅ Port : `5432` (par défaut)
   - ✅ Locale : Par défaut
   - ✅ Cliquer "Next" jusqu'à "Finish"

3. **Finir l'installation** (peut prendre 2-3 minutes)

---

### ÉTAPE 3 : Démarrer PostgreSQL

**Méthode 1 : Services Windows (Recommandé)**

1. Appuyer sur `Win + R`
2. Taper `services.msc` et appuyer sur Entrée
3. Chercher **"PostgreSQL"** dans la liste
4. **Clic droit** → **Démarrer**

**Méthode 2 : PowerShell**

```powershell
# Trouver le service
Get-Service -Name "*postgres*"

# Démarrer (remplacer "postgresql-x64-15" par le nom réel)
Start-Service -Name "postgresql-x64-15"
```

---

### ÉTAPE 4 : Créer la base de données

**Option A : Via pgAdmin (Interface graphique)**

1. Ouvrir **pgAdmin 4** (dans le menu Démarrer)
2. Se connecter avec le mot de passe `postgres`
3. Clic droit sur "Databases" → Create → Database
4. Nom : `flightradar`
5. Cliquer "Save"

**Option B : Via PowerShell**

```powershell
# Ajouter PostgreSQL au PATH (si nécessaire)
$env:Path += ";C:\Program Files\PostgreSQL\15\bin"

# Créer la base de données
psql -U postgres -c "CREATE DATABASE flightradar;"
```

Si demandé, entrer le mot de passe : `postgres` (ou celui que vous avez choisi)

---

### ÉTAPE 5 : Configurer application.properties

Éditer le fichier : `backend/src/main/resources/application.properties`

**Remplacer** :
```properties
spring.datasource.password=postgres
```

Par votre mot de passe si vous avez choisi autre chose que `postgres`.

---

### ÉTAPE 6 : Vérifier

```powershell
# Tester la connexion
Test-NetConnection -ComputerName localhost -Port 5432
```

**Attendu** : `TcpTestSucceeded : True`

---

## ✅ RÉSUMÉ DES COMMANDES

Après installation :

```powershell
# 1. Démarrer le service
Start-Service -Name "postgresql-x64-15"

# 2. Créer la base de données
$env:Path += ";C:\Program Files\PostgreSQL\15\bin"
psql -U postgres -c "CREATE DATABASE flightradar;"

# 3. Vérifier
Test-NetConnection -ComputerName localhost -Port 5432
```

---

## 🚀 ENSUITE

Une fois PostgreSQL démarré :

```powershell
cd backend
mvn spring-boot:run
```

**L'application devrait démarrer sans erreur !** ✅

---

**Temps d'installation** : 5-10 minutes  
**Difficulté** : Facile  
**Lien de téléchargement** : https://www.postgresql.org/download/windows/

