package calculadora.dominio;

import calculadora.excepciones.CalculadoraException;

/**
 * Clase abstracta intermedia para operaciones unarias (aridad = 1).
 */
public abstract class OperacionUnaria extends OperacionAbstracta {

    protected OperacionUnaria(String simbolo, String nombre, String tipo, String lenguaje) {
        super(simbolo, nombre, tipo, lenguaje, 1);
    }

    @Override
    public final double ejecutar(double... operandos) throws CalculadoraException {
        validarOperandos(operandos);
        return calcularUnario(operandos[0]);
    }

    /**
     * Método plantilla implementado por cada operación unaria concreta.
     */
    protected abstract double calcularUnario(double operandoA) throws CalculadoraException;
}
