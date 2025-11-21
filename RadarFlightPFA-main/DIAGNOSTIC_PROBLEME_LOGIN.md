# Diagnostic du problème de connexion

## 🔍 Problème identifié

L'erreur "Erreur de connexion" s'affiche lors de la tentative de login dans l'interface.

## ✅ Corrections apportées

### 1. Amélioration de la gestion d'erreur dans `AuthContext.jsx`

- Messages d'erreur plus détaillés selon le type d'erreur
- Affichage du message exact du backend si disponible
- Message clair si le backend n'est pas accessible

### 2. Types d'erreurs gérées

- **Erreur serveur (5xx)** : Affiche le message d'erreur du serveur
- **Erreur d'authentification (401)** : Affiche "Identifiants invalides"
- **Backend non accessible** : Affiche un message clair pour vérifier que le backend est démarré
- **Erreur de configuration** : Affiche le message d'erreur technique

## 🔧 Vérifications à effectuer

### 1. Vérifier que le backend est démarré

```bash
# Vérifier si le port 8080 est en écoute
netstat -ano | findstr :8080

# Si le backend n'est pas démarré, le démarrer :
cd backend
mvn spring-boot:run
```

### 2. Vérifier que PostgreSQL est démarré

```bash
# Vérifier si PostgreSQL est en cours d'exécution
Get-Service -Name postgresql*

# Si PostgreSQL n'est pas démarré, le démarrer :
Start-Service postgresql-x64-15  # Ajuster selon votre version
```

### 3. Tester l'endpoint de login manuellement

```powershell
# Test avec PowerShell
$body = @{
    username = 'admin'
    password = 'admin123'
} | ConvertTo-Json

Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/login' `
    -Method POST `
    -Body $body `
    -ContentType 'application/json'
```

### 4. Vérifier les comptes utilisateurs

Les comptes par défaut créés par `DataInitializer` :

- **Admin** : `admin` / `admin123`
- **Pilote CMN** : `pilote_cmn1` / `pilote123`
- **Radar CMN** : `radar_cmn` / `radar123`

## 📋 Messages d'erreur possibles

### "Impossible de contacter le serveur"
- **Cause** : Le backend n'est pas démarré ou n'est pas accessible
- **Solution** : Démarrer le backend avec `mvn spring-boot:run`

### "Identifiants invalides"
- **Cause** : Le nom d'utilisateur ou le mot de passe est incorrect
- **Solution** : Vérifier les identifiants dans `DataInitializer.java`

### "Erreur 500: Internal Server Error"
- **Cause** : Erreur côté serveur (base de données, JWT, etc.)
- **Solution** : Vérifier les logs du backend pour plus de détails

### "Erreur 404: Not Found"
- **Cause** : L'endpoint `/api/auth/login` n'existe pas
- **Solution** : Vérifier que `AuthController` est bien configuré

## 🚀 Solution rapide

1. **Démarrer PostgreSQL** :
   ```powershell
   Start-Service postgresql-x64-15
   ```

2. **Démarrer le backend** :
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **Vérifier que le backend répond** :
   ```bash
   curl http://localhost:8080/api/airports
   ```

4. **Tester le login** :
   - Ouvrir l'interface sur `http://localhost:3000`
   - Utiliser les identifiants : `admin` / `admin123`

## 📝 Notes

- Les messages d'erreur sont maintenant plus détaillés dans la console du navigateur
- Ouvrir la console (F12) pour voir les messages d'erreur complets
- Vérifier les logs du backend pour plus d'informations

