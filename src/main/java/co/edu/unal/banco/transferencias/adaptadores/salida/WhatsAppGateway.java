package co.edu.unal.banco.transferencias.adaptadores.salida;

import co.edu.unal.banco.transferencias.puertos.salida.CanalNotificacion;

public class WhatsAppGateway implements CanalNotificacion {
    public void enviar(String destinatario, String mensaje) {
        System.out.println("[WHATSAPP] Conectando a la API de WhatsApp Business...");
        System.out.println("[WHATSAPP] Para " + destinatario + ": " + mensaje);
    }
}
