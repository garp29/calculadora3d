-- =============================================================================
-- PROYECTO INTEGRADO: LA CALCULADORA — JAVA, PYTHON Y SQLITE (ESEIT)
-- Archivo: bd/consultas.sql
-- Descripción: Vistas relacionales (v_historial_calculos, v_resumen_series,
--              v_estadisticas_operacion), Índices justificados y Consultas
--              analíticas con SELECT, JOIN, GROUP BY y ORDER BY.
-- =============================================================================

PRAGMA foreign_keys = ON;

-- =============================================================================
-- 1. ÍNDICES DE OPTIMIZACIÓN JUSTIFICADOS
-- =============================================================================
-- Acelera los JOIN entre calculo y operacion al filtrar o agrupar por operación:
CREATE INDEX IF NOT EXISTS idx_calculo_operacion ON calculo(operacion_id);

-- Acelera el filtrado por éxito/fallo (WHERE exito = 1 o WHERE exito = 0):
CREATE INDEX IF NOT EXISTS idx_calculo_exito     ON calculo(exito);

-- Acelera el ordenamiento cronológico descendente del historial:
CREATE INDEX IF NOT EXISTS idx_calculo_creado_en ON calculo(creado_en DESC);

-- Acelera los JOIN entre serie y operacion:
CREATE INDEX IF NOT EXISTS idx_serie_operacion   ON serie(operacion_id);

-- Acelera la lectura ordenada de los puntos (x, y) de una serie para graficar.py:
CREATE INDEX IF NOT EXISTS idx_punto_serie_x     ON punto(serie_id, x);

-- =============================================================================
-- 2. VISTAS RELACIONALES
-- =============================================================================

-- Vista Oficial: v_historial_calculos
-- Consolida el historial de cálculos uniendo `calculo` con su catálogo `operacion`.
CREATE VIEW IF NOT EXISTS v_historial_calculos AS
SELECT
    c.id AS calculo_id,
    c.creado_en,
    o.id AS operacion_id,
    o.simbolo,
    o.nombre AS nombre_operacion,
    o.tipo,
    o.lenguaje,
    o.aridad,
    c.operando_a,
    c.operando_b,
    c.modo_angulo,
    CASE
        WHEN o.aridad = 2 THEN CAST(c.operando_a AS TEXT) || ' ' || o.simbolo || ' ' || CAST(c.operando_b AS TEXT)
        WHEN c.modo_angulo IS NOT NULL THEN o.simbolo || '(' || CAST(c.operando_a AS TEXT) || ' ' || c.modo_angulo || ')'
        ELSE o.simbolo || '(' || CAST(c.operando_a AS TEXT) || ')'
    END AS expresion,
    c.resultado,
    c.exito,
    CASE WHEN c.exito = 1 THEN 'EXITOSO' ELSE 'FALLIDO' END AS estado_texto,
    COALESCE(c.mensaje_error, 'Sin errores') AS detalle_error
FROM calculo c
INNER JOIN operacion o ON c.operacion_id = o.id;

-- Vista de Resumen de Series y sus Puntos: v_resumen_series
CREATE VIEW IF NOT EXISTS v_resumen_series AS
SELECT
    s.id AS serie_id,
    s.nombre AS nombre_serie,
    o.simbolo AS funcion,
    o.nombre AS nombre_operacion,
    s.x_min,
    s.x_max,
    s.paso,
    s.modo_angulo,
    COUNT(p.id) AS total_puntos,
    COUNT(p.y) AS puntos_definidos,
    (COUNT(p.id) - COUNT(p.y)) AS puntos_nulos_asintotas,
    s.creado_en
FROM serie s
INNER JOIN operacion o ON s.operacion_id = o.id
LEFT JOIN punto p ON p.serie_id = s.id
GROUP BY
    s.id, s.nombre, o.simbolo, o.nombre,
    s.x_min, s.x_max, s.paso, s.modo_angulo, s.creado_en;

-- Vista de Estadísticas por Operación: v_estadisticas_operacion
CREATE VIEW IF NOT EXISTS v_estadisticas_operacion AS
SELECT
    o.id AS operacion_id,
    o.simbolo,
    o.nombre,
    o.tipo,
    o.lenguaje,
    COUNT(c.id) AS total_ejecuciones,
    SUM(CASE WHEN c.exito = 1 THEN 1 ELSE 0 END) AS total_exitos,
    SUM(CASE WHEN c.exito = 0 THEN 1 ELSE 0 END) AS total_fallos,
    CASE
        WHEN COUNT(c.id) = 0 THEN 0.0
        ELSE ROUND(100.0 * SUM(CASE WHEN c.exito = 1 THEN 1 ELSE 0 END) / COUNT(c.id), 2)
    END AS tasa_exito_pct
FROM operacion o
LEFT JOIN calculo c ON c.operacion_id = o.id
GROUP BY o.id, o.simbolo, o.nombre, o.tipo, o.lenguaje;

-- =============================================================================
-- 3. CONSULTAS ANALÍTICAS (SELECT, JOIN, GROUP BY)
-- =============================================================================

-- Consulta 1: Historial en orden cronológico descendente usando v_historial_calculos
SELECT
    calculo_id,
    creado_en,
    lenguaje,
    tipo,
    expresion,
    resultado,
    estado_texto,
    detalle_error
FROM v_historial_calculos
ORDER BY creado_en DESC, calculo_id DESC;

-- Consulta 2: Resumen agrupado por operación (JOIN + GROUP BY)
SELECT
    operacion_id,
    simbolo,
    nombre,
    tipo,
    lenguaje,
    total_ejecuciones,
    total_exitos,
    total_fallos,
    tasa_exito_pct
FROM v_estadisticas_operacion
ORDER BY total_ejecuciones DESC, operacion_id ASC;

-- Consulta 3: Resumen agrupado por estado de éxito vs fallo (exito = 1 vs exito = 0)
SELECT
    c.exito,
    CASE WHEN c.exito = 1 THEN 'EXITOSO (1)' ELSE 'FALLIDO (0)' END AS clasificacion,
    COUNT(c.id) AS cantidad_calculos,
    ROUND(AVG(c.resultado), 4) AS promedio_resultados
FROM calculo c
GROUP BY c.exito
ORDER BY c.exito DESC;

-- Consulta 4: Historial filtrado por operaciones fallidas (auditoría de errores G3/G4/G6)
SELECT
    c.id AS calculo_id,
    c.creado_en,
    o.simbolo,
    o.nombre,
    c.operando_a,
    c.operando_b,
    c.modo_angulo,
    c.mensaje_error
FROM calculo c
INNER JOIN operacion o ON c.operacion_id = o.id
WHERE c.exito = 0
ORDER BY c.id DESC;

-- Consulta 5: Resumen de series trigonométricas y conteo de puntos nulos (y IS NULL)
SELECT
    serie_id,
    nombre_serie,
    funcion,
    x_min,
    x_max,
    paso,
    modo_angulo,
    total_puntos,
    puntos_definidos,
    puntos_nulos_asintotas,
    creado_en
FROM v_resumen_series
ORDER BY serie_id DESC;
