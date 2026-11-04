package calculadora.ui;

import calculadora.dao.CalculadoraDAO;
import calculadora.dominio.Division;
import calculadora.dominio.Modulo;
import calculadora.dominio.Multiplicacion;
import calculadora.dominio.Operacion;
import calculadora.dominio.OperacionPython;
import calculadora.dominio.Potencia;
import calculadora.dominio.PuntoSerie;
import calculadora.dominio.RaizCuadrada;
import calculadora.dominio.RegistroCalculo;
import calculadora.dominio.Resta;
import calculadora.dominio.SerieTrigonometrica;
import calculadora.dominio.Suma;
import calculadora.excepciones.CalculadoraException;
import calculadora.excepciones.OperacionInvalidaException;
import calculadora.puente.PythonExecutor;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Servicio coordinador del dominio que aplica Polimorfismo y el Principio Abierto/Cerrado (OCP - G2, R9).
 * Registra todas las implementaciones de {@link Operacion}, coordina {@link PythonExecutor}
 * y garantiza que todo cálculo (éxito `exito=1` o fallo `exito=0`) se persista en SQLite (G3, G4, G6).
 */
public class MotorCalculadora {

    private final Map<String, Operacion> catalogoPolimorfico = new LinkedHashMap<>();
    private final CalculadoraDAO dao;
    private final PythonExecutor pythonExecutor;
    private final Path rutaBaseDatos;

    public MotorCalculadora(CalculadoraDAO dao, PythonExecutor pythonExecutor, Path rutaBaseDatos) {
        this.dao = dao;
        this.pythonExecutor = pythonExecutor;
        this.rutaBaseDatos = rutaBaseDatos;
        registrarOperacionesPorDefecto();
    }

    /**
     * Registra las 10 operaciones iniciales (Clase 2) y las 3 extensiones OCP (Clase 5).
     */
    private void registrarOperacionesPorDefecto() {
        // 1. Operaciones básicas en Java (Clase 2)
        registrarOperacion(new Suma());
        registrarOperacion(new Resta());
        registrarOperacion(new Multiplicacion());
        registrarOperacion(new Division());

        // 2. Operaciones trigonométricas en Python (Clase 2)
        registrarOperacion(new OperacionPython("sin",  "Seno",          pythonExecutor, "grados"));
        registrarOperacion(new OperacionPython("cos",  "Coseno",        pythonExecutor, "grados"));
        registrarOperacion(new OperacionPython("tan",  "Tangente",      pythonExecutor, "grados"));
        registrarOperacion(new OperacionPython("asin", "Arcoseno",      pythonExecutor, "grados"));
        registrarOperacion(new OperacionPython("acos", "Arcocoseno",    pythonExecutor, "grados"));
        registrarOperacion(new OperacionPython("atan", "Arcotangente",  pythonExecutor, "grados"));

        // 3. Extensiones sin modificar clases existentes (Clase 5 - OCP)
        registrarOperacion(new Potencia());
        registrarOperacion(new RaizCuadrada());
        registrarOperacion(new Modulo());
    }

    public void registrarOperacion(Operacion operacion) {
        catalogoPolimorfico.put(operacion.getSimbolo().toLowerCase(Locale.ROOT), operacion);
    }

    public Operacion obtenerOperacion(String simboloONombre) throws CalculadoraException {
        if (simboloONombre == null || simboloONombre.isBlank()) {
            throw new OperacionInvalidaException("VACIA", "El símbolo o nombre de la operación no puede ser vacío.");
        }
        String clave = simboloONombre.trim().toLowerCase(Locale.ROOT);
        Operacion directa = catalogoPolimorfico.get(clave);
        if (directa != null) {
            return directa;
        }
        for (Operacion op : catalogoPolimorfico.values()) {
            if (op.getNombre().equalsIgnoreCase(clave)) {
                return op;
            }
        }
        throw new OperacionInvalidaException(
            simboloONombre,
            "Operación inexistente o no soportada en el catálogo: '" + simboloONombre + "'"
        );
    }

    public Collection<Operacion> listarOperacionesRegistradas() {
        return Collections.unmodifiableCollection(catalogoPolimorfico.values());
    }

    /**
     * Ejecuta polimórficamente una operación y persiste el cálculo en la tabla `calculo`
     * tanto si tuvo éxito (`exito = 1`) como si lanzó excepción (`exito = 0`), incluyendo
     * intentos de operaciones inexistentes (G3, G4, G6).
     */
    public double ejecutarYPersistir(String simboloOperacion, String modoAngulo, double... operandos)
            throws CalculadoraException {

        double opA = (operandos != null && operandos.length >= 1) ? operandos[0] : 0.0;
        Double opB = (operandos != null && operandos.length >= 2) ? operandos[1] : null;

        Operacion op;
        int operacionId;
        try {
            op = obtenerOperacion(simboloOperacion);
            operacionId = dao.obtenerIdOperacionPorSimbolo(op.getSimbolo());
        } catch (CalculadoraException ex) {
            // Si la operación es inexistente (G3/G6), se persiste el fallo con exito = 0
            // referenciando la operación base id=1 para cumplir FK NOT NULL en `calculo`.
            int idFallback = 1;
            String modoFallido = (modoAngulo != null && ("grados".equalsIgnoreCase(modoAngulo) || "radianes".equalsIgnoreCase(modoAngulo)))
                ? modoAngulo.toLowerCase(Locale.ROOT)
                : null;
            RegistroCalculo falloOpInexistente = RegistroCalculo.fallido(
                idFallback, simboloOperacion, opA, opB, modoFallido, ex.getMessage()
            );
            dao.guardarCalculo(falloOpInexistente);
            throw ex;
        }

        String modoEfectivo = null;
        if (op instanceof OperacionPython opPy) {
            modoEfectivo = (modoAngulo == null || modoAngulo.isBlank()) ? "grados" : modoAngulo.toLowerCase(Locale.ROOT);
            opPy.setModoAngulo(modoEfectivo);
        }

        try {
            double resultado = op.ejecutar(operandos);
            RegistroCalculo exito = RegistroCalculo.exitoso(
                operacionId, op.getSimbolo(), opA, opB, modoEfectivo, resultado
            );
            dao.guardarCalculo(exito);
            return resultado;
        } catch (CalculadoraException ex) {
            RegistroCalculo fallo = RegistroCalculo.fallido(
                operacionId, op.getSimbolo(), opA, opB, modoEfectivo, ex.getMessage()
            );
            dao.guardarCalculo(fallo);
            throw ex;
        }
    }

    /**
     * Flujo completo del Flujograma 2 (Serie de puntos y su gráfica):
     * 1. Valida que la función sea trigonométrica, x_min < x_max y paso > 0 (R5).
     * 2. Pide los puntos `x;y` a `python/trig.py` vía {@link PythonExecutor}.
     * 3. Guarda de forma atómica la serie y sus puntos (incluidos `y = NULL`) en SQLite (`serie` y `punto`).
     * 4. Invoca `python/graficar.py` leyendo directamente desde SQLite para generar el archivo PNG.
     */
    public String generarPersistirYGraficarSerie(
            String nombreSerie,
            String funcion,
            double xMin,
            double xMax,
            double paso,
            String modoAngulo,
            Path rutaSalidaPng) throws CalculadoraException {

        Operacion op = obtenerOperacion(funcion);
        if (!"TRIGONOMETRICA".equalsIgnoreCase(op.getTipo())) {
            throw new OperacionInvalidaException(
                op.getSimbolo(),
                "Solo las operaciones de tipo TRIGONOMETRICA pueden generar series gráficas."
            );
        }
        if (xMin >= xMax) {
            throw new OperacionInvalidaException(
                op.getSimbolo(),
                String.format(Locale.US, "Rango inválido: x_min (%.2f) debe ser estrictamente menor que x_max (%.2f).", xMin, xMax)
            );
        }
        if (paso <= 0.0) {
            throw new OperacionInvalidaException(
                op.getSimbolo(),
                String.format(Locale.US, "Paso inválido: paso (%.2f) debe ser estrictamente mayor que 0.", paso)
            );
        }

        int operacionId = dao.obtenerIdOperacionPorSimbolo(op.getSimbolo());
        String modo = (modoAngulo == null || modoAngulo.isBlank()) ? "grados" : modoAngulo.toLowerCase(Locale.ROOT);
        String titulo = (nombreSerie == null || nombreSerie.isBlank())
            ? String.format(Locale.US, "Serie %s [%.1f, %.1f] paso %.1f (%s)", op.getSimbolo(), xMin, xMax, paso, modo)
            : nombreSerie;

        List<PuntoSerie> puntos = pythonExecutor.generarSeriePuntos(op.getSimbolo(), xMin, xMax, paso, modo);
        int serieId = dao.guardarSerieYPuntos(titulo, operacionId, xMin, xMax, paso, modo, puntos);

        Path destinoPng = rutaSalidaPng != null
            ? rutaSalidaPng
            : rutaBaseDatos.getParent().resolve(String.format(Locale.US, "serie_%s_%d.png", op.getSimbolo(), serieId));

        return pythonExecutor.graficarSerieDesdeSQLite(rutaBaseDatos, serieId, destinoPng);
    }

    public List<RegistroCalculo> obtenerHistorialReciente(int limite) throws CalculadoraException {
        return dao.consultarHistorialReciente(limite);
    }

    public List<SerieTrigonometrica> obtenerResumenSeries() throws CalculadoraException {
        return dao.consultarResumenSeries();
    }

    public List<String> obtenerEstadisticasOperaciones() throws CalculadoraException {
        return dao.consultarEstadisticasOperaciones();
    }
}
