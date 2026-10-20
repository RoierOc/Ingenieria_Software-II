package co.edu.unal.banco.transferencias.dominio;

public class ComisionLlave implements PoliticaComision {
    @Override
    public double calcular(double monto) {
        return 0;
    }
}
