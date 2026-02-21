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

# Create SNS topic
echo "Creating SNS topic: $TOPIC_NAME"
TOPIC_ARN=$(awslocal sns create-topic --name "$TOPIC_NAME" --output text)
# The above prints the ARN. Normalize to variable
if [[ -z "$TOPIC_ARN" ]]; then
  echo "Failed to create topic"
  exit 1
fi

echo "Topic ARN: $TOPIC_ARN"

# Create queues and subscribe them to the topic
for q in "${QUEUES[@]}"; do
  echo "Creating queue: $q"
  QUEUE_URL=$(awslocal sqs create-queue --queue-name "$q" --output text)
  echo "Queue URL: $QUEUE_URL"
  # Get queue ARN
  QUEUE_ARN=$(awslocal sqs get-queue-attributes --queue-url "$QUEUE_URL" --attribute-names QueueArn --output text | awk '{print $2}')
  echo "Queue ARN: $QUEUE_ARN"

  # Allow SNS to send messages to SQS queue by setting a queue policy
  POLICY=$(cat <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "Allow-SNS-SendMessage",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "SQS:SendMessage",
      "Resource": "$QUEUE_ARN",
      "Condition": {
        "ArnEquals": {"aws:SourceArn": "$TOPIC_ARN"}
      }
    }
  ]
}
POLICY
)
  awslocal sqs set-queue-attributes --queue-url "$QUEUE_URL" --attributes Policy="$POLICY"

  echo "Subscribing $QUEUE_ARN to topic"
  SUB_ARN=$(awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$QUEUE_ARN" --output text)
  echo "Subscribed $q -> $SUB_ARN"

  # Apply filter policy if defined for this queue
  FILTER_POLICY="${FILTERS[$q]}"
  if [[ -n "$FILTER_POLICY" ]]; then
    echo "Setting filter policy for $q: $FILTER_POLICY"
    # Note: using --attribute-value with JSON string
    awslocal sns set-subscription-attributes --subscription-arn "$SUB_ARN" --attribute-name FilterPolicy --attribute-value "$FILTER_POLICY"
  else
    echo "No filter policy for $q (receives all messages)"
  fi

done

# Optionally, you can adjust filter policies or attributes here.

echo "Localstack init completed"

exit 0

