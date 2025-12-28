$url = "http://localhost:8080/api/v1/hubs"
$body = @{
    address = "Hub Test"
    latitude = 4.05
    longitude = 9.7
    type = "WAREHOUSE"
} | ConvertTo-Json

Write-Host "Testing Hub Creation..."
try {
    $response = Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType "application/json"
    Write-Host "Success!" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Failed!" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
    if ($_.ErrorDetails) {
        Write-Host "Error: $($_.ErrorDetails.Message)"
    }
}
