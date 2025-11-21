# 🔍 Comment Voir les Logs du Backend pour Identifier l'Erreur Exacte

## 📋 ÉTAPE 1 : Localiser la Console Spring Boot

Le backend Spring Boot affiche les logs dans la **console où vous avez lancé** `mvn spring-boot:run`.

**Où chercher :**
- Terminal/PowerShell où vous avez exécuté `mvn spring-boot:run`
- Console IntelliJ/Eclipse si vous utilisez un IDE
- Fenêtre de terminal séparée

---

## 🔍 ÉTAPE 2 : Identifier les Logs d'Erreur

Quand vous essayez de créer un vol, cherchez dans les logs ces lignes :

### Logs à Chercher :

1. **Début de la création :**
   ```
   === DÉBUT CRÉATION VOL ===
   Données reçues: {...}
   ```

2. **Erreur d'intégrité :**
   ```
   ❌ ERREUR D'INTÉGRITÉ LORS DE LA SAUVEGARDE
   Message: ...
   Cause: ...
   Message de la cause: ...
   ```

3. **Erreur runtime :**
   ```
   ❌ ERREUR RUNTIME
   Message: ...
   Cause: ...
   Message de la cause: ...
   ```

4. **Erreur inattendue :**
   ```
   ❌ ERREUR INATTENDUE LORS DE LA SAUVEGARDE
   Type d'exception: ...
   Message d'erreur: ...
   Stack trace: ...
   ```

---

## 📸 ÉTAPE 3 : Copier l'Erreur Complète

**Copiez TOUTE la section d'erreur**, notamment :

1. **Le message d'erreur principal**
2. **La cause (cause)**
3. **Le message de la cause**
4. **Le stack trace complet** (si disponible)

**Exemple de ce qu'il faut copier :**
```
❌ ERREUR RUNTIME
Message: Erreur lors de la sauvegarde du vol: ...
Cause: org.hibernate.exception.SQLGrammarException
Message de la cause: ERROR: column "xxx" does not exist
  Position: XXX
Stack trace:
  at com.flightradar.service.FlightManagementService.createFlight(...)
  ...
```

---

## 🔧 ÉTAPE 4 : Si Vous Ne Voyez Pas les Logs

### Option A : Vérifier que le Backend Tourne

```bash
# Vérifier les processus Java
jps -l | grep flightradar
```

### Option B : Relancer le Backend avec Plus de Logs

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.flightradar=DEBUG"
```

### Option C : Vérifier les Fichiers de Log

Si Spring Boot écrit dans un fichier de log, cherchez dans :
- `backend/logs/application.log`
- `backend/target/spring-boot.log`

---

## 🎯 ÉTAPE 5 : Analyser l'Erreur

Une fois que vous avez l'erreur complète, analysez :

### Erreur SQL Directe
Si vous voyez :
```
ERROR: column "xxx" does not exist
```
→ **Action :** Exécutez le script de correction SQL

### Erreur de Contrainte
Si vous voyez :
```
duplicate key value violates unique constraint
```
→ **Action :** Utilisez un autre numéro de vol

### Erreur de Clé Étrangère
Si vous voyez :
```
violates foreign key constraint
```
→ **Action :** Vérifiez que les IDs (avion, aéroport) existent

### Erreur de Format
Si vous voyez :
```
Invalid date format
```
→ **Action :** Vérifiez le format des dates envoyées

---

## 📋 CHECKLIST

- [ ] Console Spring Boot ouverte et visible
- [ ] Tentative de création de vol effectuée
- [ ] Logs d'erreur identifiés dans la console
- [ ] Erreur complète copiée (message + cause + stack trace)
- [ ] Erreur analysée pour identifier la cause

---

## 🆘 SI VOUS NE TROUVEZ PAS LES LOGS

1. **Vérifiez que le backend est bien démarré**
   - Vous devriez voir : `Started FlightRadarApplication in X.XXX seconds`

2. **Vérifiez la connexion à la base de données**
   - Vous devriez voir : `HikariPool-1 - Starting...` puis `HikariPool-1 - Start completed`

3. **Essayez de créer un vol et regardez immédiatement la console**
   - Les logs apparaissent en temps réel

4. **Prenez une capture d'écran de la console complète**

---

**Une fois que vous avez l'erreur exacte, partagez-la pour qu'on puisse la corriger précisément !**

