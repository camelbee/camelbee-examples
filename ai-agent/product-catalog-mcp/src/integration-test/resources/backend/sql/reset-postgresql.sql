SET search_path TO camelbee_user;

-- Truncate tables with cascading foreign key cleanup
BEGIN;

TRUNCATE TABLE camelbee_purchases_table_jpa CASCADE;
ALTER SEQUENCE camelbee_purchases_seq_jpa RESTART WITH 1;
ALTER SEQUENCE camelbee_items_seq_jpa RESTART WITH 1;


COMMIT;

DO $$
DECLARE
  i INT;
  purchase_id BIGINT;
BEGIN
  FOR i IN 1..12 LOOP
    INSERT INTO camelbee_purchases_table_jpa (id, saleschannel, status, purchasedate, lastupdatetimestamp)
    VALUES (nextval('camelbee_purchases_seq_jpa'), 'ONLINE', 'Pending', '2025-01-22', '2025-01-22 11:41:02')
    RETURNING id INTO purchase_id;

    INSERT INTO camelbee_purchaseitems_table_jpa (id, productid, productname, quantity, price, purchase_id)
    VALUES
      (nextval('camelbee_items_seq_jpa'), '1001', 'Product1001', 1, 10.2, purchase_id),
      (nextval('camelbee_items_seq_jpa'), '1002', 'Product1002', 2, 15.3, purchase_id),
      (nextval('camelbee_items_seq_jpa'), '1003', 'Product1003', 3, 20.4, purchase_id),
      (nextval('camelbee_items_seq_jpa'), '1004', 'Product1004', 4, 25.5, purchase_id),
      (nextval('camelbee_items_seq_jpa'), '1005', 'Product1005', 5, 30.6, purchase_id);
  END LOOP;
END
$$;

