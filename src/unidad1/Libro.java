package unidad1;

public class Libro {
    private int codigo;
    private String titulo;
    private String autor;
    private String categoria;
    private int copiasTotales;
    private int copiasDisponibles;

    // Ubicación física del libro dentro de la matriz de estantería (fila = estante, columna = posición).
    // -1 indica que todavía no se le ha asignado un lugar físico.
    private int filaEstante = -1;
    private int columnaEstante = -1;

    public Libro(int codigo, String titulo, String autor, String categoria, int copiasTotales) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.copiasTotales = copiasTotales;
        this.copiasDisponibles = copiasTotales;
    }

    public int getCodigo() { return codigo; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getCategoria() { return categoria; }
    public int getCopiasTotales() { return copiasTotales; }
    public int getCopiasDisponibles() { return copiasDisponibles; }
    public int getFilaEstante() { return filaEstante; }
    public int getColumnaEstante() { return columnaEstante; }
    public boolean tieneUbicacion() { return filaEstante >= 0 && columnaEstante >= 0; }

    // ---------------------- Setters (necesarios para el CRUD: actualización) ----------------------

    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setAutor(String autor) { this.autor = autor; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    /** Actualiza el total de copias, ajustando las disponibles en la misma proporción. */
    public void setCopiasTotales(int nuevoTotal) {
        int prestadas = copiasTotales - copiasDisponibles;
        this.copiasTotales = nuevoTotal;
        this.copiasDisponibles = Math.max(0, nuevoTotal - prestadas);
    }

    public void asignarUbicacion(int fila, int columna) {
        this.filaEstante = fila;
        this.columnaEstante = columna;
    }

    public void liberarUbicacion() {
        this.filaEstante = -1;
        this.columnaEstante = -1;
    }

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

    @Override
    public String toString() {
        return String.format("[%03d] %-35s | Autor: %-20s | Cat: %-12s | Disp: %d/%d",
                codigo, titulo, autor, categoria, copiasDisponibles, copiasTotales);
    }
}
