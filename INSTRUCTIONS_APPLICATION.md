# 📋 INSTRUCTIONS D'APPLICATION DES PATCHES

## 🚀 MÉTHODE RAPIDE (Recommandée)

### Option 1 : Script automatique (Linux/Mac/Git Bash)

```bash
# Rendre le script exécutable
chmod +x apply-patches.sh

# Exécuter le script
./apply-patches.sh

# Vérifier les corrections
./verify.sh
```

### Option 2 : Commandes manuelles

```bash
# Supprimer les anciens fichiers backend
rm backend/src/main/java/com/flightradar/model/Aeroport.java
rm backend/src/main/java/com/flightradar/model/Avion.java
rm backend/src/main/java/com/flightradar/model/Pilote.java
rm backend/src/main/java/com/flightradar/model/CentreRadar.java
rm backend/src/main/java/com/flightradar/model/Meteo.java
rm backend/src/main/java/com/flightradar/repository/AeroportRepository.java
rm backend/src/main/java/com/flightradar/repository/AvionRepository.java
rm backend/src/main/java/com/flightradar/repository/PiloteRepository.java
rm backend/src/main/java/com/flightradar/repository/CentreRadarRepository.java
rm backend/src/main/java/com/flightradar/repository/MeteoRepository.java
rm backend/src/main/java/com/flightradar/service/AvionService.java
rm backend/src/main/java/com/flightradar/service/MeteoService.java
rm backend/src/main/java/com/flightradar/service/CommunicationService.java
rm backend/src/main/java/com/flightradar/controller/AvionController.java
rm backend/src/main/java/com/flightradar/controller/AeroportController.java
rm backend/src/main/java/com/flightradar/controller/MeteoController.java

# Supprimer les anciens fichiers frontend
rm frontend/src/components/AvionList.jsx
rm frontend/src/components/MeteoPanel.jsx

# Corriger pom.xml
sed -i 's/<n>Flight Radar Backend<\/n>/<name>Flight Radar Backend<\/name>/' backend/pom.xml
# OU manuellement : éditer backend/pom.xml ligne 18 et remplacer <n> par <name>
```

### Option 3 : Git (si vous utilisez git)

```bash
# Appliquer le patch pom.xml
cd backend
git apply ../pom.xml.patch
cd ..

# Supprimer les fichiers
git rm backend/src/main/java/com/flightradar/model/Aeroport.java
git rm backend/src/main/java/com/flightradar/model/Avion.java
git rm backend/src/main/java/com/flightradar/model/Pilote.java
git rm backend/src/main/java/com/flightradar/model/CentreRadar.java
git rm backend/src/main/java/com/flightradar/model/Meteo.java
git rm backend/src/main/java/com/flightradar/repository/AeroportRepository.java
git rm backend/src/main/java/com/flightradar/repository/AvionRepository.java
git rm backend/src/main/java/com/flightradar/repository/PiloteRepository.java
git rm backend/src/main/java/com/flightradar/repository/CentreRadarRepository.java
git rm backend/src/main/java/com/flightradar/repository/MeteoRepository.java
git rm backend/src/main/java/com/flightradar/service/AvionService.java
git rm backend/src/main/java/com/flightradar/service/MeteoService.java
git rm backend/src/main/java/com/flightradar/service/CommunicationService.java
git rm backend/src/main/java/com/flightradar/controller/AvionController.java
git rm backend/src/main/java/com/flightradar/controller/AeroportController.java
git rm backend/src/main/java/com/flightradar/controller/MeteoController.java
git rm frontend/src/components/AvionList.jsx
git rm frontend/src/components/MeteoPanel.jsx
```

---

## ✅ VÉRIFICATIONS POST-APPLICATION

### 1. Compilation Backend

```bash
cd backend
mvn clean compile
```

**Attendu** : `BUILD SUCCESS`

### 2. Vérification des références orphelines

```bash
# Doit retourner 0 résultats
grep -r "import.*Aeroport\|import.*Avion\|import.*Pilote\|import.*CentreRadar\|import.*Meteo" backend/src/main/java --exclude-dir=target | grep -v "Airport\|Aircraft\|Pilot\|RadarCenter\|WeatherData"
```

### 3. Vérification pom.xml

```bash
# Doit trouver la ligne avec <name>
grep "<name>Flight Radar Backend</name>" backend/pom.xml
```

### 4. Test de démarrage (optionnel)

```bash
cd backend
mvn spring-boot:run
# Attendre "Started FlightRadarApplication"
# Ctrl+C pour arrêter
```

---

## 📝 NOTES IMPORTANTES

1. **Windows** : Si vous êtes sur Windows sans Git Bash, utilisez PowerShell ou l'éditeur de texte pour :
   - Supprimer manuellement les fichiers listés
   - Éditer `backend/pom.xml` ligne 18 : remplacer `<n>` par `<name>`

2. **Base de données** : Si vous avez déjà une base de données avec les anciennes tables, vous pouvez :
   - La supprimer et la recréer : `dropdb flightradar && createdb flightradar`
   - OU laisser Hibernate recréer les tables avec `spring.jpa.hibernate.ddl-auto=update`

3. **Fichiers déjà corrigés** : Les fichiers suivants ont déjà été corrigés dans les modifications précédentes :
   - `DataInitializer.java` ✅
   - `FlightMap.jsx` ✅
   - `AircraftList.jsx` ✅
   - `WeatherPanel.jsx` ✅
   - `Dashboard.jsx` ✅
   - `CommunicationPanel.jsx` ✅
   - `AlertPanel.jsx` ✅

---

## 🎯 RÉSULTAT ATTENDU

Après application des patches :
- ✅ Le backend compile sans erreur
- ✅ Le backend démarre sans crash
- ✅ La base de données est initialisée avec 4 aéroports, 8 avions, 8 pilotes, 13 utilisateurs
- ✅ Le frontend se connecte au backend
- ✅ La carte affiche les aéroports et avions
- ✅ Les données météo s'affichent
- ✅ Les communications fonctionnent

---

**Temps estimé** : 5-10 minutes
**Difficulté** : Facile
**Risque** : Faible

