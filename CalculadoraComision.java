import java.util.Map;

public class CalculadoraComision {
    private final Map<String, PoliticaComision> politicas;

    public CalculadoraComision(Map<String, PoliticaComision> politicas) {
        this.politicas = Map.copyOf(politicas);
    }

    public double calcular(double monto, String tipo) {
        PoliticaComision politica = politicas.get(tipo);
        if (politica == null) {
            throw new IllegalArgumentException("Tipo de transferencia desconocido");
        }
        return politica.calcular(monto);
    }
}
