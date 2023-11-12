import pg from "pg";
import dotenv from 'dotenv';

dotenv.config();

class DbClient {
    constructor(
        user = process.env.POSTGRES_USER,
        host = process.env.POSTGRES_HOST,
        database = process.env.POSTGRES_DB,
        password = process.env.POSTGRES_PASSWORD,
        port = process.env.POSTGRES_PORT
    ) {
        this.credentials = {
            user,
            host,
            database,
            password,
            port
        }
        this.client = new pg.Client(this.credentials)
    }

    async executeQuery(query) {
        try {
            await this.client.connect();
            const result = await this.client.query(query);
            await this.client.end();
            return JSON.stringify(result);
        } catch (error) {
            console.log(error);
            await this.client.end();
        }
    }
}

const dbSchema = `
CREATE TABLE  IF NOT EXISTS "user"
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100),
    age        INTEGER,
    sex        CHAR(1),
    email      VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS "address"
(
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER REFERENCES "user" (id),
    street      VARCHAR(255),
    city        VARCHAR(100),
    state       VARCHAR(50),
    postal_code VARCHAR(20)
);

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
`

export { DbClient, dbSchema }