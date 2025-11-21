# Script PowerShell pour exécuter les migrations SQL
# Usage: .\EXECUTER_MIGRATIONS.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  EXÉCUTION DES MIGRATIONS SQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Chemin du projet
$projectPath = $PSScriptRoot
$dbPath = Join-Path $projectPath "backend\database"

# Chemin PostgreSQL (ajuster si nécessaire)
$postgresPath = "C:\Program Files\PostgreSQL\16\bin"
if (-not (Test-Path $postgresPath)) {
    $postgresPath = "C:\Program Files\PostgreSQL\15\bin"
    if (-not (Test-Path $postgresPath)) {
        Write-Host "❌ PostgreSQL non trouvé dans les emplacements standards" -ForegroundColor Red
        Write-Host "Veuillez spécifier le chemin manuellement" -ForegroundColor Yellow
        exit 1
    }
}

$psqlExe = Join-Path $postgresPath "psql.exe"

# Vérifier que psql existe
if (-not (Test-Path $psqlExe)) {
    Write-Host "❌ psql.exe non trouvé à: $psqlExe" -ForegroundColor Red
    exit 1
}

Write-Host "✅ PostgreSQL trouvé: $psqlExe" -ForegroundColor Green
Write-Host ""

# Demander le mot de passe PostgreSQL
$password = Read-Host "Entrez le mot de passe PostgreSQL (postgres)" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
)

# Définir la variable d'environnement pour le mot de passe
$env:PGPASSWORD = $passwordPlain

# Scripts à exécuter
$scripts = @(
    @{
        Name = "Ajout des colonnes à la table flights"
        File = "add_flight_fields.sql"
    },
    @{
        Name = "Création de la table activity_logs"
        File = "add_activity_logs_table.sql"
    }
)

Write-Host "Exécution des scripts de migration..." -ForegroundColor Yellow
Write-Host ""

foreach ($script in $scripts) {
    $scriptPath = Join-Path $dbPath $script.File
    
    if (-not (Test-Path $scriptPath)) {
        Write-Host "❌ Script non trouvé: $scriptPath" -ForegroundColor Red
        continue
    }
    
    Write-Host "📄 Exécution: $($script.Name)" -ForegroundColor Cyan
    Write-Host "   Fichier: $scriptPath" -ForegroundColor Gray
    
    try {
        & $psqlExe -U postgres -d flightradar -f $scriptPath
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   ✅ Succès" -ForegroundColor Green
        } else {
            Write-Host "   ❌ Erreur (code: $LASTEXITCODE)" -ForegroundColor Red
        }
    } catch {
        Write-Host "   ❌ Exception: $_" -ForegroundColor Red
    }
    
    Write-Host ""
}

# Nettoyer la variable d'environnement
Remove-Item Env:\PGPASSWORD

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MIGRATIONS TERMINÉES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Prochaines étapes:" -ForegroundColor Yellow
Write-Host "1. Redémarrer le backend Spring Boot" -ForegroundColor White
Write-Host "2. Tester la création d'un vol depuis l'interface Admin" -ForegroundColor White
Write-Host ""

