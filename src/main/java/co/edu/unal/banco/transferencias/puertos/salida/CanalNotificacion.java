package co.edu.unal.banco.transferencias.puertos.salida;

public interface CanalNotificacion {
    void enviar(String destinatario, String mensaje);
}
