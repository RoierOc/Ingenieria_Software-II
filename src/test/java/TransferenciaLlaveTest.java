import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferenciaLlaveTest {
    @Test
    void llaveDescuentaExactamenteElMontoSinComision() {
        Cuenta origen = new CuentaAhorros("001", "Ana", 1_000_000);
        Cuenta destino = new CuentaAhorros("002", "Luis", 500_000);
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        TransaccionService servicio = new TransaccionService(
            new ValidadorTransferencia(),
            new CalculadoraComision(Map.of("LLAVE", new ComisionLlave())),
            repositorio, (cuentaOrigen, cuentaDestino, monto, comision) -> {},
            new NotificadorEnMemoria(), (cuentaOrigen, cuentaDestino, monto, tipo) -> {}
        );

        servicio.transferir(origen, destino, 50_000, "LLAVE");

        assertAll(
            () -> assertEquals(950_000, origen.getSaldo()),
            () -> assertEquals(550_000, destino.getSaldo()),
            () -> assertEquals(0, repositorio.transacciones.getFirst().comision())
        );
    }
}
