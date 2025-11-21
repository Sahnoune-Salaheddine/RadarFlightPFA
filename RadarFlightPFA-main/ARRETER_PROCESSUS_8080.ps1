# Script pour arrêter le processus utilisant le port 8080

Write-Host "Recherche du processus utilisant le port 8080..." -ForegroundColor Yellow

$connection = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue

if ($connection) {
    $processId = $connection.OwningProcess
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    
    if ($process) {
        Write-Host "Processus trouvé:" -ForegroundColor Green
        Write-Host "  ID: $($process.Id)" -ForegroundColor Cyan
        Write-Host "  Nom: $($process.ProcessName)" -ForegroundColor Cyan
        Write-Host "  Chemin: $($process.Path)" -ForegroundColor Cyan
        
        $response = Read-Host "Voulez-vous arrêter ce processus? (O/N)"
        
        if ($response -eq "O" -or $response -eq "o") {
            try {
                Stop-Process -Id $processId -Force
                Write-Host "Processus arrêté avec succès!" -ForegroundColor Green
                Write-Host "Vous pouvez maintenant redémarrer le backend avec: mvn spring-boot:run" -ForegroundColor Yellow
            } catch {
                Write-Host "Erreur lors de l'arrêt du processus: $_" -ForegroundColor Red
            }
        } else {
            Write-Host "Processus non arrêté." -ForegroundColor Yellow
        }
    } else {
        Write-Host "Impossible de trouver les détails du processus avec l'ID $processId" -ForegroundColor Red
    }
} else {
    Write-Host "Aucun processus n'utilise le port 8080." -ForegroundColor Green
}

