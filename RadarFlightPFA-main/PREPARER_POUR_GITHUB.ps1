# Script PowerShell pour préparer le projet pour GitHub

Write-Host "=== Préparation du Projet pour GitHub ===" -ForegroundColor Cyan
Write-Host ""

# Vérifier si Git est installé
try {
    $gitVersion = git --version
    Write-Host "✅ Git installé: $gitVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Git n'est pas installé. Installez Git depuis https://git-scm.com/" -ForegroundColor Red
    exit 1
}

# Vérifier si .git existe
if (Test-Path .git) {
    Write-Host "✅ Git est déjà initialisé" -ForegroundColor Green
} else {
    Write-Host "Initialisation de Git..." -ForegroundColor Yellow
    git init
    Write-Host "✅ Git initialisé" -ForegroundColor Green
}

Write-Host ""
Write-Host "Vérification des fichiers sensibles..." -ForegroundColor Yellow

# Vérifier que application.properties n'est pas suivi
$appProps = "backend/src/main/resources/application.properties"
if (Test-Path $appProps) {
    Write-Host "⚠️  Fichier application.properties trouvé" -ForegroundColor Yellow
    Write-Host "   Ce fichier contient des mots de passe et sera ignoré par .gitignore" -ForegroundColor Yellow
    
    # Vérifier s'il est déjà suivi
    $tracked = git ls-files $appProps 2>&1
    if ($tracked) {
        Write-Host "   ⚠️  Le fichier est déjà suivi par Git !" -ForegroundColor Red
        Write-Host "   Exécutez: git rm --cached $appProps" -ForegroundColor Yellow
    } else {
        Write-Host "   ✅ Le fichier sera ignoré (dans .gitignore)" -ForegroundColor Green
    }
} else {
    Write-Host "✅ application.properties n'existe pas (normal si pas encore configuré)" -ForegroundColor Green
}

# Vérifier que application.properties.example existe
$appPropsExample = "backend/src/main/resources/application.properties.example"
if (Test-Path $appPropsExample) {
    Write-Host "✅ application.properties.example existe" -ForegroundColor Green
} else {
    Write-Host "⚠️  application.properties.example n'existe pas" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== État des Fichiers ===" -ForegroundColor Cyan
Write-Host ""

# Afficher le statut Git
Write-Host "Fichiers modifiés/nouveaux:" -ForegroundColor Yellow
git status --short | Select-Object -First 20

Write-Host ""
Write-Host "=== Prochaines Étapes ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Vérifiez que application.properties n'est PAS dans la liste ci-dessus" -ForegroundColor White
Write-Host "2. Si nécessaire, exécutez:" -ForegroundColor White
Write-Host "   git rm --cached backend/src/main/resources/application.properties" -ForegroundColor Yellow
Write-Host ""
Write-Host "3. Ajoutez tous les fichiers:" -ForegroundColor White
Write-Host "   git add ." -ForegroundColor Yellow
Write-Host ""
Write-Host "4. Créez le premier commit:" -ForegroundColor White
Write-Host "   git commit -m 'Initial commit: FlightRadar24-like system'" -ForegroundColor Yellow
Write-Host ""
Write-Host "5. Créez un repository sur GitHub, puis:" -ForegroundColor White
Write-Host "   git remote add origin https://github.com/VOTRE_USERNAME/PFA-2026.git" -ForegroundColor Yellow
Write-Host "   git branch -M main" -ForegroundColor Yellow
Write-Host "   git push -u origin main" -ForegroundColor Yellow
Write-Host ""
Write-Host "Consultez GUIDE_GITHUB.md pour plus de details" -ForegroundColor Cyan
Write-Host ""

