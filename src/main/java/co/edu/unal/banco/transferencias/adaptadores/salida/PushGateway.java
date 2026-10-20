package co.edu.unal.banco.transferencias.adaptadores.salida;

import co.edu.unal.banco.transferencias.puertos.salida.CanalNotificacion;

public class PushGateway implements CanalNotificacion {
    public void enviar(String destinatario, String mensaje) {
        System.out.println("[PUSH] Conectando al servicio de notificaciones de la app...");
        System.out.println("[PUSH] Para " + destinatario + ": " + mensaje);
    }
}
