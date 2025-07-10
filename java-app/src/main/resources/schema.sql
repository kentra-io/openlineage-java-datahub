CREATE TABLE IF NOT EXISTS product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    price INTEGER,
    creation_date TIMESTAMP
);
