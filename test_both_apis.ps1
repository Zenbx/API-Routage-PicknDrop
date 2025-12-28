$hubUrl = "http://localhost:8080/api/v1/hubs"
$parcelUrl = "http://localhost:8080/api/v1/parcels"

Write-Host "==== Testing Hub Creation ====" -ForegroundColor Cyan
$hubBody = @{
    address   = "Test Hub"
    latitude  = 48.0
    longitude = 2.0
    type      = "WAREHOUSE"
} | ConvertTo-Json

try {
    $hubResponse = Invoke-RestMethod -Uri $hubUrl -Method Post -Body $hubBody -ContentType "application/json"
    Write-Host "Hub created successfully!" -ForegroundColor Green
    $hubResponse | ConvertTo-Json
}
catch {
    Write-Host "Hub creation failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n==== Testing Parcel Creation ====" -ForegroundColor Cyan
$parcelBody = @{
    senderName       = "Jean Dupont"
    senderPhone      = "+237 600 000 000"
    recipientName    = "Marie Claire"
    recipientPhone   = "+237 611 111 111"
    pickupLocation   = "POINT(9.7 4.0)"
    deliveryLocation = "POINT(11.5 3.8)"
    weightKg         = 12.5
} | ConvertTo-Json

try {
    $parcelResponse = Invoke-RestMethod -Uri $parcelUrl -Method Post -Body $parcelBody -ContentType "application/json"
    Write-Host "Parcel created successfully!" -ForegroundColor Green
    $parcelResponse | ConvertTo-Json
}
catch {
    Write-Host "Parcel creation failed!" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
    if ($_.ErrorDetails) {
        Write-Host "Error: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
    }
}
