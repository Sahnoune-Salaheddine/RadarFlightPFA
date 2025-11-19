# 🔧 Solution : "Aucun avion assigné"

## 🔍 Diagnostic

Le message "Aucun avion assigné" apparaît quand :
1. Le pilote n'a pas d'avion lié dans la base de données
2. La liaison `pilot_id` ou `username_pilote` n'est pas définie
3. Le `DataInitializer` ne s'est pas exécuté (base de données non vide)

## ✅ Solutions

### Solution 1 : Script SQL (Recommandé)

**Étape 1 :** Ouvrir pgAdmin ou psql

**Étape 2 :** Exécuter le script SQL :

```sql
-- Assigner un avion existant au pilote
UPDATE aircraft 
SET 
    pilot_id = (SELECT p.id FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1' LIMIT 1),
    username_pilote = 'pilote_cmn1'
WHERE id = (
    SELECT id FROM aircraft 
    WHERE (pilot_id IS NULL OR username_pilote IS NULL)
    LIMIT 1
);

-- Ou créer un nouvel avion si aucun n'existe
INSERT INTO aircraft (
    registration, model, status, airport_id, pilot_id, username_pilote,
    position_lat, position_lon, altitude, speed, heading,
    air_speed, vertical_speed, transponder_code, last_update
)
SELECT 
    'CN-ABC', 'A320', 'AU_SOL',
    (SELECT id FROM airports WHERE code_iata = 'CMN' LIMIT 1),
    (SELECT p.id FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1' LIMIT 1),
    'pilote_cmn1',
    33.367500, -7.589800, 0.0, 0.0, 0.0, 0.0, 0.0, '1200', NOW()
WHERE NOT EXISTS (SELECT 1 FROM aircraft WHERE registration = 'CN-ABC')
AND EXISTS (SELECT 1 FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1');
```

**Fichier complet :** `ASSIGNER_AVION_PILOTE.sql`

### Solution 2 : Script PowerShell

```powershell
# Exécuter le script PowerShell
.\VERIFIER_ET_ASSIGNER_AVION.ps1
```

### Solution 3 : Réinitialiser la Base de Données

**Option A : Supprimer et recréer**

```sql
-- ATTENTION : Cela supprime toutes les données !
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

Puis redémarrer le backend pour que `DataInitializer` s'exécute.

**Option B : Forcer l'initialisation**

Modifier temporairement `DataInitializer.java` :

```java
@Override
public void run(String... args) throws Exception {
    // Forcer l'initialisation (temporaire)
    initializeData();
}
```

Puis redémarrer le backend.

## 🔍 Vérification

### Vérifier dans la Base de Données

```sql
-- Vérifier l'utilisateur
SELECT id, username, role FROM users WHERE username = 'pilote_cmn1';

-- Vérifier le pilote
SELECT p.id, p.name, u.username 
FROM pilots p 
JOIN users u ON p.user_id = u.id 
WHERE u.username = 'pilote_cmn1';

-- Vérifier l'avion assigné
SELECT a.id, a.registration, a.model, a.pilot_id, a.username_pilote, u.username
FROM aircraft a
LEFT JOIN pilots p ON a.pilot_id = p.id
LEFT JOIN users u ON p.user_id = u.id
WHERE u.username = 'pilote_cmn1' OR a.username_pilote = 'pilote_cmn1';
```

### Vérifier via l'API

```powershell
# Obtenir le token
$login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username": "pilote_cmn1", "password": "pilote123"}'

$TOKEN = $login.token

# Tester l'endpoint
Invoke-RestMethod -Uri "http://localhost:8080/api/aircraft/pilot/pilote_cmn1" `
    -Method GET `
    -Headers @{"Authorization" = "Bearer $TOKEN"}
```

## 📝 Étapes Rapides

1. **Ouvrir pgAdmin** ou utiliser psql
2. **Se connecter** à la base `flightradar`
3. **Exécuter** le script `ASSIGNER_AVION_PILOTE.sql`
4. **Vérifier** le résultat
5. **Rafraîchir** le frontend

## ✅ Résultat Attendu

Après l'assignation, vous devriez voir :
- ✅ L'avion assigné au pilote dans la base de données
- ✅ Le dashboard s'affiche correctement dans le frontend
- ✅ Toutes les informations du vol sont visibles

## 🐛 Si le Problème Persiste

1. **Vérifier que le backend est démarré**
2. **Vérifier les logs du backend** pour les erreurs
3. **Vérifier la console du navigateur** (F12) pour les erreurs
4. **Vérifier que PostgreSQL est démarré**

