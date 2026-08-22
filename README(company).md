# 🏢 Company Service (`companyms`)

> Module of the [**Job Portal Microservices**](../README.md) project

Owns everything about **companies** — the organizations that post jobs and receive reviews. Runs as an independent Spring Boot service with **its own PostgreSQL database** (`company_db`).

## 🎯 Role in the Architecture

- Job postings in `jobms` store only a `companyId` reference — the company data itself lives **here** (database-per-service rule)
- `jobms` calls this service via **OpenFeign** (`GET /api/v1/companies/{id}`) to enrich job responses with company details
- Registers with Eureka as **`COMPANY-SERVICE`**, so other services reach it by name

## 🔌 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/companies` | List all companies |
| `GET` | `/api/v1/companies/{id}` | Get company by ID *(called by jobms via Feign)* |
| `POST` | `/api/v1/companies` | Create a company |
| `PUT` | `/api/v1/companies/{id}` | Update a company |
| `DELETE` | `/api/v1/companies/{id}` | Delete a company |

> 📝 *Adjust to match your actual controller routes.*

## ⚙️ Configuration (`application.properties`)

```properties
spring.application.name=company-service
server.port=8082

spring.datasource.url=jdbc:postgresql://localhost:5432/company_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

## 🚀 Run

```bash
# 1️⃣ Start service-reg FIRST, then:
cd companyms
mvn spring-boot:run
```

Verify: Eureka dashboard shows `COMPANY-SERVICE` → **UP**

## 🧠 Design decisions demonstrated here

- **Database-per-service** — no shared tables across services; ownership is clean
- **Loose coupling** — other services know this one only by its Eureka name and API contract
- **Single responsibility** — one bounded context (companies) per service

## 🧰 Tech

Spring Boot 3.1 • Spring Data JPA • PostgreSQL • Eureka Client • OpenFeign • Java 17

---

*Part of the Job Portal Microservices project — see the [root README](../README.md) for the full architecture.*
