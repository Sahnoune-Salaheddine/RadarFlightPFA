# Script pour démarrer le Frontend
Write-Host "🚀 Démarrage du Frontend..." -ForegroundColor Cyan

cd frontend

Write-Host "📦 Installation des dépendances (si nécessaire)..." -ForegroundColor Yellow
npm install

Write-Host "🌐 Démarrage du serveur de développement..." -ForegroundColor Yellow
npm run dev

