public class PagoServicios {
    public static final String TIPO_TRANSACCION = "PAGO_SERVICIO";

    private final TransaccionService transacciones;

    public PagoServicios(TransaccionService transacciones) {
        this.transacciones = transacciones;
    }

    public void pagar(CuentaTransaccional origen, TipoServicio servicio, String referencia, double valor) {
        transacciones.transferir(origen, new FacturaServicio(servicio, referencia), valor, TIPO_TRANSACCION);
    }
}