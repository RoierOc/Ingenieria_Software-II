import java.util.ArrayList;
import java.util.List;

class NotificadorEnMemoria implements NotificacionTransferencia {
    record Notificacion(String origen, String destino, double monto) {}

    final List<Notificacion> notificaciones = new ArrayList<>();

    @Override
    public void notificar(CuentaTransaccional origen, CuentaTransaccional destino, double monto) {
        notificaciones.add(new Notificacion(origen.getNumero(), destino.getNumero(), monto));
    }
}
