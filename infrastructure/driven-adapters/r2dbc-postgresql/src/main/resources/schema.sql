-- Schema initialization for R2DBC
-- This file is automatically executed by Spring Boot on startup

-- Create role table
CREATE TABLE IF NOT EXISTS role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Create users table with FK
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    address VARCHAR(200),
    phone VARCHAR(15),
    email VARCHAR(255) NOT NULL UNIQUE,
    base_salary DECIMAL(15,2) NOT NULL,
    role_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_users_role 
        FOREIGN KEY (role_id) 
        REFERENCES role(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
