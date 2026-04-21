-- =============================================================================
-- SISTEMA DISTRIBUIDO DE PROCESAMIENTO DE IMAGENES
-- Script DDL - PostgreSQL
-- =============================================================================

CREATE DATABASE imageprocessing_db;
\c imageprocessing_db;

-- EXTENSION para UUID (opcional)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ─── TABLA: usuario ───────────────────────────────────────────────────────────
CREATE TABLE usuario (
    id_usuario      BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(100)    NOT NULL,
    email           VARCHAR(120)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    rol             VARCHAR(50)     NOT NULL DEFAULT 'USER'
);

-- ─── TABLA: sesion ────────────────────────────────────────────────────────────
CREATE TABLE sesion (
    id_sesion       BIGSERIAL PRIMARY KEY,
    id_usuario      BIGINT          NOT NULL REFERENCES usuario(id_usuario),
    token           VARCHAR(255)    NOT NULL,
    fecha_creacion  TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_expiracion TIMESTAMP      NOT NULL,
    estado          VARCHAR(30)     NOT NULL DEFAULT 'ACTIVO'
);

-- ─── TABLA: nodo_trabajador ───────────────────────────────────────────────────
CREATE TABLE nodo_trabajador (
    id_nodo             BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(100)    NOT NULL,
    direccion_red       VARCHAR(155)    NOT NULL,
    estado              VARCHAR(30)     NOT NULL DEFAULT 'INACTIVO',
    carga_actual        INTEGER         NOT NULL DEFAULT 0,
    ultima_conexion     TIMESTAMP
);

-- ─── TABLA: solicitud_lote ────────────────────────────────────────────────────
CREATE TABLE solicitud_lote (
    id_lote             BIGSERIAL PRIMARY KEY,
    usuario_id          BIGINT          NOT NULL REFERENCES usuario(id_usuario),
    fecha_recepcion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    estado              VARCHAR(30)     NOT NULL DEFAULT 'PENDIENTE',
    porcentaje_progreso DECIMAL(5,2)    NOT NULL DEFAULT 0.00
);

-- ─── TABLA: imagen_solicitud ──────────────────────────────────────────────────
CREATE TABLE imagen_solicitud (
    id_imagen           BIGSERIAL PRIMARY KEY,
    id_lote             BIGINT          NOT NULL REFERENCES solicitud_lote(id_lote),
    id_nodo             BIGINT          REFERENCES nodo_trabajador(id_nodo),
    nombre_archivo      VARCHAR(255)    NOT NULL,
    ruta_original       VARCHAR(255)    NOT NULL,
    ruta_resultado      VARCHAR(255),
    estado              VARCHAR(30)     NOT NULL DEFAULT 'PENDIENTE',
    fecha_recepcion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_procesamiento TIMESTAMP
);

-- ─── TABLA: transformacion ────────────────────────────────────────────────────
CREATE TABLE transformacion (
    id_transformacion   BIGSERIAL PRIMARY KEY,
    id_imagen           BIGINT          NOT NULL REFERENCES imagen_solicitud(id_imagen),
    tipo                VARCHAR(50)     NOT NULL,   -- ESCALA_GRISES, ROTAR, etc.
    orden               INTEGER         NOT NULL,
    parametros          TEXT                        -- JSON con config especifica
);

-- ─── TABLA: log_trabajo ───────────────────────────────────────────────────────
CREATE TABLE log_trabajo (
    id_log      BIGSERIAL PRIMARY KEY,
    id_imagen   BIGINT          NOT NULL REFERENCES imagen_solicitud(id_imagen),
    nivel       VARCHAR(20)     NOT NULL,   -- INFO, WARN, ERROR, DEBUG
    mensaje     TEXT,
    fecha_hora  TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ─── INDICES ──────────────────────────────────────────────────────────────────
CREATE INDEX idx_imagen_lote     ON imagen_solicitud(id_lote);
CREATE INDEX idx_imagen_estado   ON imagen_solicitud(estado);
CREATE INDEX idx_imagen_nodo     ON imagen_solicitud(id_nodo);
CREATE INDEX idx_lote_usuario    ON solicitud_lote(usuario_id);
CREATE INDEX idx_lote_estado     ON solicitud_lote(estado);
CREATE INDEX idx_log_imagen      ON log_trabajo(id_imagen);
CREATE INDEX idx_trans_imagen    ON transformacion(id_imagen);
CREATE INDEX idx_nodo_estado     ON nodo_trabajador(estado);
