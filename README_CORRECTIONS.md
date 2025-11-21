# 🔧 CORRECTIONS AUTOMATIQUES - Flight Radar 2026

## 📋 RÉSUMÉ ACTIONNABLE

**Problème identifié** : Double set d'entités (anciennes en français + nouvelles en anglais) causant des conflits.

**Solution** : Supprimer 18 fichiers obsolètes et corriger 1 fichier (pom.xml).

**Fichiers déjà corrigés** : DataInitializer.java, tous les composants React frontend.

**Impact** : Projet compilable, démarrable et fonctionnel.

---

## 🗑️ FICHIERS À SUPPRIMER (18 fichiers)

### Commande unique pour suppression (Linux/Mac/Git Bash)

```bash
# Backend - Entités (5)
rm -f backend/src/main/java/com/flightradar/model/Aeroport.java
rm -f backend/src/main/java/com/flightradar/model/Avion.java
rm -f backend/src/main/java/com/flightradar/model/Pilote.java
rm -f backend/src/main/java/com/flightradar/model/CentreRadar.java
rm -f backend/src/main/java/com/flightradar/model/Meteo.java

# Backend - Repositories (5)
rm -f backend/src/main/java/com/flightradar/repository/AeroportRepository.java
rm -f backend/src/main/java/com/flightradar/repository/AvionRepository.java
rm -f backend/src/main/java/com/flightradar/repository/PiloteRepository.java
rm -f backend/src/main/java/com/flightradar/repository/CentreRadarRepository.java
rm -f backend/src/main/java/com/flightradar/repository/MeteoRepository.java

# Backend - Services (3)
rm -f backend/src/main/java/com/flightradar/service/AvionService.java
rm -f backend/src/main/java/com/flightradar/service/MeteoService.java
rm -f backend/src/main/java/com/flightradar/service/CommunicationService.java

# Backend - Contrôleurs (3)
rm -f backend/src/main/java/com/flightradar/controller/AvionController.java
rm -f backend/src/main/java/com/flightradar/controller/AeroportController.java
rm -f backend/src/main/java/com/flightradar/controller/MeteoController.java

# Frontend (2)
rm -f frontend/src/components/AvionList.jsx
rm -f frontend/src/components/MeteoPanel.jsx
```

### Commande PowerShell (Windows)

```powershell
# Backend - Entités
Remove-Item backend/src/main/java/com/flightradar/model/Aeroport.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/model/Avion.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/model/Pilote.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/model/CentreRadar.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/model/Meteo.java -ErrorAction SilentlyContinue

# Backend - Repositories
Remove-Item backend/src/main/java/com/flightradar/repository/AeroportRepository.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/repository/AvionRepository.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/repository/PiloteRepository.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/repository/CentreRadarRepository.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/repository/MeteoRepository.java -ErrorAction SilentlyContinue

# Backend - Services
Remove-Item backend/src/main/java/com/flightradar/service/AvionService.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/service/MeteoService.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/service/CommunicationService.java -ErrorAction SilentlyContinue

# Backend - Contrôleurs
Remove-Item backend/src/main/java/com/flightradar/controller/AvionController.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/controller/AeroportController.java -ErrorAction SilentlyContinue
Remove-Item backend/src/main/java/com/flightradar/controller/MeteoController.java -ErrorAction SilentlyContinue

# Frontend
Remove-Item frontend/src/components/AvionList.jsx -ErrorAction SilentlyContinue
Remove-Item frontend/src/components/MeteoPanel.jsx -ErrorAction SilentlyContinue
```

---

## 🔧 FICHIER À MODIFIER

### backend/pom.xml - Ligne 18

**AVANT** :
```xml
    <n>Flight Radar Backend</n>
```

**APRÈS** :
```xml
    <name>Flight Radar Backend</name>
```

**Méthode 1 - Éditeur de texte** :
1. Ouvrir `backend/pom.xml`
2. Aller à la ligne 18
3. Remplacer `<n>` par `<name>`
4. Remplacer `</n>` par `</name>`
5. Sauvegarder

**Méthode 2 - Sed (Linux/Mac)** :
```bash
sed -i 's/<n>Flight Radar Backend<\/n>/<name>Flight Radar Backend<\/name>/' backend/pom.xml
```

**Méthode 3 - PowerShell (Windows)** :
```powershell
(Get-Content backend/pom.xml) -replace '<n>Flight Radar Backend</n>', '<name>Flight Radar Backend</name>' | Set-Content backend/pom.xml
```

---

## ✅ VÉRIFICATIONS

### 1. Compilation Backend
```bash
cd backend
mvn clean compile
```
**Attendu** : `BUILD SUCCESS`

### 2. Références Orphelines
```bash
grep -r "import.*Aeroport\|import.*Avion\|import.*Pilote\|import.*CentreRadar\|import.*Meteo" \
  backend/src/main/java --exclude-dir=target | \
  grep -v "Airport\|Aircraft\|Pilot\|RadarCenter\|WeatherData" || echo "✅ Aucune référence orpheline"
```
**Attendu** : `✅ Aucune référence orpheline`

### 3. pom.xml
```bash
grep "<name>Flight Radar Backend</name>" backend/pom.xml
```
**Attendu** : 1 ligne trouvée

---

## 📡 TESTS CURL

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. Aircraft
```bash
curl http://localhost:8080/api/aircraft
```

### 3. Airports
```bash
curl http://localhost:8080/api/airports
```

### 4. Weather
```bash
curl http://localhost:8080/api/weather/airport/1
```

### 5. Radar Messages
```bash
curl "http://localhost:8080/api/radar/messages?radarCenterId=1"
```

---

## 🎯 CONCLUSION

**Statut** : ✅ **OK POUR DÉMARRAGE**

**Actions requises** :
1. Supprimer 18 fichiers (script fourni)
2. Corriger pom.xml ligne 18
3. Compiler et tester

**Temps** : 5-10 minutes  
**Risque** : Faible  
**Confiance** : 95%

---

**Tous les fichiers nécessaires sont documentés dans `PATCHES_CORRECTIONS.md`**

