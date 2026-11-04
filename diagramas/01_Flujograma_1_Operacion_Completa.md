# 01. Flujograma 1: La Operación Completa

[Volver a la Galería de Diagramas](./README.md)

---

## Reglas del Flujograma Verificadas (Sección 4 del Enunciado Maestro)
- **Óvalo (`([...])`):** Un solo `Inicio` y un `Fin` donde convergen todos los caminos.
- **Paralelogramo (`[/"..."/]`):** Entrada de operandos y salida en pantalla.
- **Rectángulo (`["..."]`):** Cada caja de proceso empieza con un verbo y ejecuta una sola acción.
- **Rombo (`{"¿...?"}`):** Cada decisión tiene exactamente dos salidas rotuladas **`Sí`** y **`No`**.
- **Rectángulo con barras laterales (`[["..."]]`):** Operaciones de lectura/escritura sobre la base de datos `bd/calculadora.db`.

---

## Diagrama Normativo

```mermaid
flowchart TD
    F1_INICIO(["Inicio"]) --> F1_IO1[/"Elegir operación y escribir operandos"/]
    F1_IO1 --> F1_P1["Buscar operación en el catálogo"]
    F1_P1 --> F1_D1{"¿Existe la operación?"}

    F1_D1 -- "No" --> F1_E1["Registrar fallo por operación inexistente (exito = 0)"]
    F1_D1 -- "Sí" --> F1_DB1[["Consultar id de operación en la BD (tabla operacion)"]]
    F1_DB1 --> F1_D2{"¿Es operación básica (Java)?"}

    %% Rama Java (BASICA)
    F1_D2 -- "Sí" --> F1_D3{"¿Es división (/) o módulo (%)?"}
    F1_D3 -- "Sí" --> F1_D4{"¿El divisor operando_b es igual a 0?"}
    F1_D4 -- "Sí" --> F1_E2["Lanzar DivisionPorCeroException (exito = 0)"]
    F1_D4 -- "No" --> F1_P2["Calcular resultado aritmético en Java"]

    F1_D3 -- "No" --> F1_D5{"¿Es raíz cuadrada con operando_a < 0?"}
    F1_D5 -- "Sí" --> F1_E3["Lanzar OperacionInvalidaException por radicando negativo (exito = 0)"]
    F1_D5 -- "No" --> F1_P2

    %% Rama Python (TRIGONOMETRICA)
    F1_D2 -- "No" --> F1_P3["Invocar trig.py mediante ProcessBuilder con List<String>"]
    F1_P3 --> F1_D6{"¿Salida de trig.py inicia con OK;?"}
    F1_D6 -- "No" --> F1_E4["Lanzar OperacionInvalidaException con mensaje de Python (tan 90°, asin 2)"]
    F1_D6 -- "Sí" --> F1_P4["Convertir valor limpio de Python a número real"]

    %% Persistencia y Salida
    F1_P2 --> F1_P5["Preparar registro de cálculo exitoso (exito = 1, mensaje_error = NULL)"]
    F1_P4 --> F1_P5

    F1_E1 --> F1_DB2[["Guardar cálculo fallido en la BD (tabla calculo: exito = 0, resultado = NULL)"]]
    F1_E2 --> F1_DB2
    F1_E3 --> F1_DB2
    F1_E4 --> F1_DB2

    F1_P5 --> F1_DB3[["Guardar cálculo exitoso en la BD (tabla calculo: exito = 1, resultado = valor)"]]

    F1_DB3 --> F1_IO2[/"Mostrar resultado numérico al usuario"/]
    F1_DB2 --> F1_IO3[/"Mostrar mensaje de error controlado al usuario"/]

    F1_IO2 --> F1_FIN(["Fin"])
    F1_IO3 --> F1_FIN
```

---

## Conexión Flujograma 1 $\rightarrow$ Diagrama de Clases $\rightarrow$ Base de Datos

| Caja del Flujograma 1 | Método Candidato en el Diagrama de Clases (POO2) | Tabla y Columnas en SQLite (`bd/calculadora.db`) |
| :--- | :--- | :--- |
| Buscar operación en el catálogo | `MotorCalculadora.obtenerOperacion(simboloONombre: String): Operacion` | `operacion (simbolo, nombre)` |
| Consultar `id` de operación en la BD | `CalculadoraSQLiteDAO.obtenerIdOperacionPorSimbolo(simbolo: String): int` | `operacion (id)` |
| Calcular resultado aritmético en Java | `Operacion.ejecutar(operandos: double...): double`<br>`OperacionBinaria.calcularBinario(a: double, b: double): double`<br>`OperacionUnaria.calcularUnario(a: double): double` | *(En memoria Java)* |
| Invocar `trig.py` mediante `ProcessBuilder` | `OperacionPython.calcularUnario(operandoA: double): double`<br>`PythonExecutor.evaluarFuncionPuntual(fn: String, val: double, modo: String): double` | *(Contrato CLI `OK;<res>` / `ERROR;<msg>`)* |
| Guardar cálculo exitoso o fallido en la BD | `MotorCalculadora.ejecutarYPersistir(simbolo: String, modo: String, operandos: double...): double`<br>`CalculadoraSQLiteDAO.guardarCalculo(registro: RegistroCalculo): int` | `calculo (operacion_id, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error, creado_en)` |
