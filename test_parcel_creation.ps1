$url = "http://localhost:8080/api/v1/parcels"

$body = @{
    senderName = "Jean Dupont"
    senderPhone = "+237 600 000 000"
    recipientName = "Marie Claire"
    recipientPhone = "+237 611 111 111"
    pickupLocation = "POINT(9.7 4.0)"
    pickupAddress = "Rue de la Joie, Douala"
    deliveryLocation = "POINT(11.5 3.8)"
    deliveryAddress = "Bastos, Yaoundé"
    weightKg = 12.5
    declaredValueXaf = 50000.0
    notes = "Test parcel creation"
} | ConvertTo-Json

Write-Host "Testing Parcel Creation API..."
Write-Host "URL: $url"
Write-Host "Body:" 
Write-Host $body

try {
    $response = Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType "application/json"
    Write-Host "`nSuccess!" -ForegroundColor Green
    Write-Host "Response:"
    $response | ConvertTo-Json
} catch {
    Write-Host "`nError!" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)"
    Write-Host "Error Message: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Host "Details: $($_.ErrorDetails.Message)"
    }
}
