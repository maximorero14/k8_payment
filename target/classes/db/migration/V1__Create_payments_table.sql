CREATE TABLE payments (
    id VARCHAR(255) PRIMARY KEY,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    method VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    is_fraud BOOLEAN NOT NULL,
    status VARCHAR(50) NOT NULL
);