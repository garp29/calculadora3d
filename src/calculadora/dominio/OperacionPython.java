package calculadora.dominio;

import calculadora.excepciones.CalculadoraException;
import calculadora.puente.PythonExecutor;

/**
 * Clase abstracta/concreta intermedia (G2) para las operaciones trigonométricas
 * (`sin`, `cos`, `tan`, `asin`, `acos`, `atan`) que cumplen `lenguaje = 'PYTHON'`,
 * `tipo = 'TRIGONOMETRICA'` y `aridad = 1`, delegando el cálculo a {@link PythonExecutor}.
 */
public class OperacionPython extends OperacionUnaria {

    private final PythonExecutor pythonExecutor;
    private String modoAngulo;

    public OperacionPython(String simbolo, String nombre, PythonExecutor pythonExecutor, String modoPorDefecto) {
        super(simbolo, nombre, "TRIGONOMETRICA", "PYTHON");
        this.pythonExecutor = pythonExecutor;
        this.modoAngulo = (modoPorDefecto == null || modoPorDefecto.isBlank()) ? "grados" : modoPorDefecto;
    }

    public String getModoAngulo() {
        return modoAngulo;
    }

    public void setModoAngulo(String modoAngulo) {
        if ("grados".equalsIgnoreCase(modoAngulo) || "radianes".equalsIgnoreCase(modoAngulo)) {
            this.modoAngulo = modoAngulo.toLowerCase();
        }
    }

    @Override
    protected double calcularUnario(double operandoA) throws CalculadoraException {
        return pythonExecutor.evaluarFuncionPuntual(getSimbolo(), operandoA, modoAngulo);
    }
}
