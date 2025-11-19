# Résolution de l'erreur de connexion API

## 🔍 Diagnostic

L'erreur `Erreur de connexion au serveur. Vérifiez que le backend est démarré sur http://localhost:8080` s'affiche dans la console du navigateur, mais le backend **fonctionne correctement** sur le port 8080.

## ✅ Vérifications effectuées

1. **Backend actif** : Le serveur Spring Boot écoute sur le port 8080
2. **Endpoints accessibles** : Les endpoints `/api/airports`, `/api/aircraft` répondent correctement
3. **CORS configuré** : La configuration CORS autorise `http://localhost:3000`

## 🔧 Corrections apportées

### 1. Amélioration de la gestion d'erreur dans `api.js`

Le message d'erreur générique a été remplacé par une gestion plus précise :
- **Erreur de connexion réseau** (`ECONNREFUSED`, `ERR_NETWORK`) : Affiche le message d'erreur
- **Timeout** (`ECONNABORTED`) : Affiche un avertissement de timeout
- **Erreurs HTTP** (4xx, 5xx) : Gestion spécifique selon le code de statut
- **404** : Ne log pas en erreur (ressource normale)

### 2. Fichier modifié

**`frontend/src/services/api.js`** : Gestion d'erreur améliorée avec messages plus précis

## 🎯 Causes possibles de l'erreur

1. **Appel API qui échoue** : Un composant fait un appel API qui échoue (404, 500, etc.)
2. **Timeout** : Une requête prend plus de 10 secondes
3. **Problème CORS** : Bien que configuré, un problème peut survenir
4. **Backend non prêt** : Le frontend fait des appels avant que le backend soit complètement initialisé

## 📋 Actions recommandées

### Vérifier les appels API dans la console

1. Ouvrez la console du navigateur (F12)
2. Allez dans l'onglet **Network**
3. Filtrez par **XHR** ou **Fetch**
4. Identifiez les requêtes qui échouent (rouge)
5. Vérifiez le code de statut et le message d'erreur

### Endpoints à vérifier

- ✅ `/api/airports` - Doit retourner 200
- ✅ `/api/aircraft` - Doit retourner 200
- ✅ `/api/weather/alerts` - Peut retourner 200 (vide si pas d'alertes)
- ✅ `/api/conflicts` - Peut retourner 200 (vide si pas de conflits)
- ⚠️ `/api/radar/messages?radarCenterId=1` - Nécessite authentification si configuré

### Tester manuellement

```bash
# Test des endpoints principaux
curl http://localhost:8080/api/airports
curl http://localhost:8080/api/aircraft
curl http://localhost:8080/api/weather/alerts
curl http://localhost:8080/api/conflicts
```

## 🚀 Solution

L'amélioration de la gestion d'erreur devrait maintenant afficher des messages plus précis dans la console. Si l'erreur persiste :

1. **Vérifiez les logs du backend** : Regardez les logs Spring Boot pour voir s'il y a des erreurs
2. **Vérifiez la console du navigateur** : Identifiez l'appel API spécifique qui échoue
3. **Vérifiez les endpoints** : Assurez-vous que tous les endpoints utilisés existent dans le backend

## 📝 Notes

- Le message d'erreur générique peut être trompeur
- Les erreurs HTTP (404, 500) ne sont pas des erreurs de connexion
- Le backend fonctionne correctement, le problème vient probablement d'un appel API spécifique

