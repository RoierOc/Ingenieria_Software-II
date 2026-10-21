package co.edu.unal.banco.transferencias.aplicacion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comprueba AC-03: el núcleo no importa adaptadores ni la consola. */
class ArquitecturaHexagonalTest {
    private static final Path RAIZ = Path.of("src/main/java/co/edu/unal/banco");

    private List<String> importacionesProhibidas(Path carpeta) throws IOException {
        try (Stream<Path> archivos = Files.walk(carpeta)) {
            return archivos.filter(p -> p.toString().endsWith(".java"))
                .flatMap(p -> lineas(p).filter(l -> l.startsWith("import co.edu.unal.banco.")
                        && (l.contains(".adaptadores.") || l.startsWith("import co.edu.unal.banco.entrada.")))
                    .map(l -> p.getFileName() + ": " + l))
                .toList();
        }
    }

    private Stream<String> lineas(Path archivo) {
        try {
            return Files.readAllLines(archivo).stream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void dominioPuertosYProductosNoImportanAdaptadores() throws IOException {
        assertTrue(Files.isDirectory(RAIZ.resolve("transferencias/dominio")));
        assertEquals(List.of(), importacionesProhibidas(RAIZ.resolve("transferencias/dominio")));
        assertEquals(List.of(), importacionesProhibidas(RAIZ.resolve("transferencias/puertos")));
        assertEquals(List.of(), importacionesProhibidas(RAIZ.resolve("productos")));
    }

    @Test
    void laAplicacionSoloConocePuertosYDominio() throws IOException {
        assertEquals(List.of(), importacionesProhibidas(RAIZ.resolve("transferencias/aplicacion")));
    }
}
