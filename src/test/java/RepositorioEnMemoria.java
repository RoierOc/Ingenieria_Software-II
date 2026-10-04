import java.util.ArrayList;
import java.util.List;

class RepositorioEnMemoria implements RepositorioTransacciones {
    record Registro(String origen, String destino, double monto, double comision) {}

    final List<Registro> transacciones = new ArrayList<>();

    @Override
    public void guardarTransaccion(String origen, String destino, double monto, double comision) {
        transacciones.add(new Registro(origen, destino, monto, comision));
    }
}
