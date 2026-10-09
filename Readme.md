<div align="center">

#  NCBA LOOP TEST

**A reference high level markdown documentation for building and running the microservices locally**


*Service discovery • API gateway • Distributed caching • SOAP Web Services integration • Container-first*

</div>

---

##  Table of Contents

- [A. Overview](#a-overview)
- [B. Architecture](#b-architecture)
- [C. Technologies Used](#c-technologies-used)
- [D. Running the Application](#d-running-the-application)
- [E. Docker](#e-docker)
- [F. Deployment](#f-deployment)
- [G. Author](#f-author)

---

## A. Overview

### What is this?

This project is a **small but complete microservices ecosystem** — a hands-on reference for building distributed systems the way modern teams do. Instead of one giant application, the work is split across **three independently deployable services** that talk to each other, share a cache, and stay resilient when things go wrong.

Think of it like a well-organized kitchen:
- One person takes orders at the door (**API Gateway**).
- One person coordinates who's working where (**Eureka Server**).
- One person does the actual cooking and talks to the suppliers (**CRUD Service**).

### The three services at a glance

| Service         |   | Port  | What it does                                                                                |
|-----------------|-|-------|---------------------------------------------------------------------------------------------|
| `eureka-server` |     | 8761  | Keeps track of every running service so they can find each other without hardcoded URLs.    |
| `api-gateway`   |        | 8086  | The single entry point for clients. Handles authentication, routing, and rate-limiting.     |
| `crud-service`  |  | 8087  | The brain of the system: reads/writes to MySQL, caches in Redis, and calls an external SOAP service. |

### Key characteristics

-  **Stateless** — any service can be scaled up or down freely; no user session is tied to one machine.
-  **Redis-backed caching** — repeated requests are served in milliseconds without hitting the database.
- ️ **Resilient** — if the external service fails, retries kick in with exponential backoff, then a circuit breaker prevents cascading failures.
-  **Discoverable** — services find each other by name, not IP address.
-  **Container-first** — the entire stack spins up with a single Docker Compose command.

---

## B. Architecture

### Visual flow


```
                         ┌──────────────────────┐
                         │   Client / Postman   │
                         │      Web / SPA       │
                         └───────────┬──────────┘
                                     │
                                     │ HTTPS
                                     ▼
                         ┌──────────────────────┐
                         │     API Gateway      │
                         │        :8086         │
                         │  Auth · Rate Limit   │
                         └───────────┬──────────┘
                                     │
                                     │ lb://crud-service
                                     │ (round-robin via Eureka)
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
              ▼                      ▼                      ▼
      ┌──────────────┐       ┌──────────────┐       ┌──────────────┐
      │ CRUD Service │       │ CRUD Service │       │ CRUD Service │
      │  replica 1   │       │  replica 2   │       │  replica N   │
      │    :8087     │       │    :8087     │       │    :8087     │
      │ Resilience4j │       │ Resilience4j │       │ Resilience4j │
      └──────┬───────┘       └──────┬───────┘       └──────┬───────┘
             │                      │                      │
             └──────────────┬───────┴──────────────────────┘
                            │
              ┌─────────────┴─────────────┐
              │                           │
              ▼                           ▼
     ┌──────────────────┐       ┌──────────────────┐
     │      MySQL       │       │      Redis       │
     │      :3306       │       │      :6379       │
     │    ncbaTest      │       │      Cache       │
     └──────────────────┘       └──────────────────┘
                            │
                            │ HTTPS
                            ▼
                   ┌──────────────────┐
                   │  SOAP / WSDL     │
                   │  External        │
                   │  CountryInfo     │
                   └──────────────────┘

                     ┌──────────────────┐
                     │  Eureka Server   │
                     │      :8761       │
                     │   (discovery)    │
                     └──────────────────┘
                          ▲       ▲
                          │       │
                    register  register
                          │       │
                    gateway  crud-service
```



###  Request Flow (High-Level)

1. **Client sends a request** to the API Gateway — the only public entry point.
2. **API Gateway** routes to the specific service , applies rate limits (Redis).
3. **Eureka Server** informs the gateway which `crud-service` replica is healthy (round-robin).
4. **CRUD Service** handles the request:
   - Checks **Redis** first — if found, returns immediately (cache hit).
   - On cache miss, calls the **external SOAP/WSDL** service.
   - All SOAP calls are wrapped in **Resilience4j** — retries with exponential backoff, plus a circuit breaker with a fallback.
   - Persists the result to **MySQL**.
   - Updates **Redis** so the next request is fast.
5. **Response** flows back through the gateway to the client.

**Key points:**

- **Cache-first** — most repeated requests never touch MySQL or SOAP.
- **Resilient** — external failures don't crash the system thanks to Resilience4j.
- **Stateless** — any replica can serve any request; scale freely.
- **Discoverable** — services find each other via Eureka, no hardcoded URLs.
---

## C. Technologies Used

###  Core platform

| Technology              | Version     | Why we use it                             |
|-------------------------|-------------|-------------------------------------------|
| Java                    | 21 (LTS)    | Modern language runtime with long support.|
| Spring Boot             | 3.4.1       | The batteries-included Java framework.    |
| Spring Cloud            | 2024.0.0    | Microservice building blocks.             |
| Spring Cloud Gateway    | (from BOM)  | Fast, reactive API gateway.               |
| Netflix Eureka          | (from BOM)  | Service registry & discovery.             |

### Data & caching

| Technology              | Why we use it                                             |
|-------------------------|-----------------------------------------------------------|
| MySQL 8                 | Relational database — the source of truth.                |
| Flyway                  | Version-controlled database migrations.                   |
| Spring Data JPA         | Clean repository abstraction over the database.           |
| Hibernate               | The JPA provider under the hood.                          |
| Redis 7                 | In-memory cache for fast, repeated reads.                 |
| Spring Cache            | Declarative caching via simple annotations.               |

###  Resilience & integration

| Technology              | Why we use it                                             |
|-------------------------|-----------------------------------------------------------|
| Resilience4j            | Retries, circuit breakers, rate limiters — fault tolerance.|
| JAX-WS                  | Standard SOAP client runtime.                             |
| `jaxws-maven-plugin`    | Generates Java classes from a WSDL at build time.         |
| MapStruct               | Compile-time bean mapping (no reflection).                |
| Lombok                  | Reduces boilerplate (`@Data`, `@Slf4j`, etc.).            |
| Jackson                 | JSON serialization.                                       |

###  Containerization

| Technology              | Why we use it                                             |
|-------------------------|-----------------------------------------------------------|
| Docker                  | Packages every service into a portable container.         |
| Docker Compose          | One command to start the whole stack.                     |
| MySQL / Redis images    | Official, battle-tested images.                           |
| CloudBeaver             | Web-based DB browser for development.                     |

###  Build & quality

| Technology              | Why we use it                                             |
|-------------------------|-----------------------------------------------------------|
| Maven                   | Build and dependency management.                          |
| Spring Boot Actuator    | Health checks for Docker and Kubernetes.                  |
| JUnit 5                 | Automated testing.                                        |

---

## D. Running the Application

### Prerequisites

Before you start, make sure you have:

-  **Docker Desktop** v4.30 or newer (WSL2 backend on Windows).
-  **Docker Compose v2** — use `docker compose`, not the old `docker-compose`.
-  **JDK 21** — *only* required if you want to run services outside Docker.
-  **8GB RAM** available to Docker (Settings → Resources → Memory).
-  **Internet access** on first run (Maven downloads dependencies).

### 🚀 Quick start (5 minutes)

#### 1️⃣ Get the code

```bash
git clone https://github.com/<your-org>/<your-repo>.git
cd <your-repo>
```

#### 2️⃣ Create your environment file

Copy the  `.env.example` file to ``.env`` and populate the values


#### 3️⃣ Start the stack

```bash
docker compose -f docker-compose.yml --env-file .env up -d --build
```

Docker starts the services in the right order, waiting for each to become **healthy** before moving on:

```
1. mysql-db, redis        ✅ healthy
2. eureka-server          ✅ healthy
3. crud-service           ✅ healthy
4. api-gateway            ✅ up
```

#### 4️⃣ Verify it's running

Open these in your browser:

| URL                                        | What you'll see              |
|--------------------------------------------|------------------------------|
| http://localhost:8761                    | Eureka dashboard             |
| http://localhost:8086/actuator/health    | `{"status":"UP"}`            |
| http://localhost:8087/actuator/health    | `{"status":"UP"}`            |
| http://localhost:5050                    | CloudBeaver DB browser       |

#### 5️⃣ Try it out

```bash
curl -X POST http://localhost:8086/api/v1/countries/lookup \
  -H "Content-Type: application/json" \
  -d '{"name":"tanzania"}'
```

You should get back `201 Created` with a JSON response.

#### 6️⃣ Stop the stack

```bash
# Stop but keep your data
docker compose -f docker-compose.dev.yml --env-file .env.dev down

# Stop and delete all data
docker compose -f docker-compose.dev.yml --env-file .env.dev down -v
```

---

## E. Docker

###  Compose files

| File                       | When to use it                                        |
|----------------------------|--------------------------------------------------------|
| `docker-compose.dev.yml`   | Development — hot reload, CloudBeaver, faster cycles.|
| `docker-compose.yml`       | Production-style — pre-built JARs, slimmer images.   |

###  Dockerfiles per service

| Service         | Development            | Production       |
|-----------------|------------------------|------------------|
| `eureka-server` | `Dockerfile.dev`       | `Dockerfile`     |
| `api-gateway`   | `Dockerfile.dev`       | `Dockerfile`     |
| `crud-service`  | `Dockerfile.dev`       | `Dockerfile`     |


### Healthchecks

Compose uses health checks so services only start when their dependencies are **truly ready**, not just launched.

| Service         | Healthcheck                                                            |
|-----------------|------------------------------------------------------------------------|
| `mysql-db`      | `mysqladmin ping`                                                      |
| `redis`         | `redis-cli ping`                                                       |
| `eureka-server` | TCP connect on port `8761`                                             |
| `crud-service`  | `curl -fsS http://localhost:8087/actuator/health/readiness`            |
| `api-gateway`   | Actuator readiness (optional)                                          |

### Networks & volumes

**Network:** a single user-defined bridge network (`backend-network`) lets services resolve each other by name — no IP addresses needed.

**Volumes:**

| Volume              | What it stores                           |
|---------------------|------------------------------------------|
| `mysql-data`        | MySQL data (survives restarts).           |
| `redis-data`        | Redis snapshots.                          |
| `cloudbeaver-data`  | CloudBeaver workspace.                    |
| `maven-cache`       | Maven local repo (dev only, saves time).  |

###  Common commands

```bash
# Build a single service
docker compose -f docker-compose.dev.yml --env-file .env build crud-service

# Rebuild without using cache
docker compose -f docker-compose.dev.yml --env-file .env build --no-cache crud-service

# Follow logs
docker compose -f docker-compose.dev.yml logs -f crud-service

# Open a shell inside a container
docker compose -f docker-compose.dev.yml exec crud-service sh

# Full cleanup
docker compose -f docker-compose.dev.yml --env-file .env down -v --rmi local
docker builder prune -a -f
```

---

## F. Deployment

### Prerequisites

- [kind](https://kind.sigs.k8s.io/) installed
- [kubectl](https://kubernetes.io/docs/tasks/tools/) installed
- Docker running

### Run the deployment

From the project root:

```bash
# change directory into deployment folder
cd deployment

# Make the deploy script executable
chmod +x k8s/deploy.sh

# Run it — creates the Kind cluster, builds & loads images, applies manifests
./k8s/deploy.sh

# Check Pods
kubectl get pods -n microservices

# Access the Gateway via NodePort
curl http://localhost:30086/api/v1/countries/lookup

# View Eureka Dashboard (Port Forwarding):
kubectl port-forward svc/eureka-server 8761:8761 -n microservices

# Check Logs
kubectl logs -f deployment/crud-service -n microservices


```

---

## G. Author

<div align="center">

**<Your Name>**

[![GitHub](https://img.shields.io/badge/GitHub-@github.com/patwan-181717?logo=github&logoColor=white)](https://github.com/patwan)
[![Email](https://img.shields.io/badge/Email-mndungu730@gmail.com-EA4335?logo=gmail&logoColor=white)](mndungu730@gmail.com)
[![LinkedIn](https://img.shields.io/badge/linkedin.com/martin_ndungu-0A66C2?logo=linkedin&logoColor=white)](https://www.linkedin.com/in/martin-ndungu-93b747153/)
</div>


---


<div align="center">

</div>