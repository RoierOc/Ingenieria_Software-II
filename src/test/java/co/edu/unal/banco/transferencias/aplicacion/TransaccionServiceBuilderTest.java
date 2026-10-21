package co.edu.unal.banco.transferencias.aplicacion;

import co.edu.unal.banco.productos.dominio.Cuenta;
import co.edu.unal.banco.productos.dominio.CuentaAhorros;
import co.edu.unal.banco.transferencias.dominio.ComisionMismoBanco;
import co.edu.unal.banco.transferencias.dominio.ComisionOtroBanco;
import co.edu.unal.banco.transferencias.puertos.entrada.TransferirFondos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransaccionServiceBuilderTest {
    @Test
    void construyeElServicioConLaFamiliaEnMemoria() {
        Cuenta origen = new CuentaAhorros("001", "Ana", 1_000_000);
        Cuenta destino = new CuentaAhorros("002", "Luis", 500_000);
        InfraestructuraEnMemoria infraestructura = new InfraestructuraEnMemoria();
        TransferirFondos servicio = new TransaccionServiceBuilder()
            .comision("OTRO_BANCO", new ComisionOtroBanco())
            .infraestructura(infraestructura)
            .construir();

        servicio.transferir(origen, destino, 150_000, "OTRO_BANCO");

        assertAll(
            () -> assertEquals(842_500, origen.getSaldo()),
            () -> assertEquals(new RepositorioEnMemoria.Registro("001", "002", 150_000, 7_500),
                infraestructura.repositorio.transacciones.getFirst()),
            () -> assertEquals(1, infraestructura.notificador.notificaciones.size())
        );
    }

    @Test
    void comisionNuevaSeRegistraSinModificarElServicioNiLaCalculadora() {
        Cuenta origen = new CuentaAhorros("001", "Ana", 1_000_000);
        Cuenta destino = new CuentaAhorros("002", "Luis", 0);
        InfraestructuraEnMemoria infraestructura = new InfraestructuraEnMemoria();
        TransferirFondos servicio = new TransaccionServiceBuilder()
            .comision("PRIORITARIA", monto -> monto * 0.01)
            .infraestructura(infraestructura)
            .construir();

        servicio.transferir(origen, destino, 200_000, "PRIORITARIA");

        assertAll(
            () -> assertEquals(798_000, origen.getSaldo()),
            () -> assertEquals(2_000, infraestructura.repositorio.transacciones.getFirst().comision())
        );
    }

    @Test
    void sinInfraestructuraNoSeConstruye() {
        TransaccionServiceBuilder builder = new TransaccionServiceBuilder()
            .comision("MISMO_BANCO", new ComisionMismoBanco());

        IllegalStateException error = assertThrows(IllegalStateException.class, builder::construir);

        assertEquals("Falta la infraestructura de transferencias", error.getMessage());
    }

    @Test
    void unTipoNoSePuedeRegistrarDosVeces() {
        TransaccionServiceBuilder builder = new TransaccionServiceBuilder()
            .comision("OTRO_BANCO", new ComisionOtroBanco());

        assertThrows(IllegalArgumentException.class,
            () -> builder.comision("OTRO_BANCO", new ComisionMismoBanco()));
    }
}
