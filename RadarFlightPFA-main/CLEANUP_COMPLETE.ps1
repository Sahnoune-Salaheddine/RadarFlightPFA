# Script de nettoyage complet du projet Flight Radar 2026 (PowerShell)
# Supprime tous les fichiers obsolètes (anciennes entités françaises)

Write-Host "🧹 Nettoyage complet du projet Flight Radar 2026..." -ForegroundColor Cyan
Write-Host ""

# Backend - Anciennes entités
Write-Host "📦 Suppression des anciennes entités..." -ForegroundColor Yellow
$filesToDelete = @(
    "backend\src\main\java\com\flightradar\model\Aeroport.java",
    "backend\src\main\java\com\flightradar\model\Avion.java",
    "backend\src\main\java\com\flightradar\model\Pilote.java",
    "backend\src\main\java\com\flightradar\model\CentreRadar.java",
    "backend\src\main\java\com\flightradar\model\Meteo.java",
    "backend\src\main\java\com\flightradar\model\StatutVol.java",
    "backend\src\main\java\com\flightradar\model\TypeCommunication.java",
    "backend\src\main\java\com\flightradar\repository\AeroportRepository.java",
    "backend\src\main\java\com\flightradar\repository\AvionRepository.java",
    "backend\src\main\java\com\flightradar\repository\PiloteRepository.java",
    "backend\src\main\java\com\flightradar\repository\CentreRadarRepository.java",
    "backend\src\main\java\com\flightradar\repository\MeteoRepository.java",
    "backend\src\main\java\com\flightradar\service\AvionService.java",
    "backend\src\main\java\com\flightradar\service\MeteoService.java",
    "backend\src\main\java\com\flightradar\service\CommunicationService.java",
    "backend\src\main\java\com\flightradar\controller\AvionController.java",
    "backend\src\main\java\com\flightradar\controller\AeroportController.java",
    "backend\src\main\java\com\flightradar\controller\MeteoController.java",
    "backend\src\main\java\com\flightradar\controller\CommunicationController.java",
    "frontend\src\components\AvionList.jsx",
    "frontend\src\components\MeteoPanel.jsx"
)

foreach ($file in $filesToDelete) {
    if (Test-Path $file) {
        Remove-Item $file -Force
        Write-Host "  ✓ Supprimé: $file" -ForegroundColor Green
    } else {
        Write-Host "  ⊘ Non trouvé: $file" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "✅ Nettoyage terminé!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Prochaines étapes:" -ForegroundColor Cyan
Write-Host "1. cd backend && mvn clean compile"
Write-Host "2. cd backend && mvn spring-boot:run"
Write-Host "3. cd frontend && npm install && npm run dev"

