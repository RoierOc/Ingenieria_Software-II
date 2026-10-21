package co.edu.unal.banco.transferencias.dominio;

public class CuentasDistintas extends ReglaTransferencia {
    @Override
    protected void comprobar(CuentaTransaccional origen, CuentaTransaccional destino, double monto) {
        if (origen.getNumero().equals(destino.getNumero())) {
            throw new IllegalArgumentException("El origen y el destino deben ser cuentas distintas");
        }
    }
}
