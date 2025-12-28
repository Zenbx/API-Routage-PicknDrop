$baseUrl = "http://localhost:8080/api/v1"
Write-Host "`n=== COMPLETE ENDPOINT TEST ===`n" -ForegroundColor Cyan

# Hub 1
Write-Host "[1/5] Hub 1..." -ForegroundColor Yellow
try {
    $h1 = Invoke-RestMethod -Uri "$baseUrl/hubs" -Method Post -Body '{"address":"Hub Douala","latitude":4.05,"longitude":9.7,"type":"WAREHOUSE"}' -ContentType "application/json"
    Write-Host "✓ ID: $($h1.id)" -ForegroundColor Green
    $hub1Id = $h1.id
} catch { Write-Host "✗ $($_.Exception.Message)" -ForegroundColor Red; exit 1 }

# Hub 2
Write-Host "[2/5] Hub 2..." -ForegroundColor Yellow
try {
    $h2 = Invoke-RestMethod -Uri "$baseUrl/hubs" -Method Post -Body '{"address":"Hub Yaoundé","latitude":3.85,"longitude":11.5,"type":"DISTRIBUTION_CENTER"}' -ContentType "application/json"
    Write-Host "✓ ID: $($h2.id)" -ForegroundColor Green
    $hub2Id = $h2.id
} catch { Write-Host "✗ $($_.Exception.Message)" -ForegroundColor Red; exit 1 }

# Parcel
Write-Host "[3/5] Parcel..." -ForegroundColor Yellow
try {
    $p = Invoke-RestMethod -Uri "$baseUrl/parcels" -Method Post -Body '{"senderName":"Jean","senderPhone":"+237600000000","recipientName":"Marie","recipientPhone":"+237611111111","pickupLocation":"POINT(9.7 4.05)","pickupAddress":"Douala","deliveryLocation":"POINT(11.5 3.85)","deliveryAddress":"Yaoundé","weightKg":12.5}' -ContentType "application/json"
    Write-Host "✓ Tracking: $($p.trackingCode)" -ForegroundColor Green
    $parcelId = $p.id
} catch { Write-Host "✗ $($_.Exception.Message)" -ForegroundColor Red; exit 1 }

# Route Calculate
Write-Host "[4/5] Route Calculate..." -ForegroundColor Yellow
try {
    $r = Invoke-RestMethod -Uri "$baseUrl/routes/calculate" -Method Post -Body "{`"parcelId`":`"$parcelId`",`"startHubId`":`"$hub1Id`",`"endHubId`":`"$hub2Id`"}" -ContentType "application/json"
    Write-Host "✓ Distance: $($r.totalDistanceKm) km" -ForegroundColor Green
    $routeId = $r.id
} catch { Write-Host "⚠ $($_.Exception.Message)" -ForegroundColor Yellow }

# List Parcels
Write-Host "[5/5] List Parcels..." -ForegroundColor Yellow
try {
    $all = Invoke-RestMethod -Uri "$baseUrl/parcels" -Method Get
    Write-Host "✓ Total: $($all.Count)" -ForegroundColor Green
} catch { Write-Host "✗ $($_.Exception.Message)" -ForegroundColor Red }

Write-Host "`n=== DONE ===`n" -ForegroundColor Cyan
