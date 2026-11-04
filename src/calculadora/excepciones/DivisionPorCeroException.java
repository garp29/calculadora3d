package calculadora.excepciones;

/**
 * Excepción específica lanzada cuando el divisor en una división (/) o módulo (%) es igual a cero.
 */
public class DivisionPorCeroException extends OperacionInvalidaException {

    public DivisionPorCeroException(String simboloOperacion, String mensaje) {
        super(simboloOperacion, mensaje);
    }
}
