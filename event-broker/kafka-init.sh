#!/bin/bash

KAFKA_CONTAINER=kafka

TOPICS=(
  order-created
  payment-success
  payment-failed
  inventory-success
  inventory-failed
  shipping-success
  notification-success
)

for TOPIC in "${TOPICS[@]}"
do
  docker exec $KAFKA_CONTAINER kafka-topics --create \
    --topic "$TOPIC" \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 1
  echo "Tópico '$TOPIC' criado."
done