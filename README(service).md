# 📡 Service Registry (`service-reg`)

> Module of the [**Job Portal Microservices**](../README.md) project

The **phone book of the system** — a Netflix **Eureka Server** where every microservice registers itself on startup, and where clients discover services **by name** instead of hardcoded URLs.

## 🎯 Role in the Architecture

- Every service (`job-service`, `company-service`, `review-service`) registers here at boot
- Services find each other via Eureka lookup (used by OpenFeign clients) — no hardcoded `localhost:808x` URLs between services
- Tracks instance health via 30-second heartbeats

## ⚙️ How It Works

```java
@SpringBootApplication
@EnableEurekaServer          // turns this Spring Boot app into a registry
public class ServiceRegApplication { ... }
```

Runs in **standalone mode** — it does NOT register with itself:

```properties
spring.application.name=service-registry
server.port=8761

eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

## 🚀 Run (ALWAYS START THIS FIRST)

```bash
cd service-reg
mvn spring-boot:run
```

Then open the dashboard → **http://localhost:8761**

You should see `JOB-SERVICE`, `COMPANY-SERVICE`, `REVIEW-SERVICE` appear in the *Instances currently registered* table once the other modules start.

## 🔍 Things worth observing (great interview talking points)

| Observation | Why it matters |
|---|---|
| Instances appear/disappear as services start/stop | Dynamic service discovery |
| Heartbeat every 30s keeps registrations alive | Health monitoring |
| **Self-preservation mode** during network hiccups | Eureka favours availability over strict consistency (CAP: AP) |

## 🧰 Tech

Spring Boot 3.1 • Spring Cloud Netflix Eureka Server • Java 17

---

*Part of the Job Portal Microservices project — see the [root README](../README.md) for the full architecture.*
