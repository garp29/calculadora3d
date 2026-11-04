package calculadora.dominio;

/**
 * Operación básica de Suma (+).
 */
public class Suma extends OperacionBinaria {

    public Suma() {
        super("+", "Suma");
    }

    @Override
    protected double calcularBinario(double operandoA, double operandoB) {
        return operandoA + operandoB;
    }
}
