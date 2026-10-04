public class NotificadorTransferencia implements NotificacionTransferencia {
    private final CanalNotificacion canal;

    public NotificadorTransferencia(CanalNotificacion canal) {
        this.canal = canal;
    }

    public void notificar(CuentaTransaccional origen, CuentaTransaccional destino, double monto) {
        canal.enviar(origen.getTitular(), "Transferiste $" + monto + " a la cuenta " + destino.getNumero());
    }
}
