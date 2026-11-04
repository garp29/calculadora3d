package calculadora.dominio;

/**
 * Operación básica de Multiplicación (*).
 */
public class Multiplicacion extends OperacionBinaria {

    public Multiplicacion() {
        super("*", "Multiplicación");
    }

    @Override
    protected double calcularBinario(double operandoA, double operandoB) {
        return operandoA * operandoB;
    }
}
