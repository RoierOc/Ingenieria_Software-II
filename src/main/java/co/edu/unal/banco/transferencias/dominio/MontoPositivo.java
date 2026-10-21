package co.edu.unal.banco.transferencias.dominio;

public class MontoPositivo extends ReglaTransferencia {
    @Override
    protected void comprobar(CuentaTransaccional origen, CuentaTransaccional destino, double monto) {
        if (monto <= 0) throw new IllegalArgumentException("Monto inválido");
    }
}
