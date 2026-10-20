package co.edu.unal.banco.productos.aplicacion;

import co.edu.unal.banco.productos.dominio.ProductoBancario;

import java.util.List;

public class GeneradorExtractos {
    public void imprimir(List<? extends ProductoBancario> productos) {
        for (ProductoBancario producto : productos) {
            System.out.println(producto.generarExtracto());
        }
    }
}
