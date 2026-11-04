-- =============================================================================
-- PROYECTO INTEGRADO: LA CALCULADORA — JAVA, PYTHON Y SQLITE (ESEIT)
-- Archivo: bd/seed.sql
-- Descripción: Catálogo semilla de operaciones (10 operaciones iniciales de
--              la Clase 2 y las 3 extensiones OCP de la Clase 5).
-- =============================================================================

PRAGMA foreign_keys = ON;

-- 1. Las 10 operaciones iniciales (Clase 2: 4 básicas en Java + 6 trigonométricas en Python)
INSERT OR IGNORE INTO operacion (id, simbolo, nombre, tipo, lenguaje, aridad) VALUES
    (1,  '+',    'Suma',           'BASICA',         'JAVA',   2),
    (2,  '-',    'Resta',          'BASICA',         'JAVA',   2),
    (3,  '*',    'Multiplicación', 'BASICA',         'JAVA',   2),
    (4,  '/',    'División',       'BASICA',         'JAVA',   2),
    (5,  'sin',  'Seno',           'TRIGONOMETRICA', 'PYTHON', 1),
    (6,  'cos',  'Coseno',         'TRIGONOMETRICA', 'PYTHON', 1),
    (7,  'tan',  'Tangente',       'TRIGONOMETRICA', 'PYTHON', 1),
    (8,  'asin', 'Arcoseno',       'TRIGONOMETRICA', 'PYTHON', 1),
    (9,  'acos', 'Arcocoseno',     'TRIGONOMETRICA', 'PYTHON', 1),
    (10, 'atan', 'Arcotangente',   'TRIGONOMETRICA', 'PYTHON', 1);

-- 2. Las 3 operaciones extensibles sin alterar estructura (Clase 5 - Principio Abierto/Cerrado OCP)
INSERT OR IGNORE INTO operacion (id, simbolo, nombre, tipo, lenguaje, aridad) VALUES
    (11, '^',    'Potencia',       'BASICA',         'JAVA',   2),
    (12, 'sqrt', 'Raíz Cuadrada',  'BASICA',         'JAVA',   1),
    (13, '%',    'Módulo',         'BASICA',         'JAVA',   2);
