# 🔧 Résolution du Problème "NO AIRCRAFT ASSIGNED"

## 📋 Problèmes Identifiés

1. **Erreur 404** sur `/api/pilots/{username}/dashboard`
   - Cause : Le pilote n'a pas d'avion assigné
   - Le service `PilotDashboardService` lance une exception `NO_AIRCRAFT_ASSIGNED`

2. **Erreur 403** sur `/api/flight/pilot/username/{username}`
   - Cause : Problème d'authentification/autorisation
   - Le pilote doit pouvoir accéder à ses propres vols

3. **Message "NO AIRCRAFT ASSIGNED"** sur le dashboard
   - Cause : Aucun avion n'est assigné au pilote dans la base de données

## ✅ Solution

### Étape 1 : Assigner un avion à tous les pilotes

Exécutez le script SQL pour assigner automatiquement un avion à chaque pilote :

```powershell
.\EXECUTER_ASSIGNATION_AVIONS.ps1
```

**OU** exécutez directement le script SQL :

```powershell
psql -U postgres -d flightradar -f backend\database\ASSIGNER_AVION_TOUS_PILOTES.sql
```

### Étape 2 : Vérifier l'assignation

Connectez-vous à PostgreSQL et vérifiez :

```sql
SELECT 
    u.username,
    p.name as pilot_name,
    a.registration as aircraft_registration,
    a.model as aircraft_model
FROM users u
LEFT JOIN pilots p ON p.user_id = u.id
LEFT JOIN aircraft a ON a.pilot_id = p.id
WHERE u.role = 'PILOTE';
```

### Étape 3 : Redémarrer le backend

```powershell
cd backend
mvn spring-boot:run
```

### Étape 4 : Rafraîchir le frontend

Appuyez sur **F5** dans le navigateur pour rafraîchir la page.

## 🔍 Vérification

1. Connectez-vous en tant que pilote (`pilote_cmn1`)
2. Accédez au dashboard pilote : `http://localhost:3000/pilot`
3. Le dashboard devrait maintenant afficher les informations de l'avion assigné

## 📝 Notes Techniques

### Structure de la Base de Données

- **Table `pilots`** : Contient les informations des pilotes
- **Table `aircraft`** : Contient les avions avec une colonne `pilot_id`
- **Relation** : Un pilote peut avoir plusieurs avions (OneToMany)

### Endpoints API

- **GET `/api/pilots/{username}/dashboard`** : Récupère le dashboard complet du pilote
  - Nécessite : `hasAnyRole('PILOTE', 'ADMIN')`
  - Retourne 404 si aucun avion n'est assigné

- **GET `/api/flight/pilot/username/{username}`** : Récupère les vols assignés au pilote
  - Nécessite : `hasAnyRole('ADMIN', 'PILOTE')`
  - Vérifie que le pilote demande ses propres vols

### Code Backend

Le service `PilotDashboardService` vérifie si un avion est assigné :

```java
List<Aircraft> aircraftList = aircraftRepository.findByPilotId(pilot.getId());
if (aircraftList.isEmpty()) {
    throw new RuntimeException("NO_AIRCRAFT_ASSIGNED: Aucun avion assigné au pilote.");
}
```

## 🚨 Dépannage

### Si le script SQL échoue

1. Vérifiez que PostgreSQL est démarré
2. Vérifiez que la base de données `flightradar` existe
3. Vérifiez que l'utilisateur `postgres` a les permissions nécessaires

### Si le dashboard affiche toujours "NO AIRCRAFT ASSIGNED"

1. Vérifiez dans la base de données que l'avion est bien assigné :
   ```sql
   SELECT * FROM aircraft WHERE pilot_id IS NOT NULL;
   ```

2. Vérifiez que le pilote existe :
   ```sql
   SELECT * FROM pilots p 
   JOIN users u ON p.user_id = u.id 
   WHERE u.username = 'pilote_cmn1';
   ```

3. Redémarrez le backend Spring Boot

### Si l'erreur 403 persiste

1. Vérifiez que le token JWT est valide
2. Vérifiez que l'utilisateur a le rôle `PILOTE`
3. Vérifiez que le pilote demande ses propres vols (username correspond)

## 📚 Fichiers Créés

- `backend/database/ASSIGNER_AVION_TOUS_PILOTES.sql` : Script SQL pour assigner des avions
- `EXECUTER_ASSIGNATION_AVIONS.ps1` : Script PowerShell pour exécuter le script SQL
- `RESOLUTION_DASHBOARD_PILOTE.md` : Ce document

## ✅ Résultat Attendu

Après avoir exécuté le script, chaque pilote devrait avoir :
- ✅ Un avion assigné dans la table `aircraft`
- ✅ Le dashboard devrait s'afficher correctement
- ✅ Les vols assignés devraient être visibles
- ✅ Plus d'erreur 404 ou 403

