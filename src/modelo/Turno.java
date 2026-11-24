package modelo;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  UNIDAD 3 · COLA (FIFO + PRIORIDAD) — turno de atención en el mostrador
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class Turno {

    /** Nivel 0 = se atiende primero. Dentro de un mismo nivel se respeta el orden de llegada (FIFO). */
    public enum Prioridad {
        PRIORITARIO(0, "Prioritario"),
        PREFERENTE(1, "Preferente"),
        NORMAL(2, "Normal");

        private final int nivel;
        private final String etiqueta;

        Prioridad(int nivel, String etiqueta) {
            this.nivel = nivel;
            this.etiqueta = etiqueta;
        }

        public int getNivel() { return nivel; }

        @Override
        public String toString() { return etiqueta; }
    }

    private final int numero;
    private final String socio;
    private final String servicio;
    private final Prioridad prioridad;
    private final String horaLlegada;
    private String horaAtencion;

    public Turno(int numero, String socio, String servicio, Prioridad prioridad, String horaLlegada) {
        this.numero = numero;
        this.socio = socio;
        this.servicio = servicio;
        this.prioridad = prioridad;
        this.horaLlegada = horaLlegada;
    }

    public int getNumero() { return numero; }
    public String getCodigo() { return String.format("T-%03d", numero); }
    public String getSocio() { return socio; }
    public String getServicio() { return servicio; }
    public Prioridad getPrioridad() { return prioridad; }
    public String getHoraLlegada() { return horaLlegada; }
    public String getHoraAtencion() { return horaAtencion; }
    public void setHoraAtencion(String horaAtencion) { this.horaAtencion = horaAtencion; }

    @Override
    public String toString() {
        return getCodigo() + " · " + socio + " · " + servicio + " · " + prioridad.name();
    }
}
