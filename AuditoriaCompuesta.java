import java.util.List;

public class AuditoriaCompuesta implements AuditoriaTransferencia {
    private final List<AuditoriaTransferencia> auditorias;

    public AuditoriaCompuesta(List<AuditoriaTransferencia> auditorias) {
        this.auditorias = List.copyOf(auditorias);
    }

    public void registrar(CuentaTransaccional origen, CuentaTransaccional destino, double monto, String tipo) {
        for (AuditoriaTransferencia auditoria : auditorias) {
            auditoria.registrar(origen, destino, monto, tipo);
        }
    }
}
