package modelo;

import java.util.Comparator;

/**
 * Libro del catálogo (una fila de la tabla libro).
 *
 * Es la única clase Libro del proyecto: la usan tanto el primer avance (arreglo,
 * ordenamiento, búsqueda, árbol, pila, cola, matriz de estantería) como la Unidad 2
 * (listas enlazadas simples y dobles circulares).
 *
 * La ubicación física se guarda como texto de estantería ("A1", "B3", ...):
 * la letra es la fila (A = 0) y el número es la columna (1 = 0). La matriz de
 * estantería del primer avance trabaja con (fila, columna) numéricos, que se
 * obtienen de ese mismo texto.
 */
public class Libro {

    public static final Comparator<Libro> POR_CODIGO = Comparator.comparingInt(Libro::getCodigo);
    public static final Comparator<Libro> POR_TITULO = (a, b) -> a.titulo.compareToIgnoreCase(b.titulo);
    public static final Comparator<Libro> POR_AUTOR = (a, b) -> a.autor.compareToIgnoreCase(b.autor);
    public static final Comparator<Libro> POR_DISPONIBLES = Comparator.comparingInt(Libro::getCopiasDisponibles);

    private final int codigo;
    private String titulo;
    private String autor;
    private String categoria;
    private String ubicacion; // null si no tiene estante
    private int copiasTotales;
    private int copiasDisponibles;

    public Libro(int codigo, String titulo, String autor, String categoria,
                 String ubicacion, int copiasTotales, int copiasDisponibles) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.ubicacion = (ubicacion == null || ubicacion.isBlank()) ? null : ubicacion.trim().toUpperCase();
        this.copiasTotales = copiasTotales;
        this.copiasDisponibles = copiasDisponibles;
    }

    /** Libro nuevo: sin estante y con todas las copias disponibles. */
    public Libro(int codigo, String titulo, String autor, String categoria, int copiasTotales) {
        this(codigo, titulo, autor, categoria, null, copiasTotales, copiasTotales);
    }

    /**
     * UNIDAD 3 · PILA (LIFO): copia independiente del libro. La pila de deshacer guarda
     * "fotos" del libro antes/después de cada acción para poder revertirla.
     */
    public Libro copiar() {
        return new Libro(codigo, titulo, autor, categoria, ubicacion, copiasTotales, copiasDisponibles);
    }

    public int getCodigo() { return codigo; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getCategoria() { return categoria; }
    public String getUbicacion() { return ubicacion; }
    public int getCopiasTotales() { return copiasTotales; }
    public int getCopiasDisponibles() { return copiasDisponibles; }

    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setAutor(String autor) { this.autor = autor; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setUbicacion(String ubicacion) {
        this.ubicacion = (ubicacion == null || ubicacion.isBlank()) ? null : ubicacion.trim().toUpperCase();
    }
    public void setCopiasDisponibles(int copiasDisponibles) { this.copiasDisponibles = copiasDisponibles; }

    /** Actualiza el total de copias, ajustando las disponibles según las que están prestadas. */
    public void setCopiasTotales(int nuevoTotal) {
        int prestadas = copiasTotales - copiasDisponibles;
        this.copiasTotales = nuevoTotal;
        this.copiasDisponibles = Math.max(0, nuevoTotal - prestadas);
    }

    // ---------------------- Ubicación en la matriz de estantería ----------------------

    /** Fila de la matriz (A = 0), o -1 si no tiene ubicación válida. */
    public int getFilaEstante() {
        if (ubicacion == null || ubicacion.length() < 2) return -1;
        char c = ubicacion.charAt(0);
        return (c >= 'A' && c <= 'Z') ? c - 'A' : -1;
    }

    /** Columna de la matriz (1 = 0), o -1 si no tiene ubicación válida. */
    public int getColumnaEstante() {
        if (ubicacion == null || ubicacion.length() < 2) return -1;
        try {
            return Integer.parseInt(ubicacion.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public boolean tieneUbicacion() {
        return getFilaEstante() >= 0 && getColumnaEstante() >= 0;
    }

    public void asignarUbicacion(int fila, int columna) {
        this.ubicacion = "" + (char) ('A' + fila) + (columna + 1);
    }

    public void liberarUbicacion() {
        this.ubicacion = null;
    }

    // ---------------------- Préstamos ----------------------

    public boolean hayDisponible() {
        return copiasDisponibles > 0;
    }

    /** Reduce en 1 las copias disponibles (préstamo). Devuelve false si no hay stock. */
    public boolean prestarCopia() {
        if (copiasDisponibles <= 0) return false;
        copiasDisponibles--;
        return true;
    }

    /** Aumenta en 1 las copias disponibles (devolución), sin superar el total. */
    public void devolverCopia() {
        if (copiasDisponibles < copiasTotales) {
            copiasDisponibles++;
        }
    }

    /** True si el texto aparece en el título, autor o categoría (sin distinguir mayúsculas). */
    public boolean coincideCon(String texto) {
        String t = texto.toLowerCase();
        return titulo.toLowerCase().contains(t)
                || autor.toLowerCase().contains(t)
                || categoria.toLowerCase().contains(t);
    }

    @Override
    public String toString() {
        return String.format("[%03d] %-35s | Autor: %-20s | Cat: %-12s | Disp: %d/%d",
                codigo, titulo, autor, categoria, copiasDisponibles, copiasTotales);
    }
}
