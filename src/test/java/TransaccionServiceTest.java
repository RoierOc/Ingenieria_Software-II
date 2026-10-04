import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransaccionServiceTest {
    private Cuenta origen;
    private Cuenta destino;
    private RepositorioEnMemoria repositorio;
    private NotificadorEnMemoria notificador;
    private TransaccionService servicio;

    @BeforeEach
    void prepararTransferencia() {
        origen = new CuentaAhorros("001", "Ana", 1_000_000);
        destino = new CuentaAhorros("002", "Luis", 500_000);
        repositorio = new RepositorioEnMemoria();
        notificador = new NotificadorEnMemoria();
        CalculoComision calculadora = new CalculadoraComision(Map.of(
            "MISMO_BANCO", new ComisionMismoBanco(),
            "OTRO_BANCO", new ComisionOtroBanco(),
            "INTERNACIONAL", new ComisionInternacional()
        ));
        servicio = new TransaccionService(
            new ValidadorTransferencia(), calculadora, repositorio,
            (cuentaOrigen, cuentaDestino, monto, comision) -> {}, notificador,
            (cuentaOrigen, cuentaDestino, monto, tipo) -> {}
        );
    }

    @Test
    void mismoBancoMueveElMontoSinCobrarComision() {
        servicio.transferir(origen, destino, 150_000, "MISMO_BANCO");

        assertAll(
            () -> assertEquals(850_000, origen.getSaldo()),
            () -> assertEquals(650_000, destino.getSaldo()),
            () -> assertEquals(0, repositorio.transacciones.getFirst().comision())
        );
    }

    @Test
    void otroBancoDescuentaElMontoMasLaComisionDe7500() {
        servicio.transferir(origen, destino, 150_000, "OTRO_BANCO");

        assertAll(
            () -> assertEquals(842_500, origen.getSaldo()),
            () -> assertEquals(650_000, destino.getSaldo()),
            () -> assertEquals(7_500, repositorio.transacciones.getFirst().comision())
        );
    }

    @Test
    void saldoInsuficienteRechazaSinGuardarNiNotificar() {
        origen = new CuentaAhorros("001", "Ana", 150_000);

        IllegalStateException error = assertThrows(IllegalStateException.class,
            () -> servicio.transferir(origen, destino, 150_000, "OTRO_BANCO"));

        assertAll(
            () -> assertEquals("Saldo insuficiente", error.getMessage()),
            () -> assertEquals(150_000, origen.getSaldo()),
            () -> assertEquals(500_000, destino.getSaldo()),
            () -> assertTrue(repositorio.transacciones.isEmpty()),
            () -> assertTrue(notificador.notificaciones.isEmpty())
        );
    }

    @Test
    void transferenciaExitosaGuardaYNotificaUnaSolaVez() {
        servicio.transferir(origen, destino, 150_000, "OTRO_BANCO");

        assertAll(
            () -> assertEquals(1, repositorio.transacciones.size()),
            () -> assertEquals(1, notificador.notificaciones.size())
        );
        assertAll(
            () -> assertEquals(new RepositorioEnMemoria.Registro("001", "002", 150_000, 7_500),
                repositorio.transacciones.getFirst()),
            () -> assertEquals(new NotificadorEnMemoria.Notificacion("001", "002", 150_000),
                notificador.notificaciones.getFirst())
        );
    }

    @Test
    void tipoDesconocidoRechazaSinCambiarSaldos() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> servicio.transferir(origen, destino, 150_000, "DESCONOCIDO"));

        assertAll(
            () -> assertEquals("Tipo de transferencia desconocido", error.getMessage()),
            () -> assertEquals(1_000_000, origen.getSaldo()),
            () -> assertEquals(500_000, destino.getSaldo()),
            () -> assertTrue(repositorio.transacciones.isEmpty()),
            () -> assertTrue(notificador.notificaciones.isEmpty())
        );
    }
}
