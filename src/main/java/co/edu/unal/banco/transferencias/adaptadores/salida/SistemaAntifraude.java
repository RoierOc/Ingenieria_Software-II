package co.edu.unal.banco.transferencias.adaptadores.salida;

import co.edu.unal.banco.transferencias.dominio.CuentaTransaccional;
import co.edu.unal.banco.transferencias.puertos.salida.AuditoriaTransferencia;

public class SistemaAntifraude implements AuditoriaTransferencia {
    public void registrar(CuentaTransaccional origen, CuentaTransaccional destino, double monto, String tipo) {
        System.out.println("[ANTIFRAUDE] Reportando transacción " + tipo
            + " " + origen.getNumero() + " -> " + destino.getNumero() + " $" + monto);
    }
}
