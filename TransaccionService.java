public class TransaccionService {
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
