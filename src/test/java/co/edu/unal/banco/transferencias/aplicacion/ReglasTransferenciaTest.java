package co.edu.unal.banco.transferencias.aplicacion;

import co.edu.unal.banco.productos.dominio.Cuenta;
import co.edu.unal.banco.productos.dominio.CuentaAhorros;
import co.edu.unal.banco.transferencias.dominio.ComisionOtroBanco;
import co.edu.unal.banco.transferencias.dominio.CuentaTransaccional;
import co.edu.unal.banco.transferencias.dominio.CuentasDistintas;
import co.edu.unal.banco.transferencias.dominio.MontoPositivo;
import co.edu.unal.banco.transferencias.dominio.ReglaTransferencia;
import co.edu.unal.banco.transferencias.dominio.TopePorTransferencia;
import co.edu.unal.banco.transferencias.dominio.ValidacionTransferencia;
import co.edu.unal.banco.transferencias.puertos.entrada.TransferirFondos;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglasTransferenciaTest {
    @Test
    void transferenciaALaMismaCuentaSeRechazaSinCobrarComision() {
        Cuenta ana = new CuentaAhorros("001", "Ana", 1_000_000);
        InfraestructuraEnMemoria infraestructura = new InfraestructuraEnMemoria();
        TransferirFondos servicio = new TransaccionServiceBuilder()
            .comision("OTRO_BANCO", new ComisionOtroBanco())
            .infraestructura(infraestructura)
            .construir();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> servicio.transferir(ana, ana, 150_000, "OTRO_BANCO"));

        assertAll(
            () -> assertEquals("El origen y el destino deben ser cuentas distintas", error.getMessage()),
            () -> assertEquals(1_000_000, ana.getSaldo()),
            () -> assertTrue(infraestructura.repositorio.transacciones.isEmpty())
        );
    }

    @Test
    void laCadenaSeDetieneEnLaPrimeraReglaQueFalla() {
        List<String> evaluadas = new ArrayList<>();
        ValidacionTransferencia cadena = ReglaTransferencia.encadenar(
            new Registrada("monto", evaluadas, new MontoPositivo()),
            new Registrada("tope", evaluadas, new TopePorTransferencia(5_000_000)),
            new Registrada("cuentas", evaluadas, new CuentasDistintas()));
        Cuenta origen = new CuentaAhorros("001", "Ana", 1_000_000);
        Cuenta destino = new CuentaAhorros("002", "Luis", 0);

        assertThrows(IllegalArgumentException.class,
            () -> cadena.validar(origen, destino, 6_000_000));

        assertEquals(List.of("monto", "tope"), evaluadas);
    }

    @Test
    void unaReglaNuevaSeAgregaSinModificarLasExistentes() {
        ReglaTransferencia minimoDeMil = new ReglaTransferencia() {
            @Override
            protected void comprobar(CuentaTransaccional origen, CuentaTransaccional destino, double monto) {
                if (monto < 1_000) throw new IllegalArgumentException("Monto menor al mínimo");
            }
        };
        ValidacionTransferencia cadena = ReglaTransferencia.encadenar(
            new MontoPositivo(), minimoDeMil, new CuentasDistintas());
        Cuenta origen = new CuentaAhorros("001", "Ana", 1_000_000);
        Cuenta destino = new CuentaAhorros("002", "Luis", 0);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> cadena.validar(origen, destino, 500));

        assertEquals("Monto menor al mínimo", error.getMessage());
    }

    private static class Registrada extends ReglaTransferencia {
        private final String nombre;
        private final List<String> evaluadas;
        private final ReglaTransferencia regla;

        Registrada(String nombre, List<String> evaluadas, ReglaTransferencia regla) {
            this.nombre = nombre;
            this.evaluadas = evaluadas;
            this.regla = regla;
        }

        @Override
        protected void comprobar(CuentaTransaccional origen, CuentaTransaccional destino, double monto) {
            evaluadas.add(nombre);
            regla.validar(origen, destino, monto);
        }
    }
}
