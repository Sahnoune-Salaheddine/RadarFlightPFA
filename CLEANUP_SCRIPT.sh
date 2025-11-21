#!/bin/bash
# Script de nettoyage complet du projet Flight Radar 2026
# Supprime tous les fichiers obsolètes (anciennes entités françaises)

set -e

echo "🧹 Nettoyage du projet Flight Radar 2026..."
echo ""

# Backend - Anciennes entités
echo "📦 Suppression des anciennes entités..."
rm -f backend/src/main/java/com/flightradar/model/Aeroport.java
rm -f backend/src/main/java/com/flightradar/model/Avion.java
rm -f backend/src/main/java/com/flightradar/model/Pilote.java
rm -f backend/src/main/java/com/flightradar/model/CentreRadar.java
rm -f backend/src/main/java/com/flightradar/model/Meteo.java
rm -f backend/src/main/java/com/flightradar/model/StatutVol.java
rm -f backend/src/main/java/com/flightradar/model/TypeCommunication.java

# Backend - Anciens repositories
echo "📦 Suppression des anciens repositories..."
rm -f backend/src/main/java/com/flightradar/repository/AeroportRepository.java
rm -f backend/src/main/java/com/flightradar/repository/AvionRepository.java
rm -f backend/src/main/java/com/flightradar/repository/PiloteRepository.java
rm -f backend/src/main/java/com/flightradar/repository/CentreRadarRepository.java
rm -f backend/src/main/java/com/flightradar/repository/MeteoRepository.java

# Backend - Anciens services
echo "📦 Suppression des anciens services..."
rm -f backend/src/main/java/com/flightradar/service/AvionService.java
rm -f backend/src/main/java/com/flightradar/service/MeteoService.java
rm -f backend/src/main/java/com/flightradar/service/CommunicationService.java

# Backend - Anciens contrôleurs
echo "📦 Suppression des anciens contrôleurs..."
rm -f backend/src/main/java/com/flightradar/controller/AvionController.java
rm -f backend/src/main/java/com/flightradar/controller/AeroportController.java
rm -f backend/src/main/java/com/flightradar/controller/MeteoController.java
rm -f backend/src/main/java/com/flightradar/controller/CommunicationController.java

# Frontend - Anciens composants
echo "📦 Suppression des anciens composants frontend..."
rm -f frontend/src/components/AvionList.jsx
rm -f frontend/src/components/MeteoPanel.jsx

# Corriger pom.xml
echo "🔧 Correction de pom.xml..."
sed -i.bak 's/<n>Flight Radar Backend<\/n>/<name>Flight Radar Backend<\/name>/' backend/pom.xml 2>/dev/null || true
rm -f backend/pom.xml.bak

echo ""
echo "✅ Nettoyage terminé!"
echo ""
echo "📋 Prochaines étapes:"
echo "1. cd backend && mvn clean compile"
echo "2. cd backend && mvn spring-boot:run"
echo "3. cd frontend && npm install && npm run dev"

