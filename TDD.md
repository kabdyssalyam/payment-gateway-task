# General flow overview

## Payment Processing
```mermaid
sequenceDiagram
  actor client as Merchant
  participant service as PaymentGW
  participant db as DB
  participant bank as Acquirer 

  client->>service: POST /api/payment
  service->>db:  Saves payment data
  service->>bank: Send request
  bank-->>service: Response
  service->>db: Saves response result
  service-->>client: Payment response
  
```

## Retrieving payment data
```mermaid
sequenceDiagram
  actor client as Merchant
  participant service as PaymentGW
  participant db as DB

  client->>service: GET /api/payment/:id
  service->>db: Retrieve payment data
  db-->>service: Return data
  service-->>client: Return payment data
 ``` 
  
