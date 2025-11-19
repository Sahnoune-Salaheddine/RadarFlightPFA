#!/bin/bash
# Script de vérification après application des patches
# Usage: ./verify.sh

set -e

echo "🔍 Vérification du projet Flight Radar 2026..."
echo ""

# 1. Vérifier la compilation backend
echo "1️⃣  Vérification de la compilation backend..."
cd backend
if mvn clean compile -q > /dev/null 2>&1; then
    echo "   ✅ Compilation réussie"
else
    echo "   ❌ Erreur de compilation"
    mvn clean compile
    exit 1
fi
cd ..

# 2. Vérifier les références orphelines
echo "2️⃣  Vérification des références orphelines..."
ORPHANED=$(grep -r "import.*Aeroport\|import.*Avion\|import.*Pilote\|import.*CentreRadar\|import.*Meteo" backend/src/main/java --exclude-dir=target 2>/dev/null | grep -v "Airport\|Aircraft\|Pilot\|RadarCenter\|WeatherData" || true)
if [ -z "$ORPHANED" ]; then
    echo "   ✅ Aucune référence orpheline trouvée"
else
    echo "   ⚠️  Références orphelines trouvées:"
    echo "$ORPHANED"
fi

# 3. Vérifier pom.xml
echo "3️⃣  Vérification de pom.xml..."
if grep -q "<name>Flight Radar Backend</name>" backend/pom.xml; then
    echo "   ✅ pom.xml corrigé"
else
    echo "   ❌ pom.xml contient encore <n> au lieu de <name>"
    exit 1
fi

# 4. Vérifier les fichiers frontend
echo "4️⃣  Vérification des fichiers frontend..."
if [ ! -f "frontend/src/components/AvionList.jsx" ] && [ ! -f "frontend/src/components/MeteoPanel.jsx" ]; then
    echo "   ✅ Anciens composants supprimés"
else
    echo "   ⚠️  Anciens composants encore présents"
fi

if [ -f "frontend/src/components/AircraftList.jsx" ] && [ -f "frontend/src/components/WeatherPanel.jsx" ]; then
    echo "   ✅ Nouveaux composants présents"
else
    echo "   ⚠️  Nouveaux composants manquants"
fi

# 5. Vérifier les endpoints dans le frontend
echo "5️⃣  Vérification des endpoints dans le frontend..."
OLD_ENDPOINTS=$(grep -r "/api/avions\|/api/aeroports\|/api/meteo" frontend/src 2>/dev/null || true)
if [ -z "$OLD_ENDPOINTS" ]; then
    echo "   ✅ Aucun ancien endpoint trouvé"
else
    echo "   ⚠️  Anciens endpoints encore utilisés:"
    echo "$OLD_ENDPOINTS"
fi

echo ""
echo "✅ Vérification terminée!"
echo ""
echo "📋 Pour tester le démarrage:"
echo "   cd backend && mvn spring-boot:run"
echo ""
echo "📋 Pour tester le frontend:"
echo "   cd frontend && npm install && npm run dev"

