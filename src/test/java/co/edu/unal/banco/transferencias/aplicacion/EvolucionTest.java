package co.edu.unal.banco.transferencias.aplicacion;

import co.edu.unal.banco.productos.dominio.Cuenta;
import co.edu.unal.banco.productos.dominio.CuentaAhorros;
import co.edu.unal.banco.transferencias.adaptadores.salida.InfraestructuraProduccion;
import co.edu.unal.banco.transferencias.dominio.ComisionMismoBanco;
import co.edu.unal.banco.transferencias.dominio.ComisionPagoServicios;
import co.edu.unal.banco.transferencias.puertos.entrada.TransferirFondos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Fase 7: los tres escenarios de cambio. Ninguna prueba anterior se modificó. */
class EvolucionTest {
    private Cuenta origen;
    private Cuenta destino;

    @BeforeEach
    void preparar() {
        origen = new CuentaAhorros("001", "Ana", 1_000_000);
        destino = new CuentaAhorros("002", "Luis", 0);
    }

    @Test
    void escenario1_whatsAppRecibeLaNotificacionJuntoConSmsYPush() {
        TransferirFondos servicio = new TransaccionServiceBuilder()
            .comision("MISMO_BANCO", new ComisionMismoBanco())
            .infraestructura(new InfraestructuraProduccion())
            .construir();

        String salida = SalidaConsola.capturar(() -> servicio.transferir(origen, destino, 50_000, "MISMO_BANCO"));

        assertAll(
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[SMS] Para")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[PUSH] Para")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[WHATSAPP] Para Ana: Transferiste $50000.0"))
        );
    }

    @Test
    void escenario2_pagoDeServiciosCobraUnaComisionFijaDe1500() {
        InfraestructuraEnMemoria infraestructura = new InfraestructuraEnMemoria();
        TransferirFondos servicio = new TransaccionServiceBuilder()
            .comision("PAGO_SERVICIOS", new ComisionPagoServicios())
            .infraestructura(infraestructura)
            .construir();

        servicio.transferir(origen, destino, 184_300, "PAGO_SERVICIOS");

        assertAll(
            () -> assertEquals(814_200, origen.getSaldo()),
            () -> assertEquals(184_300, destino.getSaldo()),
            () -> assertEquals(1_500, infraestructura.repositorio.transacciones.getFirst().comision())
        );
    }

    @Test
    void escenario3_unMontoInferiorAlMinimoSeRechazaSinMoverDinero() {
        InfraestructuraEnMemoria infraestructura = new InfraestructuraEnMemoria();
        TransferirFondos servicio = new TransaccionServiceBuilder()
            .comision("MISMO_BANCO", new ComisionMismoBanco())
            .infraestructura(infraestructura)
            .construir();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> servicio.transferir(origen, destino, 500, "MISMO_BANCO"));

        assertAll(
            () -> assertEquals("Inferior al monto mínimo", error.getMessage()),
            () -> assertEquals(1_000_000, origen.getSaldo()),
            () -> assertEquals(0, infraestructura.repositorio.transacciones.size())
        );
    }
}
