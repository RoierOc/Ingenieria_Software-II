public interface CuentaTransaccional {
    String getNumero();
    String getTitular();
    void depositar(double monto);
    void retirar(double monto);
}
