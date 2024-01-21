-- Insert sample data into "user" table
INSERT INTO "user" (name, age, sex, email)
VALUES ('John Doe', 25, 'male', 'john.doe@example.com'),
       ('Jane Smith', 30, 'female', 'jane.smith@example.com'),
       ('Jane Doe', 28, 'female', 'jane.doe@example.com'),
       ('Bob Smith', 35, 'male', 'bob.smith@example.com'),
       ('Emily Johnson', 40, 'female', 'emily.johnson@example.com'),
       ('Michael Davis', 22, 'male', 'michael.davis@example.com'),
       ('Sarah Brown', 29, 'female', 'sarah.brown@example.com'),
       ('James Wilson', 33, 'male', 'james.wilson@example.com'),
       ('Jessica Lee', 26, 'female', 'jessica.lee@example.com'),
       ('David Taylor', 45, 'male', 'david.taylor@example.com'),
       ('Amanda Martinez', 31, 'female', 'amanda.martinez@example.com'),
       ('Daniel Miller', 27, 'male', 'daniel.miller@example.com'),
       ('Olivia Garcia', 38, 'female', 'olivia.garcia@example.com'),
       ('Matthew Hernandez', 23, 'male', 'matthew.hernandez@example.com'),
       ('Sophia Lopez', 32, 'female', 'sophia.lopez@example.com'),
       ('Andrew Young', 36, 'male', 'andrew.young@example.com'),
       ('Emma Scott', 30, 'female', 'emma.scott@example.com'),
       ('William Davis', 41, 'male', 'william.davis@example.com'),
       ('Ella Thomas', 24, 'female', 'ella.thomas@example.com'),
       ('Christopher Johnson', 37, 'male', 'christopher.johnson@example.com'),
       ('Grace Miller', 34, 'female', 'grace.miller@example.com'),
       ('Nicholas Brown', 39, 'male', 'nicholas.brown@example.com');


-- Insert sample data into "address" table
INSERT INTO "address" (user_id, street, city, state, postal_code)
VALUES (1, '123 Main St', 'Anytown', 'CA', '12345'),
       (2, '456 Oak Ave', 'Sometown', 'NY', '54321'),
       (3, '789 Maple Dr', 'Othertown', 'TX', '67890'),
       (4, '101 Pine Ln', 'Smallville', 'FL', '45678'),
       (5, '202 Cedar Ct', 'Villagetown', 'WA', '23456'),
       (6, '303 Redwood Rd', 'Hometown', 'IL', '78901'),
       (7, '404 Birch Blvd', 'Townsville', 'OH', '34567'),
       (8, '505 Elm Way', 'Cityville', 'PA', '89012'),
       (9, '606 Spruce Ave', 'Suburbia', 'MI', '01234'),
       (10, '707 Fir Place', 'Ruraltown', 'GA', '56789'),
       (11, '808 Cedar Rd', 'Countyville', 'IN', '12345'),
       (12, '909 Oak Ln', 'Hamletown', 'NJ', '67890'),
       (13, '1010 Pine Dr', 'Villageland', 'NV', '34567'),
       (14, '1111 Maple Ct', 'Outskirtsville', 'AZ', '78901'),
       (15, '1212 Redwood Blvd', 'Ruralville', 'NC', '23456'),
       (16, '1313 Birch Way', 'Farmland', 'MO', '89012'),
       (17, '1414 Elm Place', 'Mountaintown', 'TN', '01234'),
       (18, '1515 Spruce Rd', 'Countryside', 'KY', '56789'),
       (19, '1616 Fir Ave', 'Wildwesttown', 'OR', '12345'),
       (20, '1717 Cedar Dr', 'Frontiertown', 'SC', '67890');


-- Insert sample data into "order" table
INSERT INTO "order" (user_id, order_date, total_amount, payment_method, shipping_address_id, is_shipped,
                     tracking_number, status, notes)
VALUES (1, '2023-11-05', 100.00, 'Credit Card', 1, true, '1234567890', 'Shipped', 'Sample order notes 1'),
       (2, '2023-11-06', 150.00, 'PayPal', 2, false, NULL, 'Pending', 'Sample order notes 2'),
       (3, '2023-11-07', 200.00, 'Credit Card', 3, true, '0987654321', 'Shipped', 'Sample order notes 3'),
       (4, '2023-11-08', 75.50, 'PayPal', 4, false, NULL, 'Pending', 'Sample order notes 4'),
       (5, '2023-11-09', 120.00, 'Credit Card', 5, true, '1357924680', 'Shipped', 'Sample order notes 5'),
       (6, '2023-11-10', 90.25, 'PayPal', 6, false, NULL, 'Pending', 'Sample order notes 6'),
       (7, '2023-11-11', 300.75, 'Credit Card', 7, true, '2468013579', 'Shipped', 'Sample order notes 7'),
       (8, '2023-11-12', 50.00, 'PayPal', 8, false, NULL, 'Pending', 'Sample order notes 8'),
       (9, '2023-11-13', 175.30, 'Credit Card', 9, true, '9876543210', 'Shipped', 'Sample order notes 9'),
       (10, '2023-11-14', 95.20, 'PayPal', 10, false, NULL, 'Pending', 'Sample order notes 10'),
       (11, '2023-11-15', 180.50, 'Credit Card', 11, true, '0123456789', 'Shipped', 'Sample order notes 11'),
       (12, '2023-11-16', 65.75, 'PayPal', 12, false, NULL, 'Pending', 'Sample order notes 12'),
       (13, '2023-11-17', 220.00, 'Credit Card', 13, true, '5432109876', 'Shipped', 'Sample order notes 13'),
       (14, '2023-11-18', 75.10, 'PayPal', 14, false, NULL, 'Pending', 'Sample order notes 14'),
       (15, '2023-11-19', 160.25, 'Credit Card', 15, true, '6789012345', 'Shipped', 'Sample order notes 15'),
       (16, '2023-11-20', 110.00, 'PayPal', 16, false, NULL, 'Pending', 'Sample order notes 16'),
       (17, '2023-11-21', 190.75, 'Credit Card', 17, true, '3210987654', 'Shipped', 'Sample order notes 17'),
       (18, '2023-11-22', 80.20, 'PayPal', 18, false, NULL, 'Pending', 'Sample order notes 18'),
       (19, '2023-11-23', 250.50, 'Credit Card', 19, true, '7654321098', 'Shipped', 'Sample order notes 19'),
       (20, '2023-11-24', 70.30, 'PayPal', 20, false, NULL, 'Pending', 'Sample order notes 20');

-- Inserting sample data into specialisation table
INSERT INTO cvut.specialisation (name, garant)
VALUES ('Computer Science', 'Dr. Smith'),
       ('Electrical Engineering', 'Prof. Johnson'),
       ('Mechanical Engineering', 'Dr. Williams');

-- Inserting sample data into student table
INSERT INTO cvut.student (name, birthdate, grade, specialization_id)
VALUES ('Alice', '1998-05-15', 1, 1),
       ('Bob', '1999-09-22', 2, 2),
       ('Charlie', '2000-02-10', 3, 3),
       ('David', '1997-11-28', 3, 1),
       ('Emma', '1999-07-09', 4, 2),
       ('Frank', '2001-04-18', 5, 3),
       ('Grace', '2000-12-05', 3, 1),
       ('Henry', '1998-08-14', 1, 2);
