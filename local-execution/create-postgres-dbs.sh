#!/usr/bin/env bash

set -euo pipefail

# This script runs in the postgres docker-entrypoint-initdb.d directory during container init
# It creates one database per service for demonstration: order, payment, shipping, notification, inventory

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
  CREATE DATABASE order_service;
  CREATE DATABASE payment_service;
  CREATE DATABASE shipping_service;
  CREATE DATABASE notification_service;
  CREATE DATABASE inventory_service;
EOSQL

echo "Databases created: order_service, payment_service, shipping_service, notification_service, inventory_service"
# TODO include ddl for order_service
# TODO include ddl for payment_service
# TODO include ddl for shipping_service
# TODO include ddl for notification_service
# TODO include ddl for inventory_service
# TODO include dml for order_service. Include samples customer, product and order with items