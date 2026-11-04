package calculadora.dominio;

/**
 * Representa un punto individual (x, y) de la tabla oficial `punto`.
 * El atributo `y` es de tipo {@link Double} para admitir {@code null}
 * cuando la función presenta asíntota o indeterminación (ej. tan 90°, R6).
 */
public class PuntoSerie {

    private final Integer id;
    private final Integer serieId;
    private final double x;
    private final Double y;

    public PuntoSerie(double x, Double y) {
        this(null, null, x, y);
    }

    public PuntoSerie(Integer id, Integer serieId, double x, Double y) {
        this.id = id;
        this.serieId = serieId;
        this.x = x;
        this.y = y;
    }

    public Integer getId() {
        return id;
    }

    public Integer getSerieId() {
        return serieId;
    }

    public double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public boolean esDiscontinuidad() {
        return y == null;
    }
}
