#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
===============================================================================
Script: python/graficar.py - Generador de Gráficas
===============================================================================
Genera imágenes PNG a partir de series numéricas almacenadas en la base de datos SQLite.

Interfaz CLI:
  python graficar.py <ruta_db> <serie_id> <salida.png>

Sustentación / Puntos clave:
  - Backend non-interactive 'Agg' de matplotlib: evita abrir ventanas de interfaz gráfica
    bloqueantes cuando el script es invocado en segundo plano por Java.
  - Tratamiento de discontinuidades: los valores y NULL en la BD representan asíntotas / puntos
    indefinidos y se convierten a float('nan'). Matplotlib deja huecos limpios en la curva
    sin trazar líneas verticales erróneas uniendo asíntotas (ej. tan(90°)).
  - Manejo robusto de rutas: soporta rutas absolutas, relativas y con espacios típicas en Windows.
  - Contrato de salida estricto: OK;<salida.png> o ERROR;<mensaje>.
===============================================================================
"""

import sys
import os
import math
import sqlite3
import matplotlib
matplotlib.use('Agg') # Fuerza backend no interactivo antes de importar pyplot
import matplotlib.pyplot as plt

def generar_grafica(ruta_db, serie_id, salida_png):
    if not os.path.exists(ruta_db):
        return f"ERROR;La base de datos '{ruta_db}' no existe"

    try:
        serie_id_num = int(serie_id)
    except ValueError:
        return f"ERROR;El ID de serie '{serie_id}' debe ser un numero entero"

    conn = None
    try:
        conn = sqlite3.connect(ruta_db)
        cursor = conn.cursor()

        # Habilitar claves foráneas
        cursor.execute("PRAGMA foreign_keys = ON;")

        # Consultar metadatos de la serie e información de la operación asociada
        query_meta = """
            SELECT s.id_serie, o.nombre AS operacion_nombre, o.simbolo, s.xmin, s.xmax, s.paso, s.modo
            FROM serie s
            JOIN operacion o ON s.id_operacion = o.id_operacion
            WHERE s.id_serie = ?
        """
        cursor.execute(query_meta, (serie_id_num,))
        meta = cursor.fetchone()

        if not meta:
            return f"ERROR;No se encontro la serie con ID {serie_id_num} en la base de datos"

        _, op_nombre, op_simbolo, xmin, xmax, paso, modo = meta

        # Consultar los puntos de la serie ordenados por X
        query_puntos = """
            SELECT x, y
            FROM punto
            WHERE id_serie = ?
            ORDER BY x
        """
        cursor.execute(query_puntos, (serie_id_num,))
        puntos = cursor.fetchall()

        if not puntos:
            return f"ERROR;No se encontraron puntos asociados a la serie con ID {serie_id_num}"

        # Extraer listas de coordenadas. Puntos con y NULL se convierten a float('nan')
        x_vals = []
        y_vals = []
        for x, y in puntos:
            x_vals.append(float(x))
            if y is None:
                y_vals.append(float('nan'))
            else:
                y_vals.append(float(y))

        # Configuración de la figura Matplotlib
        plt.figure(figsize=(9, 5))
        plt.plot(x_vals, y_vals, label=f"f(x) = {op_simbolo}(x)", color='#1f77b4', linewidth=2)

        # Título y etiquetas de ejes
        plt.title(f"Función {op_nombre} ({op_simbolo}) - Serie #{serie_id_num}", fontsize=14, fontweight='bold')
        plt.xlabel(f"x ({modo})", fontsize=11)
        plt.ylabel(f"f(x) = {op_simbolo}(x)", fontsize=11)
        plt.grid(True, linestyle='--', alpha=0.6)
        plt.legend(loc='upper right', fontsize=10)

        # Ajuste razonable de ejes si es tangente o hay valores muy grandes
        val_y_validos = [v for v in y_vals if not math.isnan(v)]
        if val_y_validos:
            q_min = min(val_y_validos)
            q_max = max(val_y_validos)
            if q_max - q_min > 50:
                plt.ylim(max(-10.0, q_min), min(10.0, q_max))

        # Garantizar que el directorio destino exista (soporta rutas complejas/con espacios)
        dir_salida = os.path.dirname(os.path.abspath(salida_png))
        if dir_salida:
            os.makedirs(dir_salida, exist_ok=True)

        # Guardar la gráfica en formato PNG
        plt.savefig(salida_png, dpi=150, bbox_inches='tight')
        plt.close()

        return f"OK;{salida_png}"

    except sqlite3.Error as e:
        return f"ERROR;Error de SQLite al consultar la serie: {str(e)}"
    except Exception as e:
        return f"ERROR;Excepcion durante la generacion de la grafica: {str(e)}"
    finally:
        if conn:
            conn.close()

def main():
    try:
        args = sys.argv[1:]
        if len(args) < 3:
            print("ERROR;Argumentos insuficientes. Uso: python graficar.py <ruta_db> <serie_id> <salida.png>")
            return

        ruta_db = args[0]
        serie_id = args[1]
        salida_png = args[2]

        resultado = generar_grafica(ruta_db, serie_id, salida_png)
        print(resultado)

    except Exception as e:
        print(f"ERROR;Excepcion no controlada: {str(e)}")

if __name__ == "__main__":
    main()
