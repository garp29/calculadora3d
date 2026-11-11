package model;

public interface Operacion {
    double calcular(double... operandos) throws Exception;
    String getSimbolo();
    String getNombre();
}