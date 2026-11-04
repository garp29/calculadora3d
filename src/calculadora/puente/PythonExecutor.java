package calculadora.puente;

import calculadora.dominio.PuntoSerie;
import calculadora.excepciones.CalculadoraException;
import calculadora.excepciones.OperacionInvalidaException;
import calculadora.excepciones.ProcesoPythonException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Puente de ejecución hacia los scripts de Python (`python/trig.py` y `python/graficar.py`)
 * mediante {@link ProcessBuilder}.
 *
 * Cumple el Requisito G1 (Rutas con espacios en Windows):
 * - Jamás concatena el comando en un solo String; cada argumento se entrega como un
 *   elemento independiente de {@code List<String>}.
 * - El ejecutable de Python (`python` o `py`) es configurable.
 */
public class PythonExecutor {

    private String ejecutablePython;
    private final Path rutaTrigPy;
    private final Path rutaGraficarPy;
    private final File directorioTrabajo;

    public PythonExecutor(String ejecutablePython, Path rutaTrigPy, Path rutaGraficarPy, File directorioTrabajo) {
        this.ejecutablePython = (ejecutablePython == null || ejecutablePython.isBlank()) ? "python" : ejecutablePython;
        this.rutaTrigPy = rutaTrigPy;
        this.rutaGraficarPy = rutaGraficarPy;
        this.directorioTrabajo = directorioTrabajo;
    }

    public String getEjecutablePython() {
        return ejecutablePython;
    }

    public void setEjecutablePython(String ejecutablePython) {
        if (ejecutablePython != null && !ejecutablePython.isBlank()) {
            this.ejecutablePython = ejecutablePython.trim();
        }
    }

    /**
     * Invoca: {@code python trig.py <fn> <valor> --modo <grados|radianes>}
     * Contrato: exactamente UNA línea {@code OK;<resultado>} o {@code ERROR;<mensaje>}.
     */
    public double evaluarFuncionPuntual(String funcion, double valor, String modoAngulo) throws CalculadoraException {
        String simbolo = funcion.toLowerCase(Locale.ROOT);
        String modo = (modoAngulo == null || modoAngulo.isBlank()) ? "grados" : modoAngulo.toLowerCase(Locale.ROOT);

        List<String> comando = List.of(
            ejecutablePython,
            rutaTrigPy.toAbsolutePath().toString(),
            simbolo,
            formatearDoublePlano(valor),
            "--modo",
            modo
        );

        List<String> lineas = ejecutarSubproceso(comando, simbolo);
        if (lineas.isEmpty()) {
            throw new ProcesoPythonException(simbolo, "El script trig.py no devolvió ninguna línea de salida.");
        }

        String primeraLinea = lineas.get(0).trim();
        if (primeraLinea.startsWith("OK;")) {
            String valorTexto = primeraLinea.substring(3).trim();
            try {
                return Double.parseDouble(valorTexto);
            } catch (NumberFormatException e) {
                throw new ProcesoPythonException(
                    simbolo,
                    "Formato numérico inválido en respuesta de trig.py: '" + primeraLinea + "'",
                    e
                );
            }
        } else if (primeraLinea.startsWith("ERROR;")) {
            String detalleError = primeraLinea.substring(6).trim();
            throw new OperacionInvalidaException(simbolo, detalleError);
        } else {
            throw new ProcesoPythonException(
                simbolo,
                "Respuesta fuera de contrato desde trig.py: '" + primeraLinea + "'"
            );
        }
    }

    /**
     * Invoca: {@code python trig.py serie <fn> <xmin> <xmax> <paso> --modo <grados|radianes>}
     * Contrato: líneas {@code x;y} donde {@code y} vacío se traduce a {@code null} (R6).
     */
    public List<PuntoSerie> generarSeriePuntos(
            String funcion,
            double xmin,
            double xmax,
            double paso,
            String modoAngulo) throws CalculadoraException {

        String simbolo = funcion.toLowerCase(Locale.ROOT);
        String modo = (modoAngulo == null || modoAngulo.isBlank()) ? "grados" : modoAngulo.toLowerCase(Locale.ROOT);

        List<String> comando = List.of(
            ejecutablePython,
            rutaTrigPy.toAbsolutePath().toString(),
            "serie",
            simbolo,
            formatearDoublePlano(xmin),
            formatearDoublePlano(xmax),
            formatearDoublePlano(paso),
            "--modo",
            modo
        );

        List<String> lineas = ejecutarSubproceso(comando, simbolo);
        if (lineas.isEmpty()) {
            throw new ProcesoPythonException(simbolo, "trig.py no devolvió puntos para la serie solicitada.");
        }

        String primera = lineas.get(0).trim();
        if (primera.startsWith("ERROR;")) {
            throw new OperacionInvalidaException(simbolo, primera.substring(6).trim());
        }

        List<PuntoSerie> puntos = new ArrayList<>(lineas.size());
        for (String lineaRaw : lineas) {
            String linea = lineaRaw.trim();
            if (linea.isEmpty()) {
                continue;
            }
            String[] partes = linea.split(";", -1);
            if (partes.length != 2) {
                throw new ProcesoPythonException(
                    simbolo,
                    "Línea de serie mal formada en salida de trig.py: '" + linea + "'"
                );
            }
            try {
                double x = Double.parseDouble(partes[0].trim());
                String yTexto = partes[1].trim();
                Double y = yTexto.isEmpty() ? null : Double.parseDouble(yTexto);
                puntos.add(new PuntoSerie(x, y));
            } catch (NumberFormatException e) {
                throw new ProcesoPythonException(
                    simbolo,
                    "No se pudo convertir par (x;y) devuelto por trig.py: '" + linea + "'",
                    e
                );
            }
        }
        return puntos;
    }

    /**
     * Invoca: {@code python graficar.py <ruta_db> <serie_id> <salida.png>}
     */
    public String graficarSerieDesdeSQLite(Path rutaDb, int serieId, Path rutaSalidaPng) throws CalculadoraException {
        List<String> comando = List.of(
            ejecutablePython,
            rutaGraficarPy.toAbsolutePath().toString(),
            rutaDb.toAbsolutePath().toString(),
            String.valueOf(serieId),
            rutaSalidaPng.toAbsolutePath().toString()
        );

        List<String> lineas = ejecutarSubproceso(comando, "graficar");
        if (lineas.isEmpty()) {
            throw new ProcesoPythonException("graficar", "graficar.py no produjo respuesta en consola.");
        }

        String primera = lineas.get(0).trim();
        if (primera.startsWith("OK;")) {
            return primera.substring(3).trim();
        } else if (primera.startsWith("ERROR;")) {
            throw new ProcesoPythonException("graficar", primera.substring(6).trim());
        } else {
            throw new ProcesoPythonException("graficar", "Salida inesperada de graficar.py: " + primera);
        }
    }

    private List<String> ejecutarSubproceso(List<String> comando, String simboloOp) throws ProcesoPythonException {
        ProcessBuilder pb = new ProcessBuilder(comando);
        if (directorioTrabajo != null && directorioTrabajo.exists()) {
            pb.directory(directorioTrabajo);
        }
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        pb.redirectErrorStream(true);

        List<String> salida = new ArrayList<>();
        try {
            Process proceso = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    if (!linea.isBlank()) {
                        salida.add(linea);
                    }
                }
            }
            int exitCode = proceso.waitFor();
            if (exitCode != 0 && salida.isEmpty()) {
                throw new ProcesoPythonException(
                    simboloOp,
                    "El subproceso Python finalizó con código de error " + exitCode
                );
            }
            return salida;
        } catch (IOException e) {
            throw new ProcesoPythonException(
                simboloOp,
                "No se pudo iniciar el proceso Python (" + ejecutablePython + "): " + e.getMessage(),
                e
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcesoPythonException(simboloOp, "La ejecución del proceso Python fue interrumpida.", e);
        }
    }

    private static String formatearDoublePlano(double valor) {
        if (valor == (long) valor) {
            return String.format(Locale.US, "%d", (long) valor);
        }
        return String.valueOf(valor);
    }
}
