# ⚡ SOLUTION RAPIDE - PostgreSQL

## 🎯 VOTRE SITUATION

✅ **PostgreSQL n'est PAS installé** sur votre machine.

---

## 🚀 SOLUTION LA PLUS RAPIDE : Docker

### Si vous avez Docker Desktop

**1. Vérifier Docker** :
```powershell
docker --version
```

**2. Démarrer PostgreSQL** :
```powershell
docker run --name postgres-flightradar `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_DB=flightradar `
  -p 5432:5432 `
  -d postgres:15
```

**3. Vérifier** :
```powershell
docker ps
```

**4. Configurer application.properties** :
```properties
spring.datasource.password=postgres
```

**✅ C'est tout !** PostgreSQL est maintenant disponible.

---

## 📥 SI VOUS N'AVEZ PAS DOCKER : Installation PostgreSQL

### Option 1 : Installer Docker Desktop (Recommandé)

1. Télécharger : https://www.docker.com/products/docker-desktop/
2. Installer Docker Desktop
3. Redémarrer l'ordinateur
4. Utiliser la solution Docker ci-dessus

### Option 2 : Installer PostgreSQL Natif

1. **Télécharger** : https://www.postgresql.org/download/windows/
   - Cliquer sur "Download the installer"
   - Choisir PostgreSQL 15 ou 16

2. **Installer** :
   - Lancer l'installateur
   - Suivre l'assistant
   - **⚠️ NOTER LE MOT DE PASSE** (ex: `postgres`)
   - Port : 5432 (par défaut)

3. **Démarrer le service** :
   ```powershell
   # Trouver le service
   Get-Service -Name "*postgres*"
   
   # Démarrer (remplacer par le nom réel)
   Start-Service -Name "postgresql-x64-15"
   ```

4. **Créer la base de données** :
   ```powershell
   psql -U postgres
   CREATE DATABASE flightradar;
   \q
   ```

5. **Configurer application.properties** :
   ```properties
   spring.datasource.password=VOTRE_MOT_DE_PASSE
   ```

---

## 🎯 RECOMMANDATION

**Utilisez Docker** si possible :
- ✅ Plus rapide à installer
- ✅ Plus facile à gérer
- ✅ Pas de configuration complexe
- ✅ Facile à supprimer si besoin

---

## ✅ VÉRIFICATION

Après installation/démarrage :

```powershell
# Tester la connexion
Test-NetConnection -ComputerName localhost -Port 5432
```

**Attendu** : `TcpTestSucceeded : True`

---

**Date** : 2026  
**Temps d'installation** : 5-10 minutes

