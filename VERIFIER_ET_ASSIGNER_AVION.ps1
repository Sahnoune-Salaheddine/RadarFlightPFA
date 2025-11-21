# Script PowerShell pour vérifier et assigner un avion au pilote
# Utilise psql pour exécuter les commandes SQL

Write-Host "🔍 Vérification et Assignation d'Avion au Pilote" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration PostgreSQL
$DB_NAME = "flightradar"
$DB_USER = "postgres"
$DB_PASSWORD = "postgres"
$DB_HOST = "localhost"
$DB_PORT = "5432"

# Vérifier que PostgreSQL est accessible
Write-Host "1️⃣  Vérification de la connexion PostgreSQL..." -ForegroundColor Yellow
try {
    $env:PGPASSWORD = $DB_PASSWORD
    $testConnection = & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT version();" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ PostgreSQL accessible" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Erreur de connexion PostgreSQL" -ForegroundColor Red
        Write-Host "   💡 Vérifiez que PostgreSQL est démarré et que les identifiants sont corrects" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "   ❌ psql non trouvé. Installez PostgreSQL ou utilisez pgAdmin" -ForegroundColor Red
    Write-Host "   💡 Vous pouvez exécuter le script SQL manuellement dans pgAdmin" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "   Script SQL disponible dans: backend/database/assign_aircraft_to_pilot.sql" -ForegroundColor Cyan
    exit 1
}

Write-Host ""

# Vérifier l'utilisateur pilote
Write-Host "2️⃣  Vérification de l'utilisateur pilote..." -ForegroundColor Yellow
$userCheck = & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT id, username, role FROM users WHERE username = 'pilote_cmn1';" 2>&1
if ($userCheck -match "pilote_cmn1") {
    Write-Host "   ✅ Utilisateur pilote_cmn1 trouvé" -ForegroundColor Green
    Write-Host "   $userCheck" -ForegroundColor Gray
} else {
    Write-Host "   ❌ Utilisateur pilote_cmn1 non trouvé" -ForegroundColor Red
    Write-Host "   💡 L'utilisateur doit être créé par DataInitializer.java au démarrage" -ForegroundColor Yellow
}

Write-Host ""

# Vérifier le pilote
Write-Host "3️⃣  Vérification du profil pilote..." -ForegroundColor Yellow
$pilotCheck = & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT p.id, p.name, p.license FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1';" 2>&1
if ($pilotCheck -match "\d+") {
    Write-Host "   ✅ Profil pilote trouvé" -ForegroundColor Green
    Write-Host "   $pilotCheck" -ForegroundColor Gray
} else {
    Write-Host "   ❌ Profil pilote non trouvé" -ForegroundColor Red
}

Write-Host ""

# Vérifier les avions
Write-Host "4️⃣  Vérification des avions..." -ForegroundColor Yellow
$aircraftCheck = & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT id, registration, model, pilot_id, username_pilote FROM aircraft LIMIT 5;" 2>&1
if ($aircraftCheck -match "\d+") {
    Write-Host "   ✅ Avions trouvés" -ForegroundColor Green
    Write-Host "   $aircraftCheck" -ForegroundColor Gray
} else {
    Write-Host "   ⚠️  Aucun avion trouvé" -ForegroundColor Yellow
}

Write-Host ""

# Vérifier si le pilote a un avion assigné
Write-Host "5️⃣  Vérification de l'assignation..." -ForegroundColor Yellow
$assignmentCheck = & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c @"
SELECT 
    a.id as aircraft_id,
    a.registration,
    a.model,
    p.id as pilot_id,
    u.username
FROM aircraft a
LEFT JOIN pilots p ON a.pilot_id = p.id
LEFT JOIN users u ON p.user_id = u.id
WHERE u.username = 'pilote_cmn1' OR a.username_pilote = 'pilote_cmn1';
"@ 2>&1

if ($assignmentCheck -match "pilote_cmn1") {
    Write-Host "   ✅ Avion déjà assigné au pilote" -ForegroundColor Green
    Write-Host "   $assignmentCheck" -ForegroundColor Gray
} else {
    Write-Host "   ❌ Aucun avion assigné au pilote" -ForegroundColor Red
    Write-Host ""
    Write-Host "6️⃣  Assignation d'un avion..." -ForegroundColor Yellow
    
    # Assigner un avion existant ou créer un nouveau
    $assignSQL = @"
-- Assigner un avion existant au pilote
UPDATE aircraft 
SET pilot_id = (
    SELECT p.id 
    FROM pilots p 
    JOIN users u ON p.user_id = u.id 
    WHERE u.username = 'pilote_cmn1' 
    LIMIT 1
),
username_pilote = 'pilote_cmn1'
WHERE id = (
    SELECT id FROM aircraft 
    WHERE pilot_id IS NULL 
    LIMIT 1
);

-- Si aucun avion disponible, créer un nouveau
INSERT INTO aircraft (
    registration, 
    model, 
    status, 
    airport_id, 
    pilot_id,
    username_pilote,
    position_lat, 
    position_lon, 
    altitude, 
    speed, 
    heading,
    air_speed,
    vertical_speed,
    transponder_code,
    last_update
)
SELECT 
    'CN-ABC',
    'A320',
    'AU_SOL',
    (SELECT id FROM airports WHERE code_iata = 'CMN' LIMIT 1),
    (SELECT p.id FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1' LIMIT 1),
    'pilote_cmn1',
    33.367500,
    -7.589800,
    0.0,
    0.0,
    0.0,
    0.0,
    0.0,
    '1200',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM aircraft WHERE registration = 'CN-ABC'
) AND EXISTS (
    SELECT 1 FROM pilots p JOIN users u ON p.user_id = u.id WHERE u.username = 'pilote_cmn1'
);
"@
    
    $assignResult = & psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c $assignSQL 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ Avion assigné avec succès" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Erreur lors de l'assignation" -ForegroundColor Red
        Write-Host "   $assignResult" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "✅ Vérification terminée!" -ForegroundColor Green
Write-Host ""
Write-Host "💡 Si le problème persiste, redémarrez le backend pour que DataInitializer s'exécute" -ForegroundColor Yellow

