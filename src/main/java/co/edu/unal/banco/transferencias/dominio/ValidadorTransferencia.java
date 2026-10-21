package co.edu.unal.banco.transferencias.dominio;

public class ValidadorTransferencia implements ValidacionTransferencia {
    private final ValidacionTransferencia reglas = ReglaTransferencia.encadenar(
        new MontoPositivo(), new TopePorTransferencia(5_000_000), new CuentasDistintas());

    public void validar(CuentaTransaccional origen, CuentaTransaccional destino, double monto) {
        reglas.validar(origen, destino, monto);
    }
}
