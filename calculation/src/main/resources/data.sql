CREATE TABLE user_account (
    account_number VARCHAR(20) PRIMARY KEY,
    balance DECIMAL(15, 2) NOT NULL
);

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_account_number VARCHAR(20) NOT NULL,
    target_account_number VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL
);