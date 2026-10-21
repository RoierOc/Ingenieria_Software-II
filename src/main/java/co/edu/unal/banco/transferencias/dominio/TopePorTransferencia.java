package co.edu.unal.banco.transferencias.dominio;

public class TopePorTransferencia extends ReglaTransferencia {
    private final double tope;

    public TopePorTransferencia(double tope) {
        this.tope = tope;
    }

    @Override
    protected void comprobar(CuentaTransaccional origen, CuentaTransaccional destino, double monto) {
        if (monto > tope) throw new IllegalArgumentException("Supera el tope diario");
    }
}
