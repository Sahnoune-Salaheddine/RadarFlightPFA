# Diagnostic - Erreur "Transaction silently rolled back"

## 🔍 Problème

L'erreur "Transaction silently rolled back because it has been marked as rollback-only" se produit lors de la création d'un vol.

## ✅ Corrections Apportées

### 1. Amélioration de la gestion des exceptions
- Meilleure gestion des erreurs de validation
- Messages d'erreur plus explicites
- Gestion spécifique des erreurs d'intégrité de données

### 2. Isolation de la journalisation
- La journalisation utilise maintenant `REQUIRES_NEW` avec `noRollbackFor = Exception.class`
- Si la table `activity_logs` n'existe pas, l'erreur est ignorée (non bloquante)

### 3. Validation améliorée
- Vérification de l'unicité du numéro de vol avant sauvegarde
- Validation des formats de date
- Messages d'erreur plus clairs

## 🔧 Vérifications à Faire

### 1. Vérifier que les nouvelles colonnes existent

Exécutez ce script SQL pour vérifier :

```sql
-- Vérifier les colonnes de la table flights
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'flights' 
ORDER BY ordinal_position;
```

**Colonnes attendues :**
- `cruise_altitude` (INTEGER)
- `cruise_speed` (INTEGER)
- `flight_type` (VARCHAR(20))
- `alternate_airport_id` (BIGINT)
- `estimated_time_enroute` (INTEGER)
- `pilot_id` (BIGINT)

**Si les colonnes n'existent pas**, exécutez :
```bash
psql -U postgres -d flightradar -f backend/database/add_flight_fields.sql
```

### 2. Vérifier que la table activity_logs existe

```sql
-- Vérifier si la table existe
SELECT EXISTS (
   SELECT FROM information_schema.tables 
   WHERE table_name = 'activity_logs'
);
```

**Si la table n'existe pas**, exécutez :
```bash
psql -U postgres -d flightradar -f backend/database/add_activity_logs_table.sql
```

### 3. Vérifier les contraintes

```sql
-- Vérifier les contraintes sur flights
SELECT 
    conname AS constraint_name,
    contype AS constraint_type,
    pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint
WHERE conrelid = 'flights'::regclass;
```

## 📋 Checklist de Diagnostic

1. ✅ **Vérifier les logs du backend** - Regardez les logs Spring Boot pour voir l'erreur exacte
2. ✅ **Vérifier la console du navigateur** - Regardez les détails de l'erreur dans la console
3. ✅ **Vérifier les colonnes de la table flights** - Utilisez le script SQL ci-dessus
4. ✅ **Vérifier la table activity_logs** - Utilisez le script SQL ci-dessus
5. ✅ **Vérifier les données envoyées** - Regardez dans la console du navigateur les données du formulaire

## 🐛 Erreurs Courantes

### Erreur : "column flights.cruise_altitude does not exist"
**Solution :** Exécutez `add_flight_fields.sql`

### Erreur : "relation activity_logs does not exist"
**Solution :** Exécutez `add_activity_logs_table.sql` (ou ignorez si vous ne voulez pas de journalisation)

### Erreur : "duplicate key value violates unique constraint"
**Solution :** Le numéro de vol existe déjà. Choisissez un autre numéro.

### Erreur : "foreign key constraint fails"
**Solution :** Vérifiez que les IDs d'avion, aéroport ou pilote existent dans la base de données.

## 🔄 Test de Création de Vol

Pour tester, utilisez ces données minimales :

```json
{
  "flightNumber": "TEST001",
  "airline": "Test Airline",
  "aircraftId": 1,
  "departureAirportId": 1,
  "arrivalAirportId": 2,
  "scheduledDeparture": "2024-12-20T10:00",
  "scheduledArrival": "2024-12-20T12:00",
  "flightType": "COMMERCIAL",
  "flightStatus": "PLANIFIE"
}
```

## 📝 Logs à Surveiller

Dans les logs Spring Boot, cherchez :
- `Tentative de création de vol par ...`
- `Erreur de validation lors de la création du vol`
- `Erreur d'intégrité des données`
- `Erreur inattendue lors de la création du vol`

Ces messages vous indiqueront la cause exacte du problème.

