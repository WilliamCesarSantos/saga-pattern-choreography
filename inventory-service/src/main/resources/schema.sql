CREATE TABLE IF NOT EXISTS inventory_reservations (
  id SERIAL PRIMARY KEY,
  order_id VARCHAR,
  product_id VARCHAR,
  quantity INT,
  status VARCHAR,
  reserved_at TIMESTAMP
);

