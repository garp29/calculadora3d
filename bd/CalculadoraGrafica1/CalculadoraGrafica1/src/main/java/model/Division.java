package model;

public class Division implements Operacion {
    @Override
    public double calcular(double... operandos) throws Exception {
        if (operandos[1] == 0) {
            throw new DivisionPorCeroException();
        }
        return operandos[0] / operandos[1];
    }
    @Override
    public String getSimbolo() { return "/"; }
    @Override
    public String getNombre() { return "División"; }
}