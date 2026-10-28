#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
===============================================================================
Script: python/trig.py - Motor Trigonométrico
===============================================================================
Módulo para cálculos trigonométricos unicos y generación de series de puntos.
Diseñado para integración con Java (ProcessBuilder) e interacciones CLI.

Contrato CLI:
  1. Cálculo único:
     python trig.py <fn> <valor> [--modo grados|radianes]
     Salida: OK;<resultado>  o  ERROR;<mensaje>

  2. Generación de serie:
     python trig.py serie <fn> <xmin> <xmax> <paso> [--modo grados|radianes]
     Salida: Múltiples líneas x;y (si indefinido: x;)

Sustentación / Puntos clave:
  - Cero uso de input(); todo el flujo de ejecución depende de sys.argv.
  - Limpieza de punto flotante: valores < 1e-12 se ajustan a 0 para evitar errores como sin(180) = 1.22e-16.
  - Manejo de asíntotas: en tan(x) con |cos(x)| < 1e-12, se retorna ERROR en modo único y punto discontinuo en serie.
  - Dominio de funciones inversas: asin/acos fuera de [-1, 1] son rechazados con contrato de error explicito.
===============================================================================
"""

import sys
import math

def format_num(val):
    """
    Limpia el ruido numérico de punto flotante.
    1. Si la magnitud es menor a 1e-12, se redondea a 0.
    2. Redondea a 10 decimales para eliminar imprecisiones flotantes.
    3. Si el número es un entero exacto (ej. 0.0, 1.0), lo retorna como entero sin decimales ('0', '1').
    """
    if abs(val) < 1e-12:
        val = 0.0
    else:
        val = round(val, 10)

    if val == int(val):
        return str(int(val))
    return str(val)

def parse_modo(args):
    """
    Extrae la opción opcional '--modo grados|radianes' de la lista de argumentos CLI.
    Retorna la modalidad ('grados' por defecto) y los argumentos restantes.
    """
    modo = 'grados'
    remaining = []
    i = 0
    while i < len(args):
        if args[i] == '--modo':
            if i + 1 < len(args):
                modo = args[i + 1]
                i += 2
            else:
                raise ValueError("Falta el valor para el argumento --modo")
        elif args[i].startswith('--modo='):
            modo = args[i].split('=', 1)[1]
            i += 1
        else:
            remaining.append(args[i])
            i += 1
    return modo, remaining

def eval_single(fn, valor_str, modo):
    """
    Realiza un cálculo trigonométrico único.
    """
    try:
        val = float(valor_str)
    except ValueError:
        return f"ERROR;El valor '{valor_str}' debe ser un número válido"

    fn = fn.lower()
    modo = modo.lower()

    if modo not in ('grados', 'radianes'):
        return f"ERROR;Modo invalido: {modo}. Debe ser 'grados' o 'radianes'"

    # Funciones directas: sin, cos, tan
    if fn in ('sin', 'cos', 'tan'):
        rad = math.radians(val) if modo == 'grados' else val

        if fn == 'tan':
            # Verificación estricta de asíntotas donde |cos(x)| < 1e-12
            if abs(math.cos(rad)) < 1e-12:
                v_str = format_num(val)
                return f"ERROR;Tangente indefinida en {v_str}"
            res = math.tan(rad)
        elif fn == 'sin':
            res = math.sin(rad)
        elif fn == 'cos':
            res = math.cos(rad)

        return f"OK;{format_num(res)}"

    # Funciones inversas: asin, acos, atan
    elif fn in ('asin', 'acos', 'atan'):
        if fn in ('asin', 'acos') and (val < -1.0 or val > 1.0):
            return f"ERROR;Dominio invalido para {fn}: el valor debe estar entre -1 y 1"

        if fn == 'asin':
            res_rad = math.asin(val)
        elif fn == 'acos':
            res_rad = math.acos(val)
        elif fn == 'atan':
            res_rad = math.atan(val)

        res = math.degrees(res_rad) if modo == 'grados' else res_rad
        return f"OK;{format_num(res)}"

    else:
        return f"ERROR;Funcion no soportada: {fn}"

def eval_series(fn, xmin_str, xmax_str, paso_str, modo):
    """
    Genera una serie de puntos x;y en el rango [xmin, xmax] con incremento de paso.
    Retorna una lista de cadenas con el formato 'x;y' o 'x;' (si la función no está definida).
    """
    try:
        xmin = float(xmin_str)
        xmax = float(xmax_str)
        paso = float(paso_str)
    except ValueError:
        return ["ERROR;Parametros numericos invalidos en la serie"]

    if xmin >= xmax:
        return ["ERROR;xmin debe ser menor que xmax"]
    if paso <= 0:
        return ["ERROR;paso debe ser mayor a 0"]

    fn = fn.lower()
    modo = modo.lower()

    if modo not in ('grados', 'radianes'):
        return [f"ERROR;Modo invalido: {modo}. Debe ser 'grados' o 'radianes'"]

    if fn not in ('sin', 'cos', 'tan', 'asin', 'acos', 'atan'):
        return [f"ERROR;Funcion no soportada: {fn}"]

    results = []
    # Cálculo preciso del número de pasos para evitar imprecisiones acumuladas
    n_steps = int(math.floor((xmax - xmin) / paso + 1e-9))

    for i in range(n_steps + 1):
        x_raw = xmin + i * paso
        if x_raw > xmax + 1e-9:
            break

        x_str = format_num(x_raw)

        if fn in ('sin', 'cos', 'tan'):
            rad = math.radians(x_raw) if modo == 'grados' else x_raw
            # Manejo de asíntota en serie (hueco)
            if fn == 'tan' and abs(math.cos(rad)) < 1e-12:
                results.append(f"{x_str};")
                continue

            if fn == 'sin':
                y = math.sin(rad)
            elif fn == 'cos':
                y = math.cos(rad)
            elif fn == 'tan':
                y = math.tan(rad)

        else: # funciones inversas
            if fn in ('asin', 'acos') and (x_raw < -1.0 or x_raw > 1.0):
                results.append(f"{x_str};")
                continue

            if fn == 'asin':
                y_rad = math.asin(x_raw)
            elif fn == 'acos':
                y_rad = math.acos(x_raw)
            elif fn == 'atan':
                y_rad = math.atan(x_raw)

            y = math.degrees(y_rad) if modo == 'grados' else y_rad

        results.append(f"{x_str};{format_num(y)}")

    return results

def main():
    try:
        args = sys.argv[1:]
        if not args:
            print("ERROR;Argumentos insuficientes. Uso: trig.py <fn> <valor> [--modo grados|radianes] o trig.py serie <fn> <xmin> <xmax> <paso> [--modo grados|radianes]")
            return

        modo, remaining = parse_modo(args)

        if not remaining:
            print("ERROR;Parametros incompletos")
            return

        if remaining[0].lower() == 'serie':
            # Submodo 2: Generación de serie
            if len(remaining) < 5:
                print("ERROR;Parametros insuficientes para el modo serie. Uso: trig.py serie <fn> <xmin> <xmax> <paso>")
                return
            fn = remaining[1]
            xmin_str = remaining[2]
            xmax_str = remaining[3]
            paso_str = remaining[4]
            out = eval_series(fn, xmin_str, xmax_str, paso_str, modo)
            for line in out:
                print(line)
        else:
            # Submodo 1: Cálculo único
            if len(remaining) < 2:
                print("ERROR;Parametros insuficientes para calculo unico. Uso: trig.py <fn> <valor>")
                return
            fn = remaining[0]
            valor_str = remaining[1]
            out = eval_single(fn, valor_str, modo)
            print(out)

    except Exception as e:
        # Garantiza que cualquier error no controlado se capture bajo el contrato ERROR;<mensaje>
        print(f"ERROR;Excepcion no controlada: {str(e)}")

if __name__ == "__main__":
    main()
