import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagoServiciosTest {
    private Cuenta origen;
    private RepositorioEnMemoria repositorio;
    private NotificadorEnMemoria notificador;
    private PagoServicios pagos;

    @BeforeEach
    void prepararPagos() {
        origen = new CuentaAhorros("001", "Ana", 1_000_000);
        repositorio = new RepositorioEnMemoria();
        notificador = new NotificadorEnMemoria();
        TransaccionService servicio = new TransaccionService(
            new ValidadorTransferencia(),
            new CalculadoraComision(Map.of(PagoServicios.TIPO_TRANSACCION, new ComisionFija(1_500))),
            repositorio, new GeneradorComprobante(), notificador,
            new AuditoriaCompuesta(List.of(new RegistroAuditoria(), new SistemaAntifraude()))
        );
        pagos = new PagoServicios(servicio);
    }

    @Test
    void pagoDescuentaElValorMasLaComisionFijaYImprimeElComprobanteConLaReferencia() {
        String salida = SalidaConsola.capturar(
            () -> pagos.pagar(origen, TipoServicio.LUZ, "REF-884213", 184_300));

        assertAll(
            () -> assertEquals(1_000_000 - 185_800, origen.getSaldo()),
            () -> assertEquals(new RepositorioEnMemoria.Registro("001", "REF-884213", 184_300, 1_500),
                repositorio.transacciones.getFirst()),
            () -> assertTrue(salida.contains("Destino: REF-884213")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[AUDITORIA]")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[ANTIFRAUDE]")),
            () -> assertEquals(1, notificador.notificaciones.size())
        );
    }

    @Test
    void aplicaLasMismasValidacionesDeMontoQueLasTransferencias() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                () -> pagos.pagar(origen, TipoServicio.AGUA, "REF-1", 0)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> pagos.pagar(origen, TipoServicio.GAS, "REF-1", 5_000_001)),
            () -> assertEquals(1_000_000, origen.getSaldo()),
            () -> assertTrue(repositorio.transacciones.isEmpty())
        );
    }

    @Test
    void saldoInsuficienteParaValorMasComisionRechazaSinEfectos() {
        Cuenta corta = new CuentaAhorros("003", "Eva", 185_000);

        assertThrows(IllegalStateException.class,
            () -> pagos.pagar(corta, TipoServicio.INTERNET, "REF-2", 184_300));

        assertAll(
            () -> assertEquals(185_000, corta.getSaldo()),
            () -> assertTrue(repositorio.transacciones.isEmpty()),
            () -> assertTrue(notificador.notificaciones.isEmpty())
        );
    }

    @Test
    void referenciaVaciaSeRechazaAntesDeMoverDinero() {
        assertThrows(IllegalArgumentException.class,
            () -> pagos.pagar(origen, TipoServicio.AGUA, "  ", 50_000));
        assertEquals(1_000_000, origen.getSaldo());
    }
}import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagoServiciosTest {
    private Cuenta origen;
    private RepositorioEnMemoria repositorio;
    private NotificadorEnMemoria notificador;
    private PagoServicios pagos;

    @BeforeEach
    void prepararPagos() {
        origen = new CuentaAhorros("001", "Ana", 1_000_000);
        repositorio = new RepositorioEnMemoria();
        notificador = new NotificadorEnMemoria();
        TransaccionService servicio = new TransaccionService(
            new ValidadorTransferencia(),
            new CalculadoraComision(Map.of(PagoServicios.TIPO_TRANSACCION, new ComisionFija(1_500))),
            repositorio, new GeneradorComprobante(), notificador,
            new AuditoriaCompuesta(List.of(new RegistroAuditoria(), new SistemaAntifraude()))
        );
        pagos = new PagoServicios(servicio);
    }

    @Test
    void pagoDescuentaElValorMasLaComisionFijaYImprimeElComprobanteConLaReferencia() {
        String salida = SalidaConsola.capturar(
            () -> pagos.pagar(origen, TipoServicio.LUZ, "REF-884213", 184_300));

        assertAll(
            () -> assertEquals(1_000_000 - 185_800, origen.getSaldo()),
            () -> assertEquals(new RepositorioEnMemoria.Registro("001", "REF-884213", 184_300, 1_500),
                repositorio.transacciones.getFirst()),
            () -> assertTrue(salida.contains("Destino: REF-884213")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[AUDITORIA]")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[ANTIFRAUDE]")),
            () -> assertEquals(1, notificador.notificaciones.size())
        );
    }

    @Test
    void aplicaLasMismasValidacionesDeMontoQueLasTransferencias() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                () -> pagos.pagar(origen, TipoServicio.AGUA, "REF-1", 0)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> pagos.pagar(origen, TipoServicio.GAS, "REF-1", 5_000_001)),
            () -> assertEquals(1_000_000, origen.getSaldo()),
            () -> assertTrue(repositorio.transacciones.isEmpty())
        );
    }

    @Test
    void saldoInsuficienteParaValorMasComisionRechazaSinEfectos() {
        Cuenta corta = new CuentaAhorros("003", "Eva", 185_000);

        assertThrows(IllegalStateException.class,
            () -> pagos.pagar(corta, TipoServicio.INTERNET, "REF-2", 184_300));

        assertAll(
            () -> assertEquals(185_000, corta.getSaldo()),
            () -> assertTrue(repositorio.transacciones.isEmpty()),
            () -> assertTrue(notificador.notificaciones.isEmpty())
        );
    }

    @Test
    void referenciaVaciaSeRechazaAntesDeMoverDinero() {
        assertThrows(IllegalArgumentException.class,
            () -> pagos.pagar(origen, TipoServicio.AGUA, "  ", 50_000));
        assertEquals(1_000_000, origen.getSaldo());
    }
}