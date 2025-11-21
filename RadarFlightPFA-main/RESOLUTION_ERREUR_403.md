# 🔧 Résolution de l'Erreur 403 (Forbidden) sur le Dashboard Admin

## 📋 Problème Identifié

L'erreur 403 (Forbidden) indique que l'utilisateur n'a pas les permissions nécessaires pour accéder à `/api/admin/dashboard`.

### Causes Possibles

1. **Token JWT non envoyé** : Le token n'est pas inclus dans les headers de la requête
2. **Token JWT expiré** : Le token a expiré et n'est plus valide
3. **Token JWT invalide** : Le token est corrompu ou mal formé
4. **Rôle incorrect** : L'utilisateur n'a pas le rôle ADMIN
5. **Filtre JWT ne fonctionne pas** : Le filtre JWT ne valide pas correctement le token

## ✅ Corrections Appliquées

### 1. Frontend - Intercepteur de Requête

**Fichier** : `frontend/src/services/api.js`

- Ajout d'un intercepteur de requête pour s'assurer que le token JWT est toujours envoyé
- Amélioration de la gestion des erreurs 403 avec des messages plus clairs

### 2. Backend - Amélioration du Logging

**Fichier** : `backend/src/main/java/com/flightradar/config/JwtAuthenticationFilter.java`

- Ajout de logs détaillés pour diagnostiquer les problèmes d'authentification
- Meilleure gestion des erreurs (token invalide, utilisateur non trouvé, rôle incorrect)

## 🚀 Solutions

### Solution 1 : Vérifier que vous êtes connecté

1. Ouvrez la console du navigateur (F12)
2. Vérifiez dans l'onglet "Application" > "Local Storage" :
   - `token` : Doit contenir un token JWT
   - `username` : Doit contenir votre nom d'utilisateur
   - `role` : Doit contenir "ADMIN"

### Solution 2 : Se reconnecter

Si le token est expiré ou invalide :

1. Déconnectez-vous
2. Reconnectez-vous avec vos identifiants admin
3. Vérifiez que le token est bien sauvegardé dans localStorage

### Solution 3 : Vérifier les Logs Backend

Vérifiez les logs du backend pour voir les messages d'erreur :

```powershell
# Les logs devraient afficher :
# - "Token JWT invalide ou expiré" si le token est invalide
# - "Aucun token JWT fourni" si le token n'est pas envoyé
# - "Rôle dans le token ne correspond pas" si le rôle est incorrect
```

### Solution 4 : Vérifier le Rôle de l'Utilisateur

Connectez-vous à PostgreSQL et vérifiez :

```sql
SELECT username, role FROM users WHERE username = 'votre_username';
```

Le rôle doit être `ADMIN` (pas `ROLE_ADMIN`).

### Solution 5 : Vérifier le Token dans la Requête

Ouvrez la console du navigateur (F12) > Onglet "Network" :
1. Rechargez la page admin
2. Cliquez sur la requête `/api/admin/dashboard`
3. Vérifiez l'onglet "Headers" :
   - Doit contenir : `Authorization: Bearer <token>`

## 🔍 Dépannage Détaillé

### Étape 1 : Vérifier le Token dans localStorage

```javascript
// Dans la console du navigateur
console.log('Token:', localStorage.getItem('token'))
console.log('Username:', localStorage.getItem('username'))
console.log('Role:', localStorage.getItem('role'))
```

### Étape 2 : Vérifier que le Token est Envoyé

Ouvrez l'onglet "Network" dans les DevTools :
- Recherchez la requête vers `/api/admin/dashboard`
- Vérifiez les "Request Headers"
- Doit contenir : `Authorization: Bearer <votre_token>`

### Étape 3 : Vérifier les Logs Backend

Les logs devraient maintenant afficher :
- Si le token est manquant : `"Aucun token JWT fourni pour l'URL protégée: /api/admin/dashboard"`
- Si le token est invalide : `"Token JWT invalide ou expiré pour l'URL /api/admin/dashboard: ..."`
- Si l'authentification réussit : `"Authentification réussie pour l'utilisateur <username> avec le rôle ADMIN"`

### Étape 4 : Tester avec un Nouveau Token

1. Déconnectez-vous complètement
2. Supprimez le localStorage :
   ```javascript
   localStorage.clear()
   ```
3. Reconnectez-vous
4. Testez à nouveau

## 📝 Fichiers Modifiés

- `frontend/src/services/api.js` : Ajout de l'intercepteur de requête
- `backend/src/main/java/com/flightradar/config/JwtAuthenticationFilter.java` : Amélioration du logging

## ✅ Résultat Attendu

Après ces corrections :
- ✅ Le token JWT est automatiquement ajouté à toutes les requêtes
- ✅ Les erreurs 403 sont mieux gérées avec des messages clairs
- ✅ Les logs backend permettent de diagnostiquer les problèmes
- ✅ Le dashboard admin devrait fonctionner si vous êtes connecté en tant qu'admin

## 🚨 Si l'Erreur Persiste

1. **Vérifiez que le backend est démarré** : `http://localhost:8080`
2. **Vérifiez que vous êtes connecté** : Le token doit être dans localStorage
3. **Vérifiez votre rôle** : Vous devez avoir le rôle `ADMIN`
4. **Consultez les logs backend** : Ils indiqueront la cause exacte de l'erreur 403

