package co.edu.unal.banco.productos.dominio;

public interface ProductoCredito {
    double calcularIntereses();
    void pagarCuota(double monto);
}
