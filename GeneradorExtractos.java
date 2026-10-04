import java.util.List;

public class GeneradorExtractos {
    public void imprimir(List<? extends ProductoBancario> productos) {
        for (ProductoBancario producto : productos) {
            System.out.println(producto.generarExtracto());
        }
    }
}
