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
CREATE TABLE IF NOT EXISTS address
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT,
    street      VARCHAR(255),
    city        VARCHAR(100),
    state       VARCHAR(50),
    postal_code VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES `user` (id)
);

-- command separator
CREATE TABLE IF NOT EXISTS `order`
(
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    user_id             INT,
    order_date          DATE,
    total_amount        DECIMAL(10, 2),
    payment_method      VARCHAR(50),
    shipping_address_id INT,
    is_shipped          BOOLEAN,
    tracking_number     VARCHAR(50),
    status              VARCHAR(20),
    notes               TEXT,
    FOREIGN KEY (user_id) REFERENCES `user` (id),
    FOREIGN KEY (shipping_address_id) REFERENCES address (id)
);

-- command separator
CREATE TABLE IF NOT EXISTS specialisation
(
    id      INT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    manager VARCHAR(100) NOT NULL
);

-- command separator
CREATE TABLE IF NOT EXISTS student
(
    id                INT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    birthdate         DATE         NOT NULL,
    grade             INT,
    specialisation_id INT,
    FOREIGN KEY (specialisation_id) REFERENCES specialisation (id)
);

-- command separator
CREATE TABLE IF NOT EXISTS fit_wiki
(
    identifier       INT AUTO_INCREMENT PRIMARY KEY,
    data             VARCHAR(300),
    author           INT,
    reviewer_of_data INT,
    FOREIGN KEY (author) REFERENCES user (id),
    FOREIGN KEY (reviewer_of_data) REFERENCES student (id)
);

-- command separator
CREATE TABLE IF NOT EXISTS course
(
    `(identifier of course` INT AUTO_INCREMENT PRIMARY KEY,
    name                   VARCHAR(200)
);

-- command separator
CREATE TABLE IF NOT EXISTS exam
(
    student INT,
    course  INT,
    PRIMARY KEY (student, course),
    FOREIGN KEY (student) REFERENCES student (id),
    FOREIGN KEY (course) REFERENCES course (`(identifier of course`)
);