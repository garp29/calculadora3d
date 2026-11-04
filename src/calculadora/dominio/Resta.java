package calculadora.dominio;

/**
 * Operación básica de Resta (-).
 */
public class Resta extends OperacionBinaria {

    public Resta() {
        super("-", "Resta");
    }

    @Override
    protected double calcularBinario(double operandoA, double operandoB) {
        return operandoA - operandoB;
    }
}
