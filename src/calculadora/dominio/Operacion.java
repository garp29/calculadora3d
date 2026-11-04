package calculadora.dominio;

import calculadora.excepciones.CalculadoraException;

/**
 * Interfaz común que define el contrato polimórfico de toda operación matemática (G2, R9).
 * Cumple el Principio Abierto/Cerrado (OCP): agregar nuevas operaciones (como ^, sqrt, %)
 * consiste en añadir una clase que implemente {@link Operacion} y una fila en la tabla `operacion`
 * sin modificar el motor ni las clases de cálculo existentes.
 */
public interface Operacion {

    /**
     * @return Símbolo único en la tabla `operacion` (ej. "+", "-", "*", "/", "^", "sqrt", "%", "sin").
     */
    String getSimbolo();

    /**
     * @return Nombre descriptivo único en la tabla `operacion` (ej. "Suma", "Raíz Cuadrada").
     */
    String getNombre();

    /**
     * @return Tipo de operación según el contrato de BD ("BASICA" o "TRIGONOMETRICA").
     */
    String getTipo();

    /**
     * @return Lenguaje ejecutor ("JAVA" o "PYTHON").
     */
    String getLenguaje();

    /**
     * @return Cantidad de operandos requeridos (1 o 2).
     */
    int getAridad();

    /**
     * Ejecuta polimórficamente el cálculo matemático sobre los operandos recibidos.
     *
     * @param operandos Arreglo de operandos reales (longitud igual a {@link #getAridad()}).
     * @return Resultado numérico real.
     * @throws CalculadoraException Si ocurre un error matemático (división por cero, fuera de dominio)
     *                              o fallo en el subproceso de Python.
     */
    double ejecutar(double... operandos) throws CalculadoraException;
}
