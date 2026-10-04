import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuentaInfantilTest {
    private static final Instant HOY = Instant.parse("2026-10-04T15:00:00Z");
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    @Test
    void retiroQueSuperaElLimiteDiarioSeRechazaSinCambiarElSaldo() {
        CuentaInfantil cuenta = new CuentaInfantil("INF-1", "Sofía", 1_000_000, Clock.fixed(HOY, BOGOTA));
        cuenta.retirar(150_000);

        IllegalStateException error = assertThrows(IllegalStateException.class,
            () -> cuenta.retirar(60_000));

        assertAll(
            () -> assertEquals("Supera el límite diario de retiros", error.getMessage()),
            () -> assertEquals(850_000, cuenta.getSaldo())
        );
    }

    @Test
    void elLimiteSeReiniciaAlDiaSiguiente() {
        RelojAjustable reloj = new RelojAjustable(Clock.fixed(HOY, BOGOTA));
        CuentaInfantil cuenta = new CuentaInfantil("INF-1", "Sofía", 1_000_000, reloj);
        cuenta.retirar(200_000);

        reloj.avanzar(Duration.ofDays(1));
        cuenta.retirar(200_000);

        assertEquals(600_000, cuenta.getSaldo());
    }

    private static class RelojAjustable extends Clock {
        private Clock actual;

        RelojAjustable(Clock inicial) { this.actual = inicial; }

        void avanzar(Duration duracion) { actual = Clock.offset(actual, duracion); }

        @Override public ZoneId getZone() { return actual.getZone(); }
        @Override public Clock withZone(ZoneId zona) { return actual.withZone(zona); }
        @Override public Instant instant() { return actual.instant(); }
    }

    @Test
    void recibeDepositosSinLimite() {
        CuentaInfantil cuenta = new CuentaInfantil("INF-1", "Sofía", 0);

        cuenta.depositar(10_000_000);

        assertEquals(10_000_000, cuenta.getSaldo());
    }

    @Test
    void puedeSerOrigenDeTransferenciasYPagaCuotaDeManejo() {
        CuentaInfantil infantil = new CuentaInfantil("INF-1", "Sofía", 500_000);
        Cuenta destino = new CuentaAhorros("002", "Luis", 0);
        TransaccionService servicio = new TransaccionService(
            new ValidadorTransferencia(),
            new CalculadoraComision(Map.of("MISMO_BANCO", new ComisionMismoBanco())),
            new RepositorioEnMemoria(), (cuentaOrigen, cuentaDestino, monto, comision) -> {},
            new NotificadorEnMemoria(), (cuentaOrigen, cuentaDestino, monto, tipo) -> {}
        );

        servicio.transferir(infantil, destino, 100_000, "MISMO_BANCO");
        new CobroCuotaManejo().cobrarMensual(List.of(infantil));

        assertAll(
            () -> assertEquals(387_100, infantil.getSaldo()),
            () -> assertEquals(100_000, destino.getSaldo())
        );
    }

    @Test
    void transferenciaQueSuperaElLimiteConLaComisionSeRechaza() {
        CuentaInfantil infantil = new CuentaInfantil("INF-1", "Sofía", 1_000_000, Clock.fixed(HOY, BOGOTA));
        Cuenta destino = new CuentaAhorros("002", "Luis", 0);
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        TransaccionService servicio = new TransaccionService(
            new ValidadorTransferencia(),
            new CalculadoraComision(Map.of("OTRO_BANCO", new ComisionOtroBanco())),
            repositorio, (cuentaOrigen, cuentaDestino, monto, comision) -> {},
            new NotificadorEnMemoria(), (cuentaOrigen, cuentaDestino, monto, tipo) -> {}
        );
        infantil.retirar(150_000);

        assertThrows(IllegalStateException.class,
            () -> servicio.transferir(infantil, destino, 45_000, "OTRO_BANCO"));

        assertAll(
            () -> assertEquals(850_000, infantil.getSaldo()),
            () -> assertEquals(0, destino.getSaldo()),
            () -> assertTrue(repositorio.transacciones.isEmpty())
        );
    }

    @Test
    void laCuotaDeManejoSeCobraAunqueYaAlcanzoElLimiteDiario() {
        CuentaInfantil infantil = new CuentaInfantil("INF-1", "Sofía", 500_000, Clock.fixed(HOY, BOGOTA));
        Cuenta ahorros = new CuentaAhorros("002", "Luis", 100_000);
        infantil.retirar(200_000);

        new CobroCuotaManejo().cobrarMensual(List.of(infantil, ahorros));

        assertAll(
            () -> assertEquals(287_100, infantil.getSaldo()),
            () -> assertEquals(87_100, ahorros.getSaldo())
        );
    }
}
