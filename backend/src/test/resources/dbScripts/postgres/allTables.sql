-- create table user
CREATE TABLE IF NOT EXISTS "user"
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100),
    age        INTEGER,
    sex        CHAR(10),
    email      VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

-- create table address
CREATE TABLE IF NOT EXISTS "address"
(
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER REFERENCES "user" (id),
    street      VARCHAR(255),
    city        VARCHAR(100),
    state       VARCHAR(50),
    postal_code VARCHAR(20)
);

-- create table order
CREATE TABLE IF NOT EXISTS "order"
(
    id                  SERIAL PRIMARY KEY,
    user_id             INTEGER REFERENCES "user" (id),
    order_date          DATE,
    total_amount        DECIMAL(10, 2),
    payment_method      VARCHAR(50),
    shipping_address_id INTEGER REFERENCES address (id),
    is_shipped          BOOLEAN,
    tracking_number     VARCHAR(50),
    status              VARCHAR(20),
    notes               TEXT
);

-- create schema cvut
CREATE SCHEMA IF NOT EXISTS "cvut";

-- create table specialization
CREATE TABLE IF NOT EXISTS cvut.specialisation
(
    id     SERIAL PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    manager VARCHAR(100) NOT NULL
);

-- create table student
CREATE TABLE IF NOT EXISTS cvut.student
(
    id                SERIAL PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    birthdate         DATE         NOT NULL,
    grade             INT,
    specialisation_id INT REFERENCES cvut.specialisation (id)
);

-- create table fit-wiki, foreign key constraint is defined differently in this definition.
CREATE TABLE IF NOT EXISTS cvut.fit_wiki
(
    identifier       SERIAL PRIMARY KEY,
    data             VARCHAR(300),
    author           INT,
    reviewer_of_data INTEGER REFERENCES cvut.student (id),
    CONSTRAINT name_of_author_reference FOREIGN KEY (author) REFERENCES public."user" (id)
);

-- create course table, the identifier contains "(" character
CREATE TABLE IF NOT EXISTS cvut.course
(
    "(identifier of course" SERIAL PRIMARY KEY,
    name                    VARCHAR(200)
);

-- create table exam, the primary key is composed from multiple columns
CREATE TABLE IF NOT EXISTS cvut.exam
(
    student INTEGER REFERENCES cvut.student (id),
    course  INTEGER REFERENCES cvut.course ("(identifier of course"),
    PRIMARY KEY (student, course)
);

