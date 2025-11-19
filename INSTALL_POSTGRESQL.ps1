# Script d'installation et configuration PostgreSQL pour Flight Radar 2026
# Vérifie si PostgreSQL est installé et aide à l'installer/démarrer

Write-Host "🐘 Vérification PostgreSQL pour Flight Radar 2026" -ForegroundColor Cyan
Write-Host ""

# Vérifier si PostgreSQL est installé
Write-Host "📋 Vérification de l'installation..." -ForegroundColor Yellow

$psqlExists = Get-Command psql -ErrorAction SilentlyContinue
$dockerExists = Get-Command docker -ErrorAction SilentlyContinue
$postgresService = Get-Service -Name "*postgres*" -ErrorAction SilentlyContinue

# Option 1 : Vérifier Docker
if ($dockerExists) {
    Write-Host "✅ Docker est installé" -ForegroundColor Green
    Write-Host ""
    Write-Host "🚀 Solution recommandée : Utiliser Docker" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Exécutez ces commandes :" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "docker run --name postgres-flightradar \`" -ForegroundColor White
    Write-Host "  -e POSTGRES_PASSWORD=postgres \`" -ForegroundColor White
    Write-Host "  -e POSTGRES_DB=flightradar \`" -ForegroundColor White
    Write-Host "  -p 5432:5432 \`" -ForegroundColor White
    Write-Host "  -d postgres:15" -ForegroundColor White
    Write-Host ""
    Write-Host "Puis vérifiez : docker ps" -ForegroundColor Yellow
    Write-Host ""
    
    $useDocker = Read-Host "Voulez-vous utiliser Docker ? (O/N)"
    if ($useDocker -eq "O" -or $useDocker -eq "o") {
        Write-Host ""
        Write-Host "🚀 Démarrage de PostgreSQL dans Docker..." -ForegroundColor Cyan
        
        # Vérifier si le conteneur existe déjà
        $containerExists = docker ps -a --filter "name=postgres-flightradar" --format "{{.Names}}" | Select-String "postgres-flightradar"
        
        if ($containerExists) {
            Write-Host "📦 Conteneur existant trouvé, démarrage..." -ForegroundColor Yellow
            docker start postgres-flightradar
        } else {
            Write-Host "📦 Création et démarrage du conteneur..." -ForegroundColor Yellow
            docker run --name postgres-flightradar `
                -e POSTGRES_PASSWORD=postgres `
                -e POSTGRES_DB=flightradar `
                -p 5432:5432 `
                -d postgres:15
        }
        
        Start-Sleep -Seconds 3
        
        # Vérifier
        $containerRunning = docker ps --filter "name=postgres-flightradar" --format "{{.Names}}" | Select-String "postgres-flightradar"
        if ($containerRunning) {
            Write-Host ""
            Write-Host "✅ PostgreSQL démarré avec succès dans Docker !" -ForegroundColor Green
            Write-Host ""
            Write-Host "📝 Configuration application.properties :" -ForegroundColor Cyan
            Write-Host "spring.datasource.password=postgres" -ForegroundColor White
            Write-Host ""
            Write-Host "✅ Vous pouvez maintenant démarrer l'application Spring Boot" -ForegroundColor Green
            exit 0
        } else {
            Write-Host "❌ Erreur lors du démarrage du conteneur" -ForegroundColor Red
            Write-Host "Vérifiez les logs : docker logs postgres-flightradar" -ForegroundColor Yellow
            exit 1
        }
    }
}

# Option 2 : Vérifier PostgreSQL natif
if ($psqlExists) {
    Write-Host "✅ PostgreSQL est installé (version: $(psql --version))" -ForegroundColor Green
} else {
    Write-Host "❌ PostgreSQL n'est PAS installé" -ForegroundColor Red
    Write-Host ""
    Write-Host "📥 Pour installer PostgreSQL :" -ForegroundColor Yellow
    Write-Host "1. Télécharger depuis : https://www.postgresql.org/download/windows/" -ForegroundColor White
    Write-Host "2. Installer avec l'installateur" -ForegroundColor White
    Write-Host "3. Noter le mot de passe du superutilisateur 'postgres'" -ForegroundColor White
    Write-Host "4. Relancer ce script après l'installation" -ForegroundColor White
    Write-Host ""
    exit 1
}

# Vérifier le service
if ($postgresService) {
    Write-Host "✅ Service PostgreSQL trouvé : $($postgresService.Name)" -ForegroundColor Green
    
    if ($postgresService.Status -eq "Running") {
        Write-Host "✅ Service déjà démarré" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Service arrêté, démarrage..." -ForegroundColor Yellow
        try {
            Start-Service -Name $postgresService.Name
            Write-Host "✅ Service démarré avec succès" -ForegroundColor Green
        } catch {
            Write-Host "❌ Erreur lors du démarrage : $_" -ForegroundColor Red
            Write-Host "Essayez de démarrer manuellement depuis services.msc" -ForegroundColor Yellow
            exit 1
        }
    }
} else {
    Write-Host "⚠️  Aucun service PostgreSQL trouvé" -ForegroundColor Yellow
    Write-Host "PostgreSQL peut être installé mais le service n'est pas configuré" -ForegroundColor Yellow
}

# Vérifier la connexion
Write-Host ""
Write-Host "🔍 Vérification de la connexion au port 5432..." -ForegroundColor Yellow
$portTest = Test-NetConnection -ComputerName localhost -Port 5432 -WarningAction SilentlyContinue

if ($portTest.TcpTestSucceeded) {
    Write-Host "✅ Port 5432 accessible" -ForegroundColor Green
} else {
    Write-Host "❌ Port 5432 non accessible" -ForegroundColor Red
    Write-Host "PostgreSQL n'est peut-être pas démarré" -ForegroundColor Yellow
    exit 1
}

# Vérifier/Créer la base de données
Write-Host ""
Write-Host "🗄️  Vérification de la base de données 'flightradar'..." -ForegroundColor Yellow

try {
    $dbExists = psql -U postgres -lqt 2>&1 | Select-String "flightradar"
    if ($dbExists) {
        Write-Host "✅ Base de données 'flightradar' existe déjà" -ForegroundColor Green
    } else {
        Write-Host "📦 Création de la base de données 'flightradar'..." -ForegroundColor Yellow
        $password = Read-Host "Entrez le mot de passe PostgreSQL (postgres)" -AsSecureString
        $passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))
        
        $env:PGPASSWORD = $passwordPlain
        psql -U postgres -c "CREATE DATABASE flightradar;" 2>&1 | Out-Null
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Base de données créée avec succès" -ForegroundColor Green
        } else {
            Write-Host "⚠️  Erreur lors de la création (peut-être qu'elle existe déjà)" -ForegroundColor Yellow
        }
        Remove-Item Env:\PGPASSWORD
    }
} catch {
    Write-Host "⚠️  Impossible de vérifier/créer la base de données" -ForegroundColor Yellow
    Write-Host "Créez-la manuellement : psql -U postgres -c 'CREATE DATABASE flightradar;'" -ForegroundColor White
}

Write-Host ""
Write-Host "✅ PostgreSQL est prêt !" -ForegroundColor Green
Write-Host ""
Write-Host "📝 N'oubliez pas de configurer application.properties avec votre mot de passe" -ForegroundColor Cyan
Write-Host ""

