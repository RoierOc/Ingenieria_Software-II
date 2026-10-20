package co.edu.unal.banco.transferencias.aplicacion;

import co.edu.unal.banco.productos.dominio.Cuenta;
import co.edu.unal.banco.productos.dominio.CuentaAhorros;
import co.edu.unal.banco.transferencias.adaptadores.salida.CanalNotificacionCompuesto;
import co.edu.unal.banco.transferencias.adaptadores.salida.NotificadorTransferencia;
import co.edu.unal.banco.transferencias.adaptadores.salida.PushGateway;
import co.edu.unal.banco.transferencias.adaptadores.salida.SmsGateway;
import co.edu.unal.banco.transferencias.dominio.CalculadoraComision;
import co.edu.unal.banco.transferencias.dominio.ComisionOtroBanco;
import co.edu.unal.banco.transferencias.dominio.ValidadorTransferencia;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificacionPushTest {
    @Test
    void transferenciaExitosaNotificaPorSmsYPorPush() {
        Cuenta origen = new CuentaAhorros("001", "Ana", 1_000_000);
        Cuenta destino = new CuentaAhorros("002", "Luis", 500_000);
        TransaccionService servicio = new TransaccionService(
            new ValidadorTransferencia(),
            new CalculadoraComision(Map.of("OTRO_BANCO", new ComisionOtroBanco())),
            new RepositorioEnMemoria(), (cuentaOrigen, cuentaDestino, monto, comision) -> {},
            new NotificadorTransferencia(new CanalNotificacionCompuesto(
                List.of(new SmsGateway(), new PushGateway()))),
            (cuentaOrigen, cuentaDestino, monto, tipo) -> {}
        );

        String salida = SalidaConsola.capturar(
            () -> servicio.transferir(origen, destino, 150_000, "OTRO_BANCO"));

        assertAll(
            () -> assertTrue(salida.contains("[SMS] Para Ana: Transferiste $150000.0 a la cuenta 002")),
            () -> assertTrue(salida.contains("[PUSH] Para Ana: Transferiste $150000.0 a la cuenta 002"))
        );
    }
}
