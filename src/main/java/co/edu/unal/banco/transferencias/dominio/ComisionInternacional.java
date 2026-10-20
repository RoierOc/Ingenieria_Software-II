package co.edu.unal.banco.transferencias.dominio;

public class ComisionInternacional implements PoliticaComision {
    @Override
    public double calcular(double monto) {
        return monto * 0.03 + 25_000;
    }
}
