# 🔧 Corriger la Liaison Pilote ⇄ Avion

## 🔍 Problème

Le frontend affiche "Aucun avion assigné" pour le pilote `pilote_cmn1`.

## ✅ Solutions

### Solution 1 : Exécuter le Script SQL (Recommandé)

1. **Ouvrir pgAdmin ou psql**

2. **Se connecter à la base de données** `flightradar`

3. **Exécuter le script** :
   ```sql
   -- Copier-coller le contenu de backend/database/verifier_et_corriger_pilotes.sql
   ```

4. **Vérifier** que le pilote a maintenant un avion assigné

### Solution 2 : Vérifier via psql (Ligne de commande)

```powershell
# Se connecter à PostgreSQL
psql -U postgres -d flightradar

# Vérifier les données
SELECT u.username, p.name, a.registration 
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.username = 'pilote_cmn1';
```

### Solution 3 : Réinitialiser les Données

Si les données sont corrompues, vous pouvez :

1. **Supprimer toutes les données** (ATTENTION : perte de données)
   ```sql
   TRUNCATE TABLE flights, aircraft, pilots, users, airports CASCADE;
   ```

2. **Redémarrer le backend** - Le `DataInitializer` recréera toutes les données

### Solution 4 : Créer Manuellement via SQL

```sql
-- 1. Vérifier que l'utilisateur existe
SELECT id, username FROM users WHERE username = 'pilote_cmn1';

-- 2. Créer le pilote s'il n'existe pas
INSERT INTO pilots (name, license, experience_years, user_id)
SELECT 'Pilote CMN1', 'CMN1', 5, id
FROM users WHERE username = 'pilote_cmn1'
ON CONFLICT DO NOTHING;

-- 3. Créer un avion et l'assigner au pilote
INSERT INTO aircraft (registration, model, status, airport_id, position_lat, position_lon, altitude, speed, heading, last_update, pilot_id, username_pilote)
SELECT 
    'CN-AT01',
    'A320',
    'AU_SOL',
    (SELECT id FROM airports WHERE code_iata = 'CMN' LIMIT 1),
    33.3675,
    -7.5898,
    0.0,
    0.0,
    0.0,
    NOW(),
    (SELECT id FROM pilots WHERE user_id = (SELECT id FROM users WHERE username = 'pilote_cmn1')),
    'pilote_cmn1'
ON CONFLICT (registration) DO UPDATE
SET pilot_id = EXCLUDED.pilot_id,
    username_pilote = 'pilote_cmn1';

-- 4. Créer un vol
INSERT INTO flights (flight_number, aircraft_id, departure_airport_id, arrival_airport_id, flight_status, scheduled_departure, scheduled_arrival, created_at, airline)
SELECT 
    'AT1001',
    (SELECT id FROM aircraft WHERE registration = 'CN-AT01'),
    (SELECT id FROM airports WHERE code_iata = 'CMN'),
    (SELECT id FROM airports WHERE code_iata = 'RBA'),
    'PLANIFIE',
    NOW() + INTERVAL '1 hour',
    NOW() + INTERVAL '2 hours',
    NOW(),
    'Royal Air Maroc'
ON CONFLICT (flight_number) DO NOTHING;
```

---

## 🔍 Diagnostic

### Vérifier les Données Actuelles

```sql
-- Vérifier tous les pilotes et leurs avions
SELECT 
    u.username,
    p.id as pilot_id,
    a.id as aircraft_id,
    a.registration,
    a.pilot_id as aircraft_pilot_id
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE'
ORDER BY u.username;
```

### Problèmes Possibles

1. **Pilote existe mais pas d'avion assigné**
   - Solution : Exécuter le script SQL de correction

2. **Avion existe mais pas de pilote**
   - Solution : Assigner le pilote à l'avion

3. **Liaison incorrecte**
   - Solution : Corriger la liaison `aircraft.pilot_id`

4. **Base de données non initialisée**
   - Solution : Supprimer les données et redémarrer le backend

---

## ✅ Vérification Après Correction

Après avoir exécuté le script, vérifier :

```sql
SELECT 
    u.username,
    a.registration,
    a.model,
    a.status,
    f.flight_number,
    f.airline
FROM users u
JOIN pilots p ON p.user_id = u.id
JOIN aircraft a ON a.pilot_id = p.id
LEFT JOIN flights f ON f.aircraft_id = a.id
WHERE u.username = 'pilote_cmn1';
```

**Résultat attendu :**
- ✅ Username : `pilote_cmn1`
- ✅ Registration : `CN-AT01` (ou autre)
- ✅ Model : `A320` (ou autre)
- ✅ Status : `AU_SOL` (ou autre)
- ✅ Flight Number : `AT1001` (ou autre)

---

## 🚀 Après Correction

1. **Redémarrer le backend** (si nécessaire)
2. **Rafraîchir le frontend**
3. **Se reconnecter** avec `pilote_cmn1` / `pilote123`
4. **Vérifier** que le dashboard s'affiche correctement

