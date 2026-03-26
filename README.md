# Saga Pattern — Choreography

A practical example of the **Choreography-based Saga Pattern** applied to an order flow (e-commerce), using **Kotlin**, **Spring Boot 4**, **AWS SNS/SQS**, and **Clean Architecture**.

---

## What is the Saga Pattern?

The **Saga Pattern** is a strategy for managing distributed transactions in microservices architectures. Since each service has its own database, it is not possible to use a single ACID transaction spanning multiple services. Saga solves this by breaking the transaction into a **sequence of local transactions**, where each step publishes an event that triggers the next one.

### Why is the Saga Pattern useful?

| Problem | Solution with Saga |
|---|---|
| Distributed transactions are impossible with multiple databases | Each service executes its local transaction and emits an event |
| Partial failure leaves the system in an inconsistent state | Compensating transactions revert what was done |
| Strong coupling between services | Services communicate only via events, without direct calls |
| Difficulty scaling critical operations | Each service scales independently |

### Orchestration vs. Choreography

This project implements the **Choreography** approach: there is no central coordinator. Each service only knows its own role and reacts to the events it receives, publishing new events in return. The flow emerges from the collaboration between services.

---

## Project Overview

```
saga-pattern-choreography/
├── shared/               # DTOs and models shared across services
├── order-service/        # Manages orders and starts the flow (port 8080)
├── payment-service/      # Processes and reverts payments (port 8081)
├── inventory-service/    # Controls stock and performs write-offs (port 8082)
├── shipping-service/     # Manages deliveries (port 8083)
├── notification-service/ # Sends notifications to the customer (port 8084)
└── local-execution/      # Docker Compose + initialization scripts
```

### Stack

- **Language:** Kotlin 2.3
- **Framework:** Spring Boot 4.0
- **Messaging:** AWS SNS (fan-out topic) + AWS SQS (per-service queues)
- **Database:** PostgreSQL 16 (separate database per service)
- **Local infrastructure:** LocalStack 4 (emulates AWS SNS/SQS)
- **Build:** Gradle (multi-module)

---

## Saga Flow

The flow is initiated by calling the checkout endpoint for an order. From that point on, services communicate exclusively through events via SNS/SQS.

### Happy path ✅

```
Client
  │
  ▼
POST /orders/{id}/checkout
  │  status: ORDER_CHECKOUT
  ▼
[order-service] ──────────────────────────────────────────────► SNS: ORDER_ACTION
                                                                        │
                         ┌──────────────────────────────────────────────┤
                         │ filter: ORDER_CHECKOUT                       │ (all statuses)
                         ▼                                              ▼
               [payment-service]                              [notification-service]
               Processes payment                              Notifies customer by e-mail
               status: ORDER_PAID
                         │
                         ▼ SNS: ORDER_ACTION
                         │ filter: ORDER_PAID
                         ▼
               [inventory-service]
               Stock write-off
               status: INVENTORY_WRITE_OFF
                         │
                         ▼ SNS: ORDER_ACTION
                         │ filter: INVENTORY_WRITE_OFF
                         ▼
               [shipping-service]
               Creates shipment + tracking number
               status: ORDER_DELIVERING
                         │
                         ▼ (manual intervention via API)
PUT /shippings/{tracking}/delivery { success: true }
                         │
               [shipping-service]
               Confirms delivery
               status: ORDER_DELIVERED
```

### Compensation paths ↩️

**Out of stock:**
```
[inventory-service] → OUT_OF_STOCK
  └─► [payment-service] reverts payment → ORDER_PAID_REVERSED
```

**Delivery failed:**
```
PUT /shippings/{tracking}/delivery { success: false }
  └─► [shipping-service] → ORDER_NOT_DELIVERED
        ├─► [payment-service] reverts payment → ORDER_PAID_REVERSED
        └─► [inventory-service] returns items to stock
```

### Events and queues map

| Published event | Topic | Consumer queue | Consumer service |
|---|---|---|---|
| `ORDER_CHECKOUT` | `ORDER_ACTION` | `PAYMENT_SERVICE_ORDER_CHECKOUT_QUEUE` | payment-service |
| `ORDER_PAID` | `ORDER_ACTION` | `INVENTORY_SERVICE_INVENTORY_WRITE_OFF_QUEUE` | inventory-service |
| `INVENTORY_WRITE_OFF` | `ORDER_ACTION` | `SHIPPING_SERVICE_INVENTORY_WRITE_OFF_QUEUE` | shipping-service |
| `OUT_OF_STOCK` | `ORDER_ACTION` | `PAYMENT_SERVICE_PAYMENT_REVERT_QUEUE` | payment-service |
| `ORDER_NOT_DELIVERED` | `ORDER_ACTION` | `PAYMENT_SERVICE_PAYMENT_REVERT_QUEUE` | payment-service |
| `ORDER_NOT_DELIVERED` | `ORDER_ACTION` | `INVENTORY_SERVICE_PUT_BACK_QUEUE` | inventory-service |
| `*` (all) | `ORDER_ACTION` | `ORDER_SERVICE_STATUS_QUEUE` | order-service |
| `*` (all) | `ORDER_ACTION` | `NOTIFICATION_SERVICE_ORDER_QUEUE` | notification-service |

---

## Prerequisites

- **Java 21+**
- **Docker** and **Docker Compose**
- **Gradle** (or use the wrapper `./gradlew`)

---

## How to run

### 1. Start the infrastructure (PostgreSQL + LocalStack)

```bash
cd local-execution
docker compose up -d
```

Wait a few seconds. LocalStack will automatically create the SNS topic, SQS queues, and all subscriptions with filters via the `localstack-init.sh` script.

To verify that the queues were created:

```bash
aws --endpoint-url=http://localhost:4566 \
    --region sa-east-1 \
    --no-cli-pager \
    sqs list-queues
```

### 2. Build all modules

From the project root:

```bash
./gradlew build -x test
```

### 3. Start the services

Each service is an independent Gradle module. Open a terminal for each one:

```bash
# Terminal 1 — order-service (port 8080)
./gradlew :order-service:bootRun

# Terminal 2 — payment-service (port 8081)
./gradlew :payment-service:bootRun

# Terminal 3 — inventory-service (port 8082)
./gradlew :inventory-service:bootRun

# Terminal 4 — shipping-service (port 8083)
./gradlew :shipping-service:bootRun

# Terminal 5 — notification-service (port 8084)
./gradlew :notification-service:bootRun
```

---

## Testing the full flow

The database is pre-populated with sample data (customers, products, orders) via the PostgreSQL initialization script.

### Step 1 — Start the checkout of an order

```bash
curl -s -X POST http://localhost:8080/orders/1/checkout | jq
```

From this point on, follow the logs of each service to observe the saga flow unfolding automatically via events.

### Step 2 — Check the order status

```bash
curl -s http://localhost:8080/orders/1 | jq
```

### Step 3 — Confirm delivery (success)

Get the `trackingNumber` from the `shipping-service` logs and run:

```bash
curl -s -X PUT http://localhost:8083/shippings/{trackingNumber}/delivery \
  -H "Content-Type: application/json" \
  -d '{"success": true, "receivedBy": "John Smith"}' | jq
```

### Step 3 (alternative) — Register a delivery failure

```bash
curl -s -X PUT http://localhost:8083/shippings/{trackingNumber}/delivery \
  -H "Content-Type: application/json" \
  -d '{"success": false, "failureReason": "Address not found"}' | jq
```

When a failure is registered, the saga initiates **compensating transactions**: the payment is reverted and the stock is automatically restocked.

---

## Service ports

| Service | Port |
|---|---|
| order-service | `8080` |
| payment-service | `8081` |
| inventory-service | `8082` |
| shipping-service | `8083` |
| notification-service | `8084` |
| PostgreSQL | `5432` |
| LocalStack (SNS/SQS) | `4566` |

---

## Stopping the environment

```bash
cd local-execution
docker compose down
```

To also remove the volumes (database and queues):

```bash
docker compose down -v
```
