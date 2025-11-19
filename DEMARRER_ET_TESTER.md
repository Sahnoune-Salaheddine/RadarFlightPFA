# 🚀 Guide de Démarrage et Test - PFA 2026

## 📋 Étape 1 : Démarrer les Services

### 1.1 Démarrer PostgreSQL

```powershell
# Vérifier que PostgreSQL est démarré
Get-Service -Name "*postgres*"

# Si non démarré, démarrer :
Start-Service postgresql-x64-16
```

### 1.2 Démarrer le Backend

```powershell
# Aller dans le dossier backend
cd backend

# Démarrer le backend Spring Boot
mvn spring-boot:run
```

**Attendre le message :** `Started FlightRadarApplication`

**Le backend sera accessible sur :** `http://localhost:8080`

### 1.3 Démarrer le Frontend (dans un nouveau terminal)

```powershell
# Ouvrir un nouveau terminal PowerShell
# Aller dans le dossier frontend
cd frontend

# Démarrer le frontend
npm run dev
```

**Le frontend sera accessible sur :** `http://localhost:3000` ou `http://localhost:3001`

---

## 🧪 Étape 2 : Tester avec cURL

### 2.1 Obtenir un Token JWT

**Commande PowerShell :**

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username": "pilote_cmn1", "password": "pilote123"}'

$TOKEN = $response.token
Write-Host "Token: $TOKEN"
```

**Ou avec curl (si installé) :**

```powershell
curl -X POST "http://localhost:8080/api/auth/login" `
  -H "Content-Type: application/json" `
  -d '{\"username\": \"pilote_cmn1\", \"password\": \"pilote123\"}'
```

### 2.2 Tester le Dashboard Pilote

```powershell
# Utiliser le token obtenu
$headers = @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
}

# Récupérer le dashboard complet
$dashboard = Invoke-RestMethod -Uri "http://localhost:8080/api/pilots/pilote_cmn1/dashboard" `
    -Method GET `
    -Headers $headers

# Afficher les informations principales
Write-Host "Numéro de vol: $($dashboard.flightNumber)"
Write-Host "Compagnie: $($dashboard.airline)"
Write-Host "Route: $($dashboard.route)"
Write-Host "Statut: $($dashboard.flightStatus)"
```

### 2.3 Tester l'Autorisation de Décollage

```powershell
# D'abord, récupérer l'ID de l'avion
$aircraft = Invoke-RestMethod -Uri "http://localhost:8080/api/aircraft/pilot/pilote_cmn1" `
    -Method GET `
    -Headers $headers

$aircraftId = $aircraft.id

# Demander l'autorisation
$clearanceBody = @{
    aircraftId = $aircraftId
} | ConvertTo-Json

$clearance = Invoke-RestMethod -Uri "http://localhost:8080/api/atc/request-takeoff-clearance" `
    -Method POST `
    -Headers $headers `
    -Body $clearanceBody

# Afficher le résultat
Write-Host "Statut: $($clearance.status)"
Write-Host "Message: $($clearance.message)"
Write-Host "Détails: $($clearance.details)"
```

---

## 🎯 Étape 3 : Utiliser le Script de Test Automatique

### 3.1 Exécuter le Script PowerShell

```powershell
# Exécuter le script de test
.\test_api.ps1
```

**Le script va :**
1. ✅ Se connecter et obtenir un token
2. ✅ Tester le dashboard pilote
3. ✅ Tester la récupération de l'avion
4. ✅ Tester la demande d'autorisation
5. ✅ Afficher un résumé des tests

---

## 📊 Étape 4 : Vérifications Visuelles

### 4.1 Tester le Frontend

1. Ouvrir le navigateur : `http://localhost:3000` ou `http://localhost:3001`
2. Se connecter avec :
   - Username: `pilote_cmn1`
   - Password: `pilote123`
3. Vérifier que :
   - ✅ Redirection automatique vers `/pilot`
   - ✅ Dashboard affiché avec toutes les sections
   - ✅ Carte interactive visible
   - ✅ Bouton "Demander Autorisation" visible (si avion au sol)
   - ✅ Données se rafraîchissent toutes les 5 secondes

### 4.2 Tester le Bouton d'Autorisation

1. Cliquer sur "✈️ Demander Autorisation de Décollage"
2. Observer la réponse :
   - ✅ Message vert si GRANTED
   - ✅ Message rouge si REFUSED
   - ✅ Message jaune si PENDING
3. Vérifier que les détails sont affichés

---

## 🔍 Vérifications Rapides

### Vérifier que le Backend est Démarré

```powershell
# Test simple
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/airports"
    Write-Host "✅ Backend démarré - $($response.Count) aéroports trouvés" -ForegroundColor Green
} catch {
    Write-Host "❌ Backend non accessible" -ForegroundColor Red
}
```

### Vérifier que le Frontend est Démarré

```powershell
# Ouvrir dans le navigateur
Start-Process "http://localhost:3000"
```

### Vérifier les Ports Utilisés

```powershell
# Vérifier le port 8080 (backend)
netstat -ano | findstr :8080

# Vérifier le port 3000 ou 3001 (frontend)
netstat -ano | findstr :3000
netstat -ano | findstr :3001
```

---

## 🐛 Dépannage

### Problème : Backend ne démarre pas

**Solutions :**
1. Vérifier que PostgreSQL est démarré
2. Vérifier que le port 8080 n'est pas utilisé :
   ```powershell
   netstat -ano | findstr :8080
   ```
3. Vérifier les logs dans la console
4. Vérifier `application.properties` pour la configuration de la base de données

### Problème : Frontend ne démarre pas

**Solutions :**
1. Vérifier que Node.js est installé : `node --version`
2. Installer les dépendances : `npm install`
3. Vérifier que le port 3000/3001 n'est pas utilisé

### Problème : Erreur 401 (Unauthorized)

**Solutions :**
1. Vérifier que le token est correct
2. Se reconnecter pour obtenir un nouveau token
3. Vérifier que le token n'a pas expiré

### Problème : Erreur 404 (Not Found)

**Solutions :**
1. Vérifier l'URL de l'endpoint
2. Vérifier que l'ID de l'avion existe
3. Vérifier que le pilote a un avion assigné dans la base de données

---

## ✅ Checklist de Test

### Backend
- [ ] PostgreSQL démarré
- [ ] Backend démarré sur port 8080
- [ ] Login fonctionne
- [ ] Dashboard endpoint fonctionne
- [ ] Autorisation endpoint fonctionne

### Frontend
- [ ] Frontend démarré sur port 3000/3001
- [ ] Connexion fonctionne
- [ ] Redirection vers `/pilot` fonctionne
- [ ] Dashboard affiché correctement
- [ ] Bouton autorisation fonctionne

### Intégration
- [ ] Données se rafraîchissent automatiquement
- [ ] Carte interactive fonctionne
- [ ] Messages ATC affichés
- [ ] KPIs calculés et affichés

---

## 📝 Notes

- Le backend doit être démarré avant le frontend
- Les tokens JWT expirent après 24 heures
- Le rafraîchissement automatique se fait toutes les 5 secondes
- Les erreurs sont loggées dans la console du navigateur (F12)

