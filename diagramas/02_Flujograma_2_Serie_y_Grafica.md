# 02. Flujograma 2: La Serie de Puntos y su Gráfica

[Volver a la Galería de Diagramas](./README.md)

---

## Diagrama Normativo

```mermaid
flowchart TD
    F2_INICIO(["Inicio"]) --> F2_IO1[/"Elegir función trigonométrica, rango (xmin, xmax), paso y modo de ángulo"/]
    F2_IO1 --> F2_P1["Verificar que la operación seleccionada sea de tipo TRIGONOMETRICA"]
    F2_P1 --> F2_D1{"¿La operación es trigonométrica?"}

    F2_D1 -- "No" --> F2_E1["Construir error de tipo de operación inválido para serie"]
    F2_D1 -- "Sí" --> F2_D2{"¿Se cumple xmin < xmax?"}

    F2_D2 -- "No" --> F2_E2["Construir error de rango inválido (xmin >= xmax)"]
    F2_D2 -- "Sí" --> F2_D3{"¿Se cumple paso > 0?"}

    F2_D3 -- "No" --> F2_E3["Construir error de paso no positivo (paso <= 0)"]
    F2_D3 -- "Sí" --> F2_P2["Pedir serie de puntos x;y a Python (trig.py serie)"]

    F2_P2 --> F2_D4{"¿trig.py devolvió líneas x;y válidas?"}
    F2_D4 -- "No" --> F2_E4["Capturar mensaje de error devuelto por trig.py"]
    F2_D4 -- "Sí" --> F2_P3["Convertir líneas x;y en lista de objetos PuntoSerie (y vacío -> null)"]

    F2_P3 --> F2_DB1[["Guardar serie y sus puntos en la BD en una transacción (tablas serie y punto)"]]
    F2_DB1 --> F2_D5{"¿La transacción en SQLite fue exitosa?"}
    F2_D5 -- "No" --> F2_E5["Deshacer transacción (rollback) y construir error de persistencia"]
    F2_D5 -- "Sí" --> F2_P4["Invocar graficar.py pasando ruta_db, serie_id y salida.png"]

    F2_P4 --> F2_DB2[["Leer puntos (x, y) desde la BD (tabla punto ordenados por x ASC)"]]
    F2_DB2 --> F2_P5["Transformar puntos sin valor (y IS NULL) en float('nan') y graficar con matplotlib"]
    F2_P5 --> F2_D6{"¿graficar.py respondió OK;<ruta.png>?"}

    F2_D6 -- "No" --> F2_E6["Capturar error de generación gráfica"]
    F2_D6 -- "Sí" --> F2_IO2[/"Mostrar confirmación y ruta del archivo PNG generado"/]

    F2_E1 --> F2_IO3[/"Mostrar mensaje de error al usuario"/]
    F2_E2 --> F2_IO3
    F2_E3 --> F2_IO3
    F2_E4 --> F2_IO3
    F2_E5 --> F2_IO3
    F2_E6 --> F2_IO3

    F2_IO2 --> F2_FIN(["Fin"])
    F2_IO3 --> F2_FIN
```

---

## Conexión Flujograma 2 $\rightarrow$ Diagrama de Clases $\rightarrow$ Base de Datos

| Caja del Flujograma 2 | Método Candidato en el Diagrama de Clases (POO2 / Python) | Tabla y Columnas en SQLite (`bd/calculadora.db`) |
| :--- | :--- | :--- |
| Validar `xmin < xmax`, `paso > 0` y tipo `TRIGONOMETRICA` | `MotorCalculadora.generarPersistirYGraficarSerie(...): String` | `serie (CHECK (x_min < x_max), CHECK (paso > 0))` |
| Pedir serie de puntos `x;y` a Python | `PythonExecutor.generarSeriePuntos(fn: String, xmin: double, xmax: double, paso: double, modo: String): List<PuntoSerie>` | *(Contrato CLI `x;y` con `y` vacío en asíntotas)* |
| Guardar serie y sus puntos en la BD (incluidos `y = NULL`) | `CalculadoraSQLiteDAO.guardarSerieYPuntos(nombre: String, operacionId: int, xMin: double, xMax: double, paso: double, modo: String, puntos: List<PuntoSerie>): int` | `serie (id, nombre, operacion_id, x_min, x_max, paso, modo_angulo, creado_en)`<br>`punto (id, serie_id, x, y)` |
| Leer puntos `(x, y)` de la BD y generar PNG | `PythonExecutor.graficarSerieDesdeSQLite(rutaDb: Path, serieId: int, rutaSalidaPng: Path): String`<br>`graficar.py -> graficar_serie(...)` | `serie (SELECT)` y `punto (SELECT x, y WHERE serie_id = ? ORDER BY x ASC)` |
