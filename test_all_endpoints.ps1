$baseUrl = "http://localhost:8080/api/v1"

Write-Host "`n=== 1. Creating Test Hubs ===" -ForegroundColor Cyan
$hub1Body = @{
    address = "Hub Douala"
    latitude = 4.05
    longitude = 9.7
    type = "WAREHOUSE"
} | ConvertTo-Json

$hub2Body = @{
    address = "Hub Yaoundé"
    latitude = 3.85
    longitude = 11.5
    type = "DISTRIBUTION_CENTER"
} | ConvertTo-Json

try {
    $hub1 = Invoke-RestMethod -Uri "$baseUrl/hubs" -Method Post -Body $hub1Body -ContentType "application/json"
    Write-Host "✓ Hub 1 created: $($hub1.id)" -ForegroundColor Green
    
    $hub2 = Invoke-RestMethod -Uri "$baseUrl/hubs" -Method Post -Body $hub2Body -ContentType "application/json"
    Write-Host "✓ Hub 2 created: $($hub2.id)" -ForegroundColor Green
} catch {
    $hub1 = $null
    $hub2 = $null
    Write-Host "✗ Hub creation failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 2. Creating Test Parcel ===" -ForegroundColor Cyan
$parcelBody = @{
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
    $parcel = Invoke-RestMethod -Uri "$baseUrl/parcels" -Method Post -Body $parcelBody -ContentType "application/json"
    Write-Host "✓ Parcel created: $($parcel.trackingCode) (ID: $($parcel.id))" -ForegroundColor Green
} catch {
    $parcel = $null
    Write-Host "✗ Parcel creation failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
    }
}

Write-Host "`n=== 3. Testing Route Calculation ===" -ForegroundColor Cyan
if ($hub1 -and $hub2 -and $parcel) {
    $routeBody = @{
        parcelId = $parcel.id
        startHubId = $hub1.id
        endHubId = $hub2.id
    } | ConvertTo-Json

    try {
        $route = Invoke-RestMethod -Uri "$baseUrl/routes/calculate" -Method Post -Body $routeBody -ContentType "application/json"
        Write-Host "✓ Route calculated: $($route.totalDistanceKm) km, $($route.estimatedDurationMin) min" -ForegroundColor Green
        Write-Host "  Route ID: $($route.id)" -ForegroundColor Gray
    } catch {
        $route = $null
        Write-Host "✗ Route calculation failed: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails) {
            Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "⊘ Skipped (prerequisites missing)" -ForegroundColor Yellow
    $route = $null
}

Write-Host "`n=== 4. Testing Route Recalculation ===" -ForegroundColor Cyan
if ($route) {
    $incidentBody = @{
        type = "ROAD_CLOSURE"
        location = @{
            latitude = 4.0
            longitude = 10.0
        }
        radius = 5000
        description = "Test incident"
    } | ConvertTo-Json

    try {
        $newRoute = Invoke-RestMethod -Uri "$baseUrl/routes/$($route.id)/recalculate" -Method Post -Body $incidentBody -ContentType "application/json"
        Write-Host "✓ Route recalculated: $($newRoute.totalDistanceKm) km" -ForegroundColor Green
    } catch {
        Write-Host "✗ Route recalculation failed: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails) {
            Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "⊘ Skipped (no route to recalculate)" -ForegroundColor Yellow
}

Write-Host "`n=== 5. Testing Delivery Tracking ===" -ForegroundColor Cyan
if ($route) {
    try {
        $tracking = Invoke-RestMethod -Uri "$baseUrl/deliveries/$($route.id)/tracking" -Method Get -ContentType "application/json"
        Write-Host "✓ Tracking info retrieved" -ForegroundColor Green
    } catch {
        Write-Host "✗ Tracking failed: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "⊘ Skipped (no route to track)" -ForegroundColor Yellow
}

Write-Host "`n=== Summary ===" -ForegroundColor Cyan
Write-Host "Hub Creation: $(if ($hub1 -and $hub2) { '✓' } else { '✗' })"
Write-Host "Parcel Creation: $(if ($parcel) { '✓' } else { '✗' })"
Write-Host "Route Calculation: $(if ($route) { '✓' } else { '✗' })"
