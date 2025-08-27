-- Data initialization for R2DBC
-- This file is automatically executed by Spring Boot after schema.sql

-- Insert default roles
INSERT INTO roles (id, name, description) VALUES
    (1, 'APPLICANT', 'Usuario que puede solicitar préstamos personales'),
    (2, 'ADMIN', 'Usuario con permisos administrativos completos del sistema'),
    (3, 'ASESOR', 'Asesor de crédito con permisos de gestión')
ON CONFLICT (id) DO NOTHING;

-- Insert default ADMIN user for system bootstrap
-- Insertar usuarios iniciales con contraseñas en texto plano (TEMPORAL PARA PRUEBAS)
INSERT INTO users (first_name, last_name, birth_date, address, phone, email, password, role_id, base_salary, created_at, updated_at) 
VALUES 
    (
        'Admin',
        'Sistema',
        '1980-01-01',
        'Oficina Principal CrediYa',
        '+573001234567',
        'admin@crediya.com',
        'admin123456',
        2,
        15000000,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'Asesor',
        'Principal',
        '1985-05-15',
        'Sucursal Norte CrediYa',
        '+573009876543',
        'asesor@crediya.com',
        'asesor123456',
        3,
        8000000,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (email) DO UPDATE SET
    password = EXCLUDED.password,
    birth_date = EXCLUDED.birth_date,
    address = EXCLUDED.address,
    phone = EXCLUDED.phone,
    updated_at = CURRENT_TIMESTAMP;