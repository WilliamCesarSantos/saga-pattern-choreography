#!/usr/bin/env bash

set -euo pipefail

# This script runs in the postgres docker-entrypoint-initdb.d directory during container init
# It creates one database per service for demonstration: order, payment, shipping, notification, inventory

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
  CREATE DATABASE order_db;
  CREATE DATABASE payment_db;
  CREATE DATABASE shipping_db;
  CREATE DATABASE notification_db;
  CREATE DATABASE inventory_db;
EOSQL

echo "Databases created: order_db, payment_db, shipping_db, notification_db, inventory_db"
