package calculadora.dominio;

/**
 * Representa la cabecera y resumen de una serie trigonométrica de la tabla oficial `serie`.
 */
public class SerieTrigonometrica {

    private final Integer id;
    private final String nombre;
    private final int operacionId;
    private final String funcion;
    private final double xMin;
    private final double xMax;
    private final double paso;
    private final String modoAngulo;
    private final String creadoEn;
    private final int totalPuntos;
    private final int puntosDefinidos;
    private final int puntosNulosAsintotas;

    public SerieTrigonometrica(
            Integer id,
            String nombre,
            int operacionId,
            String funcion,
            double xMin,
            double xMax,
            double paso,
            String modoAngulo,
            String creadoEn,
            int totalPuntos,
            int puntosDefinidos,
            int puntosNulosAsintotas) {
        this.id = id;
        this.nombre = nombre;
        this.operacionId = operacionId;
        this.funcion = funcion;
        this.xMin = xMin;
        this.xMax = xMax;
        this.paso = paso;
        this.modoAngulo = modoAngulo;
        this.creadoEn = creadoEn;
        this.totalPuntos = totalPuntos;
        this.puntosDefinidos = puntosDefinidos;
        this.puntosNulosAsintotas = puntosNulosAsintotas;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getOperacionId() {
        return operacionId;
    }

    public String getFuncion() {
        return funcion;
    }

    public double getXMin() {
        return xMin;
    }

    public double getXMax() {
        return xMax;
    }

    public double getPaso() {
        return paso;
    }

    public String getModoAngulo() {
        return modoAngulo;
    }

    public String getCreadoEn() {
        return creadoEn;
    }

    public int getTotalPuntos() {
        return totalPuntos;
    }

    public int getPuntosDefinidos() {
        return puntosDefinidos;
    }

    public int getPuntosNulosAsintotas() {
        return puntosNulosAsintotas;
    }
}
