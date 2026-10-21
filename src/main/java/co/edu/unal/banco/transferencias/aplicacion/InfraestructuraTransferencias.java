package co.edu.unal.banco.transferencias.aplicacion;

import co.edu.unal.banco.transferencias.puertos.salida.AuditoriaTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.ComprobanteTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.NotificacionTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.RepositorioTransacciones;

public interface InfraestructuraTransferencias {
    RepositorioTransacciones crearRepositorio();
    ComprobanteTransferencia crearComprobante();
    NotificacionTransferencia crearNotificacion();
    AuditoriaTransferencia crearAuditoria();
}
