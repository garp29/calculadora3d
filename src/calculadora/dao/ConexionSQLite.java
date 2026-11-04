package calculadora.dao;

import calculadora.excepciones.PersistenciaException;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Proveedor centralizado de conexiones JDBC hacia SQLite (`bd/calculadora.db`).
 * Garantiza que en cada conexión activa se ejecute {@code PRAGMA foreign_keys = ON;}.
 */
public class ConexionSQLite {

    private final Path rutaBaseDatos;
    private final String urlJdbc;

    public ConexionSQLite(Path rutaBaseDatos) {
        this.rutaBaseDatos = rutaBaseDatos;
        this.urlJdbc = "jdbc:sqlite:" + rutaBaseDatos.toAbsolutePath();
    }

    public Connection obtenerConexion() throws PersistenciaException {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(urlJdbc);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        } catch (ClassNotFoundException e) {
            throw new PersistenciaException(
                "No se encontró el driver JDBC de SQLite (org.sqlite.JDBC). Verifique lib/sqlite-jdbc.jar en el classpath.",
                e
            );
        } catch (SQLException e) {
            throw new PersistenciaException(
                "Error al conectar con la base de datos SQLite en '" + rutaBaseDatos + "': " + e.getMessage(),
                e
            );
        }
    }

    public Path getRutaBaseDatos() {
        return rutaBaseDatos;
    }
}
