package modelo;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  UNIDAD 3 · PILA (LIFO) — elemento que se guarda en la pila de deshacer
 * ═══════════════════════════════════════════════════════════════════════════
 * Representa UNA acción hecha sobre el catálogo (agregar, eliminar, modificar o
 * prestar/devolver). Guarda el estado del libro ANTES y DESPUÉS, de modo que la
 * acción se pueda revertir (deshacer) y volver a aplicar (rehacer).
 *
 *   AGREGAR    antes = null      despues = libro agregado
 *   ELIMINAR   antes = libro     despues = null
 *   MODIFICAR  antes = libro     despues = libro cambiado (incluye préstamos/devoluciones)
 */
public class AccionCatalogo {

    public enum Tipo { AGREGAR, ELIMINAR, MODIFICAR }

    private final Tipo tipo;
    private final Libro antes;
    private final Libro despues;
    private final String detalle;

    public AccionCatalogo(Tipo tipo, Libro antes, Libro despues, String detalle) {
        this.tipo = tipo;
        this.antes = antes;
        this.despues = despues;
        this.detalle = detalle;
    }

    public Tipo getTipo() { return tipo; }
    public Libro getAntes() { return antes; }
    public Libro getDespues() { return despues; }
    public String getDetalle() { return detalle; }

    public int getCodigo() {
        return (despues != null ? despues : antes).getCodigo();
    }

    public String getTitulo() {
        return (despues != null ? despues : antes).getTitulo();
    }

    @Override
    public String toString() {
        return tipo + " · [" + getCodigo() + "] " + getTitulo() + (detalle == null ? "" : " (" + detalle + ")");
    }
}
