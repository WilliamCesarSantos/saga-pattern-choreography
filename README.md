# Saga Pattern — Choreography

Exemplo prático de **Saga Pattern Coreografado** aplicado a um fluxo de pedidos (e-commerce), utilizando **Kotlin**, **Spring Boot 4**, **AWS SNS/SQS** e arquitetura **Clean Architecture**.

---

## O que é o Saga Pattern?

O **Saga Pattern** é uma estratégia para gerenciar transações distribuídas em arquiteturas de microsserviços. Como cada serviço possui seu próprio banco de dados, não é possível usar uma transação ACID única que abranja múltiplos serviços. O Saga resolve isso quebrando a transação em uma **sequência de transações locais**, onde cada etapa publica um evento que aciona a próxima.

### Por que o Saga Pattern é útil?

| Problema | Solução com Saga |
|---|---|
| Transações distribuídas impossíveis com múltiplos bancos | Cada serviço executa sua transação local e emite um evento |
| Falha parcial deixa o sistema em estado inconsistente | Transações compensatórias revertem o que foi feito |
| Acoplamento forte entre serviços | Serviços se comunicam apenas via eventos, sem chamadas diretas |
| Dificuldade de escalar operações críticas | Cada serviço escala de forma independente |

### Orquestração vs. Coreografia

Este projeto implementa a abordagem de **Coreografia**: não existe um coordenador central. Cada serviço conhece apenas seu papel e reage aos eventos que recebe, publicando novos eventos em seguida. O fluxo emerge da colaboração entre os serviços.

---

## Visão Geral do Projeto

```
saga-pattern-choreography/
├── shared/               # DTOs e modelos compartilhados entre os serviços
├── order-service/        # Gerencia pedidos e inicia o fluxo (porta 8080)
├── payment-service/      # Processa e reverte pagamentos (porta 8081)
├── inventory-service/    # Controla estoque e realiza baixas (porta 8082)
├── shipping-service/     # Gerencia entregas (porta 8083)
├── notification-service/ # Envia notificações ao cliente (porta 8084)
└── local-execution/      # Docker Compose + scripts de inicialização
```

### Stack

- **Linguagem:** Kotlin 2.3
- **Framework:** Spring Boot 4.0
- **Mensageria:** AWS SNS (tópico fan-out) + AWS SQS (filas por serviço)
- **Banco de dados:** PostgreSQL 16 (banco separado por serviço)
- **Infraestrutura local:** LocalStack 4 (emula SNS/SQS da AWS)
- **Build:** Gradle (multi-módulo)

---

## Fluxo da Saga

O fluxo é iniciado com a chamada ao endpoint de checkout de um pedido. A partir daí, os serviços se comunicam exclusivamente por eventos via SNS/SQS.

### Caminho feliz ✅

```
Cliente
  │
  ▼
POST /orders/{id}/checkout
  │  status: ORDER_CHECKOUT
  ▼
[order-service] ──────────────────────────────────────────────► SNS: ORDER_ACTION
                                                                        │
                         ┌──────────────────────────────────────────────┤
                         │ filter: ORDER_CHECKOUT                       │ (todos os status)
                         ▼                                              ▼
               [payment-service]                              [notification-service]
               Processa pagamento                              Notifica cliente por e-mail
               status: ORDER_PAID
                         │
                         ▼ SNS: ORDER_ACTION
                         │ filter: ORDER_PAID
                         ▼
               [inventory-service]
               Baixa no estoque
               status: INVENTORY_WRITE_OFF
                         │
                         ▼ SNS: ORDER_ACTION
                         │ filter: INVENTORY_WRITE_OFF
                         ▼
               [shipping-service]
               Cria envio + rastreio
               status: ORDER_DELIVERING
                         │
                         ▼ (intervenção manual via API)
PUT /shippings/{tracking}/delivery { success: true }
                         │
               [shipping-service]
               Confirma entrega
               status: ORDER_DELIVERED
```

### Caminhos de compensação ↩️

**Sem estoque:**
```
[inventory-service] → OUT_OF_STOCK
  └─► [payment-service] reverte pagamento → ORDER_PAID_REVERSED
```

**Entrega falhou:**
```
PUT /shippings/{tracking}/delivery { success: false }
  └─► [shipping-service] → ORDER_NOT_DELIVERED
        ├─► [payment-service] reverte pagamento → ORDER_PAID_REVERSED
        └─► [inventory-service] devolve itens ao estoque
```

### Mapa de eventos e filas

| Evento publicado | Tópico | Fila consumidora | Serviço consumidor |
|---|---|---|---|
| `ORDER_CHECKOUT` | `ORDER_ACTION` | `PAYMENT_SERVICE_ORDER_CHECKOUT_QUEUE` | payment-service |
| `ORDER_PAID` | `ORDER_ACTION` | `INVENTORY_SERVICE_INVENTORY_WRITE_OFF_QUEUE` | inventory-service |
| `INVENTORY_WRITE_OFF` | `ORDER_ACTION` | `SHIPPING_SERVICE_INVENTORY_WRITE_OFF_QUEUE` | shipping-service |
| `OUT_OF_STOCK` | `ORDER_ACTION` | `PAYMENT_SERVICE_PAYMENT_REVERT_QUEUE` | payment-service |
| `ORDER_NOT_DELIVERED` | `ORDER_ACTION` | `PAYMENT_SERVICE_PAYMENT_REVERT_QUEUE` | payment-service |
| `ORDER_NOT_DELIVERED` | `ORDER_ACTION` | `INVENTORY_SERVICE_PUT_BACK_QUEUE` | inventory-service |
| `*` (todos) | `ORDER_ACTION` | `ORDER_SERVICE_STATUS_QUEUE` | order-service |
| `*` (todos) | `ORDER_ACTION` | `NOTIFICATION_SERVICE_ORDER_QUEUE` | notification-service |

---

## Pré-requisitos

- **Java 21+**
- **Docker** e **Docker Compose**
- **Gradle** (ou use o wrapper `./gradlew`)

---

## Como executar

### 1. Subir a infraestrutura (PostgreSQL + LocalStack)

```bash
cd local-execution
docker compose up -d
```

Aguarde alguns segundos. O LocalStack criará automaticamente o tópico SNS, as filas SQS e todas as assinaturas com filtros via o script `localstack-init.sh`.

Para verificar que as filas foram criadas:

```bash
aws --endpoint-url=http://localhost:4566 \
    --region sa-east-1 \
    --no-cli-pager \
    sqs list-queues
```

### 2. Compilar todos os módulos

Na raiz do projeto:

```bash
./gradlew build -x test
```

### 3. Iniciar os serviços

Cada serviço é um módulo Gradle independente. Abra um terminal para cada um:

```bash
# Terminal 1 — order-service (porta 8080)
./gradlew :order-service:bootRun

# Terminal 2 — payment-service (porta 8081)
./gradlew :payment-service:bootRun

# Terminal 3 — inventory-service (porta 8082)
./gradlew :inventory-service:bootRun

# Terminal 4 — shipping-service (porta 8083)
./gradlew :shipping-service:bootRun

# Terminal 5 — notification-service (porta 8084)
./gradlew :notification-service:bootRun
```

---

## Testando o fluxo completo

O banco já é populado com dados de exemplo (customers, products, orders) via script de inicialização do PostgreSQL.

### Passo 1 — Iniciar o checkout de um pedido

```bash
curl -s -X POST http://localhost:8080/orders/1/checkout | jq
```

A partir deste momento, acompanhe os logs de cada serviço para observar o fluxo da saga se desenrolando automaticamente via eventos.

### Passo 2 — Consultar o status do pedido

```bash
curl -s http://localhost:8080/orders/1 | jq
```

### Passo 3 — Confirmar entrega (sucesso)

Obtenha o `trackingNumber` nos logs do `shipping-service` e execute:

```bash
curl -s -X PUT http://localhost:8083/shippings/{trackingNumber}/delivery \
  -H "Content-Type: application/json" \
  -d '{"success": true, "receivedBy": "João Silva"}' | jq
```

### Passo 3 (alternativo) — Registrar falha na entrega

```bash
curl -s -X PUT http://localhost:8083/shippings/{trackingNumber}/delivery \
  -H "Content-Type: application/json" \
  -d '{"success": false, "failureReason": "Endereço não encontrado"}' | jq
```

Ao registrar falha, a saga inicia as **transações compensatórias**: o pagamento é revertido e o estoque é reposto automaticamente.

---

## Portas dos serviços

| Serviço | Porta |
|---|---|
| order-service | `8080` |
| payment-service | `8081` |
| inventory-service | `8082` |
| shipping-service | `8083` |
| notification-service | `8084` |
| PostgreSQL | `5432` |
| LocalStack (SNS/SQS) | `4566` |

---

## Parar o ambiente

```bash
cd local-execution
docker compose down
```

Para remover também os volumes (banco e filas):

```bash
docker compose down -v
```

