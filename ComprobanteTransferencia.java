public interface ComprobanteTransferencia {
    void generar(CuentaTransaccional origen, CuentaTransaccional destino, double monto, double comision);
}
