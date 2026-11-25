# Arrête tous les processus Java de DriveEasy écoutant sur les ports 8761, 8888, 8080..8091
$ports = @(8761, 8888, 8080, 8081, 8082, 8083, 8091)
foreach ($p in $ports) {
    $pids = @()
    try {
        $conns = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction Stop
        $pids += ($conns | Select-Object -ExpandProperty OwningProcess)
    } catch {
        $lines = netstat -ano | Select-String ":$p\s+.*LISTENING\s+(\d+)"
        foreach ($line in $lines) {
            if ($line -match ":$p\s+.*LISTENING\s+(\d+)") {
                $pids += [int]$matches[1]
            }
        }
    }
    foreach ($pidToKill in ($pids | Select-Object -Unique)) {
        if ($pidToKill -gt 0) {
            Write-Host "Arrêt du service sur le port $p (PID $pidToKill)..." -ForegroundColor Yellow
            Stop-Process -Id $pidToKill -Force -ErrorAction SilentlyContinue
        }
    }
}
Write-Host "Tous les services DriveEasy sont arrêtés." -ForegroundColor Green
