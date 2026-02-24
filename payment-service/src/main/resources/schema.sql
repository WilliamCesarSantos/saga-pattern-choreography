CREATE TABLE IF NOT EXISTS payments (
  id             BIGSERIAL PRIMARY KEY,
  order_id       VARCHAR(255)  NOT NULL,
  status         VARCHAR(50)   NOT NULL,
  amount         NUMERIC(19,2) NOT NULL,
  transaction_id VARCHAR(255)  NOT NULL,
  processed_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

