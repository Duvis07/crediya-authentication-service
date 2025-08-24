-- Inserta roles iniciales en la tabla 'role'
INSERT INTO role (id, name, description) VALUES
    (1, 'Solicitante', 'Usuario que puede solicitar préstamos personales'),
    (2, 'Administrador', 'Usuario con permisos administrativos completos del sistema')
ON CONFLICT (id) DO NOTHING;
