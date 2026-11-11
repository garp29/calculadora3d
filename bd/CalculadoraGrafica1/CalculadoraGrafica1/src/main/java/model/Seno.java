package model;

public class Seno implements Operacion {
    @Override
    public double calcular(double... operandos) throws Exception {
        double anguloRadianes = Math.toRadians(operandos[0]);
        return Math.sin(anguloRadianes);
    }
    @Override
    public String getSimbolo() { return "sin"; }
    @Override
    public String getNombre() { return "Seno"; }
}