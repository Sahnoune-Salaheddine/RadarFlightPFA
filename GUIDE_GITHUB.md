# 📤 Guide pour Pousser le Projet sur GitHub

## 🔧 Préparation

### 1. Vérifier que application.properties n'est pas commité

Le fichier `backend/src/main/resources/application.properties` contient des mots de passe et doit être ignoré. Un fichier `.gitignore` a été configuré pour cela.

**Vérification :**
```bash
# Si le fichier est déjà suivi par Git, le retirer
git rm --cached backend/src/main/resources/application.properties
```

### 2. Initialiser Git (si pas déjà fait)

```bash
# Dans le répertoire du projet
cd C:\Users\pc\Desktop\PFA-2026

# Initialiser Git
git init

# Vérifier le statut
git status
```

## 📝 Première Commande Git

### 1. Ajouter tous les fichiers

```bash
git add .
```

### 2. Vérifier ce qui sera commité

```bash
git status
```

**Important :** Vérifiez que `backend/src/main/resources/application.properties` n'apparaît PAS dans la liste. Seul `application.properties.example` doit être présent.

### 3. Créer le premier commit

```bash
git commit -m "Initial commit: FlightRadar24-like system with admin, radar, and pilot dashboards"
```

## 🔗 Créer le Repository sur GitHub

### 1. Aller sur GitHub
- Connectez-vous à [GitHub](https://github.com)
- Cliquez sur le bouton **"+"** en haut à droite
- Sélectionnez **"New repository"**

### 2. Configurer le repository
- **Repository name** : `PFA-2026` (ou le nom de votre choix)
- **Description** : "Système de suivi aérien en temps réel - FlightRadar24-like"
- **Visibility** : Public ou Private (selon votre choix)
- **NE PAS** cocher "Initialize this repository with a README" (on a déjà un README)
- Cliquez sur **"Create repository"**

### 3. Copier l'URL du repository
GitHub vous donnera une URL comme :
```
https://github.com/VOTRE_USERNAME/PFA-2026.git
```

## 🚀 Pousser le Code

### 1. Ajouter le remote

```bash
# Remplacez VOTRE_USERNAME par votre nom d'utilisateur GitHub
git remote add origin https://github.com/VOTRE_USERNAME/PFA-2026.git
```

### 2. Vérifier le remote

```bash
git remote -v
```

### 3. Pousser le code

```bash
# Pousser sur la branche main
git branch -M main
git push -u origin main
```

Si vous êtes demandé de vous authentifier :
- Utilisez votre **Personal Access Token** (pas votre mot de passe)
- Pour créer un token : GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic) → Generate new token

## ✅ Vérification

1. Allez sur votre repository GitHub
2. Vérifiez que tous les fichiers sont présents
3. Vérifiez que `application.properties` n'est **PAS** présent (seulement `.example`)

## 🔐 Sécurité - Checklist

Avant de pousser, vérifiez que :

- ✅ `application.properties` est dans `.gitignore`
- ✅ `application.properties.example` est présent (sans mots de passe)
- ✅ Aucun token API réel n'est dans le code
- ✅ Aucun mot de passe en dur dans les scripts
- ✅ Les fichiers de logs ne sont pas commités

## 📋 Commandes Utiles

### Voir les fichiers ignorés
```bash
git status --ignored
```

### Voir les fichiers qui seront commités
```bash
git status
```

### Ajouter un fichier spécifique
```bash
git add nom_du_fichier
```

### Voir l'historique des commits
```bash
git log --oneline
```

### Mettre à jour le repository
```bash
git add .
git commit -m "Description des changements"
git push
```

## 🆘 Problèmes Courants

### Erreur : "remote origin already exists"
```bash
git remote remove origin
git remote add origin https://github.com/VOTRE_USERNAME/PFA-2026.git
```

### Erreur : "failed to push some refs"
```bash
# Si quelqu'un d'autre a poussé du code
git pull origin main --allow-unrelated-histories
git push -u origin main
```

### Retirer un fichier déjà commité
```bash
git rm --cached backend/src/main/resources/application.properties
git commit -m "Remove application.properties from tracking"
git push
```

## 📝 Structure Recommandée du Repository

```
PFA-2026/
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── ...
├── frontend/
│   ├── src/
│   ├── package.json
│   └── ...
├── README.md
├── .gitignore
├── GUIDE_UTILISATION_COMPLET.md
├── PLAN_ARCHITECTURE_COMPLETE.md
└── ...
```

---

**Bon push sur GitHub ! 🚀**

