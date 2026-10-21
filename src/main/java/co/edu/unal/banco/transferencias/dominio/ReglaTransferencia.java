package co.edu.unal.banco.transferencias.dominio;

public abstract class ReglaTransferencia implements ValidacionTransferencia {
    private ReglaTransferencia siguiente;

    public static ValidacionTransferencia encadenar(ReglaTransferencia primera, ReglaTransferencia... resto) {
        ReglaTransferencia actual = primera;
        for (ReglaTransferencia regla : resto) {
            actual.siguiente = regla;
            actual = regla;
        }
        return primera;
    }

    @Override
    public final void validar(CuentaTransaccional origen, CuentaTransaccional destino, double monto) {
        comprobar(origen, destino, monto);
        if (siguiente != null) {
            siguiente.validar(origen, destino, monto);
        }
    }

    protected abstract void comprobar(CuentaTransaccional origen, CuentaTransaccional destino, double monto);
}
