public interface AuditoriaTransferencia {
    void registrar(CuentaTransaccional origen, CuentaTransaccional destino, double monto, String tipo);
}
