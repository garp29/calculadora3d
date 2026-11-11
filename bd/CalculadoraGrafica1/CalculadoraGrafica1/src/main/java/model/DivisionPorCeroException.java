package model;

public class DivisionPorCeroException extends Exception {
    public DivisionPorCeroException() {
        super("No es posible dividir entre cero");
    }
}