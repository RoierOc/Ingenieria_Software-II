package co.edu.unal.banco.transferencias.dominio;

public interface CuentaTransaccional {
    String getNumero();
    String getTitular();
    void depositar(double monto);
    void retirar(double monto);
}
