curl -X POST \
-H "Content-Type: application/vnd.kafka.json.v2+json" \
--data '{
  "records": [
    {
      "value": {
        "transactionId": "8fe95ed6-f57f-44a5-bfaf-d533837b0bae",
        "timestamp": "2025-01-01T10:30:00",
        "amount": 3,
        "sellerId": 5,
        "productId": 1
      }
    }
  ]
}' \
http://localhost:8088/topics/sales-transaction