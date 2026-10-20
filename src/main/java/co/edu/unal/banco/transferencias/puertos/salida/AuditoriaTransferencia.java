package co.edu.unal.banco.transferencias.puertos.salida;

import co.edu.unal.banco.transferencias.dominio.CuentaTransaccional;

public interface AuditoriaTransferencia {
    void registrar(CuentaTransaccional origen, CuentaTransaccional destino, double monto, String tipo);
}
