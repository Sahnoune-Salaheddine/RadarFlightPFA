# Script pour ajouter PostgreSQL au PATH de manière permanente
# Exécuter en tant qu'administrateur pour modifier le PATH système

Write-Host "🔧 Ajout de PostgreSQL au PATH..." -ForegroundColor Cyan
Write-Host ""

# Détecter la version de PostgreSQL
$postgresService = Get-Service -Name "*postgres*" | Select-Object -First 1
if (-not $postgresService) {
    Write-Host "❌ Aucun service PostgreSQL trouvé" -ForegroundColor Red
    exit 1
}

# Détecter la version (16 dans votre cas)
$postgresVersion = "16"
$postgresPath = "C:\Program Files\PostgreSQL\$postgresVersion\bin"

# Vérifier si le répertoire existe
if (-not (Test-Path $postgresPath)) {
    Write-Host "❌ Répertoire non trouvé : $postgresPath" -ForegroundColor Red
    Write-Host "Recherche d'autres versions..." -ForegroundColor Yellow
    
    # Chercher dans les versions communes
    $versions = @("16", "15", "14", "13", "12")
    $found = $false
    foreach ($ver in $versions) {
        $testPath = "C:\Program Files\PostgreSQL\$ver\bin"
        if (Test-Path $testPath) {
            $postgresPath = $testPath
            $postgresVersion = $ver
            $found = $true
            Write-Host "✅ Trouvé : $postgresPath" -ForegroundColor Green
            break
        }
    }
    
    if (-not $found) {
        Write-Host "❌ Impossible de trouver PostgreSQL" -ForegroundColor Red
        exit 1
    }
}

Write-Host "📁 Chemin PostgreSQL : $postgresPath" -ForegroundColor Yellow
Write-Host ""

# Vérifier si déjà dans le PATH
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($currentPath -like "*$postgresPath*") {
    Write-Host "✅ PostgreSQL est déjà dans le PATH utilisateur" -ForegroundColor Green
} else {
    Write-Host "➕ Ajout de PostgreSQL au PATH utilisateur..." -ForegroundColor Yellow
    
    # Ajouter au PATH utilisateur
    $newPath = $currentPath + ";$postgresPath"
    [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
    
    Write-Host "✅ PostgreSQL ajouté au PATH utilisateur" -ForegroundColor Green
}

# Vérifier le PATH système (nécessite admin)
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if ($isAdmin) {
    $systemPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
    if ($systemPath -like "*$postgresPath*") {
        Write-Host "✅ PostgreSQL est déjà dans le PATH système" -ForegroundColor Green
    } else {
        Write-Host "➕ Ajout de PostgreSQL au PATH système..." -ForegroundColor Yellow
        $newSystemPath = $systemPath + ";$postgresPath"
        [Environment]::SetEnvironmentVariable("Path", $newSystemPath, "Machine")
        Write-Host "✅ PostgreSQL ajouté au PATH système" -ForegroundColor Green
    }
} else {
    Write-Host "⚠️  Pour ajouter au PATH système, relancer en tant qu'administrateur" -ForegroundColor Yellow
}

# Mettre à jour le PATH de la session actuelle
$env:Path += ";$postgresPath"

Write-Host ""
Write-Host "✅ Configuration terminée !" -ForegroundColor Green
Write-Host ""
Write-Host "🧪 Test de la commande psql..." -ForegroundColor Cyan

# Tester psql
try {
    $psqlVersion = & "$postgresPath\psql.exe" --version 2>&1
    Write-Host "✅ PostgreSQL accessible : $psqlVersion" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Redémarrer PowerShell pour que les changements prennent effet" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "📝 Prochaines étapes :" -ForegroundColor Cyan
Write-Host "1. Fermer et rouvrir PowerShell (ou redémarrer le terminal)" -ForegroundColor White
Write-Host "2. Tester : psql --version" -ForegroundColor White
Write-Host "3. Créer la base : psql -U postgres -c 'CREATE DATABASE flightradar;'" -ForegroundColor White
Write-Host ""

