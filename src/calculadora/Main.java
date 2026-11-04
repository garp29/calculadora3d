package calculadora;

import calculadora.dao.CalculadoraDAO;
import calculadora.dao.CalculadoraSQLiteDAO;
import calculadora.dao.ConexionSQLite;
import calculadora.puente.PythonExecutor;
import calculadora.ui.MenuConsola;
import calculadora.ui.MotorCalculadora;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Punto de entrada principal del sistema "La Calculadora — Java, Python y SQLite".
 * Ensambla las tres capas mediante Inyección de Dependencias:
 * 1. Persistencia JDBC en `bd/calculadora.db` (`operacion`, `calculo`, `serie`, `punto`).
 * 2. Puente de Procesos {@link PythonExecutor} (cumple G1 para rutas con espacios en Windows).
 * 3. Motor Polimórfico {@link MotorCalculadora} (cumple G2/R9 bajo Principio Abierto/Cerrado).
 */
public class Main {

    public static void main(String[] args) {
        configurarConsolaUtf8EnWindows();
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        Path baseDir = Path.of("").toAbsolutePath();
        Path rutaDb = baseDir.resolve("bd").resolve("calculadora.db");
        Path rutaTrigPy = baseDir.resolve("python").resolve("trig.py");
        Path rutaGraficarPy = baseDir.resolve("python").resolve("graficar.py");

        // Ejecutable de Python configurable (G1): propiedad -Dcalculadora.python=py, variable PYTHON_CMD o "python"
        String pythonCmd = System.getProperty(
            "calculadora.python",
            System.getenv().getOrDefault("PYTHON_CMD", "python")
        );

        ConexionSQLite conexionSQLite = new ConexionSQLite(rutaDb);
        CalculadoraDAO calculadoraDAO = new CalculadoraSQLiteDAO(conexionSQLite);
        PythonExecutor pythonExecutor = new PythonExecutor(pythonCmd, rutaTrigPy, rutaGraficarPy, baseDir.toFile());

        MotorCalculadora motor = new MotorCalculadora(calculadoraDAO, pythonExecutor, rutaDb);
        MenuConsola menu = new MenuConsola(motor, baseDir.resolve("diagramas").resolve("img"));

        boolean modoDemo = false;
        for (String arg : args) {
            if ("--demo".equalsIgnoreCase(arg)) {
                modoDemo = true;
                break;
            }
        }

        if (modoDemo) {
            menu.ejecutarDemostracionIntegral();
        } else {
            menu.iniciarMenuInteractivo();
        }
    }

    /**
     * Cambia automáticamente la página de códigos de la consola de Windows (CP850/CP437)
     * a UTF-8 (65001) para que las tildes, el símbolo '°' y '¡' se muestren sin caracteres corruptos.
     */
    private static void configurarConsolaUtf8EnWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            try {
                new ProcessBuilder("cmd.exe", "/c", "chcp 65001 > nul")
                    .inheritIO()
                    .start()
                    .waitFor();
            } catch (Exception ignored) {
            }
        }
    }
}
