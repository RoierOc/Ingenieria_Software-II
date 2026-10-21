package co.edu.unal.banco.transferencias.dominio;

public class ComisionPagoServicios implements PoliticaComision {
    @Override
    public double calcular(double monto) {
        return 1_500;
    }
}
