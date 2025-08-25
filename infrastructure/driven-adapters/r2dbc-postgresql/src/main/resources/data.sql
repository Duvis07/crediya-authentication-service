-- Data initialization for R2DBC
-- This file is automatically executed by Spring Boot after schema.sql

-- Insert default roles
INSERT INTO role (id, name, description) VALUES
    (1, 'Solicitante', 'Usuario que puede solicitar préstamos personales'),
    (2, 'Administrador', 'Usuario con permisos administrativos completos del sistema')
ON CONFLICT (id) DO NOTHING;
