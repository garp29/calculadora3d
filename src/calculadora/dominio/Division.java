package calculadora.dominio;

import calculadora.excepciones.CalculadoraException;
import calculadora.excepciones.DivisionPorCeroException;

/**
 * Operación básica de División (/) con validación de divisor igual a cero (G3).
 */
public class Division extends OperacionBinaria {

    private static final double EPSILON = 1e-12;

    public Division() {
        super("/", "División");
    }

    @Override
    protected double calcularBinario(double operandoA, double operandoB) throws CalculadoraException {
        if (Math.abs(operandoB) < EPSILON) {
            throw new DivisionPorCeroException(
                getSimbolo(),
                "División por cero no permitida (el divisor b no puede ser 0)"
            );
        }
        return operandoA / operandoB;
    }
}
