# 🔧 Ajouter PostgreSQL au PATH - Méthode Manuelle

## ✅ VOTRE SITUATION

PostgreSQL est installé et le service tourne (`postgresql-x64-16`).

---

## 🎯 MÉTHODE 1 : Script Automatique (Recommandé)

**Exécuter le script** :
```powershell
powershell -ExecutionPolicy Bypass -File ".\AJOUTER_POSTGRESQL_AU_PATH.ps1"
```

**Ou en tant qu'administrateur** (pour PATH système) :
1. Clic droit sur PowerShell → "Exécuter en tant qu'administrateur"
2. Naviguer vers le projet : `cd C:\Users\pc\Desktop\PFA-2026`
3. Exécuter : `.\AJOUTER_POSTGRESQL_AU_PATH.ps1`

---

## 🎯 MÉTHODE 2 : Ajout Manuel au PATH

### Étape 1 : Trouver le chemin PostgreSQL

Votre chemin est probablement :
```
C:\Program Files\PostgreSQL\16\bin
```

### Étape 2 : Ajouter au PATH Utilisateur

1. **Appuyer sur** `Win + X`
2. **Choisir** "Système"
3. **Cliquer** "Paramètres système avancés" (à droite)
4. **Cliquer** "Variables d'environnement"
5. **Dans "Variables utilisateur"**, sélectionner `Path`
6. **Cliquer** "Modifier"
7. **Cliquer** "Nouveau"
8. **Ajouter** : `C:\Program Files\PostgreSQL\16\bin`
9. **Cliquer** "OK" sur toutes les fenêtres

### Étape 3 : Redémarrer PowerShell

**Fermer et rouvrir** PowerShell pour que les changements prennent effet.

---

## ✅ VÉRIFICATION

Après avoir ajouté au PATH et redémarré PowerShell :

```powershell
# Tester psql
psql --version

# Devrait afficher : psql (PostgreSQL) 16.x
```

---

## 🗄️ CRÉER LA BASE DE DONNÉES

Une fois PostgreSQL dans le PATH :

```powershell
# Créer la base de données flightradar
psql -U postgres -c "CREATE DATABASE flightradar;"

# Si demandé, entrer le mot de passe : postgres
```

---

## 🔧 CONFIGURATION application.properties

Votre fichier `application.properties` est déjà configuré :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/flightradar
spring.datasource.username=postgres
spring.datasource.password=postgres
```

**✅ C'est bon !** Le mot de passe est déjà `postgres`.

---

## 🚀 PROCHAINES ÉTAPES

1. ✅ Ajouter PostgreSQL au PATH (script ou manuel)
2. ✅ Redémarrer PowerShell
3. ✅ Créer la base : `psql -U postgres -c "CREATE DATABASE flightradar;"`
4. ✅ Tester : `cd backend && mvn spring-boot:run`

---

**Date** : 2026

