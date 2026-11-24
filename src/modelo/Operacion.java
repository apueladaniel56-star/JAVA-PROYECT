package modelo;

public class Operacion {
    public enum Tipo { PRESTAMO, DEVOLUCION }

    private Tipo tipo;
    private int codigoLibro;
    private String socio;
    private String fecha;

    public Operacion(Tipo tipo, int codigoLibro, String socio, String fecha) {
        this.tipo = tipo;
        this.codigoLibro = codigoLibro;
        this.socio = socio;
        this.fecha = fecha;
    }

    public Tipo getTipo() { return tipo; }
    public int getCodigoLibro() { return codigoLibro; }
    public String getSocio() { return socio; }
    public String getFecha() { return fecha; }

    @Override
    public String toString() {
        return String.format("%s | Libro [%03d] | Socio: %s | Fecha: %s", tipo, codigoLibro, socio, fecha);
    }
}
