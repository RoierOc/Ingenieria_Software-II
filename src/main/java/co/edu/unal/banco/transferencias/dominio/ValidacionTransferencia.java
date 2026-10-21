package co.edu.unal.banco.transferencias.dominio;

public interface ValidacionTransferencia {
    void validar(CuentaTransaccional origen, CuentaTransaccional destino, double monto);
}
