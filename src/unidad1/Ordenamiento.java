package unidad1;

public class Ordenamiento {

    /** Ordenamiento burbuja por título (ascendente, ignorando mayúsculas/minúsculas). */
    public static void burbujaPorTitulo(Libro[] libros, int n) {
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (libros[j].getTitulo().compareToIgnoreCase(libros[j + 1].getTitulo()) > 0) {
                    Libro temp = libros[j];
                    libros[j] = libros[j + 1];
                    libros[j + 1] = temp;
                }
            }
        }
    }

    /** QuickSort por código (ascendente). Útil para catálogos grandes: O(n log n) en promedio. */
    public static void quickSortPorCodigo(Libro[] libros, int inicio, int fin) {
        if (inicio < fin) {
            int posicionPivote = particionar(libros, inicio, fin);
            quickSortPorCodigo(libros, inicio, posicionPivote - 1);
            quickSortPorCodigo(libros, posicionPivote + 1, fin);
        }
    }

    private static int particionar(Libro[] libros, int inicio, int fin) {
        int pivote = libros[fin].getCodigo();
        int i = inicio - 1;
        for (int j = inicio; j < fin; j++) {
            if (libros[j].getCodigo() <= pivote) {
                i++;
                Libro temp = libros[i];
                libros[i] = libros[j];
                libros[j] = temp;
            }
        }
        Libro temp = libros[i + 1];
        libros[i + 1] = libros[fin];
        libros[fin] = temp;
        return i + 1;
    }
}
