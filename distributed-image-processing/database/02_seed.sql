-- =============================================================================
-- DATOS INICIALES
-- Contrasena: "admin123" (BCrypt hash)
-- =============================================================================
\c imageprocessing_db;

INSERT INTO usuario (nombre, email, password_hash, rol) VALUES
    ('Administrador', 'admin@imageprocessing.com',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J9Xkr5c/.',
     'ADMIN'),
    ('Usuario Demo', 'demo@imageprocessing.com',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J9Xkr5c/.',
     'USER');

INSERT INTO nodo_trabajador (nombre, direccion_red, estado, carga_actual) VALUES
    ('nodo-01', 'localhost:9090', 'INACTIVO', 0),
    ('nodo-02', 'localhost:9091', 'INACTIVO', 0);
