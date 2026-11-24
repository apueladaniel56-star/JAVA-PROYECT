package estructuras;
import modelo.Libro;
/**
 * Estructura bidimensional (matriz) que representa la ubicación física de los libros
 * dentro de la biblioteca: cada fila es un estante y cada columna una posición disponible
 * dentro de ese estante. Se usa el código del libro (0 = posición vacía) como contenido de
 * cada celda, lo que permite saber en O(1) qué libro ocupa una posición exacta y, mediante
 * un recorrido de la matriz, encontrar en qué estante y posición se encuentra un libro dado.
 *
 * Esta matriz resuelve un problema real del negocio: sin ella, el personal de la biblioteca
 * tendría que recorrer físicamente los estantes para encontrar un libro aunque el sistema
 * ya sepa que existe en el catálogo.
 */
public class EstanteMatriz {

    private final int[][] estantes;
    private final int filas;
    private final int columnas;

    public EstanteMatriz(int filas, int columnas) {
        this.filas = filas;
        this.columnas = columnas;
        this.estantes = new int[filas][columnas];
        // 0 representa una posición vacía (ningún libro tiene código 0 en este sistema)
    }

    public int getFilas() { return filas; }
    public int getColumnas() { return columnas; }

    private void validarPosicion(int fila, int columna) {
        if (fila < 0 || fila >= filas || columna < 0 || columna >= columnas) {
            throw new IllegalArgumentException("Posición fuera del rango de la estantería (" + filas + "x" + columnas + ").");
        }
    }

    public boolean estaOcupada(int fila, int columna) {
        validarPosicion(fila, columna);
        return estantes[fila][columna] != 0;
    }

    /** Asigna un libro a una posición libre de la matriz. Devuelve false si la posición ya está ocupada. */
    public boolean asignar(int fila, int columna, int codigoLibro) {
        validarPosicion(fila, columna);
        if (estantes[fila][columna] != 0) {
            return false;
        }
        estantes[fila][columna] = codigoLibro;
        return true;
    }

    /** Libera la posición donde estaba un libro (por ejemplo, al retirarlo del catálogo). */
    public void liberar(int fila, int columna) {
        validarPosicion(fila, columna);
        estantes[fila][columna] = 0;
    }

    /** Recorre la matriz buscando en qué estante/posición está un código de libro. */
    public int[] buscarPorCodigo(int codigoLibro) {
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                if (estantes[f][c] == codigoLibro) {
                    return new int[]{f, c};
                }
            }
        }
        return null; // no tiene ubicación asignada todavía
    }

    /** Busca la primera posición libre disponible (recorrido fila por fila). */
    public int[] buscarPrimeraLibre() {
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                if (estantes[f][c] == 0) {
                    return new int[]{f, c};
                }
            }
        }
        return null; // estantería llena
    }

    /** Devuelve una representación imprimible del mapa completo de la estantería. */
    public String mostrarMapa() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Mapa de estantería (fila = estante, columna = posición) ---\n");
        sb.append("        ");
        for (int c = 0; c < columnas; c++) {
            sb.append(String.format("Pos%-2d ", c));
        }
        sb.append("\n");
        for (int f = 0; f < filas; f++) {
            sb.append(String.format("Estante%-2d", f));
            for (int c = 0; c < columnas; c++) {
                if (estantes[f][c] == 0) {
                    sb.append(" [ --- ]");
                } else {
                    sb.append(String.format(" [%03d]", estantes[f][c]));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
