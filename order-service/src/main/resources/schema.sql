CREATE TABLE IF NOT EXISTS orders (
  id VARCHAR PRIMARY KEY,
  customer_id VARCHAR,
  status VARCHAR,
  total NUMERIC,
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
  id VARCHAR PRIMARY KEY,
  order_id VARCHAR,
  product_id VARCHAR,
  quantity INT,
  price NUMERIC
);

