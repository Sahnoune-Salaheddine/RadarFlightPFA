# 🔧 Résolution de l'Erreur "Query did not return a unique result: 2 results were returned"

## 📋 Problème Identifié

L'erreur indique qu'il y a **des doublons dans la table `pilots`** - plusieurs pilotes ont le même `user_id`, ce qui viole la relation `@OneToOne` entre `Pilot` et `User`.

### Erreur Complète
```
Query did not return a unique result: 2 results were returned
```

### Cause
- Plusieurs enregistrements `pilots` avec le même `user_id` dans la base de données
- La méthode `findByUserId` dans `PilotRepository` s'attend à un seul résultat mais en trouve plusieurs
- La relation `@OneToOne` entre `Pilot` et `User` n'est pas respectée au niveau de la base de données

## ✅ Solution

### Étape 1 : Nettoyer les doublons

Exécutez le script SQL pour supprimer les doublons :

```powershell
.\EXECUTER_NETTOYAGE_DOUBLONS.ps1
```

**OU** exécutez directement le script SQL :

```powershell
psql -U postgres -d flightradar -f backend\database\NETTOYER_DOUBLONS_PILOTES.sql
```

### Étape 2 : Vérifier les doublons

Connectez-vous à PostgreSQL et vérifiez :

```sql
SELECT 
    user_id,
    COUNT(*) as nombre_pilotes
FROM pilots
WHERE user_id IS NOT NULL
GROUP BY user_id
HAVING COUNT(*) > 1;
```

**Si cette requête ne retourne aucun résultat, c'est bon !**

### Étape 3 : Redémarrer le backend

```powershell
cd backend
mvn spring-boot:run
```

### Étape 4 : Tester le dashboard pilote

1. Connectez-vous en tant que pilote (`pilote_cmn1`)
2. Accédez au dashboard : `http://localhost:3000/pilot`
3. L'erreur devrait être résolue

## 🔍 Détails Techniques

### Code Corrigé

**Avant** (problématique) :
```java
Optional<Pilot> pilotOpt = pilotRepository.findAll().stream()
    .filter(p -> {
        if (p.getUser() == null) return false;
        return p.getUser().getId().equals(user.getId());
    })
    .findFirst();
```

**Après** (corrigé) :
```java
Optional<Pilot> pilotOpt = pilotRepository.findByUserId(user.getId());
```

### Contrainte Unique

Le script SQL ajoute une contrainte unique sur `user_id` pour éviter les doublons futurs :

```sql
ALTER TABLE pilots ADD CONSTRAINT pilots_user_id_unique UNIQUE (user_id);
```

### Relation @OneToOne

La relation `@OneToOne` dans l'entité `Pilot` :

```java
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
```

Cette relation devrait garantir qu'il n'y a qu'un seul pilote par utilisateur, mais si la base de données contient des doublons, cela cause l'erreur.

## 🚨 Dépannage

### Si le script échoue

1. Vérifiez que PostgreSQL est démarré
2. Vérifiez que la base de données `flightradar` existe
3. Vérifiez que l'utilisateur `postgres` a les permissions nécessaires

### Si l'erreur persiste

1. Vérifiez manuellement les doublons :
   ```sql
   SELECT * FROM pilots WHERE user_id IN (
       SELECT user_id FROM pilots 
       WHERE user_id IS NOT NULL 
       GROUP BY user_id 
       HAVING COUNT(*) > 1
   );
   ```

2. Supprimez manuellement les doublons (gardez le premier) :
   ```sql
   DELETE FROM pilots 
   WHERE id NOT IN (
       SELECT MIN(id) FROM pilots 
       GROUP BY user_id
   );
   ```

3. Redémarrez le backend

## 📚 Fichiers Créés

- `backend/database/NETTOYER_DOUBLONS_PILOTES.sql` : Script SQL pour nettoyer les doublons
- `EXECUTER_NETTOYAGE_DOUBLONS.ps1` : Script PowerShell pour exécuter le nettoyage
- `RESOLUTION_ERREUR_DOUBLONS_PILOTES.md` : Ce document

## ✅ Résultat Attendu

Après avoir exécuté le script :
- ✅ Un seul pilote par `user_id`
- ✅ Contrainte unique ajoutée sur `user_id`
- ✅ Le dashboard pilote fonctionne sans erreur
- ✅ Plus d'erreur "Query did not return a unique result"

