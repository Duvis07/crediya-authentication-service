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
INSERT INTO users (document_id,first_name, last_name, birth_date, address, phone, email, password, role_id, base_salary, created_at, updated_at)
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
        2,
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
        3,
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
        '$2a$12$Y7VTC5OdHuSPPch8YBosq.xE9SGU3Wgd.X5mn61wlicw7Y1P6UR/y',  --cliente123
        1,
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
        '$2a$12$i0C5kklV4ptLrTip.1xil.NVoG3Bv7wnuFwN6boHSEgnX0L5DdNw6',  --admin123456
        1,
        4500000,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (email) DO UPDATE SET
    birth_date = EXCLUDED.birth_date,
    address = EXCLUDED.address,
    phone = EXCLUDED.phone,
    document_id = EXCLUDED.document_id,
    updated_at = CURRENT_TIMESTAMP;