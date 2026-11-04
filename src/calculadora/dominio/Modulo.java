package calculadora.dominio;

import calculadora.excepciones.CalculadoraException;
import calculadora.excepciones.DivisionPorCeroException;

/**
 * Extensión Clase 5 (Principio Abierto/Cerrado - G2, R9): Operación de Módulo (%).
 */
public class Modulo extends OperacionBinaria {

    private static final double EPSILON = 1e-12;

    public Modulo() {
        super("%", "Módulo");
    }

    @Override
    protected double calcularBinario(double operandoA, double operandoB) throws CalculadoraException {
        if (Math.abs(operandoB) < EPSILON) {
            throw new DivisionPorCeroException(
                getSimbolo(),
                "Módulo por cero no permitido (el divisor b no puede ser 0)"
            );
        }
        return operandoA % operandoB;
    }
}
