#!/bin/bash
# Script d'application des patches pour Flight Radar 2026
# Usage: ./apply-patches.sh

set -e  # Arrêter en cas d'erreur

echo "🔧 Application des patches de correction..."

# Supprimer les anciennes entités
echo "📦 Suppression des anciennes entités..."
rm -f backend/src/main/java/com/flightradar/model/Aeroport.java
rm -f backend/src/main/java/com/flightradar/model/Avion.java
rm -f backend/src/main/java/com/flightradar/model/Pilote.java
rm -f backend/src/main/java/com/flightradar/model/CentreRadar.java
rm -f backend/src/main/java/com/flightradar/model/Meteo.java

# Supprimer les anciens repositories
echo "📦 Suppression des anciens repositories..."
rm -f backend/src/main/java/com/flightradar/repository/AeroportRepository.java
rm -f backend/src/main/java/com/flightradar/repository/AvionRepository.java
rm -f backend/src/main/java/com/flightradar/repository/PiloteRepository.java
rm -f backend/src/main/java/com/flightradar/repository/CentreRadarRepository.java
rm -f backend/src/main/java/com/flightradar/repository/MeteoRepository.java

# Supprimer les anciens services
echo "📦 Suppression des anciens services..."
rm -f backend/src/main/java/com/flightradar/service/AvionService.java
rm -f backend/src/main/java/com/flightradar/service/MeteoService.java
rm -f backend/src/main/java/com/flightradar/service/CommunicationService.java

# Supprimer les anciens contrôleurs
echo "📦 Suppression des anciens contrôleurs..."
rm -f backend/src/main/java/com/flightradar/controller/AvionController.java
rm -f backend/src/main/java/com/flightradar/controller/AeroportController.java
rm -f backend/src/main/java/com/flightradar/controller/MeteoController.java

# Supprimer les anciens composants frontend
echo "📦 Suppression des anciens composants frontend..."
rm -f frontend/src/components/AvionList.jsx
rm -f frontend/src/components/MeteoPanel.jsx

# Corriger pom.xml
echo "🔧 Correction de pom.xml..."
sed -i.bak 's/<n>Flight Radar Backend<\/n>/<name>Flight Radar Backend<\/name>/' backend/pom.xml
rm -f backend/pom.xml.bak

echo "✅ Patches appliqués avec succès!"
echo ""
echo "📋 Prochaines étapes:"
echo "1. cd backend && mvn clean compile"
echo "2. cd backend && mvn spring-boot:run"
echo "3. cd frontend && npm install && npm run dev"
echo ""
echo "🔍 Vérification des références orphelines..."
echo "Recherche des imports des anciennes entités..."
grep -r "import.*Aeroport\|import.*Avion\|import.*Pilote\|import.*CentreRadar\|import.*Meteo" backend/src/main/java --exclude-dir=target 2>/dev/null | grep -v "Airport\|Aircraft\|Pilot\|RadarCenter\|WeatherData" || echo "✅ Aucune référence orpheline trouvée"

