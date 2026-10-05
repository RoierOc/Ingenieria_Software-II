/**
 * Destino de un pago de servicios: se identifica por la referencia de la factura
 * y el servicio que la emite. Se presenta como CuentaTransaccional para reutilizar
 * el comprobante, la notificación y la auditoría de las transferencias.
 */
public class FacturaServicio implements CuentaTransaccional {
    private final TipoServicio servicio;
    private final String referencia;
    private double recibido;

    public FacturaServicio(TipoServicio servicio, String referencia) {
        if (servicio == null) throw new IllegalArgumentException("Servicio requerido");
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("Referencia de factura requerida");
        }
        this.servicio = servicio;
        this.referencia = referencia.trim();
    }

    @Override
    public String getNumero() { return referencia; }

    @Override
    public String getTitular() { return servicio.name(); }

    public double getRecibido() { return recibido; }

    @Override
    public void depositar(double monto) {
        recibido += monto;
    }

    @Override
    public void retirar(double monto) {
        throw new UnsupportedOperationException("Una factura de servicios no admite retiros");
    }
}