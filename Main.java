import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Cuenta ana   = new CuentaAhorros("001-1", "Ana", 2_000_000);
        Cuenta luis  = new CuentaAhorros("001-2", "Luis", 500_000);
        CDT cdtAna = new CDT("CDT-9", "Ana", 10_000_000, LocalDate.now().plusMonths(6));

        CalculadoraComision calculadora = new CalculadoraComision(Map.of(
            "MISMO_BANCO", new ComisionMismoBanco(),
            "OTRO_BANCO", new ComisionOtroBanco(),
            "INTERNACIONAL", new ComisionInternacional(),
            "LLAVE", new ComisionLlave(),
            PagoServicios.TIPO_TRANSACCION, new ComisionFija(1_500)
        ));
        TransaccionService servicio = new TransaccionService(
            new ValidadorTransferencia(), calculadora, new PostgresRepositorio(),
            new GeneradorComprobante(),
            new NotificadorTransferencia(new CanalNotificacionCompuesto(
                List.of(new SmsGateway(), new PushGateway()))),
            new AuditoriaCompuesta(List.of(new RegistroAuditoria(), new SistemaAntifraude()))
        );
        servicio.transferir(ana, luis, 150_000, "OTRO_BANCO");
        new PagoServicios(servicio).pagar(ana, TipoServicio.LUZ, "REF-884213", 184_300);
        new CobroCuotaManejo().cobrarMensual(List.of(ana, luis));

        List<ProductoBancario> productos =
            List.of(new TarjetaCredito(3_000_000), new CreditoVivienda(120_000_000));
        new GeneradorExtractos().imprimir(productos);
    }
}
