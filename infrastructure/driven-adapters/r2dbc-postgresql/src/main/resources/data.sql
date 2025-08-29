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
INSERT INTO users (first_name, last_name, birth_date, address, phone, email, password, document_id, role_id, base_salary, created_at, updated_at) 
VALUES 
    (
        'Admin',
        'Sistema',
        '1980-01-01',
        'Oficina Principal CrediYa',
        '+573001234567',
        'admin@crediya.com',
        'admin123456',
        '12345678',
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
        '87654321',
        3,
        8000000,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'Juan Carlos',
        'Pérez González',
        '1990-03-15',
        'Calle 123 #45-67, Bogotá',
        '+573101234567',
        'juan.perez@email.com',
        'cliente123',
        '1234567890',
        1,
        5000000,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'María Elena',
        'Rodríguez López',
        '1988-07-22',
        'Carrera 45 #12-34, Medellín',
        '+573209876543',
        'maria.rodriguez@email.com',
        'cliente456',
        '0987654321',
        1,
        7500000,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (email) DO UPDATE SET
    password = EXCLUDED.password,
    birth_date = EXCLUDED.birth_date,
    address = EXCLUDED.address,
    phone = EXCLUDED.phone,
    document_id = EXCLUDED.document_id,
    updated_at = CURRENT_TIMESTAMP;