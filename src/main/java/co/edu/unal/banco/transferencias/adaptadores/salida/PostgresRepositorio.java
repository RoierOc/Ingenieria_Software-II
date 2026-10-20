package co.edu.unal.banco.transferencias.adaptadores.salida;

import co.edu.unal.banco.transferencias.puertos.salida.RepositorioTransacciones;

public class PostgresRepositorio implements RepositorioTransacciones {
    public void guardarTransaccion(String origen, String destino,
                                   double monto, double comision) {
        System.out.println("[POSTGRES] Conectando a jdbc:postgresql://prod-db:5432/banco...");
        System.out.println("[POSTGRES] INSERT INTO transacciones VALUES ('"
            + origen + "', '" + destino + "', " + monto + ", " + comision + ")");
    }
}
