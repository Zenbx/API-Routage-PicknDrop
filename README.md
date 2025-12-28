# Logistics Routing & Delivery API

## Description
This API is part of a larger logistics microservices system, focusing on:
- Optimal route calculation
- Dynamic route recalculation
- Deterministic ETA estimation
- Real-time tracking persistence

## Tech Stack
- **Java 17**
- **Spring Boot 3.4.0**
- **PostgreSQL & PostGIS**
- **Liquibase**
- **MapStruct**
- **Swagger/OpenAPI 3.0**

## Prerequisites
- Docker & Docker Compose
- Java 17+
- Maven

## Getting Started

### 1. Database Setup
Ensure you have a PostgreSQL instance with PostGIS extension. Using Docker:
```bash
docker run --name postgis -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=logistics_db -p 5432:5432 -d postgis/postgis
```

### 2. Run the Application
```bash
./mvnw spring-boot:run
```

### 3. API Documentation
Once running, access Swagger UI at:
`http://localhost:8080/swagger-ui.html`

## API Endpoints
- `POST /api/v1/hubs` - Create a hub/point
- `GET /api/v1/hubs` - List hubs
- `POST /api/v1/routes/calculate` - Calculate route between hubs
- `GET /api/v1/routes/{id}` - Get route details

## Future Roadmap
1. Implementation of real VRP (Vehicle Routing Problem) algorithms.
2. Machine Learning based ETA using historical data.
3. Kafka integration for event-driven updates.
4. OpenStreetMap/Mapbox production integration.
