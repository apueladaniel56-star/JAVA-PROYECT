package unidad1;

public class Busqueda {

    /** Búsqueda secuencial por código. O(n). */
    public static Libro secuencialPorCodigo(Libro[] libros, int n, int codigo) {
        for (int i = 0; i < n; i++) {
            if (libros[i].getCodigo() == codigo) return libros[i];
        }
        return null;
    }

    /**
     * Búsqueda binaria por título. REQUIERE que el arreglo ya esté ordenado
     * alfabéticamente por título (ver Ordenamiento.burbujaPorTitulo). O(log n).
     */
    public static Libro binariaPorTitulo(Libro[] libros, int n, String tituloBuscado) {
        int izquierda = 0;
        int derecha = n - 1;
        while (izquierda <= derecha) {
            int medio = (izquierda + derecha) / 2;
            int comparacion = libros[medio].getTitulo().compareToIgnoreCase(tituloBuscado);
            if (comparacion == 0) {
                return libros[medio];
            } else if (comparacion < 0) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }
        return null;
    }
}
