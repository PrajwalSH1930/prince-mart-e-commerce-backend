# 🛒 Prince Mart E-Commerce Backend

**Prince Mart** is a robust, scalable e-commerce backend built using a **Microservices Architecture**.  
It is designed to handle high traffic, ensure fault tolerance, and provide a seamless shopping experience by decoupling core business functionalities into independently deployable services.

---

## 🚀 Architecture Overview

The system follows modern cloud-native patterns:

- **Service Registry Pattern** (Netflix Eureka)
- **API Gateway Pattern** (Single entry point)
- **Microservices-based modular architecture**
- **Event-driven communication**

### 🔑 Key Architectural Components

- **API Gateway**
  - Central entry point
  - Handles routing, filtering, authentication

- **Service Registry (Eureka)**
  - Dynamic service discovery
  - Load balancing support

- **Resilience Layer (Resilience4j)**
  - Circuit Breaker
  - Retry Mechanism
  - Rate Limiting
  - Fault tolerance

---

## 🧩 Core Microservices

| Service | Description |
|--------|------------|
| 🔐 Identity Service | Authentication & Authorization (JWT/OAuth2) |
| 🌐 API Gateway | Request routing & cross-cutting concerns |
| 📡 Server Registry | Service discovery using Eureka |
| 📦 Product Service | Product catalog management |
| 📊 Inventory Service | Real-time stock tracking |
| 🛒 Cart Service | User cart management |
| ❤️ Wishlist Service | Save products for later |
| 📑 Order Service | Order processing & orchestration |
| 💳 Payment Service | Secure transaction handling |
| 🎟 Coupon Service | Discount & promotion logic |
| ⭐ Review Service | Ratings & feedback |
| 🔔 Notification Service | Email/SMS alerts |
| 🚚 Shipping Service | Logistics & delivery tracking |
| 🧾 Audit Service | System-wide logging & history |

---

## 🛠 Tech Stack

### 🔹 Backend
- Java 21
- Spring Boot 3.x

### 🔹 Microservices & Cloud
- Spring Cloud
  - Eureka (Service Discovery)
  - Gateway
  - Config Server

### 🔹 Database
- MySQL

### 🔹 Security
- Spring Security
- JWT Authentication

### 🔹 Resilience & Fault Tolerance
- Resilience4j
  - Circuit Breaker
  - Retry
  - Rate Limiter

### 🔹 Build Tool
- Maven

---

## 📂 Project Structure

```

prince-mart-e-commerce-backend
├── api-gateway
├── audit-service
├── cart-service
├── coupon-service
├── identity-service
├── inventory-service
├── notification-service
├── order-service
├── payment-service
├── product-service
├── review-service
├── server-registry
├── shipping-service
└── wishlist-service

````

---

## ⚙️ Getting Started

### ✅ Prerequisites

- JDK 21
- Maven 3.x
- Docker (optional but recommended)

---

### 📥 Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/your-username/prince-mart-backend.git
cd prince-mart-backend
````

---

#### 2. Start Service Registry

```bash
cd server-registry
mvn spring-boot:run
```

---

#### 3. Start All Services

Run each microservice individually:

```bash
mvn spring-boot:run
```

👉 Ensure **API Gateway** is running to access endpoints.

---

## 🛡️ Key Features

### 🔹 Scalability

* Independent scaling of microservices

### 🔹 Fault Tolerance (Resilience4j)

* Circuit Breaker prevents cascading failures
* Retry mechanism for transient errors
* Rate limiting for traffic control

### 🔹 Centralized Configuration

* Config Server for managing service configs

### 🔹 Distributed Tracing

* Track requests across services

### 🔹 Asynchronous Communication

* Event-driven architecture using Kafka/RabbitMQ

---

## 🔄 API Flow (High Level)

```
Client → API Gateway → Microservices → Database 
```

---

## 📈 Future Enhancements

* Kubernetes deployment (K8s)
* CI/CD pipeline integration
* Observability (Prometheus + Grafana)
* ElasticSearch for logging
* OpenTelemetry tracing

---

## 👨‍💻 Author

**Prajwal Hiremath**
🚀 Java Full Stack Developer

---

## ⭐ Support

If you like this project:

* ⭐ Star the repo
* 🍴 Fork it
* 🛠 Contribute

