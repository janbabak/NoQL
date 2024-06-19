CREATE TABLE IF NOT EXISTS `user`
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100),
    age        INT,
    sex        CHAR(10),
    email      VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- command separator

INSERT INTO `user` (name, age, sex, email)
VALUES ('John Doe', 25, 'M', 'john.doe@example.com'),
       ('Jane Smith', 30, 'F', 'jane.smith@example.com'),
       ('Jane Doe', 28, 'F', 'jane.doe@example.com'),
       ('Bob Smith', 35, 'M', 'bob.smith@example.com'),
       ('Emily Johnson', 40, 'F', 'emily.johnson@example.com'),
       ('Michael Davis', 22, 'M', 'michael.davis@example.com'),
       ('Sarah Brown', 29, 'F', 'sarah.brown@example.com'),
       ('James Wilson', 33, 'M', 'james.wilson@example.com'),
       ('Jessica Lee', 26, 'F', 'jessica.lee@example.com'),
       ('David Taylor', 45, 'M', 'david.taylor@example.com'),
       ('Amanda Martinez', 31, 'F', 'amanda.martinez@example.com'),
       ('Daniel Miller', 27, 'M', 'daniel.miller@example.com'),
       ('Olivia Garcia', 38, 'F', 'olivia.garcia@example.com'),
       ('Matthew Hernandez', 23, 'M', 'matthew.hernandez@example.com'),
       ('Sophia Lopez', 32, 'F', 'sophia.lopez@example.com'),
       ('Andrew Young', 36, 'M', 'andrew.young@example.com'),
       ('Emma Scott', 30, 'F', 'emma.scott@example.com'),
       ('William Davis', 41, 'M', 'william.davis@example.com'),
       ('Ella Thomas', 24, 'F', 'ella.thomas@example.com'),
       ('Christopher Johnson', 37, 'M', 'christopher.johnson@example.com'),
       ('Grace Miller', 34, 'F', 'grace.miller@example.com'),
       ('Nicholas Brown', 39, 'M', 'nicholas.brown@example.com');