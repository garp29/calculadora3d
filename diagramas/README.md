# Galería Oficial de Diagramas — La Calculadora (Java ⇄ Python ⇄ SQLite)

> **Asignaturas Integradas:** Bases de Datos & Programación Orientada a Objetos 2 (POO2) — **ESEIT (2026-2)**  
> **Docente:** Luis Fernando Vera Jaimes

Esta carpeta centraliza todos los diagramas normativos del proyecto para su inspección directa en GitHub:

| # | Archivo de Diagrama | Contenido Principal | Materia Líder |
| :-: | :--- | :--- | :--- |
| **01** | [01_Flujograma_1_Operacion_Completa.md](./01_Flujograma_1_Operacion_Completa.md) | Flujograma normativo de una operación completa (Java básica o Python trigonométrica), manejo de errores y persistencia en `calculo`. | Bases de Datos & POO2 |
| **02** | [02_Flujograma_2_Serie_y_Grafica.md](./02_Flujograma_2_Serie_y_Grafica.md) | Flujograma normativo de generación de serie de puntos `(x, y)`, guardado transaccional (`y = NULL`) y graficación con `matplotlib`. | Bases de Datos & POO2 |
| **03** | [03_Diagrama_ER_y_Modelo_Relacional_3FN.md](./03_Diagrama_ER_y_Modelo_Relacional_3FN.md) | Mapeo de Requisitos R1–R9, Diagrama Entidad-Relación (`erDiagram`), Modelo Relacional (`operacion`, `calculo`, `serie`, `punto`) y 3FN. | Bases de Datos |
| **04** | [04_Diagrama_Clases_UML_POO2.md](./04_Diagrama_Clases_UML_POO2.md) | Diagrama de Clases UML (`classDiagram`) con atributos, firmas completas de métodos, interfaz `Operacion`, herencia, polimorfismo, OCP, DAO y excepciones. | POO2 |
| **05** | [05_Arquitectura_y_Graficas_Matplotlib.md](./05_Arquitectura_y_Graficas_Matplotlib.md) | Diagrama de arquitectura de 3 capas (Java ⇄ Python ⇄ SQLite), contratos CLI y curvas PNG generadas desde SQLite. | Integración |

---

## Vista Rápida de todos los Diagramas

### A. Arquitectura General: Java ⇄ Python ⇄ SQLite (Sección 2 del Enunciado)

```mermaid
flowchart LR
    subgraph JAVA ["JAVA (POO2)"]
        J_MENU["Menú y programa principal (Main / MenuConsola)"]
        J_BAS["Operaciones básicas: +  -  *  /\nExtensiones Clase 5 (OCP): ^  sqrt  %"]
        J_POO["Interfaz Operacion, herencia y polimorfismo\nExcepciones (DivisionPorCeroException, OperacionInvalidaException)"]
        J_PUENTE["ProcessBuilder (PythonExecutor) + JDBC (CalculadoraSQLiteDAO)"]
    end

    subgraph PYTHON ["PYTHON (3.10+)"]
        P_TRIG["trig.py: sin cos tan asin acos atan\nModo: grados | radianes"]
        P_GRAF["graficar.py: PNG con matplotlib\n(y IS NULL -> hueco en la curva)"]
    end

    subgraph SQLITE ["SQLITE 3"]
        DB[["Base de Datos: bd/calculadora.db\nTablas de referencia:\noperacion | calculo | serie | punto"]]
    end

    JAVA -- "python trig.py sin 30\nrespuesta: OK;0.5 | ERROR;mensaje" --> PYTHON
    JAVA -- "JDBC: INSERT del cálculo (éxito o fallo)\nSELECT del historial (v_historial_calculos)" --> SQLITE
    PYTHON -- "graficar.py lee los puntos (x, y)\nde las tablas serie y punto" --> SQLITE
```

---

### B. Flujograma 1: La Operación Completa (Sección 4 del Enunciado)

> **Convención de Símbolos (Sección 4):** Óvalo `([...])` para Inicio/Fin, Paralelogramo `[/"..."/]` para Entrada/Salida, Rectángulo `["..."]` para Proceso (inicia con verbo y hace una sola cosa), Rombo `{"¿...?"}` para Decisión (dos salidas `Sí` y `No`), y **Rectángulo con barras laterales `[["..."]]` para la Base de Datos**.

```mermaid
flowchart TD
    F1_INICIO(["Inicio"]) --> F1_IO1[/"Elegir operación y escribir operandos"/]
    F1_IO1 --> F1_P1["Buscar operación en el catálogo"]
    F1_P1 --> F1_D1{"¿Existe la operación?"}

    F1_D1 -- "No" --> F1_E1["Registrar fallo por operación inexistente (exito = 0)"]
    F1_D1 -- "Sí" --> F1_DB1[["Consultar id de operación en la BD (tabla operacion)"]]
    F1_DB1 --> F1_D2{"¿Es operación básica (Java)?"}

    F1_D2 -- "Sí" --> F1_D3{"¿Las entradas básicas son válidas (divisor != 0 y radicando >= 0)?"}
    F1_D3 -- "No" --> F1_E2["Capturar error aritmético en Java (división por cero o raíz negativa)"]
    F1_D3 -- "Sí" --> F1_P2["Calcular resultado en Java mediante polimorfismo"]

    F1_D2 -- "No" --> F1_P3["Invocar trig.py desde Java con ProcessBuilder"]
    F1_P3 --> F1_D4{"¿Python respondió OK;resultado?"}
    F1_D4 -- "No" --> F1_E3["Capturar mensaje ERROR de Python (tan 90° o asin 2)"]
    F1_D4 -- "Sí" --> F1_P4["Extraer resultado numérico devuelto por Python"]

    F1_P2 --> F1_DB2[["Guardar cálculo exitoso en la BD (tabla calculo: exito = 1)"]]
    F1_P4 --> F1_DB2

    F1_E1 --> F1_DB3[["Guardar cálculo fallido en la BD (tabla calculo: exito = 0, resultado = NULL)"]]
    F1_E2 --> F1_DB3
    F1_E3 --> F1_DB3

    F1_DB2 --> F1_IO2[/"Mostrar resultado del cálculo al usuario"/]
    F1_DB3 --> F1_IO3[/"Mostrar mensaje de error al usuario"/]

    F1_IO2 --> F1_FIN(["Fin"])
    F1_IO3 --> F1_FIN
```

---

### C. Flujograma 2: La Serie de Puntos y su Gráfica (Sección 4 del Enunciado)

```mermaid
flowchart TD
    F2_INICIO(["Inicio"]) --> F2_IO1[/"Elegir función trigonométrica, rango (xmin, xmax), paso y modo de ángulo"/]
    F2_IO1 --> F2_P1["Validar parámetros del intervalo y del paso"]
    F2_P1 --> F2_D1{"¿Se cumple xmin < xmax y paso > 0?"}

    F2_D1 -- "No" --> F2_E1["Construir mensaje de error de validación de rango o paso"]
    F2_D1 -- "Sí" --> F2_P2["Pedir serie de puntos (x;y) a Python (trig.py serie)"]

    F2_P2 --> F2_D2{"¿Python generó los puntos correctamente?"}
    F2_D2 -- "No" --> F2_E2["Capturar mensaje de error devuelto por trig.py"]
    F2_D2 -- "Sí" --> F2_P3["Convertir líneas x;y en lista de puntos (incluidos puntos sin valor y = NULL)"]

    F2_P3 --> F2_DB1[["Guardar serie y sus puntos en la BD en una transacción (tablas serie y punto)"]]
    F2_DB1 --> F2_D3{"¿Se guardaron la serie y sus puntos en la BD?"}

    F2_D3 -- "No" --> F2_E3["Deshacer transacción (rollback) y construir error de base de datos"]
    F2_D3 -- "Sí" --> F2_P4["Invocar graficar.py indicando ruta de la BD, serie_id y archivo PNG"]

    F2_P4 --> F2_DB2[["Leer puntos (x, y) de la serie desde la BD (tabla punto)"]]
    F2_DB2 --> F2_P5["Generar gráfica PNG con matplotlib dejando huecos donde y es NULL"]
    F2_P5 --> F2_D4{"¿Se generó el archivo PNG sin errores?"}

    F2_D4 -- "No" --> F2_E4["Capturar error de graficación"]
    F2_D4 -- "Sí" --> F2_IO2[/"Mostrar confirmación y ruta de la gráfica generada"/]

    F2_E1 --> F2_IO3[/"Mostrar mensaje de error al usuario"/]
    F2_E2 --> F2_IO3
    F2_E3 --> F2_IO3
    F2_E4 --> F2_IO3

    F2_IO2 --> F2_FIN(["Fin"])
    F2_IO3 --> F2_FIN
```

---

### D. Diagrama Entidad-Relación (`erDiagram` — 4 Tablas Oficiales)

```mermaid
erDiagram
    OPERACION ||--o{ CALCULO : "clasifica (1:N)"
    OPERACION ||--o{ SERIE : "parametriza (1:N)"
    SERIE ||--|{ PUNTO : "contiene (1:N)"

    OPERACION {
        INTEGER id PK
        TEXT simbolo UK "NOT NULL, UNIQUE"
        TEXT nombre UK "NOT NULL, UNIQUE"
        TEXT tipo "NOT NULL, CHECK IN ('BASICA', 'TRIGONOMETRICA')"
        TEXT lenguaje "NOT NULL, CHECK IN ('JAVA', 'PYTHON')"
        INTEGER aridad "NOT NULL, CHECK IN (1, 2)"
    }

    CALCULO {
        INTEGER id PK
        INTEGER operacion_id FK "NOT NULL -> operacion(id)"
        REAL operando_a "NOT NULL"
        REAL operando_b "NULL (unarias)"
        TEXT modo_angulo "NULL, CHECK IN ('grados', 'radianes') OR IS NULL"
        REAL resultado "NULL (NULL si exito = 0)"
        INTEGER exito "NOT NULL, CHECK IN (0, 1)"
        TEXT mensaje_error "NULL (NOT NULL si exito = 0)"
        TEXT creado_en "NOT NULL, DEFAULT datetime('now', 'localtime')"
    }

    SERIE {
        INTEGER id PK
        TEXT nombre "NOT NULL"
        INTEGER operacion_id FK "NOT NULL -> operacion(id)"
        REAL x_min "NOT NULL"
        REAL x_max "NOT NULL, CHECK (x_min < x_max)"
        REAL paso "NOT NULL, CHECK (paso > 0)"
        TEXT modo_angulo "NOT NULL, CHECK IN ('grados', 'radianes')"
        TEXT creado_en "NOT NULL, DEFAULT datetime('now', 'localtime')"
    }

    PUNTO {
        INTEGER id PK
        INTEGER serie_id FK "NOT NULL -> serie(id) ON DELETE CASCADE"
        REAL x "NOT NULL, UNIQUE (serie_id, x)"
        REAL y "NULL (NULL cuando la función no existe en x)"
    }
```

---

### E. Diagrama de Clases UML con Atributos y Firmas de Métodos (POO2)

```mermaid
classDiagram
    direction TB

    class Operacion {
        <<interface>>
        +getSimbolo() String
        +getNombre() String
        +getTipo() String
        +getLenguaje() String
        +getAridad() int
        +ejecutar(operandos: double...) double
    }

    class OperacionAbstracta {
        <<abstract>>
        -simbolo: String
        -nombre: String
        -tipo: String
        -lenguaje: String
        -aridad: int
        +getSimbolo() String
        +getNombre() String
        +getTipo() String
        +getLenguaje() String
        +getAridad() int
        #validarOperandos(operandos: double...) void
    }

    class OperacionBinaria {
        <<abstract>>
        +ejecutar(operandos: double...) double
        #calcularBinario(operandoA: double, operandoB: double)* double
    }

    class OperacionUnaria {
        <<abstract>>
        +ejecutar(operandos: double...) double
        #calcularUnario(operandoA: double)* double
    }

    class OperacionPython {
        -pythonExecutor: PythonExecutor
        -modoAngulo: String
        +getModoAngulo() String
        +setModoAngulo(modoAngulo: String) void
        #calcularUnario(operandoA: double) double
    }

    class Suma {
        #calcularBinario(operandoA: double, operandoB: double) double
    }
    class Resta {
        #calcularBinario(operandoA: double, operandoB: double) double
    }
    class Multiplicacion {
        #calcularBinario(operandoA: double, operandoB: double) double
    }
    class Division {
        #calcularBinario(operandoA: double, operandoB: double) double
    }
    class Potencia {
        #calcularBinario(base: double, exponente: double) double
    }
    class RaizCuadrada {
        #calcularUnario(operandoA: double) double
    }
    class Modulo {
        #calcularBinario(operandoA: double, operandoB: double) double
    }

    class PythonExecutor {
        -ejecutablePython: String
        -rutaTrigPy: Path
        -rutaGraficarPy: Path
        -directorioTrabajo: File
        +evaluarFuncionPuntual(funcion: String, valor: double, modoAngulo: String) double
        +generarSeriePuntos(funcion: String, xmin: double, xmax: double, paso: double, modoAngulo: String) List~PuntoSerie~
        +graficarSerieDesdeSQLite(rutaDb: Path, serieId: int, rutaSalidaPng: Path) String
    }

    class CalculadoraDAO {
        <<interface>>
        +obtenerIdOperacionPorSimbolo(simbolo: String) int
        +guardarCalculo(registro: RegistroCalculo) int
        +consultarHistorialReciente(limite: int) List~RegistroCalculo~
        +guardarSerieYPuntos(nombreSerie: String, operacionId: int, xMin: double, xMax: double, paso: double, modoAngulo: String, puntos: List~PuntoSerie~) int
        +consultarResumenSeries() List~SerieTrigonometrica~
        +consultarEstadisticasOperaciones() List~String~
    }

    class CalculadoraSQLiteDAO {
        -conexionSQLite: ConexionSQLite
    }

    class ConexionSQLite {
        -rutaBaseDatos: Path
        -urlJdbc: String
        +obtenerConexion() Connection
    }

    class MotorCalculadora {
        -catalogoPolimorfico: Map~String, Operacion~
        -dao: CalculadoraDAO
        -pythonExecutor: PythonExecutor
        -rutaBaseDatos: Path
        +registrarOperacion(operacion: Operacion) void
        +obtenerOperacion(simboloONombre: String) Operacion
        +ejecutarYPersistir(simboloOperacion: String, modoAngulo: String, operandos: double...) double
        +generarPersistirYGraficarSerie(nombreSerie: String, funcion: String, xMin: double, xMax: double, paso: double, modoAngulo: String, rutaSalidaPng: Path) String
    }

    class MenuConsola {
        -motor: MotorCalculadora
        -directorioBd: Path
        +iniciarMenuInteractivo() void
        +ejecutarDemostracionIntegral() void
    }

    class CalculadoraException {
        -codigoOperacion: String
        +getCodigoOperacion() String
    }
    class OperacionInvalidaException
    class DivisionPorCeroException
    class ProcesoPythonException
    class PersistenciaException

    Operacion <|.. OperacionAbstracta
    OperacionAbstracta <|-- OperacionBinaria
    OperacionAbstracta <|-- OperacionUnaria
    OperacionUnaria <|-- OperacionPython
    OperacionBinaria <|-- Suma
    OperacionBinaria <|-- Resta
    OperacionBinaria <|-- Multiplicacion
    OperacionBinaria <|-- Division
    OperacionBinaria <|-- Potencia
    OperacionBinaria <|-- Modulo
    OperacionUnaria <|-- RaizCuadrada
    OperacionPython --> PythonExecutor

    CalculadoraDAO <|.. CalculadoraSQLiteDAO
    CalculadoraSQLiteDAO --> ConexionSQLite

    CalculadoraException <|-- OperacionInvalidaException
    OperacionInvalidaException <|-- DivisionPorCeroException
    CalculadoraException <|-- ProcesoPythonException
    CalculadoraException <|-- PersistenciaException

    MotorCalculadora o-- Operacion
    MotorCalculadora --> CalculadoraDAO
    MotorCalculadora --> PythonExecutor
    MenuConsola --> MotorCalculadora
```

---

### F. Gráficas Generadas desde SQLite (`matplotlib`)

#### 1. Serie de $\sin(x)$ de $-360^\circ$ a $360^\circ$ cada $15^\circ$ (Requisito R5 y Clase 4)
![Curva de sin(x) de -360 a 360 cada 15 grados](./img/sin_-360_360_paso15.png)

#### 2. Serie de $\tan(x)$ de $-180^\circ$ a $180^\circ$ cada $15^\circ$ con huecos en asíntotas `y IS NULL` (Requisito R6)
![Curva de tan(x) con discontinuidades en -90 y 90 grados](./img/tan_-180_180_paso15.png)
