package co.edu.unal.banco.entrada.consola;

import co.edu.unal.banco.productos.aplicacion.CobroCuotaManejo;
import co.edu.unal.banco.productos.aplicacion.GeneradorExtractos;
import co.edu.unal.banco.productos.dominio.CDT;
import co.edu.unal.banco.productos.dominio.CreditoVivienda;
import co.edu.unal.banco.productos.dominio.Cuenta;
import co.edu.unal.banco.productos.dominio.CuentaAhorros;
import co.edu.unal.banco.productos.dominio.ProductoBancario;
import co.edu.unal.banco.productos.dominio.TarjetaCredito;
import co.edu.unal.banco.transferencias.adaptadores.salida.InfraestructuraProduccion;
import co.edu.unal.banco.transferencias.aplicacion.TransaccionServiceBuilder;
import co.edu.unal.banco.transferencias.dominio.ComisionInternacional;
import co.edu.unal.banco.transferencias.dominio.ComisionLlave;
import co.edu.unal.banco.transferencias.dominio.ComisionMismoBanco;
import co.edu.unal.banco.transferencias.dominio.ComisionOtroBanco;
import co.edu.unal.banco.transferencias.puertos.entrada.TransferirFondos;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Cuenta ana   = new CuentaAhorros("001-1", "Ana", 2_000_000);
        Cuenta luis  = new CuentaAhorros("001-2", "Luis", 500_000);
        CDT cdtAna = new CDT("CDT-9", "Ana", 10_000_000, LocalDate.now().plusMonths(6));

        TransferirFondos servicio = new TransaccionServiceBuilder()
            .comision("MISMO_BANCO", new ComisionMismoBanco())
            .comision("OTRO_BANCO", new ComisionOtroBanco())
            .comision("INTERNACIONAL", new ComisionInternacional())
            .comision("LLAVE", new ComisionLlave())
            .infraestructura(new InfraestructuraProduccion())
            .construir();
        servicio.transferir(ana, luis, 150_000, "OTRO_BANCO");

        new CobroCuotaManejo().cobrarMensual(List.of(ana, luis));

        List<ProductoBancario> productos =
            List.of(new TarjetaCredito(3_000_000), new CreditoVivienda(120_000_000));
        new GeneradorExtractos().imprimir(productos);
    }
}
