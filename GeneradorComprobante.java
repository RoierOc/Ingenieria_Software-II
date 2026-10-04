public class GeneradorComprobante {
    public void generar(Cuenta origen, Cuenta destino, double monto, double comision) {
        System.out.println("===== BANCO ANDINO - COMPROBANTE =====");
        System.out.println("Origen: " + origen.getNumero());
        System.out.println("Destino: " + destino.getNumero());
        System.out.println("Monto:    $" + monto);
        System.out.println("Comisión: $" + comision);
        System.out.println("======================================");
    }
}
