package co.edu.unal.banco.transferencias.puertos.salida;

import co.edu.unal.banco.transferencias.dominio.CuentaTransaccional;

public interface NotificacionTransferencia {
    void notificar(CuentaTransaccional origen, CuentaTransaccional destino, double monto);
}
