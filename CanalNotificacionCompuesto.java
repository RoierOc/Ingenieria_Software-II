import java.util.List;

public class CanalNotificacionCompuesto implements CanalNotificacion {
    private final List<CanalNotificacion> canales;

    public CanalNotificacionCompuesto(List<CanalNotificacion> canales) {
        this.canales = List.copyOf(canales);
    }

    public void enviar(String destinatario, String mensaje) {
        for (CanalNotificacion canal : canales) {
            canal.enviar(destinatario, mensaje);
        }
    }
}
