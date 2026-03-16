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

# DDL: order_service
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "order_service" <<-EOSQL

  CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
  );

  CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL
  );

  CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    customer_id BIGINT REFERENCES customers(id)
  );

  CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL,
    price NUMERIC(10,2) NOT NULL
  );

EOSQL

echo "DDL order_service applied"

# DDL: payment_service
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "payment_service" <<-EOSQL

  CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT NOW()
  );

EOSQL

echo "DDL payment_service applied"

# DDL: shipping_service
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "shipping_service" <<-EOSQL

  CREATE TABLE shipping (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    tracking_number VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    received_by VARCHAR(255),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
  );

EOSQL

echo "DDL shipping_service applied"

# DDL: notification_service
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "notification_service" <<-EOSQL

  CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT NOW()
  );

EOSQL

echo "DDL notification_service applied"

# DDL: inventory_service
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "inventory_service" <<-EOSQL

  CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0
  );

EOSQL

echo "DDL inventory_service applied"

# DML: order_service
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "order_service" <<-EOSQL

  -- 10 customers
  INSERT INTO customers (name, email) VALUES
    ('João Silva',       'joao.silva@email.com'),
    ('Maria Souza',      'maria.souza@email.com'),
    ('Carlos Oliveira',  'carlos.oliveira@email.com'),
    ('Ana Lima',         'ana.lima@email.com'),
    ('Pedro Mendes',     'pedro.mendes@email.com'),
    ('Fernanda Costa',   'fernanda.costa@email.com'),
    ('Rafael Alves',     'rafael.alves@email.com'),
    ('Juliana Rocha',    'juliana.rocha@email.com'),
    ('Bruno Ferreira',   'bruno.ferreira@email.com'),
    ('Camila Pereira',   'camila.pereira@email.com');

  -- 50 products
  INSERT INTO products (description) VALUES
    ('Notebook Dell Inspiron'),
    ('Notebook Lenovo IdeaPad'),
    ('Notebook Asus VivoBook'),
    ('Notebook HP Pavilion'),
    ('Notebook Acer Aspire'),
    ('Mouse Logitech MX Master'),
    ('Mouse Razer DeathAdder'),
    ('Mouse Microsoft Arc'),
    ('Mouse Corsair Harpoon'),
    ('Mouse Multilaser MO300'),
    ('Teclado Mecânico Redragon'),
    ('Teclado Logitech K380'),
    ('Teclado Microsoft Sculpt'),
    ('Teclado Corsair K70'),
    ('Teclado Multilaser TC179'),
    ('Monitor LG 24 Full HD'),
    ('Monitor Samsung 27 QHD'),
    ('Monitor Dell 24 IPS'),
    ('Monitor AOC 21.5 Full HD'),
    ('Monitor Philips 27 4K'),
    ('Headset HyperX Cloud II'),
    ('Headset Razer Kraken'),
    ('Headset Logitech G435'),
    ('Headset JBL Quantum'),
    ('Headset Sony WH-1000XM5'),
    ('SSD Kingston 480GB'),
    ('SSD Samsung 1TB'),
    ('SSD WD Blue 500GB'),
    ('HD Seagate 1TB'),
    ('HD WD 2TB'),
    ('Memória RAM Corsair 8GB'),
    ('Memória RAM Kingston 16GB'),
    ('Memória RAM G.Skill 32GB'),
    ('Placa de Vídeo RTX 3060'),
    ('Placa de Vídeo RX 6600'),
    ('Processador Intel i5-12400'),
    ('Processador AMD Ryzen 5 5600'),
    ('Processador Intel i7-12700'),
    ('Placa-mae Asus B560M'),
    ('Placa-mae Gigabyte B450M'),
    ('Fonte Corsair 650W'),
    ('Fonte Seasonic 550W'),
    ('Gabinete NZXT H510'),
    ('Gabinete Cooler Master Q300L'),
    ('Webcam Logitech C920'),
    ('Webcam Microsoft LifeCam'),
    ('Roteador TP-Link Archer'),
    ('Switch TP-Link 8 portas'),
    ('Cabo HDMI 2m Multilaser'),
    ('Hub USB-C 7 em 1');

  -- 20 orders (customer_id 1..10)
  INSERT INTO orders (customer_id, status, created_at) VALUES
    (1,  'CREATED', NOW() - INTERVAL '10 days'),
    (2,  'CREATED', NOW() - INTERVAL '9 days'),
    (3,  'CREATED', NOW() - INTERVAL '8 days'),
    (4,  'CREATED', NOW() - INTERVAL '7 days'),
    (5,  'CREATED', NOW() - INTERVAL '6 days'),
    (6,  'CREATED', NOW() - INTERVAL '5 days'),
    (7,  'CREATED', NOW() - INTERVAL '4 days'),
    (8,  'CREATED', NOW() - INTERVAL '3 days'),
    (9,  'CREATED', NOW() - INTERVAL '2 days'),
    (10, 'CREATED', NOW() - INTERVAL '1 day'),
    (1,  'CREATED', NOW() - INTERVAL '11 days'),
    (2,  'CREATED', NOW() - INTERVAL '12 days'),
    (3,  'CREATED', NOW() - INTERVAL '13 days'),
    (4,  'CREATED', NOW() - INTERVAL '14 days'),
    (5,  'CREATED', NOW() - INTERVAL '15 days'),
    (6,  'CREATED', NOW() - INTERVAL '16 days'),
    (7,  'CREATED', NOW() - INTERVAL '17 days'),
    (8,  'CREATED', NOW() - INTERVAL '18 days'),
    (9,  'CREATED', NOW() - INTERVAL '19 days'),
    (10, 'CREATED', NOW() - INTERVAL '20 days');

  -- order_items (1 a 5 itens por pedido; order_id e product_id são numéricos)
  INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
    -- order 1 (3 itens)
    (1,  1,  1, 3500.00),
    (1,  6,  2,  350.00),
    (1,  11, 1,  450.00),
    -- order 2 (1 item)
    (2,  17, 1, 1800.00),
    -- order 3 (5 itens)
    (3,  26, 2,  350.00),
    (3,  31, 2,  200.00),
    (3,  41, 1,  450.00),
    (3,  49, 3,   40.00),
    (3,  50, 1,  180.00),
    -- order 4 (2 itens)
    (4,  34, 1, 2800.00),
    (4,  36, 1, 1200.00),
    -- order 5 (4 itens)
    (5,  2,  1, 3200.00),
    (5,  7,  1,  280.00),
    (5,  12, 1,  300.00),
    (5,  45, 1,  600.00),
    -- order 6 (2 itens)
    (6,  21, 1,  500.00),
    (6,  22, 1,  400.00),
    -- order 7 (1 item)
    (7,  25, 1, 1800.00),
    -- order 8 (3 itens)
    (8,  3,  1, 2900.00),
    (8,  27, 1,  700.00),
    (8,  32, 2,  380.00),
    -- order 9 (5 itens)
    (9,  4,  1, 3100.00),
    (9,  16, 1, 1200.00),
    (9,  39, 1,  800.00),
    (9,  43, 1,  600.00),
    (9,  47, 1,  300.00),
    -- order 10 (2 itens)
    (10, 5,  1, 2800.00),
    (10, 9,  1,  150.00),
    -- order 11 (3 itens)
    (11, 35, 1, 2200.00),
    (11, 37, 1, 1100.00),
    (11, 40, 1,  650.00),
    -- order 12 (1 item)
    (12, 20, 1, 2500.00),
    -- order 13 (4 itens)
    (13, 8,  2,  200.00),
    (13, 13, 1,  380.00),
    (13, 23, 1,  350.00),
    (13, 46, 1,  350.00),
    -- order 14 (2 itens)
    (14, 29, 2,  280.00),
    (14, 48, 1,  200.00),
    -- order 15 (5 itens)
    (15, 10, 3,   80.00),
    (15, 15, 2,  120.00),
    (15, 28, 1,  400.00),
    (15, 33, 1,  700.00),
    (15, 42, 1,  400.00),
    -- order 16 (1 item)
    (16, 38, 1, 2100.00),
    -- order 17 (3 itens)
    (17, 18, 1, 1400.00),
    (17, 24, 1,  280.00),
    (17, 44, 1,  350.00),
    -- order 18 (4 itens)
    (18, 19, 1,  900.00),
    (18, 30, 1,  380.00),
    (18, 14, 1,  650.00),
    (18, 49, 5,   40.00),
    -- order 19 (2 itens)
    (19, 6,  1,  350.00),
    (19, 11, 1,  450.00),
    -- order 20 (3 itens)
    (20, 1,  1, 3500.00),
    (20, 26, 1,  350.00),
    (20, 50, 2,  180.00);

EOSQL

echo "DML order_service applied"

# DML: inventory_service
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "inventory_service" <<-EOSQL

  -- 1 registro por produto (50 produtos)
  INSERT INTO inventory (product_id, quantity, version) VALUES
    (1,  50, 0),
    (2,  45, 0),
    (3,  40, 0),
    (4,  38, 0),
    (5,  42, 0),
    (6, 100, 0),
    (7,  80, 0),
    (8,  90, 0),
    (9,  95, 0),
    (10, 110, 0),
    (11,  60, 0),
    (12,  75, 0),
    (13,  70, 0),
    (14,  55, 0),
    (15,  85, 0),
    (16,  30, 0),
    (17,  25, 0),
    (18,  35, 0),
    (19,  40, 0),
    (20,  20, 0),
    (21,  65, 0),
    (22,  70, 0),
    (23,  80, 0),
    (24,  90, 0),
    (25,  15, 0),
    (26, 120, 0),
    (27,  50, 0),
    (28,  60, 0),
    (29,  75, 0),
    (30,  55, 0),
    (31, 100, 0),
    (32,  80, 0),
    (33,  40, 0),
    (34,  20, 0),
    (35,  18, 0),
    (36,  35, 0),
    (37,  30, 0),
    (38,  22, 0),
    (39,  45, 0),
    (40,  50, 0),
    (41,  60, 0),
    (42,  55, 0),
    (43,  38, 0),
    (44,  42, 0),
    (45,  70, 0),
    (46,  65, 0),
    (47,  90, 0),
    (48,  85, 0),
    (49, 200, 0),
    (50, 150, 0);
EOSQL
echo "DML inventory_service applied"
