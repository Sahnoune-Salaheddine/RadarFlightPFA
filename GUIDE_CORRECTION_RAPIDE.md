# 🚀 Guide de Correction Rapide - Liaison Pilote ⇄ Avion

## ⚡ Solution Rapide (3 étapes)

### Étape 1 : Exécuter le Script SQL

**Option A : Via pgAdmin (Recommandé)**
1. Ouvrir pgAdmin
2. Se connecter à PostgreSQL
3. Sélectionner la base `flightradar`
4. Clic droit → Query Tool
5. Ouvrir le fichier `CORRIGER_PILOTE_AVION_RAPIDE.sql`
6. Exécuter (F5)

**Option B : Via PowerShell**
```powershell
.\EXECUTER_CORRECTION_SQL.ps1
```

**Option C : Via psql (ligne de commande)**
```powershell
psql -U postgres -d flightradar -f CORRIGER_PILOTE_AVION_RAPIDE.sql
```

### Étape 2 : Vérifier

Le script affichera automatiquement les données après correction :
```
✅ Vérification | pilote_cmn1 | CN-AT01 | A320 | AU_SOL | AT1001 | Royal Air Maroc
```

### Étape 3 : Tester

1. **Redémarrer le backend** (si nécessaire)
2. **Rafraîchir le frontend** (F5)
3. **Se reconnecter** avec `pilote_cmn1` / `pilote123`
4. **Vérifier** que le dashboard s'affiche

---

## 🔍 Vérification Manuelle (SQL)

Si vous voulez vérifier manuellement :

```sql
-- Vérifier que pilote_cmn1 a un avion
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
- ✅ Registration : `CN-AT01`
- ✅ Model : `A320`
- ✅ Status : `AU_SOL`
- ✅ Flight Number : `AT1001`
- ✅ Airline : `Royal Air Maroc`

---

## 🐛 Si ça ne fonctionne toujours pas

### Vérifier que PostgreSQL est démarré

```powershell
Get-Service -Name "*postgres*"
```

### Vérifier la connexion

```powershell
psql -U postgres -d flightradar -c "SELECT COUNT(*) FROM users WHERE username = 'pilote_cmn1';"
```

### Réinitialiser complètement (ATTENTION : perte de données)

```sql
-- Supprimer toutes les données
TRUNCATE TABLE flights, aircraft, pilots, users, airports CASCADE;

-- Redémarrer le backend - DataInitializer recréera tout
```

---

## ✅ Checklist

- [ ] Script SQL exécuté
- [ ] Vérification SQL réussie
- [ ] Backend redémarré (si nécessaire)
- [ ] Frontend rafraîchi
- [ ] Reconnexion effectuée
- [ ] Dashboard affiché correctement

---

## 📝 Notes

- Le script SQL est **idempotent** : vous pouvez l'exécuter plusieurs fois sans problème
- Il crée les données manquantes si elles n'existent pas
- Il met à jour les données existantes si nécessaire
- Le mot de passe hashé dans le script correspond à `pilote123`

