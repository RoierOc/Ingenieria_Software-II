package co.edu.unal.banco.productos.dominio;

import co.edu.unal.banco.transferencias.dominio.CuentaTransaccional;

public class Cuenta extends ProductoConSaldo implements ProductoBancario, CuentaTransaccional {
    public Cuenta(String numero, String titular, double saldoInicial) {
        super(numero, titular, saldoInicial);
    }

    public void retirar(double monto) {
        descontarSaldo(monto);
    }

    public final void cobrarCargo(double monto) {
        descontarSaldo(monto);
    }

    @Override
    public String generarExtracto() {
        return "Cuenta " + numero + " - saldo: $" + saldo;
    }
}
