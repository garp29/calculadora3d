#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
===============================================================================
Script: bd/sql.py - Ejecutor SQL CLI
===============================================================================
Herramienta CLI para ejecutar consultas o archivos SQL sobre la base de datos.

Interfaz CLI:
  python sql.py <ruta_db> [-c "<consulta_sql>" | -f "<archivo.sql>"]

Sustentación / Puntos clave:
  - Activación obligatoria de 'PRAGMA foreign_keys = ON;': Garantiza la integridad
    referencial en la base de datos en todas las operaciones.
  - Formateo de tablas para SELECT: Salida ASCII limpia con alineación dinámica de columnas
    para facilitar la inspección desde consola.
  - Reporte claro de errores de integridad (sqlite3.Error): Captura violaciones de UNIQUE,
    FOREIGN KEY o sintaxis sin mostrar trazas de error no controladas (Tracebacks).
  - Cumplimiento de contratos: Salida formateada o ERROR;<mensaje>.
===============================================================================
"""

import sys
import os
import sqlite3
import argparse

def format_table(headers, rows):
    """
    Formatea los resultados de una consulta SELECT en una tabla ASCII legible.
    """
    if not headers:
        return ""

    # Determinar el ancho máximo para cada columna
    widths = [len(str(h)) for h in headers]
    for row in rows:
        for i, val in enumerate(row):
            val_str = "NULL" if val is None else str(val)
            if len(val_str) > widths[i]:
                widths[i] = len(val_str)

    # Construir cabecera, separador y filas de datos
    header_line = " | ".join(f"{str(h):<{widths[i]}}" for i, h in enumerate(headers))
    separator = "-+-".join("-" * widths[i] for i in range(len(headers)))

    data_lines = []
    for row in rows:
        row_line = " | ".join(f"{('NULL' if val is None else str(val)):<{widths[i]}}" for i, val in enumerate(row))
        data_lines.append(row_line)

    return "\n".join([header_line, separator] + data_lines)

def split_sql_script(content):
    """
    Divide un script SQL con múltiples sentencias separadas por punto y coma,
    ignorando líneas de comentarios y comandos dot-shell de SQLite (ej. .headers, .mode).
    """
    lines = []
    for line in content.splitlines():
        line_strip = line.strip()
        if line_strip.startswith("--") or line_strip.startswith("."):
            continue
        lines.append(line)

    full_text = "\n".join(lines)
    raw_stmts = full_text.split(";")
    statements = []
    for s in raw_stmts:
        stmt = s.strip()
        if stmt:
            statements.append(stmt)
    return statements

def ejecutar_sql(ruta_db, comando_sql=None, archivo_sql=None):
    conn = None
    try:
        conn = sqlite3.connect(ruta_db)
        cursor = conn.cursor()

        # Habilitar siempre claves foráneas en SQLite
        cursor.execute("PRAGMA foreign_keys = ON;")

        tables_printed = False

        if comando_sql:
            cursor.execute(comando_sql)
            if cursor.description is not None:
                headers = [desc[0] for desc in cursor.description]
                rows = cursor.fetchall()
                print(format_table(headers, rows))
                tables_printed = True
            else:
                conn.commit()
                print(f"OK;Operacion completada. Filas afectadas: {cursor.rowcount}")

        elif archivo_sql:
            if not os.path.exists(archivo_sql):
                return f"ERROR;El archivo SQL '{archivo_sql}' no existe"

            with open(archivo_sql, "r", encoding="utf-8") as f:
                content = f.read()

            stmts = split_sql_script(content)
            for stmt in stmts:
                cursor.execute(stmt)
                if cursor.description is not None:
                    headers = [desc[0] for desc in cursor.description]
                    rows = cursor.fetchall()
                    if headers:
                        if tables_printed:
                            print("\n")
                        print(format_table(headers, rows))
                        tables_printed = True
                else:
                    conn.commit()

            if not tables_printed:
                print("OK;Script SQL ejecutado exitosamente")

        return None

    except sqlite3.Error as e:
        return f"ERROR;Error de SQLite: {str(e)}"
    except Exception as e:
        return f"ERROR;Excepcion no controlada: {str(e)}"
    finally:
        if conn:
            conn.close()

def main():
    parser = argparse.ArgumentParser(description="Ejecutor SQL CLI para la Calculadora", add_help=False)
    parser.add_argument("ruta_db", nargs="?", help="Ruta de la base de datos SQLite")
    parser.add_argument("-c", "--command", help="Consulta SQL a ejecutar")
    parser.add_argument("-f", "--file", help="Archivo .sql a ejecutar")

    try:
        args, unknown = parser.parse_known_args()
        if not args.ruta_db or (not args.command and not args.file):
            print("ERROR;Argumentos insuficientes. Uso: python sql.py <ruta_db> [-c \"<consulta_sql>\" | -f \"<archivo.sql>\"]")
            return

        err = ejecutar_sql(args.ruta_db, args.command, args.file)
        if err:
            print(err)

    except Exception as e:
        print(f"ERROR;Error en los argumentos de entrada: {str(e)}")

if __name__ == "__main__":
    main()
