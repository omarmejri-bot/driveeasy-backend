# =====================================================================
#  DriveEasy - Script de démarrage complet (Windows PowerShell)
#  Ordre recommandé (§3 du cahier des charges) :
#  1) Service Registry (8761)
#  2) Config Server (8888)
#  3) Microservices métier : vehicule (8081 + option 8091), client (8082), location (8083)
#  4) API Gateway (8080)
# =====================================================================
param(
    [switch]$SecondVehiculeInstance,
    [string]$ConfigMode = "native",
    [switch]$Wait
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$LogsDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force $LogsDir | Out-Null

# Résolution automatique de Java 21 si le terminal courant n'a pas encore rechargé son PATH
$JavaCmd = "java"
if (-not (Get-Command $JavaCmd -ErrorAction SilentlyContinue)) {
    $temurin = Get-ChildItem "C:\Program Files\Eclipse Adoptium\jdk-21*\bin\java.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($temurin) {
        $JavaCmd = $temurin.FullName
    }
}

function Wait-ForHttp($Url, $Name, $TimeoutSec = 60) {
    Write-Host " -> Attente de $Name ($Url)..." -NoNewline
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $resp = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2
            if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 400) {
                Write-Host " OK" -ForegroundColor Green
                return
            }
        } catch {}
        Start-Sleep -Seconds 1
        Write-Host "." -NoNewline
    }
    throw "Timeout en attendant $Name sur $Url"
}

function Start-ServiceJar($Module, $Port, $ExtraEnv = @{}) {
    $jar = Get-ChildItem (Join-Path $Root "$Module\target\$Module-*.jar") | Select-Object -First 1
    if (-not $jar) {
        throw "JAR introuvable pour $Module. Exécutez d'abord : mvn clean package -DskipTests"
    }
    $outLog = Join-Path $LogsDir "$Module-$Port.out.log"
    $errLog = Join-Path $LogsDir "$Module-$Port.err.log"
    $argsList = @(
        # Supprime les warnings « sun.misc.Unsafe terminally deprecated » (Java 23+)
        "--sun-misc-unsafe-memory-access=allow",
        "-Dserver.port=$Port"
    )
    foreach ($k in $ExtraEnv.Keys) {
        $argsList += "-D$k=$($ExtraEnv[$k])"
    }
    $argsList += @("-jar", "`"$($jar.FullName)`"")
    Write-Host "Démarrage de $Module sur le port $Port..." -ForegroundColor Cyan
    Start-Process -FilePath $JavaCmd -ArgumentList $argsList -WorkingDirectory $Root `
        -RedirectStandardOutput $outLog -RedirectStandardError $errLog -WindowStyle Hidden | Out-Null
}

# 1. Service Registry
Start-ServiceJar "service-registry" 8761
Wait-ForHttp "http://localhost:8761/actuator/health" "service-registry"

# 2. Config Server
Start-ServiceJar "config-server" 8888 @{ "spring.profiles.active" = $ConfigMode }
Wait-ForHttp "http://localhost:8888/actuator/health" "config-server"

# 3. Microservices métier
Start-ServiceJar "vehicule-service" 8081
Start-ServiceJar "client-service" 8082
Start-ServiceJar "location-service" 8083
if ($SecondVehiculeInstance) {
    Start-ServiceJar "vehicule-service" 8091
}

Wait-ForHttp "http://localhost:8081/actuator/health" "vehicule-service:8081"
Wait-ForHttp "http://localhost:8082/actuator/health" "client-service:8082"
Wait-ForHttp "http://localhost:8083/actuator/health" "location-service:8083"
if ($SecondVehiculeInstance) {
    Wait-ForHttp "http://localhost:8091/actuator/health" "vehicule-service:8091"
}

# 4. API Gateway
$busy8080 = netstat -ano | Select-String ":8080\s+.*LISTENING"
if ($busy8080) {
    Write-Host "Le port 8080 est occupé. Libération automatique (Oracle XDB)..." -ForegroundColor Yellow
    try {
        "EXEC DBMS_XDB.SETHTTPPORT(0);`nCOMMIT;`nexit;" | sqlplus -S "/ as sysdba" | Out-Null
        Start-Sleep -Seconds 2
    } catch {}
}
Start-ServiceJar "api-gateway" 8080
Wait-ForHttp "http://localhost:8080/actuator/health" "api-gateway:8080"
Wait-ForHttp "http://localhost:8080/api/vehicules" "routage Gateway -> vehicule-service"

Write-Host "`nTous les services DriveEasy sont démarrés :" -ForegroundColor Green
Write-Host " - Eureka Dashboard : http://localhost:8761"
Write-Host " - Config Server    : http://localhost:8888/vehicule-service/dev"
Write-Host " - API Gateway      : http://localhost:8080/api/vehicules"
Write-Host " - Zipkin           : http://localhost:9411"

if ($Wait) {
    Write-Host "`nServices actifs en arrière-plan (Appuyez sur Ctrl+C pour arrêter)..." -ForegroundColor Cyan
    try {
        while (1) {
            Start-Sleep -Seconds 2
        }
    } finally {
        Write-Host "`nArrêt automatique des microservices..." -ForegroundColor Yellow
        & (Join-Path $PSScriptRoot "stop-all.ps1")
    }
} else {
    Write-Host "`nAstuce : Ajoutez -Wait pour surveiller et arrêter automatiquement les services avec Ctrl+C." -ForegroundColor DarkGray
}
