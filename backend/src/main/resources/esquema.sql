-- Esquema de la base de datos SQLite de Admitienda.
-- Los montos se guardan en centavos (enteros) y las fechas en
-- milisegundos epoch, para evitar errores de precisión con SQLite.

CREATE TABLE IF NOT EXISTS usuarios (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario    TEXT    NOT NULL,
    nombre_normalizado TEXT   NOT NULL UNIQUE,
    rol               TEXT    NOT NULL CHECK (rol IN ('INVITADO', 'REGISTRADO', 'ADMIN')),
    indicio           TEXT    NOT NULL DEFAULT '',
    sal_contrasena    TEXT    NOT NULL,
    hash_contrasena   TEXT    NOT NULL,
    fecha_registro    INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS solicitudes_admin (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id      INTEGER NOT NULL,
    estado          TEXT    NOT NULL CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
    motivo          TEXT    NOT NULL DEFAULT '',
    fecha_solicitud INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_solicitudes_usuario ON solicitudes_admin (usuario_id);

CREATE TABLE IF NOT EXISTS productos (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_producto    TEXT    NOT NULL,
    nombre_producto  TEXT    NOT NULL,
    precio_neto_cent INTEGER NOT NULL,
    ganancia_cent    INTEGER NOT NULL,
    cantidad_stock   INTEGER NOT NULL,
    stock_minimo     INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_productos_nombre ON productos (nombre_producto);

CREATE TABLE IF NOT EXISTS movimientos (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_movimiento TEXT    NOT NULL CHECK (tipo_movimiento IN ('INGRESO', 'EGRESO')),
    monto_cent     INTEGER NOT NULL,
    descripcion    TEXT    NOT NULL,
    fecha          INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_movimientos_fecha ON movimientos (fecha);

CREATE TABLE IF NOT EXISTS deudores (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_deudor     TEXT    NOT NULL,
    nombre_normalizado TEXT   NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS deudas (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    deudor_id     INTEGER NOT NULL,
    cliente_nombre TEXT   NOT NULL,
    monto_cent    INTEGER NOT NULL,
    saldo_cent    INTEGER NOT NULL,
    descripcion   TEXT    NOT NULL,
    fecha         INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_deudas_deudor ON deudas (deudor_id);

CREATE TABLE IF NOT EXISTS pagos_deuda (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    deuda_id   INTEGER NOT NULL,
    monto_cent INTEGER NOT NULL,
    descripcion TEXT  NOT NULL,
    fecha      INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_pagos_deuda ON pagos_deuda (deuda_id);
