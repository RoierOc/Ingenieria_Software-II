package co.edu.unal.banco.transferencias.puertos.entrada;

import co.edu.unal.banco.transferencias.dominio.CuentaTransaccional;

public interface TransferirFondos {
    void transferir(CuentaTransaccional origen, CuentaTransaccional destino, double monto, String tipo);
}
