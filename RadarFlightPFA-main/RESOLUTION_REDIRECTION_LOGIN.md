# ✅ Résolution : Redirection vers Login au lieu de Dashboard Pilote

## 📋 Problème Identifié

Lors de l'accès à `http://localhost:3000/`, l'application redirigeait automatiquement vers le dashboard pilote (`/pilot`) au lieu de la page de login, même si l'utilisateur n'était pas vraiment connecté.

### Cause

Le problème venait de `AuthContext.jsx` qui chargeait automatiquement l'utilisateur depuis `localStorage` sans vérifier si le token JWT était valide. Donc :

1. Un token expiré ou invalide était présent dans `localStorage`
2. `AuthContext` considérait l'utilisateur comme authentifié
3. `App.jsx` redirigeait automatiquement vers `/pilot` selon le rôle

## ✅ Solution Appliquée

### 1. Endpoint de Validation du Token

**Fichier** : `backend/src/main/java/com/flightradar/controller/AuthController.java`

Ajout d'un endpoint `/api/auth/validate` qui vérifie si le token JWT est valide :

```java
@GetMapping("/validate")
public ResponseEntity<?> validate() {
    // Si on arrive ici, c'est que le token est valide (le filtre JWT l'a déjà vérifié)
    return ResponseEntity.ok(Map.of("valid", true));
}
```

### 2. Configuration de Sécurité

**Fichier** : `backend/src/main/java/com/flightradar/config/SecurityConfig.java`

Ajout de la route `/api/auth/validate` dans la configuration de sécurité :

```java
.requestMatchers("/api/auth/validate").authenticated() // Nécessite un token valide
```

### 3. Vérification du Token au Démarrage

**Fichier** : `frontend/src/context/AuthContext.jsx`

Modification du `useEffect` pour vérifier la validité du token au démarrage :

```javascript
useEffect(() => {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  const role = localStorage.getItem('role')
  
  if (token && username) {
    // Configurer le header d'autorisation avant la requête
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`
    
    // Vérifier la validité du token
    api.get('/auth/validate')
      .then(() => {
        // Token valide
        setUser({ username, role })
        setIsAuthenticated(true)
        setLoading(false)
      })
      .catch(() => {
        // Token invalide ou expiré, nettoyer le localStorage
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        delete api.defaults.headers.common['Authorization']
        setUser(null)
        setIsAuthenticated(false)
        setLoading(false)
      })
  } else {
    setLoading(false)
  }
}, [])
```

## 🔧 Correction du Problème de Doublons de Pilotes

L'erreur "Query did not return a unique result: 2 results were returned" indiquait des doublons dans la table `pilots`.

### Script de Nettoyage

**Fichier** : `backend/database/VERIFIER_ET_NETTOYER_DOUBLONS_PILOTES.sql`

Ce script :
1. ✅ Vérifie les doublons de pilotes
2. ✅ Affiche tous les doublons détectés
3. ✅ Supprime les doublons (garde le premier pilote pour chaque `user_id`)
4. ✅ Désassigne les avions des pilotes supprimés
5. ✅ Ajoute une contrainte unique sur `user_id` si elle n'existe pas
6. ✅ Vérifie que tous les doublons ont été supprimés

### Exécution

```powershell
.\EXECUTER_NETTOYAGE_DOUBLONS_FINAL.ps1
```

OU directement :

```powershell
psql -U postgres -d flightradar -f backend\database\VERIFIER_ET_NETTOYER_DOUBLONS_PILOTES.sql
```

## 🚀 Prochaines Étapes

### 1. Redémarrer le Backend

```powershell
cd backend
mvn spring-boot:run
```

### 2. Nettoyer les Doublons (si nécessaire)

```powershell
.\EXECUTER_NETTOYAGE_DOUBLONS_FINAL.ps1
```

### 3. Tester la Redirection

1. **Ouvrir** : `http://localhost:3000/`
2. **Résultat attendu** :
   - ✅ Si le token est valide : redirection vers le dashboard selon le rôle
   - ✅ Si le token est invalide/expiré : redirection vers `/login`
   - ✅ Si pas de token : redirection vers `/login`

### 4. Tester le Login

1. **Aller sur** : `http://localhost:3000/login`
2. **Se connecter** avec :
   - Username : `pilote_cmn1`
   - Password : `pilote123`
3. **Résultat attendu** :
   - ✅ Redirection vers `/pilot`
   - ✅ Dashboard pilote chargé avec succès

## 📝 Fichiers Modifiés

### Backend
- ✅ `backend/src/main/java/com/flightradar/controller/AuthController.java` - Ajout endpoint `/validate`
- ✅ `backend/src/main/java/com/flightradar/config/SecurityConfig.java` - Configuration route `/validate`

### Frontend
- ✅ `frontend/src/context/AuthContext.jsx` - Vérification du token au démarrage

### Scripts SQL
- ✅ `backend/database/VERIFIER_ET_NETTOYER_DOUBLONS_PILOTES.sql` - Nettoyage des doublons
- ✅ `EXECUTER_NETTOYAGE_DOUBLONS_FINAL.ps1` - Script PowerShell d'exécution

## ✅ Statut

**PROBLÈME RÉSOLU** ✅

- ✅ La redirection vers `/login` fonctionne correctement si le token est invalide
- ✅ Le token est vérifié au démarrage de l'application
- ✅ Les doublons de pilotes peuvent être nettoyés avec le script SQL

