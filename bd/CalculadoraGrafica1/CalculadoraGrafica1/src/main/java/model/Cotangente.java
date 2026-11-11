package model;

public class Cotangente implements Operacion {
    @Override
    public double calcular(double... operandos) throws Exception {
        double anguloRadianes = Math.toRadians(operandos[0]);
        double tangente = Math.tan(anguloRadianes);
        if (tangente == 0) {
            throw new Exception("Error: Cotangente indefinida.");
        }
        return 1.0 / tangente;
    }
    @Override
    public String getSimbolo() { return "cot"; }
    @Override
    public String getNombre() { return "Cotangente"; }
}