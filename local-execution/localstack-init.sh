#!/usr/bin/env bash

set -euo pipefail

# Este script será executado pelo LocalStack quando estiver pronto.
# Ele cria o tópico SNS ORDER_ACTION, cria as filas SQS (com sufixo _QUEUE) e inscreve as filas no tópico.
# Usa o cliente `awslocal` fornecido pelo LocalStack.

echo "Starting localstack init script"

TOPIC_NAME="ORDER_ACTION"
QUEUES=(
  "ORDER_SERVICE_STATUS_QUEUE"
  "PAYMENT_SERVICE_ORDER_CHECKOUT_QUEUE"
  "PAYMENT_SERVICE_ORDER_REVERT_QUEUE"
  "INVENTORY_SERVICE_INVENTORY_WRITE_OFF_QUEUE"
  "INVENTORY_SERVICE_PUT_BACK_QUEUE"
  "SHIPPING_SERVICE_INVENTORY_WRITE_OFF_QUEUE"
  "NOTIFICATION_SERVICE_ORDER_QUEUE"
)

# Map of queue -> SNS filter policy (JSON). Empty string => no filter (receive all messages)
# NOTE: SNS FilterPolicy matches message attributes. Services MUST publish the 'status' attribute
# in the SNS publish call for filtering to work (ex: --message-attributes 'status=ORDER_CHECKOUT').
declare -A FILTERS
FILTERS["ORDER_SERVICE_STATUS_QUEUE"]=""
FILTERS["PAYMENT_SERVICE_ORDER_CHECKOUT_QUEUE"]='{"status":["ORDER_CHECKOUT"]}'
FILTERS["PAYMENT_SERVICE_ORDER_REVERT_QUEUE"]='{"status":["OUT_OF_STOCK","ORDER_NOT_DELIVERED"]}'
FILTERS["INVENTORY_SERVICE_INVENTORY_WRITE_OFF_QUEUE"]='{"status":["ORDER_PAID"]}'
FILTERS["INVENTORY_SERVICE_PUT_BACK_QUEUE"]='{"status":["ORDER_NOT_DELIVERED"]}'
FILTERS["SHIPPING_SERVICE_INVENTORY_WRITE_OFF_QUEUE"]='{"status":["INVENTORY_WRITE_OFF"]}'
FILTERS["NOTIFICATION_SERVICE_ORDER_QUEUE"]=''

# Wait for localstack to be ready
echo "Waiting for LocalStack..."
until curl -s http://localhost:4566/health | grep -q '"services":'; do
  sleep 1
done
sleep 2

export AWS_PAGER=""
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

awslocal sns create-topic --name ORDER_ACTION || true

# Create queues
queues=(
  ORDER_SERVICE_STATUS_QUEUE
  PAYMENT_SERVICE_ORDER_CHECKOUT_QUEUE
  PAYMENT_SERVICE_ORDER_REVERT_QUEUE
  INVENTORY_SERVICE_INVENTORY_WRITE_OFF_QUEUE
  INVENTORY_SERVICE_PUT_BACK_QUEUE
  SHIPPING_SERVICE_INVENTORY_WRITE_OFF_QUEUE
  NOTIFICATION_SERVICE_ORDER_QUEUE
)

for q in "${queues[@]}"; do
  echo "Creating queue $q"
  awslocal sqs create-queue --queue-name "$q" || true
done

TOPIC_ARN=$(awslocal sns list-topics | jq -r '.Topics[] | select(.TopicArn | contains("ORDER_ACTION")) | .TopicArn')

# Subscribe queues to topic with filter policies
subscribe_queue() {
  local queueName="$1"
  local filterPolicy="$2"
  QURL=$(awslocal sqs get-queue-url --queue-name "$queueName" | jq -r '.QueueUrl')
  QARN=$(awslocal sqs get-queue-attributes --queue-url "$QURL" --attribute-names QueueArn | jq -r '.Attributes.QueueArn')

  # Allow SNS to send to SQS
  awslocal sqs set-queue-attributes --queue-url "$QURL" --attributes "{\"Policy\": \"{\\\"Version\\\":\\\"2012-10-17\\\",\\\"Statement\\\":[{\\\"Effect\\\":\\\"Allow\\\",\\\"Principal\\\":{\\\"AWS\\\":\\\"*\\\"},\\\"Action\\\":\\\"SQS:SendMessage\\\",\\\"Resource\\\":\\\"$QARN\\\"}]}\"}"

  if [ -z "$filterPolicy" ] || [ "$filterPolicy" = "ALL" ]; then
    awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$QARN"
  else
    # filterPolicy should be valid JSON
    awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$QARN" --attributes "FilterPolicy=$filterPolicy"
  fi
}

# Subscriptions according to rules
subscribe_queue "ORDER_SERVICE_STATUS_QUEUE" "ALL"
subscribe_queue "NOTIFICATION_SERVICE_ORDER_QUEUE" "ALL"
subscribe_queue "PAYMENT_SERVICE_ORDER_CHECKOUT_QUEUE" '{"status":["ORDER_CHECKOUT"]}'
subscribe_queue "PAYMENT_SERVICE_ORDER_REVERT_QUEUE" '{"status":["OUT_OF_STOCK","ORDER_NOT_DELIVERED"]}'
subscribe_queue "INVENTORY_SERVICE_INVENTORY_WRITE_OFF_QUEUE" '{"status":["ORDER_PAID"]}'
subscribe_queue "INVENTORY_SERVICE_PUT_BACK_QUEUE" '{"status":["ORDER_NOT_DELIVERED"]}'
subscribe_queue "SHIPPING_SERVICE_INVENTORY_WRITE_OFF_QUEUE" '{"status":["INVENTORY_WRITE_OFF"]}'

echo "LocalStack initialization complete."

exit 0

