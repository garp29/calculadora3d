package calculadora.excepciones;

/**
 * Excepción lanzada cuando ocurre un fallo al invocar los scripts de Python (trig.py / graficar.py)
 * mediante ProcessBuilder o cuando Python reporta el contrato ERROR;<mensaje>.
 */
public class ProcesoPythonException extends CalculadoraException {

    public ProcesoPythonException(String codigoOperacion, String mensaje) {
        super(codigoOperacion, mensaje);
    }

    public ProcesoPythonException(String codigoOperacion, String mensaje, Throwable causa) {
        super(codigoOperacion, mensaje, causa);
    }
}
