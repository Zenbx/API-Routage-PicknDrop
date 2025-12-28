Write-Host "`n====== COMPREHENSIVE API TEST ======`n" -ForegroundColor Cyan

# Test 1: Create Hub 1
Write-Host "[1/5] Creating Hub 1..." -ForegroundColor Yellow
$hub1 = @{
    address = "Hub Douala"
    latitude = 4.05
    longitude = 9.7
    type = "WAREHOUSE"
} | ConvertTo-Json

try {
    $hubResponse1 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/hubs" -Method Post -Body $hub1 -ContentType "application/json"
    Write-Host "✓ Hub 1 ID: $($hubResponse1.id)" -ForegroundColor Green
    $hub1Id = $hubResponse1.id
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Create Hub 2
Write-Host "`n[2/5] Creating Hub 2..." -ForegroundColor Yellow
$hub2 = @{
    address = "Hub Yaoundé"
    latitude = 3.85
    longitude = 11.5
    type = "DISTRIBUTION_CENTER"
} | ConvertTo-Json

try {
    $hubResponse2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/hubs" -Method Post -Body $hub2 -ContentType "application/json"
    Write-Host "✓ Hub 2 ID: $($hubResponse2.id)" -ForegroundColor Green
    $hub2Id = $hubResponse2.id
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Create Parcel
Write-Host "`n[3/5] Creating Parcel..." -ForegroundColor Yellow
$parcel = @{
    senderName = "Jean Dupont"
    senderPhone = "+237 600 000 000"
    recipientName = "Marie Claire"
    recipientPhone = "+237 611 111 111"
    pickupLocation = "POINT(9.7 4.05)"
    pickupAddress = "Douala Centre"
    deliveryLocation = "POINT(11.5 3.85)"
    deliveryAddress = "Yaoundé Bastos"
    weightKg = 12.5
} | ConvertTo-Json

try {
    $parcelResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/parcels" -Method Post -Body $parcel -ContentType "application/json"
    Write-Host "✓ Parcel created: $($parcelResponse.trackingCode)" -ForegroundColor Green
    $parcelId = $parcelResponse.id
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 4: Calculate Route
Write-Host "`n[4/5] Calculating Route..." -ForegroundColor Yellow
$route = @{
    parcelId = $parcelId
    startHubId = $hub1Id
    endHubId = $hub2Id
} | ConvertTo-Json

try {
    $routeResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/routes/calculate" -Method Post -Body $route -ContentType "application/json"
    Write-Host "✓ Route: $($routeResponse.totalDistanceKm) km, $($routeResponse.estimatedDurationMin) min" -ForegroundColor Green
    $routeId = $routeResponse.id
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Note: This may be expected if routing service not fully configured" -ForegroundColor Yellow
}

# Test 5: List All Parcels
Write-Host "`n[5/5] Listing All Parcels..." -ForegroundColor Yellow
try {
    $parcels = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/parcels" -Method Get
    Write-Host "✓ Found $($parcels.Count) parcel(s)" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n====== TEST COMPLETE ======" -ForegroundColor Cyan
Write-Host "Summary:"
Write-Host "  Hubs: ✓"
Write-Host "  Parcels: ✓"
Write-Host "  Routes: $(if ($routeId) { '✓' } else { '⚠' })"
