package co.edu.unal.banco.transferencias.aplicacion;

import co.edu.unal.banco.transferencias.dominio.CalculoComision;
import co.edu.unal.banco.transferencias.dominio.CuentaTransaccional;
import co.edu.unal.banco.transferencias.dominio.ValidacionTransferencia;
import co.edu.unal.banco.transferencias.puertos.entrada.TransferirFondos;
import co.edu.unal.banco.transferencias.puertos.salida.AuditoriaTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.ComprobanteTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.NotificacionTransferencia;
import co.edu.unal.banco.transferencias.puertos.salida.RepositorioTransacciones;

public class TransaccionService implements TransferirFondos {
    private final ValidacionTransferencia validador;
    private final CalculoComision calculadora;
    private final RepositorioTransacciones repositorio;
    private final ComprobanteTransferencia comprobante;
    private final NotificacionTransferencia notificador;
    private final AuditoriaTransferencia auditoria;

    public TransaccionService(ValidacionTransferencia validador, CalculoComision calculadora,
                              RepositorioTransacciones repositorio, ComprobanteTransferencia comprobante,
                              NotificacionTransferencia notificador, AuditoriaTransferencia auditoria) {
        this.validador = validador;
        this.calculadora = calculadora;
        this.repositorio = repositorio;
        this.comprobante = comprobante;
        this.notificador = notificador;
        this.auditoria = auditoria;
    }

    @Override
    public void transferir(CuentaTransaccional origen, CuentaTransaccional destino, double monto, String tipo) {
        validador.validar(monto);
        double comision = calculadora.calcular(monto, tipo);
        origen.retirar(monto + comision);
        destino.depositar(monto);
        repositorio.guardarTransaccion(origen.getNumero(), destino.getNumero(), monto, comision);
        comprobante.generar(origen, destino, monto, comision);
        notificador.notificar(origen, destino, monto);
        auditoria.registrar(origen, destino, monto, tipo);
    }
}
