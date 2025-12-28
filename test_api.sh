#!/bin/bash
# Simple test for hub creation first (to verify API is working)
curl -v -X POST http://localhost:8080/api/v1/hubs \
  -H "Content-Type: application/json" \
  -d '{
    "address": "Test Hub",
    "latitude": 48.0,
    "longitude": 2.0,
    "type": "WAREHOUSE"
  }'

echo "\n\n===== Now testing parcel creation ====="

# Test parcel creation
curl -v -X POST http://localhost:8080/api/v1/parcels \
  -H "Content-Type: application/json" \
  -d '{
    "senderName": "Jean Dupont",
    "senderPhone": "+237 600 000 000",
    "recipientName": "Marie Claire",
    "recipientPhone": "+237 611 111 111",
    "pickupLocation": "POINT(9.7 4.0)",
    "deliveryLocation": "POINT(11.5 3.8)",
    "weightKg": 12.5
  }'
