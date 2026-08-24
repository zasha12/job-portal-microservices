<div align="center">

# 🧩 Job Portal Microservices

### Distributed system with Spring Boot + Spring Cloud — Eureka, OpenFeign & PostgreSQL

*Service Registry • Job Service • Company Service • Review Service • Feign Calls*

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2022.0.x-6DB33F)
![Java](https://img.shields.io/badge/Java-17-orange)
![Discovery](https://img.shields.io/badge/Discovery-Eureka-blueviolet)
![Database](https://img.shields.io/badge/Database-PostgreSQL-4169E1)

</div>

---

## 📌 Overview

**Job Portal Microservices** is a distributed backend where each business domain runs as an **independent Spring Boot service** with its own database, registered with a **Eureka service registry**, and communicating via **declarative OpenFeign clients** — instead of one big monolith.

> 🎓 Built while completing the Spring Boot Microservices course by [Faisal Memon](https://www.youtube.com/@FaisalFromEmbarkX) (EmbarkX). All code was written by me as I learned each concept, then rebranded, documented, and extended beyond the course baseline (see [Roadmap](#️-roadmap)).

## 🏗️ Architecture

```
                        ┌────────────────────────┐
                        │   service-reg (Eureka) │
                        │   port 8761            │
                        └───▲──────▲──────▲──────┘
             registers ┌────┘      │      └─────┐
                       │           │            │
              ┌────────┴───┐ ┌─────┴──────┐ ┌───┴────────┐
              │   jobms    │ │ companyms  │ │  reviewms  │
              │  :8081     │ │  :8082     │ │  :8083     │
              │ PostgreSQL │ │ PostgreSQL │ │ PostgreSQL │
              └─────┬──────┘ └────────────┘ └────────────┘
                    │   OpenFeign (company details)
                    └─────────────▶
```

## 📦 Services

| Module | Port | Responsibility |
|---|---|---|
| `service-reg` | 8761 | **Eureka Server** — service registry & discovery |
| `jobms` | 8081 | Job postings CRUD; enriches responses with company data via Feign |
| `companyms` | 8082 | Company profiles CRUD |
| `reviewms` | 8083 | Company reviews CRUD |

> 📝 *Adjust ports/names to match your actual configuration.*

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.1 |
| Discovery | Spring Cloud Netflix Eureka |
| Inter-service calls | OpenFeign (declarative REST clients) |
| Persistence | Spring Data JPA + PostgreSQL (H2 for tests) |
| Build | Maven (multi-module) |
| Language | Java 17 |

## 🚀 Getting Started

### Prerequisites
- **JDK 17+** — [download here](https://adoptium.net/)
- **Maven 3.9+**
- **PostgreSQL** running locally

### 1. Create the databases

```sql
CREATE DATABASE job_db;
CREATE DATABASE company_db;
CREATE DATABASE review_db;
```

### 2. Configure credentials — placeholders only, NEVER real passwords!

In each service's `src/main/resources/application.properties`:

```properties
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
```

### 3. Start the services (ORDER MATTERS!)

```bash
# 1️⃣ Registry FIRST — every other service registers with it
cd service-reg
mvn spring-boot:run

# 2️⃣ Then the domain services (separate terminals)
cd companyms && mvn spring-boot:run
cd reviewms  && mvn spring-boot:run
cd jobms     && mvn spring-boot:run
```

### 4. Verify

- Eureka dashboard → **http://localhost:8761** — all four services (`SERVICE-REG` implicitly, `JOB-SERVICE`, `COMPANY-SERVICE`, `REVIEW-SERVICE`) should show **UP**
- Example call → `GET http://localhost:8081/api/v1/jobs`

---

## 📡 Service Registry (`service-reg`)

The **phone book of the system** — a Netflix Eureka Server where every microservice registers itself on startup, so clients discover services **by name** instead of by hardcoded URLs.

**Role:** every service (job, company, review) registers here at boot; OpenFeign clients look each other up via Eureka instead of `localhost:808x`; tracks instance health via 30-second heartbeats.

```java
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegApplication { ... }
```

Runs in **standalone mode** — it does not register with itself:

```properties
spring.application.name=service-registry
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

**Interview talking points:**

| Observation | Why it matters |
|---|---|
| Instances appear/disappear as services start/stop | Dynamic service discovery |
| Heartbeat every 30s keeps registrations alive | Health monitoring |
| **Self-preservation mode** during network hiccups | Eureka favours availability over strict consistency (CAP: AP) |

---

## 🏢 Company Service (`companyms`)

Owns everything about **companies** — the organizations that post jobs and receive reviews. Independent Spring Boot service with its own PostgreSQL database (`company_db`).

**Role:** `jobms` stores only a `companyId` reference — the company data lives here (database-per-service). `jobms` calls this service via OpenFeign (`GET /api/v1/companies/{id}`) to enrich job responses. Registers with Eureka as `COMPANY-SERVICE`.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/companies` | List all companies |
| `GET` | `/api/v1/companies/{id}` | Get company by ID *(called by jobms via Feign)* |
| `POST` | `/api/v1/companies` | Create a company |
| `PUT` | `/api/v1/companies/{id}` | Update a company |
| `DELETE` | `/api/v1/companies/{id}` | Delete a company |

```properties
spring.application.name=company-service
server.port=8082
spring.datasource.url=jdbc:postgresql://localhost:5432/company_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

**Design decisions demonstrated:** database-per-service isolation, loose coupling (consumers only know the Eureka name + API contract), single responsibility per bounded context.

---

## ⭐ Review Service (`reviewms`)

Owns **reviews** — ratings and feedback left about companies. Independent Spring Boot service with its own PostgreSQL database (`review_db`).

**Role:** each review references a company via `companyId` (never a copy of company data — database-per-service rule). Other services can fetch reviews via OpenFeign using this service's Eureka name (`REVIEW-SERVICE`).

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/reviews` | List all reviews |
| `GET` | `/api/v1/reviews/{id}` | Get review by ID |
| `GET` | `/api/v1/reviews/company/{companyId}` | All reviews for a company |
| `POST` | `/api/v1/reviews` | Create a review |
| `PUT` | `/api/v1/reviews/{id}` | Update a review |
| `DELETE` | `/api/v1/reviews/{id}` | Delete a review |

```properties
spring.application.name=review-service
server.port=8083
spring.datasource.url=jdbc:postgresql://localhost:5432/review_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

**Design decisions demonstrated:** reviews are isolated from companies so the review domain (moderation, likes, replies) can evolve independently; referential integrity across services is handled at the application layer (Feign validation) rather than foreign keys — a classic microservices trade-off worth discussing in interviews.

---

## 🔌 Key Flows

**Create a job posting**
```
POST jobms /api/v1/jobs  →  saved in job_db with companyId
```

**Fetch a job (enriched)**
```
GET jobms /api/v1/jobs/{id}
     → jobms calls companyms via Feign (GET /api/v1/companies/{companyId})
     → returns job + company details in one response
```

## 🎓 Microservice Concepts Practised

- Service decomposition by domain (why microservices vs. a monolith)
- **Service discovery** with Eureka (register/lookup instead of hardcoded URLs)
- **Synchronous communication** with OpenFeign clients
- Database-per-service isolation
- Independent build & deployment of each service

## 🛣️ Roadmap

- [ ] API Gateway (Spring Cloud Gateway) — single entry point + routing
- [ ] Config Server — centralized configuration
- [ ] Resilience: Circuit Breaker (Resilience4j) on Feign calls
- [ ] Distributed tracing (Micrometer + Zipkin)
- [ ] Docker Compose for the whole system
- [ ] CI pipeline with GitHub Actions

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Kartikeya Dhasmana**
- GitHub: [@zasha12](https://github.com/zasha12)
- LinkedIn: [Kartikeya Dhasmana](https://www.linkedin.com/in/kartikeya-dhasmana-b91389245/)
- Email: kartikeyadhasmana@gmail.com

---

<div align="center">

⭐ *If this helped you understand microservices, star it!*

</div>
