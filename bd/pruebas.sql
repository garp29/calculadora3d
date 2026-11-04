-- =============================================================================
-- PROYECTO INTEGRADO: LA CALCULADORA — JAVA, PYTHON Y SQLITE (ESEIT)
-- Archivo: bd/pruebas.sql
-- Descripción: Las 5 Pruebas obligatorias del Hito 1 sobre el modelo oficial
--              (operacion, calculo, serie, punto), verificando casos válidos
--              y rechazos intencionales por CHECK, FOREIGN KEY y UNIQUE.
-- =============================================================================

PRAGMA foreign_keys = ON;

-- -----------------------------------------------------------------------------
-- PRUEBA 1 [CASO VÁLIDO]: Inserción de cálculos exitosos (exito = 1) y
-- cálculos fallidos controlados (exito = 0 con resultado NULL y mensaje_error).
-- -----------------------------------------------------------------------------
INSERT INTO calculo (operacion_id, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error)
VALUES (
    (SELECT id FROM operacion WHERE simbolo = '+'),
    25.5,
    14.5,
    NULL,
    40.0,
    1,
    NULL
);

INSERT INTO calculo (operacion_id, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error)
VALUES (
    (SELECT id FROM operacion WHERE simbolo = 'sin'),
    180.0,
    NULL,
    'grados',
    0.0,
    1,
    NULL
);

INSERT INTO calculo (operacion_id, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error)
VALUES (
    (SELECT id FROM operacion WHERE simbolo = '/'),
    100.0,
    0.0,
    NULL,
    NULL,
    0,
    'División por cero no permitida (el divisor b no puede ser 0)'
);

INSERT INTO calculo (operacion_id, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error)
VALUES (
    (SELECT id FROM operacion WHERE simbolo = 'tan'),
    90.0,
    NULL,
    'grados',
    NULL,
    0,
    'Tangente indefinida en 90° (asíntota vertical en 90° + k·180°)'
);

-- -----------------------------------------------------------------------------
-- PRUEBA 2 [CASO VÁLIDO]: Creación de una serie trigonométrica válida (R5)
-- y sus puntos (x, y) incluyendo y = NULL en la asíntota de tan 90° (R6).
-- -----------------------------------------------------------------------------
INSERT INTO serie (id, nombre, operacion_id, x_min, x_max, paso, modo_angulo)
VALUES (
    1,
    'Tangente 0° a 180° cada 45°',
    (SELECT id FROM operacion WHERE simbolo = 'tan'),
    0.0,
    180.0,
    45.0,
    'grados'
);

INSERT INTO punto (serie_id, x, y) VALUES
    (1, 0.0,   0.0),
    (1, 45.0,  1.0),
    (1, 90.0,  NULL),  -- Indeterminación / asíntota en tan(90°): y IS NULL
    (1, 135.0, -1.0),
    (1, 180.0, 0.0);

-- -----------------------------------------------------------------------------
-- PRUEBA 3 [RECHAZO ESPERADO - CHECK en calculo (R3, R8)]:
-- Intento de insertar un cálculo con exito = 1 pero resultado = NULL.
-- Resultado esperado: SQLite IntegrityError -> CHECK constraint failed: chk_calculo_coherencia_exito
-- -----------------------------------------------------------------------------
INSERT INTO calculo (operacion_id, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error)
VALUES ((SELECT id FROM operacion WHERE simbolo = '+'), 10.0, 20.0, NULL, NULL, 1, NULL);

-- -----------------------------------------------------------------------------
-- PRUEBA 4 [RECHAZO ESPERADO - CHECK en serie (R5)]:
-- 4A: Intento de insertar una serie con x_min >= x_max (180.0 >= 0.0).
-- 4B: Intento de insertar una serie con paso <= 0 (-15.0).
-- Resultado esperado: SQLite IntegrityError -> CHECK constraint failed
-- -----------------------------------------------------------------------------
INSERT INTO serie (nombre, operacion_id, x_min, x_max, paso, modo_angulo)
VALUES ('Serie Rango Inválido', (SELECT id FROM operacion WHERE simbolo = 'sin'), 180.0, 0.0, 15.0, 'grados');

INSERT INTO serie (nombre, operacion_id, x_min, x_max, paso, modo_angulo)
VALUES ('Serie Paso Negativo', (SELECT id FROM operacion WHERE simbolo = 'sin'), 0.0, 360.0, -15.0, 'grados');

-- -----------------------------------------------------------------------------
-- PRUEBA 5 [RECHAZO ESPERADO - FOREIGN KEY y UNIQUE (R4, R7)]:
-- 5A: Intento de insertar un cálculo apuntando a una operación inexistente (operacion_id = 999).
-- 5B: Intento de insertar una abscisa x duplicada en la misma serie (serie_id = 1, x = 45.0).
-- 5C: Intento de violar coherencia de lenguaje/tipo en operacion (PYTHON con aridad 2).
-- Resultado esperado: SQLite IntegrityError -> FOREIGN KEY / UNIQUE / CHECK constraint failed
-- -----------------------------------------------------------------------------
INSERT INTO calculo (operacion_id, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error)
VALUES (999, 5.0, 5.0, NULL, 10.0, 1, NULL);

INSERT INTO punto (serie_id, x, y)
VALUES (1, 45.0, 1.0);

INSERT INTO operacion (simbolo, nombre, tipo, lenguaje, aridad)
VALUES ('atan2_invalida', 'Arcotangente Binaria Inválida', 'BASICA', 'PYTHON', 2);
