public class Cuenta extends ProductoConSaldo implements ProductoBancario, CuentaTransaccional {
    public Cuenta(String numero, String titular, double saldoInicial) {
        super(numero, titular, saldoInicial);
    }

    public void retirar(double monto) {
        descontarSaldo(monto);
    }

    @Override
    public String generarExtracto() {
        return "Cuenta " + numero + " - saldo: $" + saldo;
    }
}
