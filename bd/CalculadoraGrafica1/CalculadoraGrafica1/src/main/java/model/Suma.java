package model;

public class Suma implements Operacion {
    @Override
    public double calcular(double... operandos) throws Exception {
        return operandos[0] + operandos[1];
    }
    @Override
    public String getSimbolo() { return "+"; }
    @Override
    public String getNombre() { return "Suma"; }
}