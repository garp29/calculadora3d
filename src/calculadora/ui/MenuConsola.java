package calculadora.ui;

import calculadora.dominio.Operacion;
import calculadora.dominio.RegistroCalculo;
import calculadora.dominio.SerieTrigonometrica;
import calculadora.excepciones.CalculadoraException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Interfaz de Usuario en Consola para "La Calculadora — Java, Python y SQLite".
 * Gestiona el menú interactivo y el modo de demostración integral (`--demo`).
 */
public class MenuConsola {

    private final MotorCalculadora motor;
    private final Path directorioBd;

    public MenuConsola(MotorCalculadora motor, Path directorioBd) {
        this.motor = motor;
        this.directorioBd = directorioBd;
    }

    public void iniciarMenuInteractivo() {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            boolean continuar = true;
            while (continuar) {
                imprimirEncabezadoMenu();
                System.out.print("Seleccione una opción [0-7]: ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String opcion = scanner.nextLine().trim();
                switch (opcion) {
                    case "1" -> flujoOperacionJava(scanner, List.of("+", "-", "*", "/"));
                    case "2" -> flujoOperacionJava(scanner, List.of("^", "sqrt", "%"));
                    case "3" -> flujoOperacionTrigonometrica(scanner);
                    case "4" -> flujoSerieYGraficacion(scanner);
                    case "5" -> mostrarHistorialReciente(25);
                    case "6" -> mostrarSeriesYEstadisticas();
                    case "7" -> ejecutarDemostracionIntegral();
                    case "0" -> {
                        System.out.println("\nCerrando La Calculadora. ¡Hasta pronto!");
                        continuar = false;
                    }
                    default -> System.out.println("\n[!] Opción no válida. Intente nuevamente.");
                }
            }
        }
    }

    private void imprimirEncabezadoMenu() {
        System.out.println("\n==========================================================================");
        System.out.println("       LA CALCULADORA CIENTÍFICA Y GRAFICADORA (JAVA + PYTHON + SQLITE)   ");
        System.out.println("==========================================================================");
        System.out.println("  1. Operaciones Básicas [Java POO2]: + (Suma), - (Resta), * (Mult), / (Div)");
        System.out.println("  2. Extensiones Clase 5 OCP [Java POO2]: ^ (Potencia), sqrt (Raíz), % (Módulo)");
        System.out.println("  3. Funciones Trigonométricas [Python trig.py]: sin, cos, tan, asin, acos, atan");
        System.out.println("  4. Generar Serie Trigonométrica y Graficar PNG [SQLite + graficar.py]");
        System.out.println("  5. Consultar Historial en SQLite (v_historial_calculos: exito=1 y exito=0)");
        System.out.println("  6. Consultar Series Graficadas y Estadísticas Agrupadas (JOIN / GROUP BY)");
        System.out.println("  7. Ejecutar Batería de Demostración Integral (Pruebas E2E)");
        System.out.println("  0. Salir");
        System.out.println("--------------------------------------------------------------------------");
    }

    private void flujoOperacionJava(Scanner scanner, List<String> simbolosSugeridos) {
        System.out.println("\n--- Operaciones Disponibles ---");
        for (String simb : simbolosSugeridos) {
            try {
                Operacion op = motor.obtenerOperacion(simb);
                System.out.printf("  [%s] %s - Tipo: %s, Lenguaje: %s, Aridad: %d%n",
                    op.getSimbolo(), op.getNombre(), op.getTipo(), op.getLenguaje(), op.getAridad());
            } catch (CalculadoraException ignored) {
            }
        }
        System.out.print("Ingrese el símbolo de la operación (ej. " + simbolosSugeridos.get(0) + "): ");
        String simbolo = scanner.nextLine().trim();

        try {
            Operacion op = motor.obtenerOperacion(simbolo);
            if (op.getAridad() == 1) {
                double a = leerDouble(scanner, "Ingrese el operando (operando_a): ");
                double res = motor.ejecutarYPersistir(simbolo, null, a);
                System.out.printf(Locale.US, "[OK exito=1] %s(%.4f) = %.6f (Persistido en tabla 'calculo')%n",
                    op.getSimbolo(), a, res);
            } else {
                double a = leerDouble(scanner, "Ingrese el primer operando (operando_a): ");
                double b = leerDouble(scanner, "Ingrese el segundo operando (operando_b): ");
                double res = motor.ejecutarYPersistir(simbolo, null, a, b);
                System.out.printf(Locale.US, "[OK exito=1] %.4f %s %.4f = %.6f (Persistido en tabla 'calculo')%n",
                    a, op.getSimbolo(), b, res);
            }
        } catch (CalculadoraException e) {
            // Si la operación es inexistente o falló la validación previa, asegurar registro en BD si aún no se hizo
            if (e.getMessage() != null && e.getMessage().startsWith("Operación inexistente")) {
                try {
                    motor.ejecutarYPersistir(simbolo, null, 0.0);
                } catch (CalculadoraException ignored) {
                }
            }
            System.out.println("[ERROR REGISTRADO EN SQLITE con exito=0] " + e.getMessage());
        }
    }

    private void flujoOperacionTrigonometrica(Scanner scanner) {
        System.out.println("\n--- Funciones Trigonométricas en Python (trig.py) ---");
        System.out.println("  Funciones disponibles: sin, cos, tan, asin, acos, atan");
        System.out.print("Ingrese la función (ej. sin): ");
        String fn = scanner.nextLine().trim().toLowerCase(Locale.ROOT);

        System.out.print("Modo de ángulo [grados / radianes] (Enter = grados): ");
        String modo = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        if (modo.isEmpty()) {
            modo = "grados";
        }

        double valor = leerDouble(scanner, "Ingrese el valor numérico (operando_a): ");
        try {
            double res = motor.ejecutarYPersistir(fn, modo, valor);
            System.out.printf(Locale.US, "[OK exito=1] %s(%.4f) [%s] = %.6f (Persistido en tabla 'calculo')%n",
                fn, valor, modo, res);
        } catch (CalculadoraException e) {
            System.out.println("[ERROR REGISTRADO EN SQLITE con exito=0] " + e.getMessage());
        }
    }

    private void flujoSerieYGraficacion(Scanner scanner) {
        System.out.println("\n--- Generación de Serie y Graficación (trig.py -> SQLite -> graficar.py) ---");
        System.out.print("Nombre descriptivo de la serie: ");
        String nombreSerie = scanner.nextLine().trim();

        System.out.print("Función a graficar [sin, cos, tan, asin, acos, atan]: ");
        String fn = scanner.nextLine().trim().toLowerCase(Locale.ROOT);

        double xMin = leerDouble(scanner, "Valor mínimo (x_min): ");
        double xMax = leerDouble(scanner, "Valor máximo (x_max): ");
        double paso = leerDouble(scanner, "Paso de incremento (paso > 0): ");

        System.out.print("Modo de ángulo [grados / radianes] (Enter = grados): ");
        String modo = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        if (modo.isEmpty()) {
            modo = "grados";
        }

        System.out.print("Ruta de salida PNG (Enter = automático en carpeta bd/): ");
        String rutaInput = scanner.nextLine().trim();
        Path rutaPng = rutaInput.isEmpty() ? null : Path.of(rutaInput);

        try {
            String pngGenerado = motor.generarPersistirYGraficarSerie(nombreSerie, fn, xMin, xMax, paso, modo, rutaPng);
            System.out.println("[OK] Serie y puntos guardados en SQLite; gráfica PNG generada en: " + pngGenerado);
        } catch (CalculadoraException e) {
            System.out.println("[ERROR EN SERIE / GRAFICACIÓN] " + e.getMessage());
        }
    }

    public void mostrarHistorialReciente(int limite) {
        System.out.println("\n--- Historial Reciente en Tabla 'calculo' (Últimos " + limite + ") ---");
        try {
            List<RegistroCalculo> lista = motor.obtenerHistorialReciente(limite);
            if (lista.isEmpty()) {
                System.out.println("  (No hay cálculos registrados aún)");
                return;
            }
            System.out.printf("%-4s | %-19s | %-6s | %-14s | %-5s | %-24s | %-12s | %-5s | %s%n",
                "ID", "CREADO_EN", "LENG", "TIPO", "SIMB", "EXPRESION", "RESULTADO", "EXITO", "MENSAJE_ERROR");
            System.out.println("-".repeat(125));
            for (RegistroCalculo r : lista) {
                String expr = r.getOperandoB() != null
                    ? String.format(Locale.US, "%.2f %s %.2f", r.getOperandoA(), r.getSimbolo(), r.getOperandoB())
                    : String.format(Locale.US, "%s(%.2f)%s",
                        r.getSimbolo(),
                        r.getOperandoA(),
                        r.getModoAngulo() != null ? " [" + r.getModoAngulo() + "]" : "");
                String resStr = r.getResultado() != null
                    ? String.format(Locale.US, "%.6f", r.getResultado())
                    : "NULL";
                String errStr = r.getMensajeError() != null ? r.getMensajeError() : "NULL";
                System.out.printf("%-4d | %-19s | %-6s | %-14s | %-5s | %-24s | %-12s | %-5d | %s%n",
                    r.getId(), r.getCreadoEn(), r.getLenguaje(), r.getTipo(), r.getSimbolo(),
                    expr, resStr, r.getExito(), errStr);
            }
        } catch (CalculadoraException e) {
            System.out.println("[ERROR AL CONSULTAR HISTORIAL] " + e.getMessage());
        }
    }

    public void mostrarSeriesYEstadisticas() {
        System.out.println("\n--- Resumen de Series Trigonométricas (Tablas 'serie' y 'punto') ---");
        try {
            List<SerieTrigonometrica> series = motor.obtenerResumenSeries();
            if (series.isEmpty()) {
                System.out.println("  (No hay series registradas aún)");
            } else {
                for (SerieTrigonometrica s : series) {
                    System.out.printf(
                        Locale.US,
                        "  Serie #%d ('%s') | fn=%s | [%.1f, %.1f] paso=%.1f (%s) | Puntos=%d (Definidos=%d, NULL/Asíntotas=%d) | Creado=%s%n",
                        s.getId(), s.getNombre(), s.getFuncion(), s.getXMin(), s.getXMax(), s.getPaso(),
                        s.getModoAngulo(), s.getTotalPuntos(), s.getPuntosDefinidos(), s.getPuntosNulosAsintotas(),
                        s.getCreadoEn()
                    );
                }
            }

            System.out.println("\n--- Estadísticas Agrupadas por Operación (JOIN + GROUP BY) ---");
            for (String linea : motor.obtenerEstadisticasOperaciones()) {
                System.out.println("  " + linea);
            }
        } catch (CalculadoraException e) {
            System.out.println("[ERROR EN CONSULTA ANALÍTICA] " + e.getMessage());
        }
    }

    /**
     * Ejecuta una batería completa extremo a extremo verificando:
     * 1. Operaciones básicas (+, -, *, /).
     * 2. Extensiones OCP de Clase 5 (^, sqrt, %).
     * 3. Operaciones trigonométricas en Python (sin 180°, cos 60°, tan 45°, asin 0.5).
     * 4. Manejo robusto de fallos (G3, G4, G6): división por cero, tan 90°, asin 2, sqrt(-16) y operación inexistente.
     * 5. Serie de sin(x) de -360° a 360° cada 15° y serie de tan(x) con soporte de valores nulos (y IS NULL -> float('nan')).
     */
    public void ejecutarDemostracionIntegral() {
        System.out.println("\n==========================================================================");
        System.out.println("   INICIANDO DEMOSTRACIÓN INTEGRAL EXTREMO A EXTREMO (JAVA-PYTHON-SQLITE) ");
        System.out.println("==========================================================================");

        // 1. Operaciones Básicas en Java
        probarCalculo("+", null, 125.5, 74.5);
        probarCalculo("-", null, 90.0, 35.25);
        probarCalculo("*", null, 12.0, 8.5);
        probarCalculo("/", null, 144.0, 12.0);

        // 2. Extensiones Clase 5 (Principio Abierto/Cerrado - OCP)
        probarCalculo("^", null, 2.0, 10.0);
        probarCalculo("sqrt", null, 225.0);
        probarCalculo("%", null, 29.0, 5.0);

        // 3. Funciones Trigonométricas en Python (limpieza de ruido sin 180° = 0)
        probarCalculo("sin", "grados", 180.0);
        probarCalculo("cos", "grados", 60.0);
        probarCalculo("tan", "grados", 45.0);
        probarCalculo("asin", "grados", 0.5);

        // 4. Manejo robusto de fallos (G3, G4, G6): división por cero, tan 90°, asin 2, raíz negativa y op inexistente
        probarCalculo("/", null, 50.0, 0.0);
        probarCalculo("tan", "grados", 90.0);
        probarCalculo("asin", "grados", 2.0);
        probarCalculo("sqrt", null, -16.0);
        probarCalculo("log_inexistente", null, 10.0);

        // 5. Series y Graficación con Matplotlib (Fase 2: sin(x) de -360° a 360° cada 15° + tan(x) con y IS NULL)
        try {
            Path pngSin = directorioBd.resolve("sin_-360_360_paso15.png");
            String outSin = motor.generarPersistirYGraficarSerie(
                "Seno de -360° a 360° cada 15°",
                "sin", -360.0, 360.0, 15.0, "grados", pngSin
            );
            System.out.println("[SERIE OK] Curva sin(x) [-360°, 360°] paso 15° graficada en: " + outSin);

            Path pngTan = directorioBd.resolve("tan_-180_180_paso15.png");
            String outTan = motor.generarPersistirYGraficarSerie(
                "Tangente de -180° a 180° cada 15° (con asíntotas y IS NULL)",
                "tan", -180.0, 180.0, 15.0, "grados", pngTan
            );
            System.out.println("[SERIE OK] Curva tan(x) [-180°, 180°] paso 15° (huecos en -90° y 90°) graficada en: " + outTan);
        } catch (CalculadoraException e) {
            System.out.println("[ERROR SERIE] " + e.getMessage());
        }

        // 6. Mostrar evidencia desde las tablas oficiales de SQLite
        mostrarHistorialReciente(25);
        mostrarSeriesYEstadisticas();
    }

    private void probarCalculo(String simbolo, String modo, double... operandos) {
        try {
            double res = motor.ejecutarYPersistir(simbolo, modo, operandos);
            System.out.printf(Locale.US, "[EXITO exito=1 | %-6s] Resultado = %.6f%n", simbolo, res);
        } catch (CalculadoraException e) {
            System.out.printf("[FALLO CONTROLADO exito=0 | %-15s] Persistido en 'calculo': %s%n",
                simbolo, e.getMessage());
        }
    }

    private double leerDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String linea = scanner.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(linea);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Entrada no numérica ('" + linea + "'). Ingrese un número real válido.");
            }
        }
    }
}
