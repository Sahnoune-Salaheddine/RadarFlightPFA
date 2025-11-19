# Solution au problème de connexion

## 🔍 Problème identifié

Le message "Impossible de contacter le serveur" s'affichait alors que le backend fonctionne correctement. Le problème venait de la configuration CORS qui n'autorisait que le port 3000, alors que le frontend tourne parfois sur le port 3001.

## ✅ Corrections apportées

### 1. Mise à jour de la configuration CORS globale

**Fichier** : `backend/src/main/java/com/flightradar/config/SecurityConfig.java`

- Ajout du port 3001 aux origines autorisées
- Les deux ports (3000 et 3001) sont maintenant autorisés

### 2. Mise à jour de tous les contrôleurs REST

Tous les contrôleurs ont été mis à jour pour autoriser les deux ports :
- `AuthController.java`
- `AircraftController.java`
- `AirportController.java`
- `WeatherController.java`
- `ConflictController.java`
- `RadarController.java`
- `FlightController.java`
- `RunwayController.java`

### 3. Mise à jour de la configuration WebSocket

**Fichier** : `backend/src/main/java/com/flightradar/config/WebSocketConfig.java`

- Ajout du port 3001 aux origines autorisées pour WebSocket

## 🚀 Redémarrage nécessaire

**IMPORTANT** : Après ces modifications, vous devez **redémarrer le backend** pour que les changements prennent effet :

```bash
# Arrêter le backend (Ctrl+C dans le terminal où il tourne)
# Puis redémarrer :
cd backend
mvn spring-boot:run
```

## ✅ Vérification

Une fois le backend redémarré, testez la connexion :

1. **Ouvrir l'interface** : `http://localhost:3000` ou `http://localhost:3001`
2. **Tenter de se connecter** avec :
   - Username : `admin`
   - Password : `admin123`

3. **Vérifier la console du navigateur** (F12) :
   - Plus d'erreur CORS
   - La connexion devrait fonctionner

## 📝 Notes

- Le backend autorise maintenant les deux ports (3000 et 3001)
- Si vous utilisez un autre port, ajoutez-le dans `SecurityConfig.java` et tous les contrôleurs
- Le backend doit être redémarré après chaque modification de configuration

