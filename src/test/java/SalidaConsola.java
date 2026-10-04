import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

class SalidaConsola {
    static String capturar(Runnable accion) {
        PrintStream original = System.out;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
        try {
            accion.run();
        } catch (RuntimeException rechazo) {
            // La transferencia rechazada también se inspecciona por su salida.
        } finally {
            System.setOut(original);
        }
        return salida.toString(StandardCharsets.UTF_8);
    }

    static long lineasQueEmpiezanCon(String salida, String prefijo) {
        return salida.lines().filter(linea -> linea.startsWith(prefijo)).count();
    }
}
