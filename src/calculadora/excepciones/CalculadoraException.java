package calculadora.excepciones;

/**
 * Excepción base verificada para toda la jerarquía de errores de negocio,
 * cálculo matemático, puente de procesos Python y persistencia SQLite.
 */
public class CalculadoraException extends Exception {

    private final String codigoOperacion;

    public CalculadoraException(String mensaje) {
        super(mensaje);
        this.codigoOperacion = "GENERAL";
    }

    public CalculadoraException(String codigoOperacion, String mensaje) {
        super(mensaje);
        this.codigoOperacion = codigoOperacion;
    }

    public CalculadoraException(String codigoOperacion, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigoOperacion = codigoOperacion;
    }

    public String getCodigoOperacion() {
        return codigoOperacion;
    }
}
