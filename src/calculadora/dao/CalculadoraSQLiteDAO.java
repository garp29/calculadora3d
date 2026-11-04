package calculadora.dao;

import calculadora.dominio.PuntoSerie;
import calculadora.dominio.RegistroCalculo;
import calculadora.dominio.SerieTrigonometrica;
import calculadora.excepciones.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Implementación concreta del patrón DAO sobre SQLite (`bd/calculadora.db`) vía JDBC.
 * Cumple los requisitos R1-R9 y G3-G6 empleando {@link PreparedStatement} parametrizados
 * y transacciones atómicas (`setAutoCommit(false)` / `commit()` / `rollback()`).
 */
public class CalculadoraSQLiteDAO implements CalculadoraDAO {

    private final ConexionSQLite conexionSQLite;

    public CalculadoraSQLiteDAO(ConexionSQLite conexionSQLite) {
        this.conexionSQLite = conexionSQLite;
    }

    @Override
    public int obtenerIdOperacionPorSimbolo(String simbolo) throws PersistenciaException {
        final String sql = "SELECT id FROM operacion WHERE LOWER(simbolo) = LOWER(?) OR LOWER(nombre) = LOWER(?)";
        try (Connection conn = conexionSQLite.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String criterio = simbolo == null ? "" : simbolo.trim();
            ps.setString(1, criterio);
            ps.setString(2, criterio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
                throw new PersistenciaException(
                    "No existe la operación '" + simbolo + "' en la tabla 'operacion' (ejecute bd/seed.sql)."
                );
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar tabla 'operacion': " + e.getMessage(), e);
        }
    }

    @Override
    public int guardarCalculo(RegistroCalculo registro) throws PersistenciaException {
        final String sql = """
            INSERT INTO calculo (
                operacion_id, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = conexionSQLite.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, registro.getOperacionId());
            ps.setDouble(2, registro.getOperandoA());

            if (registro.getOperandoB() != null) {
                ps.setDouble(3, registro.getOperandoB());
            } else {
                ps.setNull(3, Types.REAL);
            }

            if (registro.getModoAngulo() != null && !registro.getModoAngulo().isBlank()) {
                ps.setString(4, registro.getModoAngulo());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            if (registro.getResultado() != null) {
                ps.setDouble(5, registro.getResultado());
            } else {
                ps.setNull(5, Types.REAL);
            }

            ps.setInt(6, registro.getExito());

            if (registro.getMensajeError() != null && !registro.getMensajeError().isBlank()) {
                ps.setString(7, registro.getMensajeError());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                return -1;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar cálculo en tabla 'calculo': " + e.getMessage(), e);
        }
    }

    @Override
    public List<RegistroCalculo> consultarHistorialReciente(int limite) throws PersistenciaException {
        final String sql = """
            SELECT
                c.id,
                c.operacion_id,
                o.simbolo,
                o.nombre,
                o.tipo,
                o.lenguaje,
                c.operando_a,
                c.operando_b,
                c.modo_angulo,
                c.resultado,
                c.exito,
                c.mensaje_error,
                c.creado_en
            FROM calculo c
            INNER JOIN operacion o ON c.operacion_id = o.id
            ORDER BY c.creado_en DESC, c.id DESC
            LIMIT ?
            """;

        List<RegistroCalculo> historial = new ArrayList<>();
        try (Connection conn = conexionSQLite.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Math.max(1, limite));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double opBVal = rs.getDouble("operando_b");
                    Double operandoB = rs.wasNull() ? null : opBVal;

                    double resVal = rs.getDouble("resultado");
                    Double resultado = rs.wasNull() ? null : resVal;

                    historial.add(new RegistroCalculo(
                        rs.getInt("id"),
                        rs.getInt("operacion_id"),
                        rs.getString("simbolo"),
                        rs.getString("nombre"),
                        rs.getString("tipo"),
                        rs.getString("lenguaje"),
                        rs.getDouble("operando_a"),
                        operandoB,
                        rs.getString("modo_angulo"),
                        resultado,
                        rs.getInt("exito"),
                        rs.getString("mensaje_error"),
                        rs.getString("creado_en")
                    ));
                }
            }
            return historial;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar historial de cálculos: " + e.getMessage(), e);
        }
    }

    @Override
    public int guardarSerieYPuntos(
            String nombreSerie,
            int operacionId,
            double xMin,
            double xMax,
            double paso,
            String modoAngulo,
            List<PuntoSerie> puntos) throws PersistenciaException {

        final String sqlCabecera = """
            INSERT INTO serie (nombre, operacion_id, x_min, x_max, paso, modo_angulo)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        final String sqlPunto = """
            INSERT INTO punto (serie_id, x, y)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = conexionSQLite.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                int serieId;
                try (PreparedStatement psCab = conn.prepareStatement(sqlCabecera, Statement.RETURN_GENERATED_KEYS)) {
                    psCab.setString(1, nombreSerie);
                    psCab.setInt(2, operacionId);
                    psCab.setDouble(3, xMin);
                    psCab.setDouble(4, xMax);
                    psCab.setDouble(5, paso);
                    psCab.setString(6, modoAngulo.toLowerCase(Locale.ROOT));
                    psCab.executeUpdate();

                    try (ResultSet rsKeys = psCab.getGeneratedKeys()) {
                        if (!rsKeys.next()) {
                            throw new PersistenciaException("No se pudo obtener el ID autogenerado para la tabla 'serie'.");
                        }
                        serieId = rsKeys.getInt(1);
                    }
                }

                try (PreparedStatement psPt = conn.prepareStatement(sqlPunto)) {
                    for (PuntoSerie punto : puntos) {
                        psPt.setInt(1, serieId);
                        psPt.setDouble(2, punto.getX());
                        if (punto.getY() != null) {
                            psPt.setDouble(3, punto.getY());
                        } else {
                            psPt.setNull(3, Types.REAL);
                        }
                        psPt.addBatch();
                    }
                    psPt.executeBatch();
                }

                conn.commit();
                return serieId;
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof PersistenciaException pe) {
                    throw pe;
                }
                throw new PersistenciaException("Transacción de serie revertida (rollback): " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error JDBC al guardar serie y puntos: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SerieTrigonometrica> consultarResumenSeries() throws PersistenciaException {
        final String sql = """
            SELECT
                s.id AS serie_id,
                s.nombre,
                s.operacion_id,
                o.simbolo AS funcion,
                s.x_min,
                s.x_max,
                s.paso,
                s.modo_angulo,
                s.creado_en,
                COUNT(p.id) AS total_puntos,
                COUNT(p.y) AS puntos_definidos,
                (COUNT(p.id) - COUNT(p.y)) AS puntos_nulos_asintotas
            FROM serie s
            INNER JOIN operacion o ON s.operacion_id = o.id
            LEFT JOIN punto p ON p.serie_id = s.id
            GROUP BY
                s.id, s.nombre, s.operacion_id, o.simbolo,
                s.x_min, s.x_max, s.paso, s.modo_angulo, s.creado_en
            ORDER BY s.id DESC
            """;

        List<SerieTrigonometrica> series = new ArrayList<>();
        try (Connection conn = conexionSQLite.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                series.add(new SerieTrigonometrica(
                    rs.getInt("serie_id"),
                    rs.getString("nombre"),
                    rs.getInt("operacion_id"),
                    rs.getString("funcion"),
                    rs.getDouble("x_min"),
                    rs.getDouble("x_max"),
                    rs.getDouble("paso"),
                    rs.getString("modo_angulo"),
                    rs.getString("creado_en"),
                    rs.getInt("total_puntos"),
                    rs.getInt("puntos_definidos"),
                    rs.getInt("puntos_nulos_asintotas")
                ));
            }
            return series;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar resumen de series: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> consultarEstadisticasOperaciones() throws PersistenciaException {
        final String sql = """
            SELECT
                o.id,
                o.simbolo,
                o.nombre,
                o.tipo,
                o.lenguaje,
                COUNT(c.id) AS total,
                SUM(CASE WHEN c.exito = 1 THEN 1 ELSE 0 END) AS exitos,
                SUM(CASE WHEN c.exito = 0 THEN 1 ELSE 0 END) AS fallos
            FROM operacion o
            LEFT JOIN calculo c ON c.operacion_id = o.id
            GROUP BY o.id, o.simbolo, o.nombre, o.tipo, o.lenguaje
            ORDER BY total DESC, o.id ASC
            """;

        List<String> filas = new ArrayList<>();
        try (Connection conn = conexionSQLite.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int total = rs.getInt("total");
                int exitos = rs.getInt("exitos");
                int fallos = rs.getInt("fallos");
                double tasa = total == 0 ? 0.0 : (100.0 * exitos / total);
                filas.add(String.format(
                    Locale.US,
                    "%-5s | %-15s | %-14s | %-6s | Total: %3d | Éxito(1): %3d | Fallo(0): %3d | Tasa: %6.2f%%",
                    rs.getString("simbolo"),
                    rs.getString("nombre"),
                    rs.getString("tipo"),
                    rs.getString("lenguaje"),
                    total,
                    exitos,
                    fallos,
                    tasa
                ));
            }
            return filas;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar estadísticas por operación: " + e.getMessage(), e);
        }
    }
}
