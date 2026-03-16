#!/bin/bash

export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=sa-east-1

echo "Creating SNS topic ORDER_ACTION"
awslocal sns create-topic --name ORDER_ACTION
export TOPIC_ARN=arn:aws:sns:sa-east-1:000000000000:ORDER_ACTION
echo "Topic ARN: $TOPIC_ARN"

echo "Creating SQS queues"
awslocal sqs create-queue --queue-name ORDER_SERVICE_STATUS_QUEUE
export ORDER_SERVICE_STATUS_ARN=arn:aws:sqs:sa-east-1:000000000000:ORDER_SERVICE_STATUS_QUEUE

awslocal sqs create-queue --queue-name PAYMENT_SERVICE_ORDER_CHECKOUT_QUEUE
export PAYMENT_CHECKOUT_ARN=arn:aws:sqs:sa-east-1:000000000000:PAYMENT_SERVICE_ORDER_CHECKOUT_QUEUE

awslocal sqs create-queue --queue-name PAYMENT_SERVICE_PAYMENT_REVERT_QUEUE
export PAYMENT_REVERT_ARN=arn:aws:sqs:sa-east-1:000000000000:PAYMENT_SERVICE_PAYMENT_REVERT_QUEUE

awslocal sqs create-queue --queue-name INVENTORY_SERVICE_INVENTORY_WRITE_OFF_QUEUE
export INVENTORY_WRITE_OFF_ARN=arn:aws:sqs:sa-east-1:000000000000:INVENTORY_SERVICE_INVENTORY_WRITE_OFF_QUEUE

awslocal sqs create-queue --queue-name INVENTORY_SERVICE_PUT_BACK_QUEUE
export INVENTORY_PUT_BACK_ARN=arn:aws:sqs:sa-east-1:000000000000:INVENTORY_SERVICE_PUT_BACK_QUEUE

awslocal sqs create-queue --queue-name SHIPPING_SERVICE_INVENTORY_WRITE_OFF_QUEUE
export SHIPPING_WRITE_OFF_ARN=arn:aws:sqs:sa-east-1:000000000000:SHIPPING_SERVICE_INVENTORY_WRITE_OFF_QUEUE

awslocal sqs create-queue --queue-name NOTIFICATION_SERVICE_ORDER_QUEUE
export NOTIFICATION_ORDER_ARN=arn:aws:sqs:sa-east-1:000000000000:NOTIFICATION_SERVICE_ORDER_QUEUE

echo "Subscribing queues to ORDER_ACTION topic"
awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$ORDER_SERVICE_STATUS_ARN" --attributes '{"RawMessageDelivery":"true"}'
awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$NOTIFICATION_ORDER_ARN"   --attributes '{"RawMessageDelivery":"true"}'
awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$PAYMENT_CHECKOUT_ARN"     --attributes '{"RawMessageDelivery":"true","FilterPolicy":"{\"status\":[\"ORDER_CHECKOUT\"]}","FilterPolicyScope":"MessageBody"}'
awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$PAYMENT_REVERT_ARN"       --attributes '{"RawMessageDelivery":"true","FilterPolicy":"{\"status\":[\"OUT_OF_STOCK\",\"ORDER_NOT_DELIVERED\"]}","FilterPolicyScope":"MessageBody"}'
awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$INVENTORY_WRITE_OFF_ARN"  --attributes '{"RawMessageDelivery":"true","FilterPolicy":"{\"status\":[\"ORDER_PAID\"]}","FilterPolicyScope":"MessageBody"}'
awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$INVENTORY_PUT_BACK_ARN"   --attributes '{"RawMessageDelivery":"true","FilterPolicy":"{\"status\":[\"ORDER_NOT_DELIVERED\"]}","FilterPolicyScope":"MessageBody"}'
awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$SHIPPING_WRITE_OFF_ARN"   --attributes '{"RawMessageDelivery":"true","FilterPolicy":"{\"status\":[\"INVENTORY_WRITE_OFF\"]}","FilterPolicyScope":"MessageBody"}'

echo "LocalStack initialization complete."

exit 0
