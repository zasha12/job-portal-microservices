# ⭐ Review Service (`reviewms`)

> Module of the [**Job Portal Microservices**](../README.md) project

Owns **reviews** — ratings and feedback that candidates leave about companies. Runs as an independent Spring Boot service with **its own PostgreSQL database** (`review_db`).

## 🎯 Role in the Architecture

- Each review belongs to a company via `companyId` — but the company data itself lives in `companyms` (database-per-service rule: we store the *reference*, never a copy)
- Other services can fetch reviews for a company via **OpenFeign** using this service's Eureka name (`REVIEW-SERVICE`)
- Registers with Eureka on startup and participates in service discovery

## 🔌 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/reviews` | List all reviews |
| `GET` | `/api/v1/reviews/{id}` | Get review by ID |
| `GET` | `/api/v1/reviews/company/{companyId}` | All reviews for a company |
| `POST` | `/api/v1/reviews` | Create a review |
| `PUT` | `/api/v1/reviews/{id}` | Update a review |
| `DELETE` | `/api/v1/reviews/{id}` | Delete a review |

> 📝 *Adjust to match your actual controller routes.*

## ⚙️ Configuration (`application.properties`)

```properties
spring.application.name=review-service
server.port=8083

spring.datasource.url=jdbc:postgresql://localhost:5432/review_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

## 🚀 Run

```bash
# 1️⃣ Start service-reg FIRST, then:
cd reviewms
mvn spring-boot:run
```

Verify: Eureka dashboard shows `REVIEW-SERVICE` → **UP**

## 🧠 Design decisions demonstrated here

- **Reviews are isolated from companies** — the review team/context could evolve (moderation, likes, replies) without touching `companyms`
- **Referential integrity across services** is handled by application logic (Feign validation), not foreign keys — a key microservices trade-off worth discussing in interviews

## 🧰 Tech

Spring Boot 3.1 • Spring Data JPA • PostgreSQL • Eureka Client • Java 17

---

*Part of the Job Portal Microservices project — see the [root README](../README.md) for the full architecture.*
