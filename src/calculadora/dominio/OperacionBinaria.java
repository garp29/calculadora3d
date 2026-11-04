package calculadora.dominio;

import calculadora.excepciones.CalculadoraException;

/**
 * Clase abstracta intermedia para operaciones binarias básicas (aridad = 2) ejecutadas en Java.
 */
public abstract class OperacionBinaria extends OperacionAbstracta {

    protected OperacionBinaria(String simbolo, String nombre) {
        super(simbolo, nombre, "BASICA", "JAVA", 2);
    }

    @Override
    public final double ejecutar(double... operandos) throws CalculadoraException {
        validarOperandos(operandos);
        return calcularBinario(operandos[0], operandos[1]);
    }

    /**
     * Método plantilla implementado por cada operación binaria concreta.
     */
    protected abstract double calcularBinario(double operandoA, double operandoB) throws CalculadoraException;
}
