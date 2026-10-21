package co.edu.unal.banco.transferencias.adaptadores.salida;

import co.edu.unal.banco.transferencias.aplicacion.InfraestructuraTransferencias;
import co.edu.unal.banco.transferencias.puertos.salida.AuditoriaTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.ComprobanteTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.NotificacionTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.RepositorioTransacciones;

import java.util.List;

public class InfraestructuraProduccion implements InfraestructuraTransferencias {
    public RepositorioTransacciones crearRepositorio() {
        return new PostgresRepositorio();
    }

    public ComprobanteTransferencia crearComprobante() {
        return new GeneradorComprobante();
    }

    public NotificacionTransferencia crearNotificacion() {
        return new NotificadorTransferencia(new CanalNotificacionCompuesto(
            List.of(new SmsGateway(), new PushGateway())));
    }

    public AuditoriaTransferencia crearAuditoria() {
        return new AuditoriaCompuesta(List.of(new RegistroAuditoria(), new SistemaAntifraude()));
    }
}
