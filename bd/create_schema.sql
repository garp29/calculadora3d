-- =============================================================================
-- PROYECTO INTEGRADO: LA CALCULADORA — JAVA, PYTHON Y SQLITE (ESEIT)
-- Archivo: bd/create_schema.sql
-- Descripción: Esquema DDL oficial en Tercera Forma Normal (3FN) para las
--              4 tablas del contrato técnico (operacion, calculo, serie, punto)
--              con integridad declarativa estricta (PRAGMA foreign_keys = ON).
-- =============================================================================

PRAGMA foreign_keys = ON;

-- Limpieza idempotente en orden inverso de dependencias
DROP VIEW IF EXISTS v_estadisticas_operacion;
DROP VIEW IF EXISTS v_resumen_series;
DROP VIEW IF EXISTS v_historial_calculos;
DROP VIEW IF EXISTS v_historial_detallado;

DROP TABLE IF EXISTS punto_serie;
DROP TABLE IF EXISTS serie_trig;
DROP TABLE IF EXISTS historial_calculo;
DROP TABLE IF EXISTS punto;
DROP TABLE IF EXISTS serie;
DROP TABLE IF EXISTS calculo;
DROP TABLE IF EXISTS operacion;

-- =============================================================================
-- 1. TABLA: operacion (Catálogo Maestro de Operaciones - R1, R2, R9)
-- Almacena las operaciones básicas (Java) y trigonométricas (Python).
-- =============================================================================
CREATE TABLE operacion (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    simbolo  TEXT    NOT NULL UNIQUE
                     CHECK (length(trim(simbolo)) > 0),
    nombre   TEXT    NOT NULL UNIQUE
                     CHECK (length(trim(nombre)) > 0),
    tipo     TEXT    NOT NULL
                     CHECK (tipo IN ('BASICA', 'TRIGONOMETRICA')),
    lenguaje TEXT    NOT NULL
                     CHECK (lenguaje IN ('JAVA', 'PYTHON')),
    aridad   INTEGER NOT NULL
                     CHECK (aridad IN (1, 2)),

    -- Restricción de coherencia inter-columna:
    -- Si lenguaje = 'PYTHON', obligatoriamente tipo = 'TRIGONOMETRICA' y aridad = 1.
    -- Si lenguaje = 'JAVA', tipo = 'BASICA' y aridad puede ser 1 (sqrt) o 2 (+, -, *, /, ^, %).
    CONSTRAINT chk_operacion_coherencia_lenguaje CHECK (
        (lenguaje = 'PYTHON' AND tipo = 'TRIGONOMETRICA' AND aridad = 1) OR
        (lenguaje = 'JAVA'   AND tipo = 'BASICA'         AND aridad IN (1, 2))
    )
);

-- =============================================================================
-- 2. TABLA: calculo (Historial Transaccional de Cálculos Puntuales - R3, R4, R8)
-- Registra cada ejecución puntual exitosa (exito = 1) o fallida (exito = 0).
-- =============================================================================
CREATE TABLE calculo (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    operacion_id  INTEGER NOT NULL,
    operando_a    REAL    NOT NULL,
    operando_b    REAL    NULL,
    modo_angulo   TEXT    NULL
                          CHECK (modo_angulo IN ('grados', 'radianes') OR modo_angulo IS NULL),
    resultado     REAL    NULL,
    exito         INTEGER NOT NULL
                          CHECK (exito IN (0, 1)),
    mensaje_error TEXT    NULL,
    creado_en     TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),

    -- Restricciones de integridad semántica (R3, R8):
    -- Si exito = 1 -> resultado IS NOT NULL AND mensaje_error IS NULL.
    -- Si exito = 0 -> resultado IS NULL AND mensaje_error IS NOT NULL (no vacío).
    CONSTRAINT chk_calculo_coherencia_exito CHECK (
        (exito = 1 AND resultado IS NOT NULL AND mensaje_error IS NULL) OR
        (exito = 0 AND resultado IS NULL     AND mensaje_error IS NOT NULL AND length(trim(mensaje_error)) > 0)
    ),

    CONSTRAINT fk_calculo_operacion
        FOREIGN KEY (operacion_id) REFERENCES operacion(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =============================================================================
-- 3. TABLA: serie (Cabecera de Series Trigonométricas para Graficación - R5)
-- =============================================================================
CREATE TABLE serie (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre       TEXT    NOT NULL
                         CHECK (length(trim(nombre)) > 0),
    operacion_id INTEGER NOT NULL,
    x_min        REAL    NOT NULL,
    x_max        REAL    NOT NULL,
    paso         REAL    NOT NULL,
    modo_angulo  TEXT    NOT NULL
                         CHECK (modo_angulo IN ('grados', 'radianes')),
    creado_en    TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),

    -- Restricciones de dominio del intervalo y paso (R5):
    CONSTRAINT chk_serie_rango_valido CHECK (x_min < x_max),
    CONSTRAINT chk_serie_paso_positivo CHECK (paso > 0),

    CONSTRAINT fk_serie_operacion
        FOREIGN KEY (operacion_id) REFERENCES operacion(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =============================================================================
-- 4. TABLA: punto (Puntos (x, y) de cada Serie - R6, R7)
-- Permite y = NULL para representar asíntotas o indeterminaciones (ej. tan 90°).
-- =============================================================================
CREATE TABLE punto (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    serie_id INTEGER NOT NULL,
    x        REAL    NOT NULL,
    y        REAL    NULL,

    CONSTRAINT uq_punto_serie_x
        UNIQUE (serie_id, x),

    CONSTRAINT fk_punto_serie
        FOREIGN KEY (serie_id) REFERENCES serie(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =============================================================================
-- ÍNDICES DE OPTIMIZACIÓN
-- =============================================================================
CREATE INDEX IF NOT EXISTS idx_calculo_operacion ON calculo(operacion_id);
CREATE INDEX IF NOT EXISTS idx_calculo_exito     ON calculo(exito);
CREATE INDEX IF NOT EXISTS idx_calculo_creado_en ON calculo(creado_en DESC);
CREATE INDEX IF NOT EXISTS idx_serie_operacion   ON serie(operacion_id);
CREATE INDEX IF NOT EXISTS idx_punto_serie_x     ON punto(serie_id, x);
