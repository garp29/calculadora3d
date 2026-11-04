#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
PROYECTO INTEGRADO: LA CALCULADORA — JAVA, PYTHON Y SQLITE (ESEIT)
Archivo: bd/sql.py
Descripción: Ejecutor CLI de scripts (.sql) y sentencias SQL sobre SQLite 3.
             Garantiza PRAGMA foreign_keys = ON, formatea resultados SELECT en
             tablas legibles y soporta la verificación de pruebas de integridad.
"""

import argparse
import os
import sqlite3
import sys
from pathlib import Path

# Forzar salida UTF-8 y página de códigos 65001 en consolas Windows (evita caracteres '├¡' de CP850)
if os.name == "nt":
    os.system("chcp 65001 >nul 2>&1")
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

BASE_DIR = Path(__file__).resolve().parent
DEFAULT_DB = BASE_DIR / "calculadora.db"


def formatear_tabla(columnas: list[str], filas: list[tuple]) -> str:
    """Formatea un conjunto de resultados SQL como tabla ASCII alineada."""
    if not columnas:
        return ""
    str_filas = [
        ["NULL" if val is None else str(val) for val in fila]
        for fila in filas
    ]
    anchos = [len(col) for col in columnas]
    for fila in str_filas:
        for i, celda in enumerate(fila):
            if len(celda) > anchos[i]:
                anchos[i] = len(celda)

    separador = "+-" + "-+-".join("-" * w for w in anchos) + "-+"
    encabezado = "| " + " | ".join(col.ljust(anchos[i]) for i, col in enumerate(columnas)) + " |"

    lineas = [separador, encabezado, separador]
    for fila in str_filas:
        lineas.append("| " + " | ".join(celda.ljust(anchos[i]) for i, celda in enumerate(fila)) + " |")
    lineas.append(separador)
    lineas.append(f"({len(filas)} fila(s))")
    return "\n".join(lineas)


def dividir_sentencias_sql(texto_sql: str) -> list[str]:
    """
    Divide un script SQL en sentencias individuales usando sqlite3.complete_statement,
    preservando comentarios previos para identificar cada bloque de prueba.
    """
    sentencias = []
    buffer_actual = []

    for linea in texto_sql.splitlines():
        strip_linea = linea.strip()
        # Ignorar líneas vacías o de comentario puro si el buffer aún no tiene código SQL
        if not buffer_actual and (not strip_linea or strip_linea.startswith("--")):
            continue
        buffer_actual.append(linea)
        candidato = "\n".join(buffer_actual)
        if sqlite3.complete_statement(candidato):
            sentencias.append(candidato.strip())
            buffer_actual = []

    if buffer_actual:
        resto = "\n".join(buffer_actual).strip()
        if resto and not all(l.strip().startswith("--") or not l.strip() for l in buffer_actual):
            sentencias.append(resto)

    return sentencias


def ejecutar_sql(
    ruta_db: Path,
    sql_texto: str,
    origen: str = "SQL",
    tolerar_errores_integridad: bool = False,
) -> int:
    """
    Ejecuta una o varias sentencias SQL sobre ruta_db con PRAGMA foreign_keys = ON.
    Retorna 0 si finalizó correctamente, o 1 si ocurrió un error inesperado.
    """
    ruta_db.parent.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(str(ruta_db))
    conn.execute("PRAGMA foreign_keys = ON;")
    cursor = conn.cursor()

    sentencias = dividir_sentencias_sql(sql_texto)
    exitos = 0
    rechazos_integridad = 0
    errores_inesperados = 0

    print(f"=== Ejecutando [{origen}] sobre [{ruta_db}] ===")
    for idx, stmt in enumerate(sentencias, start=1):
        # Extraer primera línea de código real (sin comentarios iniciales) para resumen
        lineas_codigo = [l.strip() for l in stmt.splitlines() if l.strip() and not l.strip().startswith("--")]
        resumen_stmt = lineas_codigo[0] if lineas_codigo else stmt[:60]

        try:
            cursor.execute(stmt)
            if cursor.description is not None:
                columnas = [col[0] for col in cursor.description]
                filas = cursor.fetchall()
                print(f"\n[Sentencia #{idx}] {resumen_stmt}")
                print(formatear_tabla(columnas, filas))
            else:
                conn.commit()
                print(f"[OK #{idx}] {resumen_stmt}")
            exitos += 1
        except sqlite3.IntegrityError as exc:
            conn.rollback()
            rechazos_integridad += 1
            if tolerar_errores_integridad:
                print(f"[RECHAZO ESPERADO #{idx}] {resumen_stmt}")
                print(f"   -> SQLite IntegrityError: {exc}")
            else:
                print(f"[ERROR DE INTEGRIDAD #{idx}] {resumen_stmt}", file=sys.stderr)
                print(f"   -> {exc}", file=sys.stderr)
                errores_inesperados += 1
        except sqlite3.Error as exc:
            conn.rollback()
            errores_inesperados += 1
            print(f"[ERROR SQL #{idx}] {resumen_stmt}", file=sys.stderr)
            print(f"   -> {exc}", file=sys.stderr)

    conn.close()
    print(
        f"=== Resumen [{origen}]: {exitos} exitosa(s), "
        f"{rechazos_integridad} rechazo(s) de integridad, "
        f"{errores_inesperados} error(es) inesperado(s) ===\n"
    )
    return 1 if errores_inesperados > 0 else 0


def inicializar_todo(ruta_db: Path) -> int:
    """Ejecuta en orden: create_schema.sql, seed.sql, pruebas.sql y consultas.sql."""
    orden_scripts = [
        (BASE_DIR / "create_schema.sql", False),
        (BASE_DIR / "seed.sql", False),
        (BASE_DIR / "pruebas.sql", True),
        (BASE_DIR / "consultas.sql", False),
    ]
    for script_path, es_prueba in orden_scripts:
        if not script_path.exists():
            print(f"ERROR: No se encontró el archivo {script_path}", file=sys.stderr)
            return 1
        contenido = script_path.read_text(encoding="utf-8")
        rc = ejecutar_sql(
            ruta_db=ruta_db,
            sql_texto=contenido,
            origen=script_path.name,
            tolerar_errores_integridad=es_prueba,
        )
        if rc != 0:
            return rc
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Ejecutor de scripts (.sql) y consultas SQL para SQLite 3 (La Calculadora)."
    )
    parser.add_argument(
        "args",
        nargs="*",
        help="Uso flexible: [ruta_db] <archivo.sql | sentencia_SQL>",
    )
    parser.add_argument(
        "--db",
        default=str(DEFAULT_DB),
        help=f"Ruta de la base de datos SQLite (por omisión: {DEFAULT_DB})",
    )
    parser.add_argument(
        "-c",
        "--command",
        dest="comando_sql",
        help="Ejecuta una sentencia SQL en línea (ej. py bd/sql.py bd/calculadora.db -c \"SELECT 1;\")",
    )
    parser.add_argument(
        "--init",
        action="store_true",
        help="Inicializa calculadora.db ejecutando create_schema.sql, seed.sql, pruebas.sql y consultas.sql",
    )
    parser.add_argument(
        "--tolerar-rechazos",
        action="store_true",
        help="Trata los errores de integridad (PK/FK/CHECK/UNIQUE/NOT NULL) como rechazos de prueba esperados",
    )

    parsed = parser.parse_args()
    ruta_db = Path(parsed.db)

    if parsed.args and parsed.args[0].lower().endswith(".db"):
        ruta_db = Path(parsed.args[0])
        if not ruta_db.exists() and (BASE_DIR / parsed.args[0]).exists():
            ruta_db = BASE_DIR / parsed.args[0]
        objetivos = parsed.args[1:]
    else:
        objetivos = parsed.args

    if parsed.comando_sql:
        return ejecutar_sql(
            ruta_db=ruta_db,
            sql_texto=parsed.comando_sql,
            origen="CLI -c",
            tolerar_errores_integridad=parsed.tolerar_rechazos,
        )

    if parsed.init or not objetivos:
        return inicializar_todo(ruta_db)

    codigo_salida = 0
    for objetivo in objetivos:
        posible_archivo = Path(objetivo)
        if not posible_archivo.exists() and (BASE_DIR / objetivo).exists():
            posible_archivo = BASE_DIR / objetivo

        if posible_archivo.exists() and posible_archivo.is_file():
            es_prueba = parsed.tolerar_rechazos or ("pruebas" in posible_archivo.name.lower())
            contenido = posible_archivo.read_text(encoding="utf-8")
            rc = ejecutar_sql(
                ruta_db=ruta_db,
                sql_texto=contenido,
                origen=posible_archivo.name,
                tolerar_errores_integridad=es_prueba,
            )
        else:
            rc = ejecutar_sql(
                ruta_db=ruta_db,
                sql_texto=objetivo,
                origen="CLI",
                tolerar_errores_integridad=parsed.tolerar_rechazos,
            )
        if rc != 0:
            codigo_salida = rc

    return codigo_salida


if __name__ == "__main__":
    sys.exit(main())
