# 05. Arquitectura Java ⇄ Python ⇄ SQLite y Gráficas Generadas

[Volver a la Galería de Diagramas](./README.md)

---

## 1. Diagrama de Arquitectura y Contratos (Sección 2 del Enunciado Maestro)

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

| Flecha | Cómo se comunican | Qué viaja |
| :--- | :--- | :--- |
| **Java $\rightarrow$ Python** | Java arranca un proceso con `ProcessBuilder` y le pasa una `List<String>` de argumentos (seguro ante rutas con espacios en Windows). | `python trig.py sin 30` $\rightarrow$ una línea de respuesta: `OK;0.5` o `ERROR;mensaje` |
| **Java $\rightarrow$ SQLite** | JDBC con el controlador `sqlite-jdbc` y `PreparedStatement`. | `INSERT` del cálculo (éxito o fallo), `INSERT` transaccional de `serie`/`punto` y `SELECT` del historial |
| **Python $\rightarrow$ SQLite** | `graficar.py` abre `bd/calculadora.db` y lee los puntos `(x, y)` de la tabla `punto`. | Puntos `(x, y)` (con `y IS NULL` $\rightarrow$ `float('nan')`) $\rightarrow$ archivo PNG |

---

## 2. Gráficas Reales Generadas Leyendo de SQLite (`bd/calculadora.db`)

### 2.1. Curva de $\sin(x)$ de $-360^\circ$ a $360^\circ$ cada $15^\circ$ (49 puntos definidos)
![Curva de sin(x) de -360 a 360 cada 15 grados](./img/sin_-360_360_paso15.png)

### 2.2. Curva de $\tan(x)$ de $-180^\circ$ a $180^\circ$ cada $15^\circ$ (23 puntos definidos y 2 asíntotas `y IS NULL` en $-90^\circ$ y $90^\circ$)
![Curva de tan(x) de -180 a 180 cada 15 grados](./img/tan_-180_180_paso15.png)

### 2.3. Serie de Prueba Hito 1 (`pruebas.sql` — $\tan(x)$ de $0^\circ$ a $180^\circ$ cada $45^\circ$ con hueco en $90^\circ$)
![Serie de prueba Hito 1 tan(x)](./img/serie_tan_demo.png)
