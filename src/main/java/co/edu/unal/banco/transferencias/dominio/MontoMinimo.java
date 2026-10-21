package co.edu.unal.banco.transferencias.dominio;

public class MontoMinimo extends ReglaTransferencia {
    private final double minimo;

    public MontoMinimo(double minimo) {
        this.minimo = minimo;
    }

    @Override
    protected void comprobar(CuentaTransaccional origen, CuentaTransaccional destino, double monto) {
        if (monto < minimo) throw new IllegalArgumentException("Inferior al monto mínimo");
    }
}
