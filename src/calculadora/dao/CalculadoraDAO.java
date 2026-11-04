package calculadora.dao;

import calculadora.dominio.PuntoSerie;
import calculadora.dominio.RegistroCalculo;
import calculadora.dominio.SerieTrigonometrica;
import calculadora.excepciones.PersistenciaException;

import java.util.List;

/**
 * Contrato del patrón Data Access Object (DAO) para las 4 tablas oficiales
 * (`operacion`, `calculo`, `serie`, `punto`) en `bd/calculadora.db`.
 */
public interface CalculadoraDAO {

    /**
     * Obtiene el identificador primario (`id`) de una operación a partir de su `simbolo` o `nombre`.
     */
    int obtenerIdOperacionPorSimbolo(String simbolo) throws PersistenciaException;

    /**
     * Persiste un cálculo puntual (exitoso `exito = 1` o fallido `exito = 0`) en la tabla `calculo`.
     */
    int guardarCalculo(RegistroCalculo registro) throws PersistenciaException;

    /**
     * Consulta los últimos `limite` registros del historial en orden cronológico descendente.
     */
    List<RegistroCalculo> consultarHistorialReciente(int limite) throws PersistenciaException;

    /**
     * Persiste de forma atómica (transacción `setAutoCommit(false)` / `commit()` / `rollback()`)
     * la cabecera de una serie en `serie` y todos sus puntos `(x, y)` (incluidos `y = NULL`) en `punto`.
     */
    int guardarSerieYPuntos(
            String nombreSerie,
            int operacionId,
            double xMin,
            double xMax,
            double paso,
            String modoAngulo,
            List<PuntoSerie> puntos) throws PersistenciaException;

    /**
     * Consulta el resumen consolidado de todas las series registradas en `serie` y `punto`.
     */
    List<SerieTrigonometrica> consultarResumenSeries() throws PersistenciaException;

    /**
     * Consulta las estadísticas agrupadas por operación (JOIN + GROUP BY).
     */
    List<String> consultarEstadisticasOperaciones() throws PersistenciaException;
}
