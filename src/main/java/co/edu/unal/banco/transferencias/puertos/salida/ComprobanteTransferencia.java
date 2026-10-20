package co.edu.unal.banco.transferencias.puertos.salida;

import co.edu.unal.banco.transferencias.dominio.CuentaTransaccional;

public interface ComprobanteTransferencia {
    void generar(CuentaTransaccional origen, CuentaTransaccional destino, double monto, double comision);
}
