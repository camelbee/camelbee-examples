
CREATE SCHEMA IF NOT EXISTS camelbee_user;

-- Set search path to use camelbee_user schema
SET search_path TO camelbee_user;



CREATE SEQUENCE camelbee_purchases_seq_jpa START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE camelbee_items_seq_jpa START WITH 1 INCREMENT BY 1;




CREATE TABLE camelbee_purchases_table_jpa (
  id BIGINT DEFAULT nextval('camelbee_purchases_seq_jpa') PRIMARY KEY,
  saleschannel VARCHAR(255) NOT NULL,
  status VARCHAR(20),
  purchasedate DATE,
  lastupdatetimestamp TIMESTAMP,
  CONSTRAINT chk_status_jpa CHECK (UPPER(status) IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELED', 'RETURNED', 'FAILED', 'ON HOLD'))
);

CREATE TABLE camelbee_purchaseitems_table_jpa (
  id BIGINT DEFAULT nextval('camelbee_items_seq_jpa') PRIMARY KEY,
  productid VARCHAR(50),
  productname VARCHAR(50),
  quantity INTEGER,
  price NUMERIC(10,2),
  purchase_id BIGINT,
  CONSTRAINT fk_purchase_jpa FOREIGN KEY (purchase_id) REFERENCES camelbee_purchases_table_jpa(id)
);

