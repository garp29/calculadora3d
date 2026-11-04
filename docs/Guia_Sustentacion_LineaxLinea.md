# Guía de Sustentación Oral y Defensa Línea por Línea
**Proyecto Integrado:** "La Calculadora — Java, Python y SQLite" (ESEIT)  
**Asignaturas:** Bases de Datos & Programación Orientada a Objetos 2 (POO2)  
**Propósito:** Permitir a cualquier integrante del equipo defender en vivo cada archivo `.sql`, `.py` y `.java`, explicar las decisiones de diseño 3FN y POO/OCP, y realizar modificaciones en vivo frente al jurado sin depender de IA.

---

## 1. Comandos Oficiales de Compilación y Ejecución en Windows (UTF-8)

Desde la carpeta raíz `calculadora/` en PowerShell:

### 1.1. Ejecutar todos los scripts SQL (`create_schema.sql`, `seed.sql`, `pruebas.sql`, `consultas.sql`)
```powershell
python bd/sql.py --init
```
También puedes ejecutar cualquier script individualmente:
```powershell
python bd/sql.py bd/create_schema.sql
python bd/sql.py bd/seed.sql
python bd/sql.py bd/pruebas.sql
python bd/sql.py bd/consultas.sql
```

### 1.2. Probar `python/trig.py` y `python/graficar.py` por separado
```powershell
# Evaluación puntual (ruido numérico limpio: sin 180 -> OK;0)
python python/trig.py sin 180 --modo grados

# Errores controlados de dominio / asíntotas
python python/trig.py tan 90 --modo grados
python python/trig.py asin 2 --modo grados

# Serie de puntos de sin(x) de -360° a 360° cada 15°
python python/trig.py serie sin -360 360 15 --modo grados

# Graficar una serie existente en bd/calculadora.db
python python/graficar.py bd/calculadora.db 1 bd/serie_tan_demo.png
```

### 1.3. Compilar y Ejecutar Java con `sqlite-jdbc` en Windows
```powershell
# Compilar en carpeta out/ con codificación UTF-8
$sources = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp "lib\*" -d out $sources

# Ejecutar batería completa de demostración automática (--demo)
java "-Dfile.encoding=UTF-8" -cp "out;lib\*" calculadora.Main --demo

# Ejecutar menú interactivo por consola
java "-Dfile.encoding=UTF-8" -cp "out;lib\*" calculadora.Main
```

---

## 2. Explicación Línea por Línea del SQL (`bd/`)

### 2.1. `bd/create_schema.sql`
1. **`PRAGMA foreign_keys = ON;`**:
   - SQLite desactiva las llaves foráneas por defecto al abrir una conexión. Esta instrucción activa el motor de integridad referencial para que `REFERENCES operacion(id)` y `ON DELETE CASCADE` se cumplan estrictamente.
2. **Tabla `operacion` (Requisitos R1, R2, R9)**:
   ```sql
   CREATE TABLE operacion (
       id       INTEGER PRIMARY KEY AUTOINCREMENT,
       simbolo  TEXT    NOT NULL UNIQUE CHECK (length(trim(simbolo)) > 0),
       nombre   TEXT    NOT NULL UNIQUE CHECK (length(trim(nombre)) > 0),
       tipo     TEXT    NOT NULL CHECK (tipo IN ('BASICA', 'TRIGONOMETRICA')),
       lenguaje TEXT    NOT NULL CHECK (lenguaje IN ('JAVA', 'PYTHON')),
       aridad   INTEGER NOT NULL CHECK (aridad IN (1, 2)),
       CONSTRAINT chk_operacion_coherencia_lenguaje CHECK (
           (lenguaje = 'PYTHON' AND tipo = 'TRIGONOMETRICA' AND aridad = 1) OR
           (lenguaje = 'JAVA'   AND tipo = 'BASICA'         AND aridad IN (1, 2))
       )
   );
   ```
   - **Qué explicar al profesor:**
     - `simbolo` y `nombre` son `UNIQUE` y `NOT NULL` con validación de longitud para evitar cadenas vacías.
     - `chk_operacion_coherencia_lenguaje` es una restricción `CHECK` a nivel de tabla (multi-columna): garantiza que ninguna operación en `'PYTHON'` pueda registrarse como `'BASICA'` ni con `aridad = 2`.
3. **Tabla `calculo` (Requisitos R3, R4, R8)**:
   ```sql
   CONSTRAINT chk_calculo_coherencia_exito CHECK (
       (exito = 1 AND resultado IS NOT NULL AND mensaje_error IS NULL) OR
       (exito = 0 AND resultado IS NULL     AND mensaje_error IS NOT NULL AND length(trim(mensaje_error)) > 0)
   )
   ```
   - **Qué explicar al profesor:**
     - Esta restricción impide estados inconsistentes en la auditoría: si el cálculo tuvo éxito (`exito = 1`), es obligatorio que exista `resultado` y que `mensaje_error` sea `NULL`. Si falló (`exito = 0`, p. ej. en `10 / 0`, `tan 90°` o `asin 2`), `resultado` debe ser `NULL` y `mensaje_error` debe explicar la causa del fallo.
4. **Tabla `serie` (Requisito R5)**:
   - `CONSTRAINT chk_serie_rango_valido CHECK (x_min < x_max)`: impide intervalos invertidos o de longitud cero.
   - `CONSTRAINT chk_serie_paso_positivo CHECK (paso > 0)`: impide pasos nulos o negativos que causarían lazos infinitos.
5. **Tabla `punto` (Requisitos R6, R7)**:
   - `y REAL NULL`: permite almacenar `NULL` cuando la función es indefinida en $x$ (ej. $\tan 90^\circ$).
   - `UNIQUE (serie_id, x)`: evita que para una misma serie se inserte dos veces el mismo valor de $x$.
   - `FOREIGN KEY (serie_id) REFERENCES serie(id) ON UPDATE CASCADE ON DELETE CASCADE`: si se borra una fila de `serie`, se borran automáticamente todos sus puntos en `punto`.

---

## 3. Explicación Línea por Línea de Python (`python/`)

### 3.1. `python/trig.py`
- **Limpieza de ruido numérico (`limpiar_numero`)**:
  ```python
  if abs(valor) < 1e-12:
      return 0.0
  ```
  Como $\pi$ en binario es una aproximación finita, `math.sin(math.radians(180))` da `1.2246467991473532e-16`. Al aplicar el umbral $10^{-12}$, `trig.py` imprime exactamente `OK;0`.
- **Detección de asíntotas en `tan` (`es_asintota_tangente`)**:
  ```python
  residuo = (valor - 90.0) % 180.0
  if abs(residuo) < 1e-9 or abs(residuo - 180.0) < 1e-9:
      return True
  ```
  Detecta cualquier múltiplo de $90^\circ + k \cdot 180^\circ$ (incluyendo ángulos negativos como $-90^\circ, -270^\circ$ y positivos como $90^\circ, 270^\circ$) y devuelve `ERROR;Tangente indefinida...` en modo puntual o `x;` (con `y` vacío) en modo `serie`.
- **Validación de dominio en `asin` y `acos`**:
  Verifica `valor < -1.0 - 1e-12 or valor > 1.0 + 1e-12`. Para `asin 2`, imprime `ERROR;Dominio inválido para asin(2): el argumento debe pertenecer al intervalo [-1, 1]`.

### 3.2. `python/graficar.py`
- Consulta SQLite con `SELECT x, y FROM punto WHERE serie_id = ? ORDER BY x ASC`.
- Cuando `y_val is None` (es decir, `y IS NULL` en SQLite), inserta `float("nan")` en la lista `ys_plot`:
  ```python
  if y_val is None:
      xs_plot.append(x_float)
      ys_plot.append(float("nan"))
      xs_nulos.append(x_float)
  ```
  `matplotlib` interpreta `float('nan')` como una interrupción de segmento, dejando el hueco exacto en la discontinuidad sin trazar líneas verticales falsas.

---

## 4. Explicación Línea por Línea de Java (`src/calculadora/`)

### 4.1. Jerarquía Polimórfica y Principio Abierto/Cerrado (`calculadora.dominio`)
- **`Operacion` (Interfaz)**: Define el contrato `getSimbolo()`, `getNombre()`, `getTipo()`, `getLenguaje()`, `getAridad()` y `ejecutar(double... operandos)`.
- **`OperacionAbstracta`**: Implementa los *getters* y el método `validarOperandos(double... operandos)` que verifica que la cantidad de números recibidos coincida con `aridad`.
- **`OperacionBinaria` / `OperacionUnaria` / `OperacionPython`**:
  - `OperacionBinaria` fija `tipo = "BASICA"`, `lenguaje = "JAVA"`, `aridad = 2` y delega en `calcularBinario(a, b)`.
  - `OperacionUnaria` fija `aridad = 1` y delega en `calcularUnario(a)`.
  - `OperacionPython` hereda de `OperacionUnaria`, fija `tipo = "TRIGONOMETRICA"`, `lenguaje = "PYTHON"` y delega `calcularUnario(a)` a `pythonExecutor.evaluarFuncionPuntual(getSimbolo(), operandoA, modoAngulo)`.

### 4.2. Puente Seguro para Rutas con Espacios en Windows (`PythonExecutor.java` — G1)
```java
List<String> comando = List.of(
    ejecutablePython,
    rutaTrigPy.toAbsolutePath().toString(),
    simbolo,
    formatearDoublePlano(valor),
    "--modo",
    modo
);
ProcessBuilder pb = new ProcessBuilder(comando);
```
- **Por qué cumple G1:** Nunca se concatena `"python " + rutaTrigPy + ...` en un solo `String`. Al pasar `List<String>` a `ProcessBuilder`, el sistema operativo Windows entrega cada elemento del arreglo `argv` directamente al proceso hijo sin que los espacios en rutas como `OneDrive\Escritorio\calculadora` rompan el comando.

### 4.3. Transacciones Atómicas en SQLite (`CalculadoraSQLiteDAO.guardarSerieYPuntos`)
```java
conn.setAutoCommit(false);
try {
    // 1. INSERT INTO serie ... (obtiene serieId con RETURN_GENERATED_KEYS)
    // 2. PreparedStatement psPt: si punto.getY() == null -> psPt.setNull(3, Types.REAL); psPt.addBatch();
    // 3. psPt.executeBatch();
    conn.commit();
    return serieId;
} catch (Exception e) {
    conn.rollback();
    throw new PersistenciaException("Transacción de serie revertida (rollback): " + e.getMessage(), e);
} finally {
    conn.setAutoCommit(true);
}
```
- **Qué explicar al profesor:** Si una serie de 49 puntos falla en el punto #30 (por ejemplo, por un `UNIQUE` duplicado), `conn.rollback()` deshace tanto los 29 puntos previos como la cabecera en `serie`, evitando series huérfanas o incompletas en la base de datos.

---

## 5. Cómo Resolver Modificaciones en Vivo Durante la Sustentación (Sin IA)

### Caso Práctico A: "Agreguen en vivo la operación Hipotenusa (`hypot(a, b)`) o Logaritmo (`log`)"
1. **Paso 1 (Base de Datos - 3FN):** No tocas `create_schema.sql`. Solo agregas un `INSERT` en `bd/seed.sql`:
   ```sql
   INSERT OR IGNORE INTO operacion (simbolo, nombre, tipo, lenguaje, aridad)
   VALUES ('hypot', 'Hipotenusa', 'BASICA', 'JAVA', 2);
   ```
   Y ejecutas `python bd/sql.py bd/seed.sql`.
2. **Paso 2 (Clase Java - OCP):** Creas `src/calculadora/dominio/Hipotenusa.java` extendiendo `OperacionBinaria`:
   ```java
   package calculadora.dominio;
   public class Hipotenusa extends OperacionBinaria {
       public Hipotenusa() { super("hypot", "Hipotenusa"); }
       @Override
       protected double calcularBinario(double a, double b) { return Math.hypot(a, b); }
   }
   ```
3. **Paso 3 (Registro):** Agregas `registrarOperacion(new Hipotenusa());` en `MotorCalculadora.java` y recompilas. Ninguna otra clase cambia.

### Caso Práctico B: "Cambien la restricción de `serie` para que `paso` sea mínimo `0.01`"
1. Abres `bd/create_schema.sql` en la tabla `serie`:
   Cambias `CONSTRAINT chk_serie_paso_positivo CHECK (paso > 0)` por:
   ```sql
   CONSTRAINT chk_serie_paso_positivo CHECK (paso >= 0.01)
   ```
2. Ejecutas `python bd/sql.py --init` para recrear el esquema y verificar las pruebas.

---

## 6. Tabla Oficial de Bitácora de IA

| Fecha / clase | Herramienta | Qué pedimos (resumen) | Qué aceptamos y qué cambiamos | Cómo lo verificamos |
| :--- | :--- | :--- | :--- | :--- |
| **Clase 1 (Hito 1 — Flujogramas y Modelo E-R / 3FN)** | Asistente IA (Gemini) | Generar los 2 flujogramas normativos y el DDL de las 4 tablas oficiales (`operacion`, `calculo`, `serie`, `punto`) con restricciones R1–R9. | **Aceptamos:** Las 4 tablas en 3FN y sus claves foráneas.<br>**Cambiamos:** Ajustamos los rombos de los flujogramas para tener únicamente salidas `Sí` y `No`, y añadimos los `CHECK` compuestos en `operacion` (`PYTHON` $\Rightarrow$ `TRIGONOMETRICA` y `aridad = 1`) y en `calculo` (`exito = 1` $\Leftrightarrow$ `resultado IS NOT NULL AND mensaje_error IS NULL`). | Ejecutamos `python bd/sql.py bd/create_schema.sql` y `python bd/sql.py bd/pruebas.sql` comprobando en terminal los casos válidos y los rechazos esperados. |
| **Clase 2 (Semilla y Consultas Analíticas)** | Asistente IA (Gemini) | Escribir `bd/seed.sql` y `bd/consultas.sql` con la vista `v_historial_calculos`, `JOIN`, `GROUP BY` e índices. | **Aceptamos:** La inserción de las operaciones y los índices sobre claves foráneas y fechas.<br>**Cambiamos:** Añadimos consultas de conteo de puntos nulos (`COUNT(p.id) - COUNT(p.y)`) y agrupación por `exito` (`1` vs `0`). | Ejecutamos `python bd/sql.py bd/consultas.sql` y revisamos las tablas formateadas en consola. |
| **Clase 3 (`trig.py` y `graficar.py`)** | Asistente IA (Gemini) | Implementar `python/trig.py` y `python/graficar.py` según contrato CLI. | **Aceptamos:** La estructura de funciones trigonométricas y graficación con `matplotlib`.<br>**Cambiamos:** Agregamos limpieza de ruido `abs(valor) < 1e-12 -> 0.0` (para que `sin 180` dé `0`), soporte de argumentos negativos en CLI y conversión de `y IS NULL` a `float('nan')` en `graficar.py`. | Ejecutamos `python python/trig.py sin 180`, `python python/trig.py tan 90`, `python python/trig.py asin 2` y generamos las gráficas PNG desde `bd/calculadora.db`. |
| **Clases 4, 5 y 6 (Java POO2, `PythonExecutor`, DAO y Extensiones OCP)** | Asistente IA (Gemini) | Implementar el paquete `calculadora` con `Operacion`, `OperacionBinaria`, `OperacionUnaria`, `OperacionPython`, `PythonExecutor`, `CalculadoraSQLiteDAO` y extensiones `^`, `sqrt`, `%`. | **Aceptamos:** La jerarquía de herencia, polimorfismo y excepciones personalizadas (`CalculadoraException`, `DivisionPorCeroException`, `OperacionInvalidaException`).<br>**Cambiamos:** Garantizamos que `ProcessBuilder` use `List<String>` para rutas con espacios en Windows (G1) y que todos los errores (incluida operación inexistente) se guarden en `calculo` con `exito = 0` (G3–G6). | Compilamos con `javac -encoding UTF-8 -cp "lib\*" -d out ...` y corrimos `java "-Dfile.encoding=UTF-8" -cp "out;lib\*" calculadora.Main --demo`. |
