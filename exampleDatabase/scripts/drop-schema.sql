-- Drop foreign key constraints in order table
ALTER TABLE "order"
    DROP CONSTRAINT "order_user_id_fkey";
ALTER TABLE "order"
    DROP CONSTRAINT "order_shipping_address_id_fkey";

-- Drop the tables in reverse order of creation
DROP TABLE IF EXISTS "order";
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS "user";
