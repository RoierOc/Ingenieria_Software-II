import java.time.LocalDateTime;

public class RegistroAuditoria {
    public void registrar(Cuenta origen, Cuenta destino, double monto, String tipo) {
        System.out.println("[AUDITORIA] " + LocalDateTime.now() + " " + tipo
            + " " + origen.getNumero() + " -> " + destino.getNumero() + " $" + monto);
    }
}
