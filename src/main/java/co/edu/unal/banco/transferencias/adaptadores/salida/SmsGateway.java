package co.edu.unal.banco.transferencias.adaptadores.salida;

import co.edu.unal.banco.transferencias.puertos.salida.CanalNotificacion;

public class SmsGateway implements CanalNotificacion {
    public void enviar(String destinatario, String mensaje) {
        System.out.println("[SMS] Conectando al proveedor de mensajería...");
        System.out.println("[SMS] Para " + destinatario + ": " + mensaje);
    }
}
