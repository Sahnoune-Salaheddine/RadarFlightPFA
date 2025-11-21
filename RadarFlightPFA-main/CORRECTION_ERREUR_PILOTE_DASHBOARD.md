# 🔧 Correction - Erreur Dashboard Pilote

## ❌ ERREUR

```
Query did not return a unique result: 2 results were returned
```

**Localisation :** `PilotDashboardController` - Récupération du dashboard pour `pilote_cmn1`

**Cause :** La méthode `findByAirportId` dans `RadarCenterRepository` retourne plusieurs résultats alors qu'un seul est attendu.

---

## ✅ CORRECTION APPLIQUÉE

### 1. Repository Amélioré

**Fichier :** `backend/src/main/java/com/flightradar/repository/RadarCenterRepository.java`

**Ajout :**
```java
List<RadarCenter> findAllByAirportId(Long airportId); // Pour gérer plusieurs résultats
```

### 2. Service Corrigé

**Fichier :** `backend/src/main/java/com/flightradar/service/PilotDashboardService.java`

**Avant :**
```java
Optional<RadarCenter> radarOpt = radarCenterRepository.findByAirportId(currentAirport.getId());
```

**Après :**
```java
List<RadarCenter> radarCenters = radarCenterRepository.findAllByAirportId(currentAirport.getId());
if (!radarCenters.isEmpty()) {
    dto.setRadarCenterName(radarCenters.get(0).getName()); // Prendre le premier
}
```

---

## 🔍 VÉRIFICATION DES DOUBLONS

### Option 1 : Script SQL

Exécutez le script pour vérifier les doublons :

```powershell
psql -U postgres -d flightradar -f backend/database/VERIFIER_DOUBLONS_RADAR_CENTERS.sql
```

### Option 2 : Requête Directe

```sql
-- Vérifier les doublons
SELECT 
    airport_id,
    COUNT(*) as nombre_centres
FROM radar_centers
WHERE airport_id IS NOT NULL
GROUP BY airport_id
HAVING COUNT(*) > 1;
```

---

## 🚀 ACTION REQUISE

### Redémarrer le Backend

**⚠️ IMPORTANT :** Le backend DOIT être redémarré pour que la correction prenne effet.

```bash
# Arrêter le backend (Ctrl+C)
# Puis redémarrer :
cd backend
mvn spring-boot:run
```

### Tester le Dashboard Pilote

1. **Rafraîchir le frontend** (F5)
2. **Se connecter** en tant que pilote (`pilote_cmn1`)
3. **Vérifier** que le dashboard se charge sans erreur

---

## 🔧 CORRECTION DES DOUBLONS (Optionnel)

Si vous voulez supprimer les doublons dans la base de données :

1. **Vérifier les doublons** avec le script SQL
2. **Décommenter la section de suppression** dans `VERIFIER_DOUBLONS_RADAR_CENTERS.sql`
3. **Exécuter** le script

**OU** manuellement :

```sql
-- Pour chaque aéroport avec plusieurs centres radar, garder seulement le premier
DELETE FROM radar_centers
WHERE id NOT IN (
    SELECT MIN(id)
    FROM radar_centers
    WHERE airport_id IS NOT NULL
    GROUP BY airport_id
);
```

---

## ✅ RÉSULTAT ATTENDU

Après redémarrage du backend :
- ✅ Le dashboard pilote se charge sans erreur
- ✅ Les centres radar sont correctement récupérés (premier trouvé si plusieurs)
- ✅ Aucune erreur "Query did not return a unique result"

---

**Date :** 2025-01-XX  
**Statut :** ✅ Correction appliquée, redémarrage du backend requis

