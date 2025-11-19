# 🚀 Guide Rapide : Fix "Aucun avion assigné"

## ⚡ Solution Rapide (2 minutes)

### Option 1 : pgAdmin (Recommandé)

1. **Ouvrir pgAdmin**
2. **Se connecter** à PostgreSQL
3. **Sélectionner** la base de données `flightradar`
4. **Ouvrir** Query Tool (clic droit → Query Tool)
5. **Copier-coller** le contenu de `FIX_ASSIGNER_AVION.sql`
6. **Exécuter** (F5 ou bouton Play)
7. **Rafraîchir** le frontend

### Option 2 : Ligne de Commande (psql)

```powershell
# Se connecter à PostgreSQL
psql -U postgres -d flightradar

# Puis exécuter :
UPDATE aircraft 
SET 
    pilot_id = (SELECT p.id FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1' LIMIT 1),
    username_pilote = 'pilote_cmn1'
WHERE id = (SELECT id FROM aircraft WHERE pilot_id IS NULL LIMIT 1);
```

### Option 3 : Script PowerShell

```powershell
.\VERIFIER_ET_ASSIGNER_AVION.ps1
```

---

## ✅ Vérification

Après avoir exécuté le script :

1. **Rafraîchir le frontend** (F5)
2. **Se reconnecter** si nécessaire
3. **Vérifier** que le dashboard s'affiche

---

## 🔍 Si ça ne fonctionne pas

### Vérifier que le pilote existe

```sql
SELECT id, username FROM users WHERE username = 'pilote_cmn1';
```

### Vérifier que le profil pilote existe

```sql
SELECT p.id, p.name, u.username 
FROM pilots p 
JOIN users u ON p.user_id = u.id 
WHERE u.username = 'pilote_cmn1';
```

### Vérifier l'assignation

```sql
SELECT a.registration, a.model, a.pilot_id, a.username_pilote, u.username
FROM aircraft a
LEFT JOIN pilots p ON a.pilot_id = p.id
LEFT JOIN users u ON p.user_id = u.id
WHERE u.username = 'pilote_cmn1' OR a.username_pilote = 'pilote_cmn1';
```

---

## 📝 Fichiers Disponibles

- ✅ `FIX_ASSIGNER_AVION.sql` - Script SQL simple et rapide
- ✅ `ASSIGNER_AVION_PILOTE.sql` - Script SQL complet avec vérifications
- ✅ `VERIFIER_ET_ASSIGNER_AVION.ps1` - Script PowerShell automatique
- ✅ `SOLUTION_AUCUN_AVION_ASSIGNE.md` - Guide détaillé

---

## 💡 Astuce

Si vous voulez réinitialiser complètement la base de données :

1. **Supprimer** toutes les données
2. **Redémarrer** le backend
3. Le `DataInitializer` créera automatiquement tous les pilotes et avions

