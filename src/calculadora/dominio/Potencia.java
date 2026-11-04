package calculadora.dominio;

import calculadora.excepciones.CalculadoraException;
import calculadora.excepciones.OperacionInvalidaException;

/**
 * Extensión Clase 5 (Principio Abierto/Cerrado - G2, R9): Operación de Potencia (^).
 */
public class Potencia extends OperacionBinaria {

    public Potencia() {
        super("^", "Potencia");
    }

    @Override
    protected double calcularBinario(double base, double exponente) throws CalculadoraException {
        if (Math.abs(base) < 1e-12 && exponente < 0) {
            throw new OperacionInvalidaException(
                getSimbolo(),
                "Potencia indefinida: 0 elevado a un exponente negativo implica división por cero"
            );
        }
        double resultado = Math.pow(base, exponente);
        if (Double.isNaN(resultado) || Double.isInfinite(resultado)) {
            throw new OperacionInvalidaException(
                getSimbolo(),
                String.format("Potencia no definida en los reales para %.4f ^ %.4f", base, exponente)
            );
        }
        return resultado;
    }
}
