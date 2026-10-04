public class SistemaAntifraude implements AuditoriaTransferencia {
    public void registrar(CuentaTransaccional origen, CuentaTransaccional destino, double monto, String tipo) {
        System.out.println("[ANTIFRAUDE] Reportando transacción " + tipo
            + " " + origen.getNumero() + " -> " + destino.getNumero() + " $" + monto);
    }
}
