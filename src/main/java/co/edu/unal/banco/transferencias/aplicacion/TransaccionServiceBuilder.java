package co.edu.unal.banco.transferencias.aplicacion;

import co.edu.unal.banco.transferencias.dominio.CalculadoraComision;
import co.edu.unal.banco.transferencias.dominio.PoliticaComision;
import co.edu.unal.banco.transferencias.dominio.ValidacionTransferencia;
import co.edu.unal.banco.transferencias.dominio.ValidadorTransferencia;

import java.util.HashMap;
import java.util.Map;

public class TransaccionServiceBuilder {
    private ValidacionTransferencia validador = new ValidadorTransferencia();
    private final Map<String, PoliticaComision> politicas = new HashMap<>();
    private InfraestructuraTransferencias infraestructura;

    public TransaccionServiceBuilder validarCon(ValidacionTransferencia validador) {
        this.validador = validador;
        return this;
    }

    public TransaccionServiceBuilder comision(String tipo, PoliticaComision politica) {
        if (politicas.putIfAbsent(tipo, politica) != null) {
            throw new IllegalArgumentException("Comisión ya registrada: " + tipo);
        }
        return this;
    }

    public TransaccionServiceBuilder infraestructura(InfraestructuraTransferencias infraestructura) {
        this.infraestructura = infraestructura;
        return this;
    }

    public TransaccionService construir() {
        if (infraestructura == null) {
            throw new IllegalStateException("Falta la infraestructura de transferencias");
        }
        if (politicas.isEmpty()) {
            throw new IllegalStateException("Debe registrarse al menos una comisión");
        }
        return new TransaccionService(
            validador, new CalculadoraComision(politicas),
            infraestructura.crearRepositorio(), infraestructura.crearComprobante(),
            infraestructura.crearNotificacion(), infraestructura.crearAuditoria());
    }
}
