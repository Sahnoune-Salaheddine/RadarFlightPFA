# 🔧 Guide de Résolution - Erreur Dashboard Radar

## Problème

Lors de la connexion avec un compte **CENTRE_RADAR**, vous voyez le message :
> "Erreur de chargement des données"

## Cause

L'utilisateur CENTRE_RADAR n'a pas d'`airport_id` associé dans la table `users`.

## Solution

### Option 1 : Utiliser le Script PowerShell (Recommandé)

```powershell
.\VERIFIER_ET_ASSIGNER_AEROPORT_RADAR.ps1
```

Le script va :
1. Lister tous les utilisateurs CENTRE_RADAR
2. Afficher les aéroports disponibles
3. Vous permettre d'assigner un aéroport à un utilisateur

### Option 2 : Utiliser SQL directement

1. **Ouvrir pgAdmin ou psql**

2. **Vérifier les utilisateurs CENTRE_RADAR :**
```sql
SELECT id, username, role, airport_id 
FROM users 
WHERE role = 'CENTRE_RADAR';
```

3. **Lister les aéroports disponibles :**
```sql
SELECT id, name, code_iata, city 
FROM airports 
ORDER BY id;
```

4. **Assigner un aéroport :**
```sql
-- Exemple : Assigner l'aéroport ID 1 à l'utilisateur 'radar1'
UPDATE users 
SET airport_id = 1 
WHERE username = 'radar1' AND role = 'CENTRE_RADAR';
```

### Option 3 : Utiliser l'API (ADMIN seulement)

Si vous êtes connecté en tant qu'ADMIN, vous pouvez utiliser l'API :

```http
PUT /api/auth/users/{id}
Authorization: Bearer <token_admin>
Content-Type: application/json

{
  "airportId": 1
}
```

## Vérification

Après l'assignation, reconnectez-vous avec le compte CENTRE_RADAR. Le dashboard devrait maintenant fonctionner.

## Aéroports par Défaut

Les aéroports créés automatiquement sont généralement :
- **ID 1** : Casablanca (CMN)
- **ID 2** : Marrakech (RAK)
- **ID 3** : Rabat (RBA)
- **ID 4** : Tangier (TNG)

## Notes

- Chaque utilisateur CENTRE_RADAR doit avoir **exactement un** `airport_id`
- Un aéroport peut avoir plusieurs utilisateurs RADAR (mais généralement un seul)
- L'`airport_id` doit correspondre à un aéroport existant dans la table `airports`

