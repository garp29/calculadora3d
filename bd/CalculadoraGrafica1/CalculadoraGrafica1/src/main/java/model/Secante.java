package model;

public class Secante implements Operacion {
    @Override
    public double calcular(double... operandos) throws Exception {
        double anguloRadianes = Math.toRadians(operandos[0]);
        double coseno = Math.cos(anguloRadianes);
        if (coseno == 0) {
            throw new Exception("Error: Secante indefinida (coseno de 90° o equivalente es 0).");
        }
        return 1.0 / coseno;
    }
    @Override
    public String getSimbolo() { return "sec"; }
    @Override
    public String getNombre() { return "Secante"; }
}