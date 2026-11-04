package calculadora.dominio;

import calculadora.excepciones.CalculadoraException;
import calculadora.excepciones.OperacionInvalidaException;

/**
 * Clase abstracta base que encapsula los metadatos de la tabla `operacion`
 * y la validación estructural de aridad y finitud de los operandos.
 */
public abstract class OperacionAbstracta implements Operacion {

    private final String simbolo;
    private final String nombre;
    private final String tipo;
    private final String lenguaje;
    private final int aridad;

    protected OperacionAbstracta(String simbolo, String nombre, String tipo, String lenguaje, int aridad) {
        this.simbolo = simbolo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.lenguaje = lenguaje;
        this.aridad = aridad;
    }

    @Override
    public String getSimbolo() {
        return simbolo;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public String getTipo() {
        return tipo;
    }

    @Override
    public String getLenguaje() {
        return lenguaje;
    }

    @Override
    public int getAridad() {
        return aridad;
    }

    protected void validarOperandos(double... operandos) throws CalculadoraException {
        if (operandos == null || operandos.length != aridad) {
            throw new OperacionInvalidaException(
                simbolo,
                String.format(
                    "La operación '%s' (%s) requiere exactamente %d operando(s), pero recibió %d.",
                    nombre, simbolo, aridad, operandos == null ? 0 : operandos.length
                )
            );
        }
        for (int i = 0; i < operandos.length; i++) {
            if (!Double.isFinite(operandos[i])) {
                throw new OperacionInvalidaException(
                    simbolo,
                    String.format("El operando #%d no es un número real finito.", i + 1)
                );
            }
        }
    }
}
