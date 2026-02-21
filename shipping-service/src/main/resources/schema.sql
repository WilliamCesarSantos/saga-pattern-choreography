CREATE TABLE IF NOT EXISTS shipments (
  id SERIAL PRIMARY KEY,
  order_id VARCHAR,
  status VARCHAR,
  tracking_number VARCHAR,
  shipped_at TIMESTAMP,
  delivered_at TIMESTAMP
);

