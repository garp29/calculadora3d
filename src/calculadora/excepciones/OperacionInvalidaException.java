package calculadora.excepciones;

/**
 * Excepción personalizada lanzada cuando se solicita una operación inexistente
 * o cuando los operandos violan el dominio matemático de la operación
 * (ej. tan 90°, asin 2, raíz cuadrada de número negativo, o rango xmin >= xmax).
 */
public class OperacionInvalidaException extends CalculadoraException {

    public OperacionInvalidaException(String simboloOperacion, String mensaje) {
        super(simboloOperacion, mensaje);
    }

    public OperacionInvalidaException(String mensaje) {
        super("DESCONOCIDA", mensaje);
    }
}
