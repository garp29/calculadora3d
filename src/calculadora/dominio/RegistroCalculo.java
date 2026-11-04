package calculadora.dominio;

/**
 * Modelo de dominio que representa una fila de la tabla oficial `calculo`
 * unida con los metadatos de `operacion`.
 */
public class RegistroCalculo {

    private final Integer id;
    private final int operacionId;
    private final String simbolo;
    private final String nombreOperacion;
    private final String tipo;
    private final String lenguaje;
    private final double operandoA;
    private final Double operandoB;
    private final String modoAngulo;
    private final Double resultado;
    private final int exito;
    private final String mensajeError;
    private final String creadoEn;

    public RegistroCalculo(
            Integer id,
            int operacionId,
            String simbolo,
            String nombreOperacion,
            String tipo,
            String lenguaje,
            double operandoA,
            Double operandoB,
            String modoAngulo,
            Double resultado,
            int exito,
            String mensajeError,
            String creadoEn) {
        this.id = id;
        this.operacionId = operacionId;
        this.simbolo = simbolo;
        this.nombreOperacion = nombreOperacion;
        this.tipo = tipo;
        this.lenguaje = lenguaje;
        this.operandoA = operandoA;
        this.operandoB = operandoB;
        this.modoAngulo = modoAngulo;
        this.resultado = resultado;
        this.exito = exito;
        this.mensajeError = mensajeError;
        this.creadoEn = creadoEn;
    }

    public static RegistroCalculo exitoso(
            int operacionId,
            String simbolo,
            double operandoA,
            Double operandoB,
            String modoAngulo,
            double resultado) {
        return new RegistroCalculo(
            null, operacionId, simbolo, null, null, null,
            operandoA, operandoB, modoAngulo, resultado, 1, null, null
        );
    }

    public static RegistroCalculo fallido(
            int operacionId,
            String simbolo,
            double operandoA,
            Double operandoB,
            String modoAngulo,
            String mensajeError) {
        return new RegistroCalculo(
            null, operacionId, simbolo, null, null, null,
            operandoA, operandoB, modoAngulo, null, 0, mensajeError, null
        );
    }

    public Integer getId() {
        return id;
    }

    public int getOperacionId() {
        return operacionId;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getNombreOperacion() {
        return nombreOperacion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getLenguaje() {
        return lenguaje;
    }

    public double getOperandoA() {
        return operandoA;
    }

    public Double getOperandoB() {
        return operandoB;
    }

    public String getModoAngulo() {
        return modoAngulo;
    }

    public Double getResultado() {
        return resultado;
    }

    public int getExito() {
        return exito;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public String getCreadoEn() {
        return creadoEn;
    }
}
