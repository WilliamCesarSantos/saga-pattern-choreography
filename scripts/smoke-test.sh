#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$REPO_ROOT"

echo "1) Starting LocalStack + Postgres via docker-compose"
docker-compose -f local-execution/docker-compose.yml up -d

echo "Waiting for LocalStack to be healthy..."
until curl -s http://localhost:4566/health | grep -q '"services":'; do
  sleep 1
done
sleep 2

echo "2) Build all service jars (this may take a while)"
./gradlew :order-service:bootJar :payment-service:bootJar :inventory-service:bootJar :shipping-service:bootJar :notification-service:bootJar --no-daemon

mkdir -p logs

AWS_ENDPOINT=http://localhost:4566
TOPIC_ARN=arn:aws:sns:us-east-1:000000000000:ORDER_ACTION

echo "3) Start services (each on separate port)"
# Order service
ORDER_JAR=$(ls order-service/build/libs/*-SNAPSHOT.jar 2>/dev/null || true)
if [ -z "$ORDER_JAR" ]; then ORDER_JAR=$(ls order-service/build/libs/*.jar | head -n1); fi
nohup java -Dserver.port=8081 -Daws.endpoint=$AWS_ENDPOINT -Dspring.datasource.url=jdbc:postgresql://localhost:5432/order_service -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres -jar "$ORDER_JAR" > logs/order.log 2>&1 &
ORDER_PID=$!

# Payment service
PAYMENT_JAR=$(ls payment-service/build/libs/*-SNAPSHOT.jar 2>/dev/null || true)
if [ -z "$PAYMENT_JAR" ]; then PAYMENT_JAR=$(ls payment-service/build/libs/*.jar | head -n1); fi
nohup java -Dserver.port=8082 -Daws.endpoint=$AWS_ENDPOINT -Dspring.datasource.url=jdbc:postgresql://localhost:5432/payment_service -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres -jar "$PAYMENT_JAR" > logs/payment.log 2>&1 &
PAYMENT_PID=$!

# Inventory service
INVENTORY_JAR=$(ls inventory-service/build/libs/*-SNAPSHOT.jar 2>/dev/null || true)
if [ -z "$INVENTORY_JAR" ]; then INVENTORY_JAR=$(ls inventory-service/build/libs/*.jar | head -n1); fi
nohup java -Dserver.port=8083 -Daws.endpoint=$AWS_ENDPOINT -Dspring.datasource.url=jdbc:postgresql://localhost:5432/inventory_service -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres -jar "$INVENTORY_JAR" > logs/inventory.log 2>&1 &
INVENTORY_PID=$!

# Shipping service
SHIPPING_JAR=$(ls shipping-service/build/libs/*-SNAPSHOT.jar 2>/dev/null || true)
if [ -z "$SHIPPING_JAR" ]; then SHIPPING_JAR=$(ls shipping-service/build/libs/*.jar | head -n1); fi
nohup java -Dserver.port=8084 -Daws.endpoint=$AWS_ENDPOINT -Dspring.datasource.url=jdbc:postgresql://localhost:5432/shipping_service -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres -jar "$SHIPPING_JAR" > logs/shipping.log 2>&1 &
SHIPPING_PID=$!

# Notification service
NOTIF_JAR=$(ls notification-service/build/libs/*-SNAPSHOT.jar 2>/dev/null || true)
if [ -z "$NOTIF_JAR" ]; then NOTIF_JAR=$(ls notification-service/build/libs/*.jar | head -n1); fi
nohup java -Dserver.port=8085 -Daws.endpoint=$AWS_ENDPOINT -Dspring.datasource.url=jdbc:postgresql://localhost:5432/notification_service -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres -jar "$NOTIF_JAR" > logs/notification.log 2>&1 &
NOTIF_PID=$!

# Wait for services to boot (simple sleep, or you can tail logs to check readiness)
echo "Waiting 10s for services to start..."
sleep 10

# Insert sample order into order_service DB
echo "4) Inserting sample order into order_service DB"
PGPASSWORD=postgres psql -h localhost -U postgres -d order_service -c "CREATE TABLE IF NOT EXISTS orders (id VARCHAR PRIMARY KEY, status VARCHAR, created_at TIMESTAMP, total NUMERIC, customer_id VARCHAR);"
PGPASSWORD=postgres psql -h localhost -U postgres -d order_service -c "CREATE TABLE IF NOT EXISTS order_items (id VARCHAR PRIMARY KEY, order_id VARCHAR, product_id VARCHAR, quantity INTEGER, price NUMERIC);"

PGPASSWORD=postgres psql -h localhost -U postgres -d order_service <<SQL
INSERT INTO orders (id, status, created_at, total, customer_id) VALUES ('order-1','CREATED', now(), 42, 'cust-1') ON CONFLICT (id) DO NOTHING;
INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES ('item-1','order-1','prod-1',2,21) ON CONFLICT (id) DO NOTHING;
SQL

# Trigger checkout endpoint
echo "5) Triggering checkout for order-1"
curl -v -X POST http://localhost:8081/orders/order-1/checkout || true

sleep 2

echo "Logs (tail 200 lines)"
echo "--- order.log ---"
tail -n 200 logs/order.log || true

echo "--- payment.log ---"
tail -n 200 logs/payment.log || true

echo "--- inventory.log ---"
tail -n 200 logs/inventory.log || true

echo "--- shipping.log ---"
tail -n 200 logs/shipping.log || true

echo "--- notification.log ---"
tail -n 200 logs/notification.log || true

echo "Smoke test finished. Services are running with PIDs: $ORDER_PID $PAYMENT_PID $INVENTORY_PID $SHIPPING_PID $NOTIF_PID"

echo "If you want to stop everything run:"
echo "  docker-compose -f local-execution/docker-compose.yml down"
echo "  kill $ORDER_PID $PAYMENT_PID $INVENTORY_PID $SHIPPING_PID $NOTIF_PID || true"

