#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
PROYECTO INTEGRADO: LA CALCULADORA — JAVA, PYTHON Y SQLITE (ESEIT)
Archivo: python/trig.py
Descripción: Motor trigonométrico CLI desacoplado invocado por Java (ProcessBuilder).

Contratos estrictos de salida por consola (stdout):
1. Evaluación puntual:
   python trig.py <fn> <valor> [--modo grados|radianes]
   -> Imprime exactamente UNA línea:
      OK;<resultado>
      o
      ERROR;<mensaje>

2. Generación de serie de puntos:
   python trig.py serie <fn> <xmin> <xmax> <paso> [--modo grados|radianes]
   -> Imprime una línea por punto en el formato:
      <x>;<y>
      Si la función se indetermina en x (p. ej., tan en 90° + k·180° o asin/acos
      fuera de [-1, 1]), el valor `y` queda vacío:
      <x>;
"""

import math
import sys

# Configurar codificación UTF-8 en Windows para evitar problemas con tildes o símbolos
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

FUNCIONES_DIRECTAS = {"sin", "cos", "tan"}
FUNCIONES_INVERSAS = {"asin", "acos", "atan"}
FUNCIONES_VALIDAS = FUNCIONES_DIRECTAS | FUNCIONES_INVERSAS
MODOS_VALIDOS = {"grados", "radianes"}

EPSILON_CERO = 1e-12
DECIMALES_REDONDEO = 10


def limpiar_numero(valor: float) -> float:
    """
    Elimina el ruido de punto flotante IEEE 754 (ej. sin(180°) = 1.2246e-16 -> 0.0)
    y redondea a DECIMALES_REDONDEO cifras decimales significativas.
    """
    if abs(valor) < EPSILON_CERO:
        return 0.0
    redondeado = round(valor, DECIMALES_REDONDEO)
    if abs(redondeado) < EPSILON_CERO:
        return 0.0
    return redondeado


def formatear_numero(valor: float) -> str:
    """
    Convierte un float limpio a cadena sin ceros residuales innecesarios.
    Ejemplos:
      0.0          -> "0"
      1.0000000000 -> "1"
      0.5000000000 -> "0.5"
      -30.0        -> "-30"
    """
    limpio = limpiar_numero(valor)
    if abs(limpio - round(limpio)) < EPSILON_CERO:
        return str(int(round(limpio)))
    texto = f"{limpio:.{DECIMALES_REDONDEO}f}".rstrip("0").rstrip(".")
    return "0" if texto in ("-0", "") else texto


def es_asintota_tangente(valor: float, modo: str) -> bool:
    """
    Determina si el ángulo corresponde a una asíntota vertical de tan(x):
    - En grados:   x = 90° + k·180°  (es decir, (x - 90) mod 180 == 0)
    - En radianes: cos(x) == 0       (dentro de la tolerancia EPSILON_CERO)
    """
    if modo == "grados":
        residuo = (valor - 90.0) % 180.0
        if abs(residuo) < 1e-9 or abs(residuo - 180.0) < 1e-9:
            return True
        rad = math.radians(valor)
    else:
        rad = valor

    return abs(math.cos(rad)) < EPSILON_CERO


def evaluar_funcion(fn: str, valor: float, modo: str) -> float:
    """
    Evalúa la función trigonométrica `fn` en `valor` bajo el `modo` ('grados' o 'radianes').
    Lanza ValueError con mensaje descriptivo si el valor está fuera del dominio o en una asíntota.
    """
    fn = fn.lower()
    if fn not in FUNCIONES_VALIDAS:
        raise ValueError(
            f"Función '{fn}' no soportada. Funciones válidas: sin, cos, tan, asin, acos, atan"
        )

    if modo not in MODOS_VALIDOS:
        raise ValueError(
            f"Modo angular '{modo}' inválido. Use 'grados' o 'radianes'"
        )

    # 1. Funciones trigonométricas directas: el argumento de entrada es un ángulo
    if fn in FUNCIONES_DIRECTAS:
        rad = math.radians(valor) if modo == "grados" else valor

        if fn == "sin":
            return limpiar_numero(math.sin(rad))
        if fn == "cos":
            return limpiar_numero(math.cos(rad))
        if fn == "tan":
            if es_asintota_tangente(valor, modo):
                unidad = "°" if modo == "grados" else " rad"
                raise ValueError(
                    f"Tangente indefinida en {formatear_numero(valor)}{unidad} (asíntota vertical en 90° + k·180°)"
                )
            return limpiar_numero(math.tan(rad))

    # 2. Funciones trigonométricas inversas: el argumento es una razón adimensional
    #    y el resultado es un ángulo devuelto en `modo` (grados o radianes).
    if fn in ("asin", "acos"):
        if valor < -1.0 - EPSILON_CERO or valor > 1.0 + EPSILON_CERO:
            raise ValueError(
                f"Dominio inválido para {fn}({formatear_numero(valor)}): el argumento debe pertenecer al intervalo [-1, 1]"
            )
        # Ajustar posibles bordes como 1.0000000000000002 a [-1.0, 1.0]
        valor_acotado = max(-1.0, min(1.0, valor))
        angulo_rad = math.asin(valor_acotado) if fn == "asin" else math.acos(valor_acotado)
        resultado = math.degrees(angulo_rad) if modo == "grados" else angulo_rad
        return limpiar_numero(resultado)

    if fn == "atan":
        angulo_rad = math.atan(valor)
        resultado = math.degrees(angulo_rad) if modo == "grados" else angulo_rad
        return limpiar_numero(resultado)

    raise ValueError(f"Operación no reconocida: {fn}")


def extraer_modo_y_posicionales(argv: list[str]) -> tuple[str, list[str]]:
    """
    Extrae la opción `--modo <grados|radianes>` sin usar argparse sobre los números
    posicionales para permitir valores negativos como `-90` o `-0.5` sin conflicto.
    """
    modo = "grados"
    posicionales: list[str] = []
    i = 0
    while i < len(argv):
        token = argv[i]
        if token == "--modo":
            if i + 1 >= len(argv):
                raise ValueError("Falta especificar el valor para '--modo' (grados|radianes)")
            modo = argv[i + 1].strip().lower()
            i += 2
        elif token.startswith("--modo="):
            modo = token.split("=", 1)[1].strip().lower()
            i += 1
        else:
            posicionales.append(token)
            i += 1

    if modo not in MODOS_VALIDOS:
        raise ValueError(f"Modo '{modo}' inválido. Valores permitidos: grados, radianes")

    return modo, posicionales


def ejecutar_modo_puntual(posicionales: list[str], modo: str) -> int:
    """Ejecuta `python trig.py <fn> <valor> [--modo grados|radianes]`."""
    if len(posicionales) != 2:
        print("ERROR;Uso puntual esperado: python trig.py <fn> <valor> [--modo grados|radianes]")
        return 0

    fn = posicionales[0].strip().lower()
    try:
        valor = float(posicionales[1].strip())
    except ValueError:
        print(f"ERROR;Valor numérico inválido: '{posicionales[1]}'")
        return 0

    if not math.isfinite(valor):
        print("ERROR;El valor de entrada debe ser un número real finito")
        return 0

    try:
        resultado = evaluar_funcion(fn, valor, modo)
        print(f"OK;{formatear_numero(resultado)}")
    except ValueError as exc:
        print(f"ERROR;{exc}")
    except Exception as exc:
        print(f"ERROR;Fallo inesperado en cálculo trigonométrico: {exc}")

    return 0


def ejecutar_modo_serie(posicionales: list[str], modo: str) -> int:
    """Ejecuta `python trig.py serie <fn> <xmin> <xmax> <paso> [--modo grados|radianes]`."""
    if len(posicionales) != 5:
        print("ERROR;Uso de serie esperado: python trig.py serie <fn> <xmin> <xmax> <paso> [--modo grados|radianes]")
        return 1

    _, fn_raw, xmin_raw, xmax_raw, paso_raw = posicionales
    fn = fn_raw.strip().lower()

    if fn not in FUNCIONES_VALIDAS:
        print(f"ERROR;Función '{fn}' no soportada para serie. Use: sin, cos, tan, asin, acos, atan")
        return 1

    try:
        xmin = float(xmin_raw)
        xmax = float(xmax_raw)
        paso = float(paso_raw)
    except ValueError:
        print("ERROR;Los parámetros xmin, xmax y paso deben ser números reales válidos")
        return 1

    if not (math.isfinite(xmin) and math.isfinite(xmax) and math.isfinite(paso)):
        print("ERROR;Los parámetros xmin, xmax y paso deben ser números finitos")
        return 1

    if paso <= 0.0:
        print("ERROR;El paso de incremento debe ser estrictamente mayor que cero (paso > 0)")
        return 1

    if xmax <= xmin:
        print("ERROR;El límite superior xmax debe ser estrictamente mayor que xmin (xmax > xmin)")
        return 1

    # Generar puntos evitando deriva acumulativa de punto flotante: x_i = xmin + i * paso
    indice = 0
    max_puntos = 100000
    while indice <= max_puntos:
        x_actual = limpiar_numero(xmin + indice * paso)
        if x_actual > xmax + 1e-9:
            break
        # Si por redondeo está muy cerca de xmax, ajustar a xmax
        if abs(x_actual - xmax) < 1e-9:
            x_actual = limpiar_numero(xmax)

        x_str = formatear_numero(x_actual)
        try:
            y_val = evaluar_funcion(fn, x_actual, modo)
            # Para tangente en barrido discreto, si el valor excede un umbral extremo
            # por cercanía numérica a la asíntota, tratar como indeterminación
            if fn == "tan" and abs(y_val) > 1e6:
                print(f"{x_str};")
            else:
                print(f"{x_str};{formatear_numero(y_val)}")
        except ValueError:
            # Punto fuera del dominio o asíntota vertical -> `y` vacío (`x;`)
            print(f"{x_str};")

        indice += 1

    return 0


def main() -> int:
    try:
        modo, posicionales = extraer_modo_y_posicionales(sys.argv[1:])
    except ValueError as exc:
        print(f"ERROR;{exc}")
        return 0

    if not posicionales:
        print("ERROR;Faltan argumentos. Uso: python trig.py <fn> <valor> [--modo grados|radianes]")
        return 0

    if posicionales[0].strip().lower() == "serie":
        return ejecutar_modo_serie(posicionales, modo)
    return ejecutar_modo_puntual(posicionales, modo)


if __name__ == "__main__":
    sys.exit(main())
