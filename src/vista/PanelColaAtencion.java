package vista;

import estructuras.ColaPrioridad;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.Turno;


public class PanelColaAtencion extends JPanel {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String[] SERVICIOS = {"Préstamo de libro", "Devolución", "Consulta de catálogo", "Inscripción de socio"};

    // UNIDAD 3 · COLA: la estructura que guarda a las personas en espera
    private final ColaPrioridad<Turno> cola = new ColaPrioridad<>(Turno.Prioridad.values().length);
    private int siguienteNumero = 1;

    private final JTextField txtSocio = EstiloUI.campo(14);
    private final JComboBox<String> cbServicio = new JComboBox<>(SERVICIOS);
    private final JComboBox<Turno.Prioridad> cbPrioridad = new JComboBox<>(Turno.Prioridad.values());
    private final JLabel lblEnAtencion = new JLabel("Ventanilla libre");
    private final JLabel lblResumen = EstiloUI.etiquetaInfo(" ");
    private final DefaultTableModel modeloCola = new DefaultTableModel(
            new Object[]{"Orden de atención", "Turno", "Socio", "Servicio", "Prioridad", "Llegada"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel modeloAtendidos = new DefaultTableModel(
            new Object[]{"Turno", "Socio", "Servicio", "Prioridad", "Llegada", "Atendido"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public PanelColaAtencion() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        // ---- Barra superior: registrar turno ----
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        top.setOpaque(false);
        top.add(new JLabel("Socio:"));
        top.add(txtSocio);
        top.add(new JLabel("Servicio:"));
        top.add(cbServicio);
        top.add(new JLabel("Prioridad:"));
        cbPrioridad.setSelectedItem(Turno.Prioridad.NORMAL);
        top.add(cbPrioridad);

        JButton btnGenerar = EstiloUI.boton("Generar turno (encolar)", EstiloUI.AZUL);
        btnGenerar.addActionListener(e -> generarTurno());
        top.add(btnGenerar);

        JButton btnEjemplo = EstiloUI.boton("Cargar ejemplo", EstiloUI.GRIS);
        btnEjemplo.addActionListener(e -> cargarEjemplo());
        top.add(btnEjemplo);

        JLabel info = EstiloUI.etiquetaInfo("Prioritario = adulto mayor / gestante / discapacidad · Preferente = docente / con reserva · Normal. Entre niveles gana el más urgente; dentro del mismo nivel gana quien llegó primero (FIFO).");
        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(top, BorderLayout.CENTER);
        norte.add(info, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        // ---- Centro: tabla de la cola y de atendidos ----
        JTable tablaCola = new JTable(modeloCola);
        EstiloUI.tabla(tablaCola);
        JTable tablaAtendidos = new JTable(modeloAtendidos);
        EstiloUI.tabla(tablaAtendidos);

        JPanel pCola = titulado("En espera (frente de la cola arriba)", new JScrollPane(tablaCola));
        JPanel pAtendidos = titulado("Ya atendidos (orden en que salieron)", new JScrollPane(tablaAtendidos));
        JSplitPane centro = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pCola, pAtendidos);
        centro.setResizeWeight(0.6);
        centro.setBorder(null);
        add(centro, BorderLayout.CENTER);

        // ---- Parte inferior: atender ----
        JPanel sur = new JPanel(new BorderLayout(10, 4));
        sur.setOpaque(false);
        lblEnAtencion.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblEnAtencion.setForeground(EstiloUI.VERDE.darker());
        JPanel izq = new JPanel(new GridLayout(2, 1));
        izq.setOpaque(false);
        izq.add(lblEnAtencion);
        izq.add(lblResumen);
        sur.add(izq, BorderLayout.CENTER);

        JButton btnAtender = EstiloUI.boton("Atender siguiente (desencolar)", EstiloUI.VERDE);
        btnAtender.addActionListener(e -> atenderSiguiente());
        sur.add(btnAtender, BorderLayout.EAST);
        add(sur, BorderLayout.SOUTH);

        actualizarVista();
    }

    private JPanel titulado(String titulo, JComponent contenido) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(titulo);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(l, BorderLayout.NORTH);
        p.add(contenido, BorderLayout.CENTER);
        return p;
    }

    // ---------------------------- ENCOLAR ----------------------------
    // UNIDAD 3 · COLA — operación ENCOLAR (la persona entra al final de la cola de su prioridad)
    private void generarTurno() {
        String socio = txtSocio.getText().trim();
        if (socio.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba el nombre del socio.", "Falta dato", JOptionPane.WARNING_MESSAGE);
            return;
        }
        encolar(socio, (String) cbServicio.getSelectedItem(), (Turno.Prioridad) cbPrioridad.getSelectedItem());
        txtSocio.setText("");
        txtSocio.requestFocus();
    }

    private void encolar(String socio, String servicio, Turno.Prioridad prioridad) {
        Turno t = new Turno(siguienteNumero++, socio, servicio, prioridad, LocalTime.now().format(HORA));
        cola.encolar(t, prioridad.getNivel());
        actualizarVista();
    }

    // ---------------------------- DESENCOLAR ----------------------------
    // UNIDAD 3 · COLA — operación DESENCOLAR (sale el frente de la cola más prioritaria que tenga gente)
    private void atenderSiguiente() {
        Turno t = cola.desencolar();
        if (t == null) {
            lblEnAtencion.setText("No hay personas en espera");
            return;
        }
        t.setHoraAtencion(LocalTime.now().format(HORA));
        modeloAtendidos.addRow(new Object[]{t.getCodigo(), t.getSocio(), t.getServicio(),
                t.getPrioridad().name(), t.getHoraLlegada(), t.getHoraAtencion()});
        lblEnAtencion.setText("Atendiendo: " + t.getCodigo() + " · " + t.getSocio() + " · " + t.getServicio());
        actualizarVista();
    }

    /** Carga llegadas mezcladas para demostrar la prioridad: los PRIORITARIO pasan primero aunque lleguen después. */
    private void cargarEjemplo() {
        encolar("Ana Quispe", "Préstamo de libro", Turno.Prioridad.NORMAL);
        encolar("Luis Rojas", "Devolución", Turno.Prioridad.NORMAL);
        encolar("Prof. Medina", "Consulta de catálogo", Turno.Prioridad.PREFERENTE);
        encolar("Sra. Elena (adulta mayor)", "Préstamo de libro", Turno.Prioridad.PRIORITARIO);
        encolar("Marco Díaz", "Inscripción de socio", Turno.Prioridad.NORMAL);
        encolar("Rosa (gestante)", "Devolución", Turno.Prioridad.PRIORITARIO);
    }

    private void actualizarVista() {
        modeloCola.setRowCount(0);
        int orden = 1;
        for (Turno t : cola.aListaEnOrdenDeAtencion()) {
            String posicion = (orden == 1) ? "1  ← FRENTE" : String.valueOf(orden);
            orden++;
            modeloCola.addRow(new Object[]{posicion, t.getCodigo(), t.getSocio(),
                    t.getServicio(), t.getPrioridad().name(), t.getHoraLlegada()});
        }
        lblResumen.setText("En espera: " + cola.getTamanio()
                + "   |   Prioritarios: " + cola.getTamanioNivel(0)
                + "   Preferentes: " + cola.getTamanioNivel(1)
                + "   Normales: " + cola.getTamanioNivel(2));
    }
}
