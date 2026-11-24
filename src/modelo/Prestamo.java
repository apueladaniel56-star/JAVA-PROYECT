package modelo;

public class Prestamo {
    private int codigoLibro;
    private String tituloLibro;
    private String socio;
    private String fecha;

    public Prestamo(int codigoLibro, String tituloLibro, String socio, String fecha) {
        this.codigoLibro = codigoLibro;
        this.tituloLibro = tituloLibro;
        this.socio = socio;
        this.fecha = fecha;
    }

    public int getCodigoLibro() { return codigoLibro; }
    public String getTituloLibro() { return tituloLibro; }
    public String getSocio() { return socio; }
    public String getFecha() { return fecha; }

    @Override
    public String toString() {
        return String.format("Préstamo -> Libro [%03d] %s | Socio: %s | Fecha: %s",
                codigoLibro, tituloLibro, socio, fecha);
    }
}
