package calculadora.dominio;

import calculadora.excepciones.CalculadoraException;
import calculadora.excepciones.OperacionInvalidaException;

/**
 * Extensión Clase 5 (Principio Abierto/Cerrado - G2, R9): Operación de Raíz Cuadrada (sqrt).
 */
public class RaizCuadrada extends OperacionUnaria {

    public RaizCuadrada() {
        super("sqrt", "Raíz Cuadrada", "BASICA", "JAVA");
    }

    @Override
    protected double calcularUnario(double operandoA) throws CalculadoraException {
        if (operandoA < 0.0) {
            throw new OperacionInvalidaException(
                getSimbolo(),
                String.format("Raíz cuadrada no definida en los reales para radicando negativo (%s)", operandoA)
            );
        }
        return Math.sqrt(operandoA);
    }
}
