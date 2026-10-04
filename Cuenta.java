public class Cuenta extends ProductoConSaldo {
    public Cuenta(String numero, String titular, double saldoInicial) {
        super(numero, titular, saldoInicial);
    }

    public void retirar(double monto) {
        descontarSaldo(monto);
    }
}
