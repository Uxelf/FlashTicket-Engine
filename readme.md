# FlashTicket Engine 🚀

FlashTicket Engine is the core of a high-concurrency ticket sales engine designed using **Java, Spring Boot, and PostgreSQL**.

The system is optimized to manage the reservation and sale of seats for multiple simultaneous events, guaranteeing data integrity (preventing double selling) under massive stress scenarios where thousands of users attempt to acquire the exact same seats at the same millisecond.

---

## 🛠️ Tech Stack

*   **Backend:** Java 17/21 & Spring Boot 3.x
*   **Persistence:** JPA / Hibernate & PostgreSQL
*   **Containers:** Docker & Docker Compose
*   **Testing:** JUnit 5 & Testcontainers (PostgreSQL)

---

## 📋 Prerequisites

Before running the project, make sure you have the following installed:
*   [Docker & Docker Compose](https://www.docker.com/)

---

## 🚀 Quick Start (Development)

To spin up the entire application environment (both the PostgreSQL database and the Spring Boot backend), run:

```bash
docker-compose up --build -d
```

This command will:
1. Build the Spring Boot backend image using its Dockerfile.
2. Spin up the PostgreSQL database and the backend container.
3. Expose the API on port `8080` (accessible at `http://localhost:8080`).

To stop and remove the containers:
```bash
docker-compose down
```

---

## 🗺️ API REST Design (MVP)

| Method | Endpoint | Description | Status |
| :--- | :--- | :--- | :--- |
| **`GET`** | `/api/v1/events/{eventId}/seats` | Get the list of seats for an event and their current status. | ⏳ Pending |
| **`POST`** | `/api/v1/reservations` | Create a temporary reservation for a seat (valid for 10 min). | ⏳ Pending |
| **`POST`** | `/api/v1/reservations/{reservationId}/confirm` | Confirm the purchase and make the seat reservation permanent. | ⏳ Pending |

---

## 🧪 Running Tests

The project includes integration tests that spin up a real PostgreSQL container using **Testcontainers** and simulate high-concurrency conditions.

To run the entire test suite:
```bash
./mvnw test
```

---

## 🏗️ Project Status (Checklist)

- [ ] Base project structure and Docker Compose configuration.
- [ ] Data model and entities configuration (Event, Venue, Seat, Reservation).
- [ ] Reservation logic implementation using **Optimistic Locking** (`@Version`).
- [ ] Auto-expiration mechanism for reservations (10-minute scheduler).
- [ ] Performance optimizations (preventing N+1 queries and index creation).
- [ ] Integration tests setup using **Testcontainers**.
- [ ] Concurrent stress test implementation (100 threads simulation).
- [ ] CI pipeline configuration with GitHub Actions.
