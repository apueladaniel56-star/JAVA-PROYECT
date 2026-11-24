import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import estructuras.ListaDobleCircular;
import estructuras.ListaEnlazadaSimple;
import modelo.Libro;

public class FrmPrincipal extends JFrame {

    // Configuración de MySQL local (Puerto 3306)
    private static final String URL = "jdbc:mysql://localhost:3306/biblioteca_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "Danielapuela.200609";

    // Colores estilo iOS
    private static final Color COLOR_FONDO_APP = new Color(242, 242, 247);
    private static final Color COLOR_CARD = Color.WHITE;
    private static final Color COLOR_TEXTO_TITULO = new Color(28, 28, 30);
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(142, 142, 147);
    private static final Color COLOR_AZUL_IOS = new Color(0, 122, 255);
    private static final Color COLOR_VERDE_IOS = new Color(52, 199, 89);
    private static final Color COLOR_NARANJA_IOS = new Color(255, 149, 0);
    private static final Color COLOR_ROJO_IOS = new Color(255, 59, 48);
    private static final Color COLOR_GRIS_BORDE = new Color(229, 229, 234);

    // Controles de usuario
    private JTextField txtUsuarioFiltro;

    // Componentes de Libros
    private JTable tablaLibros;
    private DefaultTableModel modeloLibros;
    private JTextField txtBuscarLibro;
    private JComboBox<String> cbOrden;

    // Estructuras de datos dinámicas (Unidad 2)
    // Lista enlazada simple: alimenta el catálogo (búsqueda, inserción, eliminación y ordenamiento)
    private final ListaEnlazadaSimple<Libro> listaLibros = new ListaEnlazadaSimple<>();
    // Lista doblemente enlazada circular: navegación Anterior/Siguiente por el catálogo
    private final ListaDobleCircular<Libro> carrusel = new ListaDobleCircular<>();
    private Comparator<Libro> ordenActual = Libro.POR_CODIGO;

    // Navegador de libros
    private JLabel lblNavPosicion;
    private JLabel lblNavTitulo;
    private JLabel lblNavDetalle;
    private JLabel lblNavAnterior;
    private JLabel lblNavSiguiente;
    private JTextField txtIrCodigo;

    // Componentes de Notificaciones
    private JTable tablaNotificaciones;
    private DefaultTableModel modeloNotificaciones;
    private JCheckBox chkSoloNoLeidas;

    // Componentes de Auditoría
    private JTable tablaAccesos;
    private DefaultTableModel modeloAccesos;

    // Estantería Bidimensional
    private JPanel panelCuadriculaEstantes;
    private JComboBox<String> cbLibrosDisponibles;
    private JComboBox<String> cbCasilleros;
    private static final String[] FILAS = {"A", "B", "C"};
    private static final int COLUMNAS = 4;

    public FrmPrincipal() {
        configurarVentana();
        iniciarComponentes();
        cargarTodosLosDatos();
    }

    private void configurarVentana() {
        setTitle("Sistema de Gestión de Biblioteca - macOS / iOS Style");
        setSize(1080, 750);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_FONDO_APP);
        setLayout(new BorderLayout(15, 15));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(18, 18, 18, 18));
    }

    private void iniciarComponentes() {
        // Encabezado
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("Gestión de Biblioteca");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(COLOR_TEXTO_TITULO);

        JLabel lblSub = new JLabel("Catálogo de libros, estantería física, notificaciones y auditoría");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(COLOR_TEXTO_SECUNDARIO);

        JPanel panelTitulos = new JPanel(new GridLayout(2, 1, 0, 2));
        panelTitulos.setOpaque(false);
        panelTitulos.add(lblTitulo);
        panelTitulos.add(lblSub);
        headerPanel.add(panelTitulos, BorderLayout.WEST);

        JPanel panelUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        panelUsuario.setOpaque(false);
        panelUsuario.add(new JLabel("Usuario Activo ID:"));

        txtUsuarioFiltro = new JTextField("1", 4);
        txtUsuarioFiltro.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtUsuarioFiltro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_GRIS_BORDE, 1, true),
                new EmptyBorder(5, 8, 5, 8)));
        panelUsuario.add(txtUsuarioFiltro);

        JButton btnActualizarTodo = crearBotonIOS("Actualizar", COLOR_AZUL_IOS);
        btnActualizarTodo.addActionListener(e -> cargarTodosLosDatos());
        panelUsuario.add(btnActualizarTodo);

        headerPanel.add(panelUsuario, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Pestañas
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(COLOR_FONDO_APP);

        tabs.addTab("Catálogo de Libros", new IconoIOS("libro", COLOR_AZUL_IOS), panelLibros());
        tabs.addTab("Navegador de Libros", new IconoIOS("libro", COLOR_AZUL_IOS), panelNavegador());
        tabs.addTab("Estantería (A1, A2...)", new IconoIOS("estante", COLOR_VERDE_IOS), panelEstanteria());
        tabs.addTab("Notificaciones", new IconoIOS("campana", COLOR_NARANJA_IOS), panelNotificaciones());
        tabs.addTab("Auditoría y Accesos", new IconoIOS("reloj", new Color(110, 110, 115)), panelAuditoria());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel panelLibros() {
        JPanel card = new TarjetaRedondeada();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        top.add(new JLabel("Filtrar libro:"));
        txtBuscarLibro = new JTextField(12);
        txtBuscarLibro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_GRIS_BORDE, 1, true),
                new EmptyBorder(5, 8, 5, 8)));
        top.add(txtBuscarLibro);

        JButton btnBuscarLibro = crearBotonIOS("Buscar", COLOR_AZUL_IOS);
        btnBuscarLibro.addActionListener(e -> mostrarCatalogo(txtBuscarLibro.getText().trim()));
        top.add(btnBuscarLibro);

        JButton btnNuevoLibro = crearBotonIOS("+ Añadir Libro", new Color(110, 110, 115));
        btnNuevoLibro.addActionListener(e -> dialogoNuevoLibro());
        top.add(btnNuevoLibro);

        top.add(new JLabel("Ordenar por:"));
        cbOrden = new JComboBox<>(new String[]{"Código", "Título", "Autor", "Disponibles"});
        top.add(cbOrden);

        JButton btnOrdenar = crearBotonIOS("Ordenar", COLOR_NARANJA_IOS);
        btnOrdenar.addActionListener(e -> ordenarCatalogo());
        top.add(btnOrdenar);

        card.add(top, BorderLayout.NORTH);

        modeloLibros = new DefaultTableModel(new Object[]{"Código", "Título", "Autor", "Categoría", "Ubicación", "Copias Tot.", "Disponibles"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaLibros = new JTable(modeloLibros);
        estilizarTabla(tablaLibros);
        tablaLibros.getColumnModel().getColumn(4).setCellRenderer(new BadgeUbicacionRenderer());
        tablaLibros.getColumnModel().getColumn(6).setCellRenderer(new BadgeStockRenderer());

        JScrollPane scroll = new JScrollPane(tablaLibros);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_GRIS_BORDE, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottom.setOpaque(false);

        JButton btnEliminarLibro = crearBotonIOS("Eliminar Libro", COLOR_ROJO_IOS);
        btnEliminarLibro.addActionListener(e -> eliminarLibroSeleccionado());
        bottom.add(btnEliminarLibro);

        JButton btnPrestar = crearBotonIOS("Prestar Copia (-1)", COLOR_AZUL_IOS);
        btnPrestar.addActionListener(e -> operarPrestamoDevolucion(-1));
        bottom.add(btnPrestar);

        JButton btnDevolver = crearBotonIOS("Devolver Copia (+1)", COLOR_VERDE_IOS);
        btnDevolver.addActionListener(e -> operarPrestamoDevolucion(1));
        bottom.add(btnDevolver);

        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // Módulo con lista doblemente enlazada circular: navegación por el catálogo
    private JPanel panelNavegador() {
        JPanel card = new TarjetaRedondeada();
        card.setLayout(new BorderLayout(14, 14));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel info = new JLabel("Lista doblemente enlazada circular: desde el último libro, Siguiente vuelve al primero; desde el primero, Anterior salta al último.");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(COLOR_TEXTO_SECUNDARIO);
        card.add(info, BorderLayout.NORTH);

        lblNavPosicion = new JLabel(" ");
        lblNavPosicion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNavPosicion.setForeground(COLOR_AZUL_IOS);

        lblNavTitulo = new JLabel(" ");
        lblNavTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblNavTitulo.setForeground(COLOR_TEXTO_TITULO);

        lblNavDetalle = new JLabel(" ");
        lblNavDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblNavDetalle.setForeground(COLOR_TEXTO_TITULO);

        lblNavAnterior = new JLabel(" ");
        lblNavAnterior.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNavAnterior.setForeground(COLOR_TEXTO_SECUNDARIO);

        lblNavSiguiente = new JLabel(" ");
        lblNavSiguiente.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNavSiguiente.setForeground(COLOR_TEXTO_SECUNDARIO);

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setBorder(new EmptyBorder(20, 30, 20, 30));
        for (JLabel l : new JLabel[]{lblNavPosicion, lblNavTitulo, lblNavDetalle, lblNavAnterior, lblNavSiguiente}) {
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            centro.add(l);
            centro.add(Box.createVerticalStrut(12));
        }
        card.add(centro, BorderLayout.CENTER);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        controles.setOpaque(false);

        JButton btnPrimero = crearBotonIOS("Primero", new Color(110, 110, 115));
        btnPrimero.addActionListener(e -> { carrusel.irAlInicio(); actualizarNavegador(); });
        controles.add(btnPrimero);

        JButton btnAnterior = crearBotonIOS("< Anterior", COLOR_AZUL_IOS);
        btnAnterior.addActionListener(e -> { carrusel.anterior(); actualizarNavegador(); });
        controles.add(btnAnterior);

        JButton btnSiguiente = crearBotonIOS("Siguiente >", COLOR_AZUL_IOS);
        btnSiguiente.addActionListener(e -> { carrusel.siguiente(); actualizarNavegador(); });
        controles.add(btnSiguiente);

        JButton btnUltimo = crearBotonIOS("Último", new Color(110, 110, 115));
        btnUltimo.addActionListener(e -> { carrusel.irAlFinal(); actualizarNavegador(); });
        controles.add(btnUltimo);

        controles.add(new JLabel("Ir al código:"));
        txtIrCodigo = new JTextField(6);
        txtIrCodigo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_GRIS_BORDE, 1, true),
                new EmptyBorder(5, 8, 5, 8)));
        controles.add(txtIrCodigo);

        JButton btnIr = crearBotonIOS("Ir", COLOR_VERDE_IOS);
        btnIr.addActionListener(e -> irACodigoEnNavegador());
        controles.add(btnIr);

        card.add(controles, BorderLayout.SOUTH);
        return card;
    }

    private JPanel panelEstanteria() {
        JPanel card = new TarjetaRedondeada();
        card.setLayout(new BorderLayout(14, 14));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel panelControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        panelControl.setOpaque(false);

        panelControl.add(new JLabel("Seleccionar Libro:"));
        cbLibrosDisponibles = new JComboBox<>();
        cbLibrosDisponibles.setPreferredSize(new Dimension(220, 30));
        panelControl.add(cbLibrosDisponibles);

        panelControl.add(new JLabel("Asignar a Casillero:"));
        cbCasilleros = new JComboBox<>();
        for (String f : FILAS) {
            for (int c = 1; c <= COLUMNAS; c++) {
                cbCasilleros.addItem(f + c);
            }
        }
        cbCasilleros.setPreferredSize(new Dimension(80, 30));
        panelControl.add(cbCasilleros);

        JButton btnAsignar = crearBotonIOS("Guardar en Estantería", COLOR_AZUL_IOS);
        btnAsignar.addActionListener(e -> asignarLibroAEstante());
        panelControl.add(btnAsignar);

        JButton btnLiberar = crearBotonIOS("Liberar Casillero", COLOR_ROJO_IOS);
        btnLiberar.addActionListener(e -> liberarCasillero());
        panelControl.add(btnLiberar);

        card.add(panelControl, BorderLayout.NORTH);

        panelCuadriculaEstantes = new JPanel(new GridLayout(FILAS.length, COLUMNAS, 10, 10));
        panelCuadriculaEstantes.setOpaque(false);

        JScrollPane scrollEstanteria = new JScrollPane(panelCuadriculaEstantes);
        scrollEstanteria.setBorder(BorderFactory.createLineBorder(COLOR_GRIS_BORDE, 1));
        scrollEstanteria.getViewport().setBackground(COLOR_FONDO_APP);
        card.add(scrollEstanteria, BorderLayout.CENTER);

        return card;
    }

    private JPanel panelNotificaciones() {
        JPanel card = new TarjetaRedondeada();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        chkSoloNoLeidas = new JCheckBox("Mostrar solo no leídas / pendientes");
        chkSoloNoLeidas.setSelected(true);
        chkSoloNoLeidas.setOpaque(false);
        chkSoloNoLeidas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkSoloNoLeidas.addActionListener(e -> cargarNotificaciones());
        top.add(chkSoloNoLeidas);
        card.add(top, BorderLayout.NORTH);

        modeloNotificaciones = new DefaultTableModel(new Object[]{"ID", "Usuario", "Mensaje", "Estado", "Fecha Envío"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaNotificaciones = new JTable(modeloNotificaciones);
        estilizarTabla(tablaNotificaciones);
        tablaNotificaciones.getColumnModel().getColumn(3).setCellRenderer(new BadgeEstadoRenderer());

        JScrollPane scroll = new JScrollPane(tablaNotificaciones);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_GRIS_BORDE, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottom.setOpaque(false);

        JButton btnEnviarAviso = crearBotonIOS("+ Enviar Aviso Manual", new Color(110, 110, 115));
        btnEnviarAviso.addActionListener(e -> enviarNuevaNotificacionModal());
        bottom.add(btnEnviarAviso);

        JButton btnMarcarLeido = crearBotonIOS("Marcar como Leída", COLOR_VERDE_IOS);
        btnMarcarLeido.addActionListener(e -> marcarNotificacionLeida());
        bottom.add(btnMarcarLeido);

        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel panelAuditoria() {
        JPanel card = new TarjetaRedondeada();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        modeloAccesos = new DefaultTableModel(new Object[]{"ID Evento", "Usuario", "Acción Registrada", "Marca Temporal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaAccesos = new JTable(modeloAccesos);
        estilizarTabla(tablaAccesos);

        JScrollPane scroll = new JScrollPane(tablaAccesos);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_GRIS_BORDE, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottom.setOpaque(false);

        JButton btnLogin = crearBotonIOS("Auditar 'INICIO_SESION'", COLOR_AZUL_IOS);
        btnLogin.addActionListener(e -> registrarAccionBitacora("INICIO_SESION"));
        bottom.add(btnLogin);

        JButton btnConsulta = crearBotonIOS("Auditar 'CONSULTA_CATALOGO'", COLOR_NARANJA_IOS);
        btnConsulta.addActionListener(e -> registrarAccionBitacora("CONSULTA_CATALOGO"));
        bottom.add(btnConsulta);

        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // Conexión con registro forzado de driver
    private Connection getConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                throw new SQLException("El conector de MySQL no se encuentra en Libraries.");
            }
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private void cargarTodosLosDatos() {
        cargarCatalogoLibros("");
        cargarEstanteriaBidimensional();
        cargarNotificaciones();
        cargarHistorialAccesos();
    }

    // Recarga la lista desde la BD y muestra el catálogo
    private void cargarCatalogoLibros(String filtro) {
        sincronizarListaDesdeBD();
        mostrarCatalogo(filtro);
    }

    // Lee la tabla libro y reconstruye la lista enlazada simple (un nodo por libro)
    private void sincronizarListaDesdeBD() {
        String sql = "SELECT codigo, titulo, autor, categoria, ubicacion_estante, copias_totales, copias_disponibles FROM libro ORDER BY codigo ASC";
        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            listaLibros.vaciar();
            while (rs.next()) {
                listaLibros.insertarFinal(new Libro(
                        rs.getInt("codigo"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("categoria"),
                        rs.getString("ubicacion_estante"),
                        rs.getInt("copias_totales"),
                        rs.getInt("copias_disponibles")));
            }
            listaLibros.ordenar(ordenActual);
            reconstruirCarrusel();
        } catch (SQLException e) {
            mostrarMensaje("Error de conexión: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    // Muestra en la tabla los nodos de la lista; con filtro, recorre la lista buscando coincidencias
    private void mostrarCatalogo(String filtro) {
        modeloLibros.setRowCount(0);
        ListaEnlazadaSimple<Libro> visibles = filtro.isEmpty() ? listaLibros : listaLibros.filtrar(l -> l.coincideCon(filtro));
        for (Libro l : visibles) {
            modeloLibros.addRow(new Object[]{
                l.getCodigo(),
                l.getTitulo(),
                l.getAutor(),
                l.getCategoria(),
                l.getUbicacion() != null ? l.getUbicacion() : "--",
                l.getCopiasTotales(),
                l.getCopiasDisponibles()
            });
        }
    }

    private void ordenarCatalogo() {
        switch (cbOrden.getSelectedIndex()) {
            case 1: ordenActual = Libro.POR_TITULO; break;
            case 2: ordenActual = Libro.POR_AUTOR; break;
            case 3: ordenActual = Libro.POR_DISPONIBLES; break;
            default: ordenActual = Libro.POR_CODIGO;
        }
        listaLibros.ordenar(ordenActual);
        reconstruirCarrusel();
        mostrarCatalogo(txtBuscarLibro.getText().trim());
    }

    private void eliminarLibroSeleccionado() {
        int fila = tablaLibros.getSelectedRow();
        if (fila == -1) {
            mostrarMensaje("Seleccione un libro de la tabla.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final int codigo = (int) modeloLibros.getValueAt(fila, 0);
        Libro libro = listaLibros.buscar(l -> l.getCodigo() == codigo);
        if (libro == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar \"" + libro.getTitulo() + "\" del catálogo?",
                "Eliminar libro", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement("DELETE FROM libro WHERE codigo = ?")) {
            ps.setInt(1, codigo);
            ps.executeUpdate();
            listaLibros.eliminar(l -> l.getCodigo() == codigo);
            reconstruirCarrusel();
            mostrarCatalogo(txtBuscarLibro.getText().trim());
            cargarEstanteriaBidimensional();
            registrarAccionBitacora("ELIMINAR_LIBRO_" + codigo);
        } catch (SQLException e) {
            mostrarMensaje("Error al eliminar: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    // La lista circular se arma con los mismos libros y el mismo orden de la lista simple
    private void reconstruirCarrusel() {
        Libro enFoco = carrusel.actual();
        carrusel.vaciar();
        for (Libro l : listaLibros) {
            carrusel.insertarFinal(l);
        }
        if (enFoco != null) {
            final int cod = enFoco.getCodigo();
            carrusel.irA(l -> l.getCodigo() == cod);
        }
        actualizarNavegador();
    }

    private void actualizarNavegador() {
        if (lblNavPosicion == null) {
            return;
        }
        Libro l = carrusel.actual();
        if (l == null) {
            lblNavPosicion.setText("Catálogo vacío");
            lblNavTitulo.setText(" ");
            lblNavDetalle.setText(" ");
            lblNavAnterior.setText(" ");
            lblNavSiguiente.setText(" ");
            return;
        }
        lblNavPosicion.setText("Libro " + carrusel.posicionActual() + " de " + carrusel.tamano() + "   |   Código " + l.getCodigo());
        lblNavTitulo.setText("<html><div style='width:620px'>" + escHtml(l.getTitulo()) + "</div></html>");
        lblNavDetalle.setText("<html>Autor: " + escHtml(l.getAutor())
                + "<br>Categoría: " + escHtml(l.getCategoria())
                + "<br>Estante: " + (l.getUbicacion() != null ? l.getUbicacion() : "sin asignar")
                + "<br>Copias disponibles: " + l.getCopiasDisponibles() + " de " + l.getCopiasTotales() + "</html>");
        lblNavAnterior.setText("<html>Anterior en la lista: " + escHtml(carrusel.verAnterior().getTitulo()) + "</html>");
        lblNavSiguiente.setText("<html>Siguiente en la lista: " + escHtml(carrusel.verSiguiente().getTitulo()) + "</html>");
    }

    private void irACodigoEnNavegador() {
        try {
            final int cod = Integer.parseInt(txtIrCodigo.getText().trim());
            if (carrusel.irA(l -> l.getCodigo() == cod) == null) {
                mostrarMensaje("No existe un libro con el código " + cod + ".", JOptionPane.WARNING_MESSAGE);
            }
            actualizarNavegador();
        } catch (NumberFormatException ex) {
            mostrarMensaje("Ingrese un código numérico.", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static String escHtml(String t) {
        return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // Carga de estantería bidimensional con capacidad de hasta 2 libros por casillero
    private void cargarEstanteriaBidimensional() {
        panelCuadriculaEstantes.removeAll();
        cbLibrosDisponibles.removeAllItems();

        Map<String, List<String>> estantesOcupados = new HashMap<>();

        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT codigo, titulo, ubicacion_estante FROM libro ORDER BY codigo ASC");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int cod = rs.getInt("codigo");
                String tit = rs.getString("titulo");
                String ubi = rs.getString("ubicacion_estante");

                cbLibrosDisponibles.addItem(cod + " - " + tit);

                if (ubi != null && !ubi.trim().isEmpty()) {
                    String ubiUpper = ubi.trim().toUpperCase();
                    estantesOcupados.putIfAbsent(ubiUpper, new ArrayList<>());
                    estantesOcupados.get(ubiUpper).add(cod + " | " + tit);
                }
            }
        } catch (SQLException ignored) {}

        for (String f : FILAS) {
            for (int c = 1; c <= COLUMNAS; c++) {
                String coord = f + c;
                List<String> librosEnCelda = estantesOcupados.getOrDefault(coord, Collections.emptyList());
                int cant = librosEnCelda.size();

                final boolean libre = (cant == 0);
                final boolean semiLleno = (cant == 1);
                final boolean lleno = (cant >= 2);

                JPanel celda = new JPanel(new BorderLayout(5, 5)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                        if (lleno) {
                            g2.setColor(new Color(255, 59, 48, 160));
                        } else if (semiLleno) {
                            g2.setColor(new Color(0, 122, 255, 140));
                        } else {
                            g2.setColor(COLOR_GRIS_BORDE);
                        }

                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                        g2.dispose();
                    }
                };
                celda.setOpaque(false);
                celda.setBorder(new EmptyBorder(10, 12, 10, 12));

                JLabel lblCoord = new JLabel("Estante " + coord);
                lblCoord.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblCoord.setForeground(COLOR_TEXTO_TITULO);

                String textoEstado = libre ? "○ LIBRE (0/2)" : (semiLleno ? "● 1/2 LIBROS" : "● LLENO (2/2)");
                Color colorEstado = libre ? COLOR_VERDE_IOS : (semiLleno ? COLOR_AZUL_IOS : COLOR_ROJO_IOS);

                JLabel lblEstado = new JLabel(textoEstado);
                lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lblEstado.setForeground(colorEstado);

                JPanel headerCelda = new JPanel(new BorderLayout());
                headerCelda.setOpaque(false);
                headerCelda.add(lblCoord, BorderLayout.WEST);
                headerCelda.add(lblEstado, BorderLayout.EAST);
                celda.add(headerCelda, BorderLayout.NORTH);

                StringBuilder sbHtml = new StringBuilder("<html><body style='width: 140px; font-size: 10px;'>");
                if (libre) {
                    sbHtml.append("<span style='color: #8E8E93;'><i>Sin asignar</i></span>");
                } else {
                    for (int i = 0; i < librosEnCelda.size(); i++) {
                        sbHtml.append("<b>[").append(i + 1).append("]</b> ").append(librosEnCelda.get(i));
                        if (i < librosEnCelda.size() - 1) {
                            sbHtml.append("<hr style='border: 0; border-top: 1px solid #E5E5EA; margin: 3px 0;'>");
                        }
                    }
                }
                sbHtml.append("</body></html>");

                JLabel lblLibro = new JLabel(sbHtml.toString());
                lblLibro.setForeground(COLOR_TEXTO_TITULO);
                celda.add(lblLibro, BorderLayout.CENTER);

                panelCuadriculaEstantes.add(celda);
            }
        }

        panelCuadriculaEstantes.revalidate();
        panelCuadriculaEstantes.repaint();
    }

    // Asignación controlada: permite hasta 2 libros por estante
    private void asignarLibroAEstante() {
        if (cbLibrosDisponibles.getSelectedItem() == null || cbCasilleros.getSelectedItem() == null) return;

        String itemLibro = (String) cbLibrosDisponibles.getSelectedItem();
        int cod = Integer.parseInt(itemLibro.split(" - ")[0]);
        String casillero = (String) cbCasilleros.getSelectedItem();

        try (Connection con = getConexion()) {
            int cantidadActual = 0;
            boolean yaEstaEnEsteCasillero = false;

            try (PreparedStatement psCount = con.prepareStatement("SELECT codigo FROM libro WHERE ubicacion_estante = ?")) {
                psCount.setString(1, casillero);
                try (ResultSet rs = psCount.executeQuery()) {
                    while (rs.next()) {
                        cantidadActual++;
                        if (rs.getInt("codigo") == cod) {
                            yaEstaEnEsteCasillero = true;
                        }
                    }
                }
            }

            if (yaEstaEnEsteCasillero) {
                mostrarMensaje("Este libro ya se encuentra asignado al casillero " + casillero + ".", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (cantidadActual >= 2) {
                mostrarMensaje("El casillero " + casillero + " está LLENO (capacidad máxima: 2 libros).\nLibere espacio o elija otro estante.", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (PreparedStatement psAsignar = con.prepareStatement("UPDATE libro SET ubicacion_estante = ? WHERE codigo = ?")) {
                psAsignar.setString(1, casillero);
                psAsignar.setInt(2, cod);
                psAsignar.executeUpdate();
            }

            registrarAccionBitacora("ALMACEN_ESTANTE_" + casillero + "_LIBRO_" + cod);
            cargarCatalogoLibros(txtBuscarLibro.getText().trim());
            cargarEstanteriaBidimensional();
            mostrarMensaje("Libro almacenado en " + casillero + " (" + (cantidadActual + 1) + "/2).", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            mostrarMensaje("Error al almacenar: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void liberarCasillero() {
        if (cbCasilleros.getSelectedItem() == null) return;
        String casillero = (String) cbCasilleros.getSelectedItem();

        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE libro SET ubicacion_estante = NULL WHERE ubicacion_estante = ?")) {
            ps.setString(1, casillero);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                registrarAccionBitacora("LIBERAR_ESTANTE_" + casillero);
                cargarCatalogoLibros(txtBuscarLibro.getText().trim());
                cargarEstanteriaBidimensional();
                mostrarMensaje("Casillero " + casillero + " liberado.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                mostrarMensaje("El casillero " + casillero + " ya estaba libre.", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            mostrarMensaje("Error: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void operarPrestamoDevolucion(int cambio) {
        int fila = tablaLibros.getSelectedRow();
        if (fila == -1) {
            mostrarMensaje("Seleccione un libro de la tabla.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int codigo = (int) modeloLibros.getValueAt(fila, 0);
        String titulo = (String) modeloLibros.getValueAt(fila, 1);
        int totales = (int) modeloLibros.getValueAt(fila, 5);
        int disponibles = (int) modeloLibros.getValueAt(fila, 6);

        if (cambio == -1 && disponibles <= 0) {
            mostrarMensaje("No hay copias disponibles para prestar.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cambio == 1 && disponibles >= totales) {
            mostrarMensaje("Todas las copias ya están en la biblioteca.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE libro SET copias_disponibles = copias_disponibles + ? WHERE codigo = ?";
        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cambio);
            ps.setInt(2, codigo);
            ps.executeUpdate();

            String accion = (cambio == -1) ? "PRESTAMO_LIBRO_" + codigo : "DEVOLUCION_LIBRO_" + codigo;
            registrarAccionBitacora(accion);

            if (cambio == -1) {
                enviarNotificacionDirecta(getUsuarioActual(), "Préstamo: " + titulo + ". Recuerde devolverlo a tiempo.");
            }

            cargarCatalogoLibros(txtBuscarLibro.getText().trim());
            mostrarMensaje("Operación realizada con éxito.", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            mostrarMensaje("Error en la BD: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void dialogoNuevoLibro() {
        JTextField fCod = new JTextField();
        JTextField fTit = new JTextField();
        JTextField fAut = new JTextField();
        JTextField fCat = new JTextField();
        JTextField fCop = new JTextField("5");
        JComboBox<String> fUbi = new JComboBox<>();
        fUbi.addItem("--");
        for (String f : FILAS) {
            for (int c = 1; c <= COLUMNAS; c++) {
                fUbi.addItem(f + c);
            }
        }

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.add(new JLabel("Código numérico:")); form.add(fCod);
        form.add(new JLabel("Título:")); form.add(fTit);
        form.add(new JLabel("Autor:")); form.add(fAut);
        form.add(new JLabel("Categoría:")); form.add(fCat);
        form.add(new JLabel("Copias totales:")); form.add(fCop);
        form.add(new JLabel("Estante inicial:")); form.add(fUbi);

        int result = JOptionPane.showConfirmDialog(this, form, "Nuevo Libro al Catálogo", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int cod = Integer.parseInt(fCod.getText().trim());
                int cop = Integer.parseInt(fCop.getText().trim());
                String ubi = fUbi.getSelectedItem().equals("--") ? null : (String) fUbi.getSelectedItem();

                String sql = "INSERT INTO libro (codigo, titulo, autor, categoria, ubicacion_estante, copias_totales, copias_disponibles) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, cod);
                    ps.setString(2, fTit.getText().trim());
                    ps.setString(3, fAut.getText().trim());
                    ps.setString(4, fCat.getText().trim());
                    ps.setString(5, ubi);
                    ps.setInt(6, cop);
                    ps.setInt(7, cop);
                    ps.executeUpdate();

                    Libro nuevo = new Libro(cod, fTit.getText().trim(), fAut.getText().trim(), fCat.getText().trim(), ubi, cop, cop);
                    listaLibros.insertarOrdenado(nuevo, ordenActual);
                    reconstruirCarrusel();
                    mostrarCatalogo(txtBuscarLibro.getText().trim());
                    cargarEstanteriaBidimensional();
                    registrarAccionBitacora("REGISTRO_LIBRO_" + cod);
                }
            } catch (Exception ex) {
                mostrarMensaje("Error: Código duplicado o datos incorrectos.", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cargarNotificaciones() {
        modeloNotificaciones.setRowCount(0);
        int uid = getUsuarioActual();
        String sql = "SELECT id, usuario_id, mensaje, leido, fecha_envio FROM notificaciones WHERE usuario_id = ?";
        if (chkSoloNoLeidas.isSelected()) sql += " AND leido = FALSE";
        sql += " ORDER BY fecha_envio DESC";

        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modeloNotificaciones.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("mensaje"),
                        rs.getBoolean("leido") ? "LEÍDO" : "PENDIENTE",
                        rs.getTimestamp("fecha_envio")
                    });
                }
            }
        } catch (SQLException ignored) {}
    }

    private void marcarNotificacionLeida() {
        int fila = tablaNotificaciones.getSelectedRow();
        if (fila == -1) {
            mostrarMensaje("Seleccione una notificación.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) modeloNotificaciones.getValueAt(fila, 0);
        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement("UPDATE notificaciones SET leido = TRUE WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            cargarNotificaciones();
        } catch (SQLException e) {
            mostrarMensaje("Error: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enviarNuevaNotificacionModal() {
        String msg = JOptionPane.showInputDialog(this, "Mensaje de la notificación:", "Aviso al Usuario", JOptionPane.PLAIN_MESSAGE);
        if (msg != null && !msg.trim().isEmpty()) {
            enviarNotificacionDirecta(getUsuarioActual(), msg.trim());
            cargarNotificaciones();
        }
    }

    private void enviarNotificacionDirecta(int uid, String mensaje) {
        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement("INSERT INTO notificaciones (usuario_id, mensaje, leido) VALUES (?, ?, FALSE)")) {
            ps.setInt(1, uid);
            ps.setString(2, mensaje);
            ps.executeUpdate();
            cargarNotificaciones();
        } catch (SQLException ignored) {}
    }

    private void cargarHistorialAccesos() {
        modeloAccesos.setRowCount(0);
        int uid = getUsuarioActual();
        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement("SELECT id, usuario_id, accion, fecha_hora FROM historial_accesos WHERE usuario_id = ? ORDER BY fecha_hora DESC LIMIT 25")) {
            ps.setInt(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modeloAccesos.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("accion"),
                        rs.getTimestamp("fecha_hora")
                    });
                }
            }
        } catch (SQLException ignored) {}
    }

    private void registrarAccionBitacora(String accion) {
        int uid = getUsuarioActual();
        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement("INSERT INTO historial_accesos (usuario_id, accion) VALUES (?, ?)")) {
            ps.setInt(1, uid);
            ps.setString(2, accion);
            ps.executeUpdate();
            cargarHistorialAccesos();
        } catch (SQLException ignored) {}
    }

    private int getUsuarioActual() {
        try {
            return Integer.parseInt(txtUsuarioFiltro.getText().trim());
        } catch (Exception e) {
            return 1;
        }
    }

    private void mostrarMensaje(String txt, int tipo) {
        JOptionPane.showMessageDialog(this, txt, "Sistema de Biblioteca", tipo);
    }

    // Elementos visuales estilo iOS
    private void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(36);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setGridColor(COLOR_GRIS_BORDE);
        tabla.setSelectionBackground(new Color(230, 240, 255));
        tabla.setSelectionForeground(COLOR_TEXTO_TITULO);
        tabla.setShowVerticalLines(false);

        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(248, 248, 250));
        tabla.getTableHeader().setForeground(COLOR_TEXTO_SECUNDARIO);
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 34));
    }

    private JButton crearBotonIOS(String texto, Color fondo) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? fondo.darker() : fondo);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private static class TarjetaRedondeada extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_CARD);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
            g2.setColor(COLOR_GRIS_BORDE);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
            g2.dispose();
        }
        public TarjetaRedondeada() { setOpaque(false); }
    }

    private static class IconoIOS implements Icon {
        private final String tipo;
        private final Color color;
        private final int size = 16;

        public IconoIOS(String tipo, Color color) {
            this.tipo = tipo;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);

            switch (tipo) {
                case "libro":
                    g2.fillRect(x + 2, y + 2, 5, 12);
                    g2.fillRect(x + 8, y + 2, 5, 12);
                    g2.setColor(Color.WHITE);
                    g2.fillRect(x + 3, y + 4, 3, 2);
                    g2.fillRect(x + 9, y + 4, 3, 2);
                    break;
                case "estante":
                    g2.drawRect(x + 2, y + 2, 12, 12);
                    g2.drawLine(x + 2, y + 8, x + 14, y + 8);
                    g2.drawLine(x + 8, y + 2, x + 8, y + 14);
                    break;
                case "campana":
                    g2.fillArc(x + 3, y + 2, 10, 10, 0, 180);
                    g2.fillRect(x + 3, y + 7, 10, 5);
                    g2.fillOval(x + 6, y + 12, 4, 3);
                    break;
                case "reloj":
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawOval(x + 1, y + 1, 13, 13);
                    g2.drawLine(x + 7, y + 4, x + 7, y + 8);
                    g2.drawLine(x + 7, y + 8, x + 11, y + 8);
                    break;
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    private static class BadgeUbicacionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String ubi = value != null ? value.toString() : "--";
            boolean tieneUbi = !ubi.equals("--");

            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(tieneUbi ? new Color(240, 235, 255) : new Color(245, 245, 247));
                    g2.fillRoundRect(8, 5, getWidth() - 16, getHeight() - 10, 14, 14);
                    g2.setColor(tieneUbi ? new Color(180, 160, 245) : COLOR_GRIS_BORDE);
                    g2.drawRoundRect(8, 5, getWidth() - 17, getHeight() - 11, 14, 14);
                    g2.dispose();
                }
            };
            p.setOpaque(false);

            JLabel lbl = new JLabel(tieneUbi ? "Estante " + ubi : "Sin estante");
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(tieneUbi ? new Color(88, 86, 214) : COLOR_TEXTO_SECUNDARIO);
            p.add(lbl);

            if (isSelected) {
                p.setBackground(table.getSelectionBackground());
                p.setOpaque(true);
            }
            return p;
        }
    }

    private static class BadgeEstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String texto = value != null ? value.toString() : "";
            boolean esPendiente = "PENDIENTE".equalsIgnoreCase(texto);

            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(esPendiente ? new Color(255, 235, 235) : new Color(230, 248, 235));
                    g2.fillRoundRect(8, 5, getWidth() - 16, getHeight() - 10, 14, 14);
                    g2.setColor(esPendiente ? new Color(255, 180, 180) : new Color(175, 230, 190));
                    g2.drawRoundRect(8, 5, getWidth() - 17, getHeight() - 11, 14, 14);
                    g2.dispose();
                }
            };
            p.setOpaque(false);

            JLabel lbl = new JLabel(esPendiente ? "Pendiente" : "Leído");
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(esPendiente ? COLOR_ROJO_IOS : COLOR_VERDE_IOS);
            p.add(lbl);

            if (isSelected) {
                p.setBackground(table.getSelectionBackground());
                p.setOpaque(true);
            }
            return p;
        }
    }

    private static class BadgeStockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            int stock = 0;
            try {
                stock = Integer.parseInt(value.toString());
            } catch (Exception ignored) {}

            final boolean agotado = (stock <= 0);
            final int cant = stock;

            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(agotado ? new Color(255, 235, 235) : new Color(235, 245, 255));
                    g2.fillRoundRect(10, 5, getWidth() - 20, getHeight() - 10, 14, 14);
                    g2.setColor(agotado ? new Color(255, 190, 190) : new Color(180, 215, 255));
                    g2.drawRoundRect(10, 5, getWidth() - 21, getHeight() - 11, 14, 14);
                    g2.dispose();
                }
            };
            p.setOpaque(false);

            JLabel lbl = new JLabel(agotado ? "Agotado" : cant + " disp.");
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(agotado ? COLOR_ROJO_IOS : COLOR_AZUL_IOS);
            p.add(lbl);

            if (isSelected) {
                p.setBackground(table.getSelectionBackground());
                p.setOpaque(true);
            }
            return p;
        }
    }

    // Método main para ejecutar directamente desde esta clase si se desea
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        java.awt.EventQueue.invokeLater(() -> {
            new FrmPrincipal().setVisible(true);
        });
    }
}