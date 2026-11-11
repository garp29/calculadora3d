package model;

public class Coseno implements Operacion {
    @Override
    public double calcular(double... operandos) throws Exception {
        double anguloRadianes = Math.toRadians(operandos[0]);
        return Math.cos(anguloRadianes);
    }
    @Override
    public String getSimbolo() { return "cos"; }
    @Override
    public String getNombre() { return "Coseno"; }
}