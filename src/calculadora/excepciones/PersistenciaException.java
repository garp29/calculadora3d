package calculadora.excepciones;

/**
 * Excepción lanzada por la capa DAO cuando ocurre un error JDBC o violación de restricciones en SQLite.
 */
public class PersistenciaException extends CalculadoraException {

    public PersistenciaException(String mensaje, Throwable causa) {
        super("BD_SQLITE", mensaje, causa);
    }

    public PersistenciaException(String mensaje) {
        super("BD_SQLITE", mensaje);
    }
}
