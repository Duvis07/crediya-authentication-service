-- CrediYa Authentication Service - Database Initialization Script
-- This script creates the necessary tables and initial data for the authentication service

-- Create extension for UUID generation if not exists
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert roles
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Administrator role with full access'),
('ASESOR', 'Credit advisor role'),
('APPLICANT', 'Loan applicant role')
ON CONFLICT (name) DO NOTHING;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    document_id VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    address VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    base_salary DECIMAL(15,2),
    role_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles(id);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_document_id ON users(document_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);

-- Insert users with real BCrypt hashes (from working data.sql)
INSERT INTO users (document_id, first_name, last_name, birth_date, address, phone, email, password, role_id, base_salary, created_at, updated_at)
VALUES 
    (
        '12345678',
        'Admin',
        'Sistema',
        '1980-01-01',
        'Oficina Principal CrediYa',
        '+573001234567',
        'admin@crediya.com',
        '$2a$12$D4xGrVZdrQgpPeJO9oUy2udUZX9yBuGRc.RzQZLZulO08MUhjejva',  -- admin123456
        (SELECT id FROM roles WHERE name = 'ADMIN'),
        10000000,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '87654321',
        'Asesor',
        'Principal',
        '1985-05-15',
        'Sucursal Norte CrediYa',
        '+573009876543',
        'asesor@crediya.com',
        '$2a$12$XAWli8/KRO1lxzuPfu67SODR9bCv5pzgIMcrfL5uAABPrnty6gxme', -- asesor123456
        (SELECT id FROM roles WHERE name = 'ASESOR'),
        8000000,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '11223344',
        'Cliente',
        'Premium',
        '1990-03-15',
        'Calle 123 #45-67, Bogotá',
        '+573101234567',
        'cliente@crediya.com',
        '$2a$12$Y7VTC5OdHuSPPch8YBosq.xE9SGU3Wgd.X5mn61wlicw7Y1P6UR/y',  -- cliente123
        (SELECT id FROM roles WHERE name = 'APPLICANT'),
        5000000,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '99887766',
        'Juan',
        'Pérez',
        '1988-07-20',
        'Carrera 15 #32-45, Medellín',
        '+573205551234',
        'juan.perez@example.com',
        '$2a$12$i0C5kklV4ptLrTip.1xil.NVoG3Bv7wnuFwN6boHSEgnX0L5DdNw6',  -- admin123456
        (SELECT id FROM roles WHERE name = 'APPLICANT'),
        4500000,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (email) DO NOTHING;

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Display initial data
SELECT 'Database initialized successfully' as status;
SELECT u.document_id, u.first_name, u.last_name, u.email, r.name as role, u.created_at 
FROM users u
JOIN roles r ON u.role_id = r.id
ORDER BY u.created_at;
