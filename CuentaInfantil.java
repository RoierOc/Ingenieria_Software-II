import java.time.Clock;
import java.time.LocalDate;

public class CuentaInfantil extends Cuenta {
    private static final double LIMITE_RETIRO_DIARIO = 200_000;

    private final Clock reloj;
    private LocalDate diaRetiros;
    private double retiradoEnElDia;

    public CuentaInfantil(String numero, String titular, double saldoInicial) {
        this(numero, titular, saldoInicial, Clock.systemDefaultZone());
    }

    public CuentaInfantil(String numero, String titular, double saldoInicial, Clock reloj) {
        super(numero, titular, saldoInicial);
        this.reloj = reloj;
    }

    @Override
    public void retirar(double monto) {
        LocalDate hoy = LocalDate.now(reloj);
        if (!hoy.equals(diaRetiros)) {
            diaRetiros = hoy;
            retiradoEnElDia = 0;
        }
        if (retiradoEnElDia + monto > LIMITE_RETIRO_DIARIO) {
            throw new IllegalStateException("Supera el límite diario de retiros");
        }
        super.retirar(monto);
        retiradoEnElDia += monto;
    }
}
