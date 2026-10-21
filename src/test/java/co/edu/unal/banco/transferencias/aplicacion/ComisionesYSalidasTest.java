package co.edu.unal.banco.transferencias.aplicacion;

import co.edu.unal.banco.productos.dominio.Cuenta;
import co.edu.unal.banco.productos.dominio.CuentaAhorros;
import co.edu.unal.banco.transferencias.adaptadores.salida.GeneradorComprobante;
import co.edu.unal.banco.transferencias.adaptadores.salida.InfraestructuraProduccion;
import co.edu.unal.banco.transferencias.dominio.ComisionInternacional;
import co.edu.unal.banco.transferencias.dominio.ComisionLlave;
import co.edu.unal.banco.transferencias.dominio.ComisionMismoBanco;
import co.edu.unal.banco.transferencias.dominio.ComisionOtroBanco;
import co.edu.unal.banco.transferencias.puertos.entrada.TransferirFondos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cubre RF-03 (internacional), RF-07 (comprobante) y la fábrica de producción. */
class ComisionesYSalidasTest {
    private TransaccionServiceBuilder builderCompleto() {
        return new TransaccionServiceBuilder()
            .comision("MISMO_BANCO", new ComisionMismoBanco())
            .comision("OTRO_BANCO", new ComisionOtroBanco())
            .comision("INTERNACIONAL", new ComisionInternacional())
            .comision("LLAVE", new ComisionLlave());
    }

    @Test
    void internacionalCobraTresPorCientoMasVeinticincoMil() {
        Cuenta origen = new CuentaAhorros("001", "Ana", 1_000_000);
        Cuenta destino = new CuentaAhorros("002", "Luis", 0);
        InfraestructuraEnMemoria infraestructura = new InfraestructuraEnMemoria();
        TransferirFondos servicio = builderCompleto().infraestructura(infraestructura).construir();

        servicio.transferir(origen, destino, 100_000, "INTERNACIONAL");

        assertAll(
            () -> assertEquals(872_000, origen.getSaldo()),
            () -> assertEquals(100_000, destino.getSaldo()),
            () -> assertEquals(28_000, infraestructura.repositorio.transacciones.getFirst().comision())
        );
    }

    @Test
    void elComprobanteIncluyeCuentasMontoYComision() {
        Cuenta origen = new CuentaAhorros("001", "Ana", 0);
        Cuenta destino = new CuentaAhorros("002", "Luis", 0);

        String salida = SalidaConsola.capturar(
            () -> new GeneradorComprobante().generar(origen, destino, 150_000, 7_500));

        assertAll(
            () -> assertTrue(salida.contains("Origen: 001")),
            () -> assertTrue(salida.contains("Destino: 002")),
            () -> assertTrue(salida.contains("Monto:    $150000.0")),
            () -> assertTrue(salida.contains("Comisión: $7500.0"))
        );
    }

    @Test
    void laFamiliaDeProduccionConectaTodasLasSalidas() {
        Cuenta origen = new CuentaAhorros("001", "Ana", 1_000_000);
        Cuenta destino = new CuentaAhorros("002", "Luis", 0);
        TransferirFondos servicio = builderCompleto().infraestructura(new InfraestructuraProduccion()).construir();

        String salida = SalidaConsola.capturar(() -> servicio.transferir(origen, destino, 150_000, "OTRO_BANCO"));

        assertAll(
            () -> assertEquals(2, SalidaConsola.lineasQueEmpiezanCon(salida, "[POSTGRES]")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "===== BANCO ANDINO - COMPROBANTE")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[SMS] Para")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[PUSH] Para")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[AUDITORIA]")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[ANTIFRAUDE]"))
        );
    }
}
