-- Drop foreign key constraints in order table
ALTER TABLE "order"
    DROP CONSTRAINT "order_user_id_fkey";
ALTER TABLE "order"
    DROP CONSTRAINT "order_shipping_address_id_fkey";
ALTER TABLE cvut.student
    DROP CONSTRAINT "student_specialization_id_fkey";
ALTER TABLE cvut.fit_wiki
    DROP CONSTRAINT "fit_wiki_reviewer_of_data_fkey";
ALTER TABLE cvut.fit_wiki
    DROP CONSTRAINT "name_of_author_reference";

-- Drop the tables in reverse order of creation
DROP TABLE IF EXISTS "order";
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS "user";
DROP TABLE  IF EXISTS cvut.student;
DROP TABLE  IF EXISTS cvut.specialisation;
DROP TABLE  IF EXISTS cvut.fit_wiki;
DROP TABLE  IF EXISTS cvut.exam;

-- Drop schema
DROP TABLE IF EXISTS cvut.student;
DROP TABLE IF EXISTS cvut.specialization;

DROP SCHEMA IF EXISTS cvut CASCADE;

