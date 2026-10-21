package co.edu.unal.banco.transferencias.aplicacion;

import co.edu.unal.banco.transferencias.puertos.salida.AuditoriaTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.ComprobanteTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.NotificacionTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.RepositorioTransacciones;

class InfraestructuraEnMemoria implements InfraestructuraTransferencias {
    final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    final NotificadorEnMemoria notificador = new NotificadorEnMemoria();

    @Override
    public RepositorioTransacciones crearRepositorio() {
        return repositorio;
    }

    @Override
    public ComprobanteTransferencia crearComprobante() {
        return (origen, destino, monto, comision) -> {};
    }

    @Override
    public NotificacionTransferencia crearNotificacion() {
        return notificador;
    }

    @Override
    public AuditoriaTransferencia crearAuditoria() {
        return (origen, destino, monto, tipo) -> {};
    }
}
