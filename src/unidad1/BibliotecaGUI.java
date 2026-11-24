package unidad1;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interfaz gráfica del Sistema de Gestión de Préstamos para Biblioteca.
 *
 * Es la misma lógica de negocio y las mismas estructuras de datos que la versión de
 * consola (BibliotecaApp): arreglo de objetos con CRUD, matriz de estantería, árbol
 * binario de búsqueda, lista enlazada, pila y cola, más la persistencia en MySQL.
 * Lo único que cambia es que la entrada y salida ahora se hacen con componentes Swing
 * (JTable, JTextField, JTabbedPane) en vez de Scanner/System.out.
 *
 * Paleta plana (sin degradados): se usa Metal Look&Feel con el tema clásico
 * (DefaultMetalTheme), que pinta con colores sólidos, y botones propios
 * (FlatButton) que se dibujan a mano con relleno sólido.
 */
public class BibliotecaGUI extends JFrame {

    // ---------------------- PALETA DE COLORES (sólidos, sin degradados) ----------------------
    private static final Color WINE = new Color(0x9E1B32);
    private static final Color WINE_DARK = new Color(0x7A1526);
    private static final Color INK = new Color(0x1F1F1F);
    private static final Color LIGHT_BG = new Color(0xF3F1EE);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(0xDDD8D2);
    private static final Color OK = new Color(0x2E7D46);
    private static final Color WARN = new Color(0xB35C00);

    // ---------------------- DATOS Y ESTRUCTURAS (idénticas a la versión de consola) ----------------------
    private Libro[] catalogo = new Libro[50];
    private int totalLibros = 0;
    private final ArbolBinarioBusqueda indiceCodigos = new ArbolBinarioBusqueda();
    private final Pila<Operacion> historial = new Pila<>();
    private final ListaEnlazada<Prestamo> prestamosActivos = new ListaEnlazada<>();
    private final Map<Integer, Cola<String>> reservasPorLibro = new HashMap<>();
    private final EstanteMatriz estanteria = new EstanteMatriz(6, 8);

    // ---------------------- COMPONENTES ----------------------
    private JLabel statusBar;

    private DefaultTableModel modeloCatalogo;
    private JTable tablaCatalogo;
    private JTextField txtCodigo, txtTitulo, txtAutor, txtCategoria, txtCopias, txtBuscar;

    private DefaultTableModel modeloPrestamos;
    private JTable tablaPrestamos;
    private JTextField txtCodigoPrestamo, txtSocio;

    private DefaultTableModel modeloHistorial;
    private JTable tablaHistorial;

    private JTextArea areaReservas;

    private JPanel panelEstanteriaGrid;
    private JTextField txtCodigoEstante;
    private JLabel[][] celdasEstante;

    public BibliotecaGUI() {
        super("Sistema de Gestión de Préstamos - Biblioteca");
        aplicarEstiloPlano();
        cargarDatosIniciales();
        construirInterfaz();
        setSize(1080, 680);
        setMinimumSize(new Dimension(920, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // ---------------------- ESTILO (Look & Feel plano, sin degradados) ----------------------

    private void aplicarEstiloPlano() {
        try {
            javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new javax.swing.plaf.metal.DefaultMetalTheme());
            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (Exception ignored) { }

        UIManager.put("control", LIGHT_BG);
        UIManager.put("nimbusLightBackground", LIGHT_BG);
        UIManager.put("Panel.background", LIGHT_BG);
        UIManager.put("TabbedPane.contentAreaColor", LIGHT_BG);
        UIManager.put("TabbedPane.selected", CARD_BG);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.selectionBackground", WINE);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", INK);
        UIManager.put("TableHeader.foreground", Color.WHITE);
    }

    /** Botón dibujado a mano con relleno sólido (nunca degradado), con estado hover/pressed. */
    private static class FlatButton extends JButton {
        private final Color base;
        private final Color hover;
        private final Color pressed;
        private final Color textColor;

        FlatButton(String texto, Color base, Color hover, Color pressed, Color textColor) {
            super(texto);
            this.base = base;
            this.hover = hover;
            this.pressed = pressed;
            this.textColor = textColor;
            setFont(getFont().deriveFont(Font.BOLD, 13f));
            setForeground(textColor);
            setFocusPainted(false);
            setBorder(new EmptyBorder(10, 18, 10, 18));
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { repaint(); }
                @Override public void mouseExited(MouseEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = base;
            if (getModel().isPressed()) fill = pressed;
            else if (getModel().isRollover()) fill = hover;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private FlatButton botonPrimario(String texto) {
        return new FlatButton(texto, WINE, WINE_DARK, INK, Color.WHITE);
    }

    private FlatButton botonSecundario(String texto) {
        return new FlatButton(texto, new Color(0xE7E2DC), new Color(0xDCD5CC), new Color(0xCFC6BA), INK);
    }

    /** "Tarjeta" (panel blanco con borde sutil) usada como contenedor de cada sección. */
    private JPanel tarjeta(String titulo) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        if (titulo != null) {
            JLabel lbl = new JLabel(titulo);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 15f));
            lbl.setForeground(INK);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(lbl);
            panel.add(Box.createVerticalStrut(10));
        }
        return panel;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(new Color(0x555555));
        l.setFont(l.getFont().deriveFont(12f));
        return l;
    }

    // ---------------------- CONSTRUCCIÓN DE LA INTERFAZ ----------------------

    private void construirInterfaz() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT_BG);

        add(crearEncabezado(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(Font.BOLD, 13f));
        tabs.setBackground(LIGHT_BG);
        tabs.addTab("Catálogo", crearPanelCatalogo());
        tabs.addTab("Préstamos", crearPanelPrestamos());
        tabs.addTab("Reservas", crearPanelReservas());
        tabs.addTab("Historial", crearPanelHistorial());
        tabs.addTab("Estantería (Matriz)", crearPanelEstanteria());
        add(tabs, BorderLayout.CENTER);

        statusBar = new JLabel("Listo.");
        statusBar.setOpaque(true);
        statusBar.setBackground(INK);
        statusBar.setForeground(Color.WHITE);
        statusBar.setBorder(new EmptyBorder(8, 16, 8, 16));
        add(statusBar, BorderLayout.SOUTH);

        refrescarTodo();
    }

    private JPanel crearEncabezado() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WINE);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel titulo = new JLabel("Sistema de Gestión de Préstamos - Biblioteca");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JLabel subtitulo = new JLabel("UTP · Algoritmos y Estructuras de Datos");
        subtitulo.setForeground(new Color(0xF0D9DE));
        subtitulo.setFont(subtitulo.getFont().deriveFont(12f));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(subtitulo);

        header.add(textos, BorderLayout.WEST);
        return header;
    }

    private void mostrarEstado(String mensaje, Color color) {
        statusBar.setText(mensaje);
        statusBar.setForeground(color);
    }

    // ---------------------- TAB: CATÁLOGO ----------------------

    private JPanel crearPanelCatalogo() {
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(new EmptyBorder(16, 16, 16, 16));
        raiz.setBackground(LIGHT_BG);

        // ---- Formulario (izquierda) ----
        JPanel form = tarjeta("Registrar / actualizar libro");
        form.setPreferredSize(new Dimension(300, 0));

        txtCodigo = new JTextField();
        txtTitulo = new JTextField();
        txtAutor = new JTextField();
        txtCategoria = new JTextField();
        txtCopias = new JTextField();

        agregarCampo(form, "Código", txtCodigo);
        agregarCampo(form, "Título", txtTitulo);
        agregarCampo(form, "Autor", txtAutor);
        agregarCampo(form, "Categoría", txtCategoria);
        agregarCampo(form, "N° de copias", txtCopias);

        form.add(Box.createVerticalStrut(10));
        JPanel filaBotones1 = new JPanel(new GridLayout(1, 2, 8, 0));
        filaBotones1.setOpaque(false);
        FlatButton btnRegistrar = botonPrimario("Registrar");
        FlatButton btnActualizar = botonSecundario("Actualizar");
        filaBotones1.add(btnRegistrar);
        filaBotones1.add(btnActualizar);
        filaBotones1.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaBotones1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(filaBotones1);

        form.add(Box.createVerticalStrut(8));
        FlatButton btnEliminar = new FlatButton("Eliminar libro", new Color(0xC0392B), new Color(0xA5301F), INK, Color.WHITE);
        btnEliminar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEliminar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        form.add(btnEliminar);

        form.add(Box.createVerticalStrut(8));
        FlatButton btnLimpiar = botonSecundario("Limpiar formulario");
        btnLimpiar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLimpiar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        form.add(btnLimpiar);

        form.add(Box.createVerticalStrut(16));
        JLabel lblBuscar = new JLabel("Buscar en el catálogo");
        lblBuscar.setFont(lblBuscar.getFont().deriveFont(Font.BOLD, 14f));
        lblBuscar.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblBuscar);
        form.add(Box.createVerticalStrut(8));
        txtBuscar = new JTextField();
        agregarCampo(form, "Código o título", txtBuscar);
        JPanel filaBusqueda = new JPanel(new GridLayout(1, 2, 8, 0));
        filaBusqueda.setOpaque(false);
        FlatButton btnBuscarCodigo = botonSecundario("Por código (ABB)");
        FlatButton btnBuscarTitulo = botonSecundario("Por título (Bin.)");
        filaBusqueda.add(btnBuscarCodigo);
        filaBusqueda.add(btnBuscarTitulo);
        filaBusqueda.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaBusqueda.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        form.add(filaBusqueda);
        form.add(Box.createVerticalGlue());

        // ---- Tabla (derecha) ----
        JPanel tablaPanel = tarjeta("Catálogo (ordenado por título)");
        modeloCatalogo = new DefaultTableModel(new Object[]{"Código", "Título", "Autor", "Categoría", "Total", "Disp.", "Estante"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaCatalogo = new JTable(modeloCatalogo);
        estilizarTabla(tablaCatalogo);
        tablaCatalogo.getSelectionModel().addListSelectionListener(e -> cargarSeleccionEnFormulario());
        JScrollPane scroll = new JScrollPane(tablaCatalogo);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        tablaPanel.add(scroll);

        raiz.add(form, BorderLayout.WEST);
        raiz.add(tablaPanel, BorderLayout.CENTER);

        btnRegistrar.addActionListener(e -> registrarLibro());
        btnActualizar.addActionListener(e -> actualizarLibro());
        btnEliminar.addActionListener(e -> eliminarLibro());
        btnLimpiar.addActionListener(e -> limpiarFormularioCatalogo());
        btnBuscarCodigo.addActionListener(e -> buscarPorCodigo());
        btnBuscarTitulo.addActionListener(e -> buscarPorTitulo());

        return raiz;
    }

    private void agregarCampo(JPanel contenedor, String etiqueta, JTextField campo) {
        JLabel lbl = etiqueta(etiqueta);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(4, 6, 4, 6)));
        contenedor.add(lbl);
        contenedor.add(Box.createVerticalStrut(2));
        contenedor.add(campo);
        contenedor.add(Box.createVerticalStrut(10));
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(26);
        tabla.setShowGrid(true);
        tabla.setGridColor(BORDER);
        tabla.setSelectionBackground(WINE);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.getTableHeader().setBackground(INK);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        tabla.setFont(tabla.getFont().deriveFont(13f));
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tablaCatalogo.getSelectedRow();
        if (fila < 0) return;
        txtCodigo.setText(modeloCatalogo.getValueAt(fila, 0).toString());
        txtTitulo.setText(modeloCatalogo.getValueAt(fila, 1).toString());
        txtAutor.setText(modeloCatalogo.getValueAt(fila, 2).toString());
        txtCategoria.setText(modeloCatalogo.getValueAt(fila, 3).toString());
        txtCopias.setText(modeloCatalogo.getValueAt(fila, 4).toString());
    }

    private void limpiarFormularioCatalogo() {
        txtCodigo.setText(""); txtTitulo.setText(""); txtAutor.setText("");
        txtCategoria.setText(""); txtCopias.setText("");
        tablaCatalogo.clearSelection();
    }

    // ---------------------- LÓGICA: CRUD DE LIBROS ----------------------

    private void registrarLibro() {
        try {
            int codigo = Integer.parseInt(txtCodigo.getText().trim());
            if (Busqueda.secuencialPorCodigo(catalogo, totalLibros, codigo) != null) {
                mostrarEstado("Ya existe un libro con el código " + codigo + ".", WARN);
                return;
            }
            String titulo = requerido(txtTitulo, "Título");
            String autor = requerido(txtAutor, "Autor");
            String categoria = requerido(txtCategoria, "Categoría");
            int copias = Integer.parseInt(txtCopias.getText().trim());

            if (totalLibros == catalogo.length) redimensionarCatalogo();
            Libro nuevo = new Libro(codigo, titulo, autor, categoria, copias);
            catalogo[totalLibros++] = nuevo;
            indiceCodigos.insertar(nuevo);
            reservasPorLibro.put(codigo, new Cola<>());
            LibroDAO.insertar(nuevo);

            limpiarFormularioCatalogo();
            refrescarTablaCatalogo();
            mostrarEstado("Libro registrado: " + nuevo.getTitulo(), OK);
        } catch (NumberFormatException ex) {
            mostrarEstado("Código y N° de copias deben ser números.", WARN);
        } catch (IllegalArgumentException ex) {
            mostrarEstado(ex.getMessage(), WARN);
        }
    }

    private void actualizarLibro() {
        try {
            int codigo = Integer.parseInt(txtCodigo.getText().trim());
            Libro libro = indiceCodigos.buscarPorCodigo(codigo);
            if (libro == null) {
                mostrarEstado("No existe un libro con ese código.", WARN);
                return;
            }
            if (!txtTitulo.getText().isBlank()) libro.setTitulo(txtTitulo.getText().trim());
            if (!txtAutor.getText().isBlank()) libro.setAutor(txtAutor.getText().trim());
            if (!txtCategoria.getText().isBlank()) libro.setCategoria(txtCategoria.getText().trim());
            if (!txtCopias.getText().isBlank()) libro.setCopiasTotales(Integer.parseInt(txtCopias.getText().trim()));

            LibroDAO.actualizar(libro);
            refrescarTablaCatalogo();
            mostrarEstado("Libro actualizado: " + libro.getTitulo(), OK);
        } catch (NumberFormatException ex) {
            mostrarEstado("Código y N° de copias deben ser números.", WARN);
        }
    }

    private void eliminarLibro() {
        try {
            int codigo = Integer.parseInt(txtCodigo.getText().trim());
            int posicion = -1;
            for (int i = 0; i < totalLibros; i++) {
                if (catalogo[i].getCodigo() == codigo) { posicion = i; break; }
            }
            if (posicion == -1) {
                mostrarEstado("No existe un libro con ese código.", WARN);
                return;
            }
            Libro libro = catalogo[posicion];
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar \"" + libro.getTitulo() + "\" del catálogo?", "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION);
            if (confirmacion != JOptionPane.YES_OPTION) return;

            for (int i = posicion; i < totalLibros - 1; i++) catalogo[i] = catalogo[i + 1];
            catalogo[totalLibros - 1] = null;
            totalLibros--;

            indiceCodigos.eliminar(codigo);
            reservasPorLibro.remove(codigo);
            if (libro.tieneUbicacion()) estanteria.liberar(libro.getFilaEstante(), libro.getColumnaEstante());
            LibroDAO.eliminar(codigo);

            limpiarFormularioCatalogo();
            refrescarTodo();
            mostrarEstado("Libro eliminado: " + libro.getTitulo(), OK);
        } catch (NumberFormatException ex) {
            mostrarEstado("Ingrese un código válido para eliminar.", WARN);
        }
    }

    private void buscarPorCodigo() {
        try {
            int codigo = Integer.parseInt(txtBuscar.getText().trim());
            Libro encontrado = indiceCodigos.buscarPorCodigo(codigo);
            if (encontrado == null) {
                mostrarEstado("No existe un libro con código " + codigo + ".", WARN);
            } else {
                mostrarEstado("Encontrado (Árbol Binario): " + encontrado.getTitulo() + " — " + encontrado.getAutor(), OK);
                seleccionarFilaPorCodigo(codigo);
            }
        } catch (NumberFormatException ex) {
            mostrarEstado("Ingrese un código numérico para buscar.", WARN);
        }
    }

    private void buscarPorTitulo() {
        String titulo = txtBuscar.getText().trim();
        if (titulo.isEmpty() || totalLibros == 0) {
            mostrarEstado("Escriba un título para buscar.", WARN);
            return;
        }
        Libro[] copia = copiarCatalogo();
        Ordenamiento.burbujaPorTitulo(copia, totalLibros);
        Libro encontrado = Busqueda.binariaPorTitulo(copia, totalLibros, titulo);
        if (encontrado == null) {
            mostrarEstado("No se encontró un libro con ese título exacto.", WARN);
        } else {
            mostrarEstado("Encontrado (Búsqueda Binaria): " + encontrado.getTitulo(), OK);
            seleccionarFilaPorCodigo(encontrado.getCodigo());
        }
    }

    private void seleccionarFilaPorCodigo(int codigo) {
        for (int i = 0; i < modeloCatalogo.getRowCount(); i++) {
            if (modeloCatalogo.getValueAt(i, 0).equals(codigo)) {
                tablaCatalogo.setRowSelectionInterval(i, i);
                tablaCatalogo.scrollRectToVisible(tablaCatalogo.getCellRect(i, 0, true));
                break;
            }
        }
    }

    private String requerido(JTextField campo, String nombre) {
        String valor = campo.getText().trim();
        if (valor.isEmpty()) throw new IllegalArgumentException("El campo \"" + nombre + "\" es obligatorio.");
        return valor;
    }

    // ---------------------- TAB: PRÉSTAMOS ----------------------

    private JPanel crearPanelPrestamos() {
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(new EmptyBorder(16, 16, 16, 16));
        raiz.setBackground(LIGHT_BG);

        JPanel form = tarjeta("Registrar préstamo / devolución");
        form.setPreferredSize(new Dimension(300, 0));
        txtCodigoPrestamo = new JTextField();
        txtSocio = new JTextField();
        agregarCampo(form, "Código del libro", txtCodigoPrestamo);
        agregarCampo(form, "Nombre del socio", txtSocio);

        FlatButton btnPrestar = botonPrimario("Prestar libro");
        btnPrestar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPrestar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        form.add(btnPrestar);
        form.add(Box.createVerticalStrut(8));

        FlatButton btnDevolver = botonSecundario("Devolver (libro seleccionado)");
        btnDevolver.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDevolver.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        form.add(btnDevolver);
        form.add(Box.createVerticalGlue());

        JPanel tablaPanel = tarjeta("Préstamos activos (Lista Enlazada)");
        modeloPrestamos = new DefaultTableModel(new Object[]{"Código", "Título", "Socio", "Fecha"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaPrestamos = new JTable(modeloPrestamos);
        estilizarTabla(tablaPrestamos);
        JScrollPane scroll = new JScrollPane(tablaPrestamos);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        tablaPanel.add(scroll);

        raiz.add(form, BorderLayout.WEST);
        raiz.add(tablaPanel, BorderLayout.CENTER);

        btnPrestar.addActionListener(e -> prestarLibro());
        btnDevolver.addActionListener(e -> devolverLibroSeleccionado());

        return raiz;
    }

    private void prestarLibro() {
        try {
            int codigo = Integer.parseInt(txtCodigoPrestamo.getText().trim());
            Libro libro = indiceCodigos.buscarPorCodigo(codigo);
            if (libro == null) {
                mostrarEstado("No existe un libro con ese código.", WARN);
                return;
            }
            String socio = requerido(txtSocio, "Nombre del socio");
            String fecha = LocalDate.now().toString();

            if (libro.prestarCopia()) {
                Prestamo prestamo = new Prestamo(codigo, libro.getTitulo(), socio, fecha);
                prestamosActivos.agregar(prestamo);
                historial.apilar(new Operacion(Operacion.Tipo.PRESTAMO, codigo, socio, fecha));
                LibroDAO.actualizar(libro);
                mostrarEstado("Préstamo registrado: " + libro.getTitulo() + " → " + socio, OK);
            } else {
                reservasPorLibro.computeIfAbsent(codigo, k -> new Cola<>()).encolar(socio);
                mostrarEstado("Sin copias disponibles: " + socio + " se agregó a la cola de reservas.", WARN);
            }
            txtCodigoPrestamo.setText(""); txtSocio.setText("");
            refrescarTodo();
        } catch (NumberFormatException ex) {
            mostrarEstado("Ingrese un código de libro válido.", WARN);
        } catch (IllegalArgumentException ex) {
            mostrarEstado(ex.getMessage(), WARN);
        }
    }

    private void devolverLibroSeleccionado() {
        int fila = tablaPrestamos.getSelectedRow();
        if (fila < 0) {
            mostrarEstado("Seleccione un préstamo de la tabla para devolver.", WARN);
            return;
        }
        int codigo = (int) modeloPrestamos.getValueAt(fila, 0);
        devolverLibro(codigo);
    }

    private void devolverLibro(int codigo) {
        Libro libro = indiceCodigos.buscarPorCodigo(codigo);
        if (libro == null) { mostrarEstado("No existe un libro con ese código.", WARN); return; }
        Prestamo prestamo = prestamosActivos.buscarSi(p -> p.getCodigoLibro() == codigo);
        if (prestamo == null) { mostrarEstado("No hay un préstamo activo para ese libro.", WARN); return; }

        prestamosActivos.eliminarSi(p -> p.getCodigoLibro() == codigo);
        libro.devolverCopia();
        historial.apilar(new Operacion(Operacion.Tipo.DEVOLUCION, codigo, prestamo.getSocio(), LocalDate.now().toString()));
        LibroDAO.actualizar(libro);
        String mensaje = "Devolución registrada: " + libro.getTitulo();

        Cola<String> cola = reservasPorLibro.get(codigo);
        if (cola != null && !cola.estaVacia()) {
            String siguienteSocio = cola.desencolar();
            libro.prestarCopia();
            Prestamo nuevoPrestamo = new Prestamo(codigo, libro.getTitulo(), siguienteSocio, LocalDate.now().toString());
            prestamosActivos.agregar(nuevoPrestamo);
            historial.apilar(new Operacion(Operacion.Tipo.PRESTAMO, codigo, siguienteSocio, LocalDate.now().toString()));
            LibroDAO.actualizar(libro);
            mensaje += " · Asignado automáticamente a " + siguienteSocio + " (siguiente en la cola).";
        }
        refrescarTodo();
        mostrarEstado(mensaje, OK);
    }

    // ---------------------- TAB: RESERVAS ----------------------

    private JPanel crearPanelReservas() {
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(new EmptyBorder(16, 16, 16, 16));
        raiz.setBackground(LIGHT_BG);

        JPanel tarjeta = tarjeta("Reservas pendientes por libro (Cola por cada libro)");
        areaReservas = new JTextArea();
        areaReservas.setEditable(false);
        areaReservas.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaReservas.setBackground(CARD_BG);
        JScrollPane scroll = new JScrollPane(areaReservas);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        tarjeta.add(scroll);

        FlatButton btnActualizar = botonSecundario("Actualizar");
        btnActualizar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnActualizar.addActionListener(e -> refrescarReservas());
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(btnActualizar);

        raiz.add(tarjeta, BorderLayout.CENTER);
        return raiz;
    }

    private void refrescarReservas() {
        StringBuilder sb = new StringBuilder();
        boolean hay = false;
        for (Map.Entry<Integer, Cola<String>> entrada : reservasPorLibro.entrySet()) {
            if (!entrada.getValue().estaVacia()) {
                hay = true;
                Libro libro = indiceCodigos.buscarPorCodigo(entrada.getKey());
                String titulo = (libro != null) ? libro.getTitulo() : "(desconocido)";
                sb.append(String.format("Libro [%03d] %s -> %s%n", entrada.getKey(), titulo, entrada.getValue().aLista()));
            }
        }
        areaReservas.setText(hay ? sb.toString() : "No hay reservas pendientes.");
    }

    // ---------------------- TAB: HISTORIAL ----------------------

    private JPanel crearPanelHistorial() {
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(new EmptyBorder(16, 16, 16, 16));
        raiz.setBackground(LIGHT_BG);

        JPanel tarjeta = tarjeta("Historial de operaciones (Pila — más reciente primero)");
        modeloHistorial = new DefaultTableModel(new Object[]{"Tipo", "Código", "Socio", "Fecha"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaHistorial = new JTable(modeloHistorial);
        estilizarTabla(tablaHistorial);
        JScrollPane scroll = new JScrollPane(tablaHistorial);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        tarjeta.add(scroll);

        FlatButton btnDeshacer = new FlatButton("Deshacer última operación", new Color(0xB35C00), new Color(0x8F4A00), INK, Color.WHITE);
        btnDeshacer.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDeshacer.addActionListener(e -> deshacerUltimaOperacion());
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(btnDeshacer);

        raiz.add(tarjeta, BorderLayout.CENTER);
        return raiz;
    }

    private void deshacerUltimaOperacion() {
        Operacion ultima = historial.desapilar();
        if (ultima == null) { mostrarEstado("No hay operaciones para deshacer.", WARN); return; }
        Libro libro = indiceCodigos.buscarPorCodigo(ultima.getCodigoLibro());
        if (libro == null) { mostrarEstado("No se pudo deshacer: el libro ya no existe.", WARN); return; }

        if (ultima.getTipo() == Operacion.Tipo.PRESTAMO) {
            libro.devolverCopia();
            prestamosActivos.eliminarSi(p -> p.getCodigoLibro() == ultima.getCodigoLibro() && p.getSocio().equals(ultima.getSocio()));
            mostrarEstado("Se deshizo el préstamo: " + ultima, OK);
        } else {
            libro.prestarCopia();
            prestamosActivos.agregar(new Prestamo(ultima.getCodigoLibro(), libro.getTitulo(), ultima.getSocio(), ultima.getFecha()));
            mostrarEstado("Se deshizo la devolución: " + ultima, OK);
        }
        LibroDAO.actualizar(libro);
        refrescarTodo();
    }

    // ---------------------- TAB: ESTANTERÍA (MATRIZ) ----------------------

    private JPanel crearPanelEstanteria() {
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(new EmptyBorder(16, 16, 16, 16));
        raiz.setBackground(LIGHT_BG);

        JPanel form = tarjeta("Ubicación física del libro");
        form.setPreferredSize(new Dimension(300, 0));
        txtCodigoEstante = new JTextField();
        agregarCampo(form, "Código del libro", txtCodigoEstante);

        FlatButton btnAsignar = botonPrimario("Asignar ubicación");
        btnAsignar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAsignar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        form.add(btnAsignar);
        form.add(Box.createVerticalStrut(8));

        FlatButton btnBuscar = botonSecundario("Buscar estante del libro");
        btnBuscar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBuscar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        form.add(btnBuscar);
        form.add(Box.createVerticalStrut(16));

        JLabel leyenda = new JLabel("<html>Cada celda es una posición del estante.<br>Gris = vacía · Vino = ocupada.</html>");
        leyenda.setForeground(new Color(0x555555));
        leyenda.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(leyenda);
        form.add(Box.createVerticalGlue());

        JPanel mapaPanel = tarjeta("Mapa de la estantería (Matriz " + estanteria.getFilas() + " x " + estanteria.getColumnas() + ")");
        panelEstanteriaGrid = new JPanel(new GridLayout(estanteria.getFilas(), estanteria.getColumnas(), 6, 6));
        panelEstanteriaGrid.setOpaque(false);
        panelEstanteriaGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        celdasEstante = new JLabel[estanteria.getFilas()][estanteria.getColumnas()];
        for (int f = 0; f < estanteria.getFilas(); f++) {
            for (int c = 0; c < estanteria.getColumnas(); c++) {
                JLabel celda = new JLabel("", SwingConstants.CENTER);
                celda.setOpaque(true);
                celda.setPreferredSize(new Dimension(70, 46));
                celda.setFont(celda.getFont().deriveFont(Font.BOLD, 12f));
                celda.setBorder(BorderFactory.createLineBorder(BORDER));
                celdasEstante[f][c] = celda;
                panelEstanteriaGrid.add(celda);
            }
        }
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(panelEstanteriaGrid);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        mapaPanel.add(wrapper);

        raiz.add(form, BorderLayout.WEST);
        raiz.add(mapaPanel, BorderLayout.CENTER);

        btnAsignar.addActionListener(e -> asignarUbicacionEnEstante());
        btnBuscar.addActionListener(e -> buscarEstanteDeLibro());

        return raiz;
    }

    private void asignarUbicacionEnEstante() {
        try {
            int codigo = Integer.parseInt(txtCodigoEstante.getText().trim());
            Libro libro = indiceCodigos.buscarPorCodigo(codigo);
            if (libro == null) { mostrarEstado("No existe un libro con ese código.", WARN); return; }
            int[] libre = estanteria.buscarPrimeraLibre();
            if (libre == null) { mostrarEstado("La estantería está completamente llena.", WARN); return; }
            if (libro.tieneUbicacion()) estanteria.liberar(libro.getFilaEstante(), libro.getColumnaEstante());
            estanteria.asignar(libre[0], libre[1], codigo);
            libro.asignarUbicacion(libre[0], libre[1]);
            refrescarEstanteria();
            mostrarEstado(String.format("Libro [%03d] ubicado en Estante %d, Posición %d.", codigo, libre[0], libre[1]), OK);
        } catch (NumberFormatException ex) {
            mostrarEstado("Ingrese un código de libro válido.", WARN);
        }
    }

    private void buscarEstanteDeLibro() {
        try {
            int codigo = Integer.parseInt(txtCodigoEstante.getText().trim());
            int[] ubicacion = estanteria.buscarPorCodigo(codigo);
            if (ubicacion == null) {
                mostrarEstado("Ese libro todavía no tiene ubicación asignada.", WARN);
                return;
            }
            mostrarEstado(String.format("El libro [%03d] está en Estante %d, Posición %d.", codigo, ubicacion[0], ubicacion[1]), OK);
            for (int f = 0; f < celdasEstante.length; f++) {
                for (int c = 0; c < celdasEstante[f].length; c++) {
                    celdasEstante[f][c].setBorder(BorderFactory.createLineBorder(BORDER));
                }
            }
            celdasEstante[ubicacion[0]][ubicacion[1]].setBorder(BorderFactory.createLineBorder(INK, 3));
        } catch (NumberFormatException ex) {
            mostrarEstado("Ingrese un código de libro válido.", WARN);
        }
    }

    private void refrescarEstanteria() {
        for (int f = 0; f < estanteria.getFilas(); f++) {
            for (int c = 0; c < estanteria.getColumnas(); c++) {
                boolean ocupada = estanteria.estaOcupada(f, c);
                JLabel celda = celdasEstante[f][c];
                if (ocupada) {
                    celda.setBackground(WINE);
                    celda.setForeground(Color.WHITE);
                    int[] ubic = {f, c};
                    Libro libro = buscarLibroEnUbicacion(f, c);
                    celda.setText(libro != null ? String.valueOf(libro.getCodigo()) : "");
                    celda.setToolTipText(libro != null ? libro.getTitulo() : null);
                } else {
                    celda.setBackground(new Color(0xE9E5DF));
                    celda.setForeground(new Color(0x999288));
                    celda.setText("—");
                    celda.setToolTipText(null);
                }
            }
        }
    }

    private Libro buscarLibroEnUbicacion(int fila, int columna) {
        for (int i = 0; i < totalLibros; i++) {
            if (catalogo[i].getFilaEstante() == fila && catalogo[i].getColumnaEstante() == columna) return catalogo[i];
        }
        return null;
    }

    // ---------------------- REFRESCO GENERAL DE TABLAS ----------------------

    private void refrescarTodo() {
        refrescarTablaCatalogo();
        refrescarTablaPrestamos();
        refrescarTablaHistorial();
        refrescarReservas();
        refrescarEstanteria();
    }

    private void refrescarTablaCatalogo() {
        modeloCatalogo.setRowCount(0);
        Libro[] copia = copiarCatalogo();
        Ordenamiento.burbujaPorTitulo(copia, totalLibros);
        for (int i = 0; i < totalLibros; i++) {
            Libro l = copia[i];
            String estante = l.tieneUbicacion() ? (l.getFilaEstante() + "-" + l.getColumnaEstante()) : "Sin asignar";
            modeloCatalogo.addRow(new Object[]{l.getCodigo(), l.getTitulo(), l.getAutor(), l.getCategoria(),
                    l.getCopiasTotales(), l.getCopiasDisponibles(), estante});
        }
    }

    private void refrescarTablaPrestamos() {
        modeloPrestamos.setRowCount(0);
        for (Prestamo p : prestamosActivos.aLista()) {
            modeloPrestamos.addRow(new Object[]{p.getCodigoLibro(), p.getTituloLibro(), p.getSocio(), p.getFecha()});
        }
    }

    private void refrescarTablaHistorial() {
        modeloHistorial.setRowCount(0);
        for (Operacion op : historial.aListaDesdeElTope()) {
            modeloHistorial.addRow(new Object[]{op.getTipo(), op.getCodigoLibro(), op.getSocio(), op.getFecha()});
        }
    }

    // ---------------------- CARGA INICIAL Y UTILIDADES ----------------------

    private void cargarDatosIniciales() {
        List<Libro> desdeBD = LibroDAO.listarTodos();
        if (!desdeBD.isEmpty()) {
            for (Libro l : desdeBD) registrarEnMemoria(l);
            return;
        }
        String[][] demo = {
                {"101", "Cien Anios de Soledad", "Gabriel Garcia Marquez", "Novela", "2"},
                {"102", "El Principito", "Antoine de Saint-Exupery", "Infantil", "3"},
                {"103", "Clean Code", "Robert C. Martin", "Tecnologia", "1"},
                {"104", "Estructuras de Datos en Java", "Frank Carrasco", "Tecnologia", "2"},
                {"105", "1984", "George Orwell", "Novela", "1"}
        };
        for (String[] d : demo) {
            Libro l = new Libro(Integer.parseInt(d[0]), d[1], d[2], d[3], Integer.parseInt(d[4]));
            registrarEnMemoria(l);
            LibroDAO.insertar(l);
        }
    }

    private void registrarEnMemoria(Libro libro) {
        if (totalLibros == catalogo.length) redimensionarCatalogo();
        catalogo[totalLibros++] = libro;
        indiceCodigos.insertar(libro);
        reservasPorLibro.put(libro.getCodigo(), new Cola<>());
    }

    private Libro[] copiarCatalogo() {
        Libro[] copia = new Libro[totalLibros];
        System.arraycopy(catalogo, 0, copia, 0, totalLibros);
        return copia;
    }

    private void redimensionarCatalogo() {
        Libro[] nuevo = new Libro[catalogo.length * 2];
        System.arraycopy(catalogo, 0, nuevo, 0, catalogo.length);
        catalogo = nuevo;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BibliotecaGUI().setVisible(true));
    }
}
