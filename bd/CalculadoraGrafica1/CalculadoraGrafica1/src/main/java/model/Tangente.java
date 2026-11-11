package model;

public class Tangente implements Operacion {
    @Override
    public double calcular(double... operandos) throws Exception {
        double anguloRadianes = Math.toRadians(operandos[0]);
        return Math.tan(anguloRadianes);
    }
    @Override
    public String getSimbolo() { return "tan"; }
    @Override
    public String getNombre() { return "Tangente"; }
}