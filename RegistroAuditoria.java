import java.time.LocalDateTime;

public class RegistroAuditoria implements AuditoriaTransferencia {
    public void registrar(CuentaTransaccional origen, CuentaTransaccional destino, double monto, String tipo) {
        System.out.println("[AUDITORIA] " + LocalDateTime.now() + " " + tipo
            + " " + origen.getNumero() + " -> " + destino.getNumero() + " $" + monto);
    }
}
