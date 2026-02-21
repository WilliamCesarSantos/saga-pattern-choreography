CREATE TABLE IF NOT EXISTS notifications (
  id SERIAL PRIMARY KEY,
  order_id VARCHAR,
  type VARCHAR,
  payload JSONB,
  created_at TIMESTAMP
);

