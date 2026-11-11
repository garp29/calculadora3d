package model;

public class Cosecante implements Operacion {
    @Override
    public double calcular(double... operandos) throws Exception {
        double anguloRadianes = Math.toRadians(operandos[0]);
        double seno = Math.sin(anguloRadianes);
        if (seno == 0) {
            throw new Exception("Error: Cosecante indefinida (seno de 0 es 0).");
        }
        return 1.0 / seno;
    }
    @Override
    public String getSimbolo() { return "csc"; }
    @Override
    public String getNombre() { return "Cosecante"; }
}