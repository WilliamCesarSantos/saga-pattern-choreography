CREATE TABLE IF NOT EXISTS payments (
  id SERIAL PRIMARY KEY,
  order_id VARCHAR,
  status VARCHAR,
  amount NUMERIC,
  processed_at TIMESTAMP
);

