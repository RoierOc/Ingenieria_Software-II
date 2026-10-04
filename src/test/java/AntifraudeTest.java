import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AntifraudeTest {
    private Cuenta destino;
    private TransaccionService servicio;

    @BeforeEach
    void prepararServicio() {
        destino = new CuentaAhorros("002", "Luis", 500_000);
        servicio = new TransaccionService(
            new ValidadorTransferencia(),
            new CalculadoraComision(Map.of("OTRO_BANCO", new ComisionOtroBanco())),
            new RepositorioEnMemoria(), (cuentaOrigen, cuentaDestino, monto, comision) -> {},
            new NotificadorEnMemoria(),
            new AuditoriaCompuesta(List.of(new RegistroAuditoria(), new SistemaAntifraude()))
        );
    }

    @Test
    void transferenciaExitosaGeneraAuditoriaYAntifraude() {
        Cuenta origen = new CuentaAhorros("001", "Ana", 1_000_000);

        String salida = SalidaConsola.capturar(
            () -> servicio.transferir(origen, destino, 150_000, "OTRO_BANCO"));

        assertAll(
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[AUDITORIA]")),
            () -> assertEquals(1, SalidaConsola.lineasQueEmpiezanCon(salida, "[ANTIFRAUDE]"))
        );
    }

    @Test
    void transferenciaRechazadaNoGeneraAuditoriaNiAntifraude() {
        Cuenta origen = new CuentaAhorros("001", "Ana", 100_000);

        String salida = SalidaConsola.capturar(
            () -> servicio.transferir(origen, destino, 150_000, "OTRO_BANCO"));

        assertAll(
            () -> assertEquals(100_000, origen.getSaldo()),
            () -> assertEquals(0, SalidaConsola.lineasQueEmpiezanCon(salida, "[AUDITORIA]")),
            () -> assertEquals(0, SalidaConsola.lineasQueEmpiezanCon(salida, "[ANTIFRAUDE]"))
        );
    }
}
