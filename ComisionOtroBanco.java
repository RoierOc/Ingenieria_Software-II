public class ComisionOtroBanco implements PoliticaComision {
    @Override
    public double calcular(double monto) {
        return 7_500;
    }
}
