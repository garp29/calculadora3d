#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
PROYECTO INTEGRADO: LA CALCULADORA — JAVA, PYTHON Y SQLITE (ESEIT)
Archivo: python/graficar.py
Descripción: Graficador desacoplado desde SQLite con Matplotlib.

Contrato CLI:
    python graficar.py <ruta_db> <serie_id> <salida.png>

Comportamiento:
1. Conecta a la base de datos SQLite en `<ruta_db>`.
2. Consulta la cabecera en `serie` (uniendo con `operacion`) y los puntos `(x, y)`
   en `punto` para `<serie_id>` ordenados por `x ASC`.
3. Transforma los registros con `y IS NULL` (Python `None`) en `float('nan')`,
   de modo que `matplotlib` deje huecos/discontinuidades reales en la curva
   (ej. asíntotas de tan en 90° + k·180°).
4. Genera y guarda el gráfico en `<salida.png>` e imprime:
   OK;<ruta_salida>
   o
   ERROR;<mensaje>
"""

import math
import sqlite3
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")  # Backend sin ventana para ejecución segura desde Java ProcessBuilder
import matplotlib.pyplot as plt

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")


def graficar_serie(ruta_db: Path, serie_id: int, ruta_salida: Path) -> None:
    if not ruta_db.exists():
        raise FileNotFoundError(f"No existe la base de datos SQLite en: {ruta_db}")

    conn = sqlite3.connect(str(ruta_db))
    conn.execute("PRAGMA foreign_keys = ON;")
    cursor = conn.cursor()

    try:
        # 1. Consultar metadatos de la serie en la tabla oficial `serie` unida con `operacion`
        cursor.execute(
            """
            SELECT s.nombre, o.simbolo, s.x_min, s.x_max, s.paso, s.modo_angulo
            FROM serie s
            INNER JOIN operacion o ON s.operacion_id = o.id
            WHERE s.id = ?
            """,
            (serie_id,),
        )
        cabecera = cursor.fetchone()
        if cabecera is None:
            raise ValueError(f"No existe la serie con id={serie_id} en la tabla 'serie'")

        nombre_serie, funcion, xmin, xmax, paso, modo_angulo = cabecera

        # 2. Consultar los puntos (x, y) de la tabla oficial `punto`
        cursor.execute(
            """
            SELECT x, y
            FROM punto
            WHERE serie_id = ?
            ORDER BY x ASC
            """,
            (serie_id,),
        )
        filas = cursor.fetchall()
        if not filas:
            raise ValueError(f"La serie id={serie_id} no contiene puntos registrados en la tabla 'punto'")

        xs_plot: list[float] = []
        ys_plot: list[float] = []
        xs_validos: list[float] = []
        ys_validos: list[float] = []
        xs_nulos: list[float] = []

        prev_y: float | None = None
        for x_val, y_val in filas:
            x_float = float(x_val)
            if y_val is None:
                # y IS NULL -> float('nan') genera un hueco/discontinuidad en matplotlib
                xs_plot.append(x_float)
                ys_plot.append(float("nan"))
                xs_nulos.append(x_float)
                prev_y = None
            else:
                y_float = float(y_val)
                # Si es tangente y dos puntos consecutivos saltan de +inf a -inf sin caer exactamente
                # sobre el punto nulo, insertar NaN intermedio para evitar trazos verticales falsos
                if (
                    funcion.lower() == "tan"
                    and prev_y is not None
                    and ((prev_y > 0 and y_float < 0) or (prev_y < 0 and y_float > 0))
                    and abs(y_float - prev_y) > 2.5
                ):
                    xs_plot.append((xs_plot[-1] + x_float) / 2.0)
                    ys_plot.append(float("nan"))

                xs_plot.append(x_float)
                ys_plot.append(y_float)
                xs_validos.append(x_float)
                ys_validos.append(y_float)
                prev_y = y_float

        # 3. Construir la figura con Matplotlib
        fig, ax = plt.subplots(figsize=(10, 6), dpi=150)

        etiqueta_curva = f"f(x) = {funcion}(x) [{modo_angulo}]"
        ax.plot(
            xs_plot,
            ys_plot,
            color="#1f77b4",
            linewidth=2.2,
            label=etiqueta_curva,
            zorder=3,
        )

        if 0 < len(xs_validos) <= 65:
            ax.scatter(
                xs_validos,
                ys_validos,
                color="#0d47a1",
                s=26,
                zorder=4,
                label=f"Puntos definidos ({len(xs_validos)})",
            )

        if xs_nulos:
            for idx_nulo, x_nulo in enumerate(xs_nulos):
                ax.axvline(
                    x=x_nulo,
                    color="#d62728",
                    linestyle="--",
                    linewidth=1.2,
                    alpha=0.75,
                    label=f"Asíntota / y IS NULL ({len(xs_nulos)})" if idx_nulo == 0 else None,
                )

        ax.axhline(0, color="#333333", linewidth=0.9, alpha=0.7)
        if xmin <= 0 <= xmax:
            ax.axvline(0, color="#333333", linewidth=0.9, alpha=0.7)

        if funcion.lower() == "tan" and ys_validos:
            max_abs = max(abs(v) for v in ys_validos)
            if max_abs > 10.0:
                ax.set_ylim(-10.0, 10.0)

        unidad_x = (
            f"Ángulo x ({modo_angulo})"
            if funcion.lower() in ("sin", "cos", "tan")
            else "Valor x (razón adimensional)"
        )
        unidad_y = (
            "Resultado f(x)"
            if funcion.lower() in ("sin", "cos", "tan")
            else f"Ángulo f(x) ({modo_angulo})"
        )

        ax.set_title(
            f"Serie #{serie_id} ({nombre_serie}): f(x) = {funcion}(x) en [{xmin}, {xmax}] (paso = {paso}, {modo_angulo})",
            fontsize=11,
            fontweight="bold",
            pad=12,
        )
        ax.set_xlabel(unidad_x, fontsize=10)
        ax.set_ylabel(unidad_y, fontsize=10)
        ax.grid(True, linestyle=":", alpha=0.6)
        ax.legend(loc="best", frameon=True)

        fig.tight_layout()

        ruta_salida.parent.mkdir(parents=True, exist_ok=True)
        fig.savefig(str(ruta_salida), dpi=150)
        plt.close(fig)
    finally:
        conn.close()


def main() -> int:
    if len(sys.argv) != 4:
        print("ERROR;Uso esperado: python graficar.py <ruta_db> <serie_id> <salida.png>")
        return 1

    ruta_db = Path(sys.argv[1])
    try:
        serie_id = int(sys.argv[2])
    except ValueError:
        print(f"ERROR;El identificador serie_id debe ser un número entero válido: '{sys.argv[2]}'")
        return 1

    ruta_salida = Path(sys.argv[3])

    try:
        graficar_serie(ruta_db, serie_id, ruta_salida)
        print(f"OK;{ruta_salida.resolve()}")
        return 0
    except Exception as exc:
        print(f"ERROR;{exc}")
        return 1


if __name__ == "__main__":
    sys.exit(main())
