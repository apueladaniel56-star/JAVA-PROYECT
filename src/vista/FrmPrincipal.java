package vista;

import datos.Conexion;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.text.Normalizer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import estructuras.ListaDobleCircular;
import estructuras.ListaEnlazadaSimple;
import estructuras.Pila;
import modelo.AccionCatalogo;
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
    private static final String TODAS_LAS_CATEGORIAS = "Todas las categorías";
    private JComboBox<String> cbCategoria;
    private boolean actualizandoCategorias = false;

    // ═══ UNIDAD 3 · PILA (LIFO): historial para DESHACER / REHACER acciones del catálogo ═══
    // pilaDeshacer: cada acción hecha se APILA (push); "Deshacer" DESAPILA (pop) la última.
    // pilaRehacer : lo que se deshace se apila aquí para poder volver a aplicarlo.
    private final Pila<AccionCatalogo> pilaDeshacer = new Pila<>();
    private Pila<AccionCatalogo> pilaRehacer = new Pila<>();
    private DefaultTableModel modeloPilaDeshacer;
    private DefaultTableModel modeloPilaRehacer;
    private JLabel lblTopePila;

    // ═══ UNIDAD 4 · ÁRBOLES (pestaña con árbol binario, ABB y AVL) ═══
    private PanelArboles panelArboles;

    // ╔════════════════════════════════════════════════════════════════════════════╗
    // ║ [AGREGADO UNIDAD 5 · GRAFOS - ATRIBUTO]                                    ║
    // ║ Referencia al panel de Grafos para la navegación y rutas entre estantes.   ║
    // ╚════════════════════════════════════════════════════════════════════════════╝
    private PanelGrafos panelGrafos;

    // Estructuras de datos dinámicas (Unidad 2)
    private final ListaEnlazadaSimple<Libro> listaLibros = new ListaEnlazadaSimple<>();
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

    // Estantería Bidimensional Dinámica
    private JPanel panelCuadriculaEstantes;
    private JComboBox<String> cbLibrosDisponibles;
    private JComboBox<String> cbCasilleros;
    private static final String[] FILAS = {"A", "B", "C"};
    private static final int COLUMNAS = 4;
    // Mapa que almacena la capacidad máxima personalizada por cada estante (por defecto 2, salvo A2=5, etc.)
    private final Map<String, Integer> capacidadesEstantes = new HashMap<>();

    public FrmPrincipal() {
        inicializarCapacidadesEstantes();
        configurarVentana();
        iniciarComponentes();
        cargarTodosLosDatos();
    }

    private void inicializarCapacidadesEstantes() {
        for (String f : FILAS) {
            for (int c = 1; c <= COLUMNAS; c++) {
                String coord = f + c;
                // Ejemplo de capacidades iniciales diferenciadas:
                if (coord.equals("A1")) {
                    capacidadesEstantes.put(coord, 2);
                } else if (coord.equals("A2")) {
                    capacidadesEstantes.put(coord, 5);
                } else {
                    capacidadesEstantes.put(coord, 3); // Capacidad por defecto para el resto
                }
            }
        }
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

        JLabel lblSub = new JLabel("Catálogo, listas enlazadas, estantería, notificaciones y auditoría");
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
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        tabs.addTab("Catálogo de Libros", new IconoIOS("libro", COLOR_AZUL_IOS), panelLibros());
        tabs.addTab("Navegador de Libros", new IconoIOS("libro", COLOR_AZUL_IOS), panelNavegador());
        tabs.addTab("Estantería", new IconoIOS("estante", COLOR_VERDE_IOS), panelEstanteria());
        tabs.addTab("Notificaciones", new IconoIOS("campana", COLOR_NARANJA_IOS), panelNotificaciones());
        tabs.addTab("Auditoría y Accesos", new IconoIOS("reloj", new Color(110, 110, 115)), panelAuditoria());

        // ═══ UNIDAD 3 · PILAS (LIFO): pestaña del historial deshacer/rehacer ═══
        tabs.addTab("Pila: Deshacer (U3)", new IconoIOS("reloj", COLOR_NARANJA_IOS), panelHistorialPila());
        // ═══ UNIDAD 3 · COLAS (FIFO con prioridad): pestaña de atención en mostrador ═══
        tabs.addTab("Cola de Atención (U3)", new IconoIOS("campana", COLOR_VERDE_IOS), new PanelColaAtencion());
        // ═══ UNIDAD 4 · ÁRBOLES: binario con recorridos, ABB y AVL ═══
        panelArboles = new PanelArboles(() -> listaLibros.aLista());
        tabs.addTab("Árboles (U4)", new IconoIOS("libro", COLOR_AZUL_IOS), panelArboles);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == panelArboles) {
                panelArboles.recargar();   // los árboles se reconstruyen con el catálogo actual
            }
        });

        // ╔════════════════════════════════════════════════════════════════════════════╗
        // ║ [AGREGADO UNIDAD 5 · GRAFOS - PESTAÑA EN JTabbedPane]                      ║
        // ║ Agrega la pestaña "Grafos (U5)" con Dijkstra, BFS y DFS sobre estantes.    ║
        // ╚════════════════════════════════════════════════════════════════════════════╝
        panelGrafos = new PanelGrafos(() -> listaLibros.aLista());
        tabs.addTab("Grafos (U5)", new IconoIOS("estante", COLOR_VERDE_IOS), panelGrafos);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == panelGrafos) {
                panelGrafos.recargar();   // actualiza el combo de libros asignados a estantes
            }
        });

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel panelLibros() {
        JPanel card = new TarjetaRedondeada();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        top.add(new JLabel("Categoría:"));
        cbCategoria = new JComboBox<>(new String[]{TODAS_LAS_CATEGORIAS});
        cbCategoria.setPreferredSize(new Dimension(170, 30));
        cbCategoria.addActionListener(e -> {
            if (actualizandoCategorias) return;
            reconstruirCarrusel();
            mostrarCatalogo(txtBuscarLibro.getText().trim());
        });
        top.add(cbCategoria);

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

        // UNIDAD 3 · PILA: botón que desapila (pop) la última acción y la revierte
        JButton btnDeshacer = crearBotonIOS("↶ Deshacer", new Color(110, 110, 115));
        btnDeshacer.addActionListener(e -> deshacer());
        bottom.add(btnDeshacer);

        JButton btnModificarLibro = crearBotonIOS("Modificar Libro", COLOR_NARANJA_IOS);
        btnModificarLibro.addActionListener(e -> modificarLibroSeleccionado());
        bottom.add(btnModificarLibro);

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

        JButton btnCapacidad = crearBotonIOS("⚙ Capacidad", new Color(110, 110, 115));
        btnCapacidad.addActionListener(e -> cambiarCapacidadEstanteDialogo());
        panelControl.add(btnCapacidad);

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

    private void cargarCatalogoLibros(String filtro) {
        sincronizarListaDesdeBD();
        mostrarCatalogo(filtro);
    }

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
            refrescarCategorias();
            reconstruirCarrusel();
        } catch (SQLException e) {
            mostrarMensaje("Error de conexión: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------- Categorías ----------------------

    private static String sinTildes(String t) {
        return Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }

    /** Categoría elegida en el filtro, o null si está en "Todas las categorías". */
    private String categoriaSeleccionada() {
        if (cbCategoria == null || cbCategoria.getSelectedItem() == null) return null;
        String c = (String) cbCategoria.getSelectedItem();
        return c.equals(TODAS_LAS_CATEGORIAS) ? null : c;
    }

    private boolean enCategoriaSeleccionada(Libro l) {
        String cat = categoriaSeleccionada();
        return cat == null || sinTildes(l.getCategoria()).equals(sinTildes(cat));
    }

    /** Reconstruye la lista de categorías del filtro a partir de los libros cargados. */
    private void refrescarCategorias() {
        if (cbCategoria == null) return;
        Map<String, String> categorias = new TreeMap<>();
        for (Libro l : listaLibros) {
            if (l.getCategoria() != null && !l.getCategoria().isBlank()) {
                categorias.putIfAbsent(sinTildes(l.getCategoria()), l.getCategoria().trim());
            }
        }
        String actual = categoriaSeleccionada();
        actualizandoCategorias = true;
        try {
            cbCategoria.removeAllItems();
            cbCategoria.addItem(TODAS_LAS_CATEGORIAS);
            for (String c : categorias.values()) cbCategoria.addItem(c);
            if (actual != null && categorias.containsKey(sinTildes(actual))) {
                cbCategoria.setSelectedItem(categorias.get(sinTildes(actual)));
            } else {
                cbCategoria.setSelectedIndex(0);
            }
        } finally {
            actualizandoCategorias = false;
        }
    }

    /** Devuelve la categoría con la ortografía ya existente (Poético, no poetico) o la deja con inicial mayúscula. */
    private String normalizarCategoria(String texto) {
        String t = texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
        if (t.isEmpty()) return t;
        for (Libro l : listaLibros) {
            if (l.getCategoria() != null && sinTildes(l.getCategoria()).equals(sinTildes(t))) {
                return l.getCategoria().trim();
            }
        }
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    /** Combo editable con las categorías existentes, para elegir una o escribir una nueva. */
    private JComboBox<String> crearComboCategoria(String inicial) {
        JComboBox<String> cb = new JComboBox<>();
        cb.setEditable(true);
        Map<String, String> categorias = new TreeMap<>();
        for (Libro l : listaLibros) {
            if (l.getCategoria() != null && !l.getCategoria().isBlank()) {
                categorias.putIfAbsent(sinTildes(l.getCategoria()), l.getCategoria().trim());
            }
        }
        for (String c : categorias.values()) cb.addItem(c);
        cb.setSelectedItem(inicial == null ? "" : inicial);
        return cb;
    }

    private String textoCategoria(JComboBox<String> cb) {
        Object o = cb.getEditor().getItem();
        return o == null ? "" : o.toString();
    }

    /** Cambia el catálogo a la categoría del libro y lo deja seleccionado en la tabla. */
    private void irACategoria(String categoria, int codigoLibro) {
        refrescarCategorias();
        actualizandoCategorias = true;
        try {
            for (int i = 0; i < cbCategoria.getItemCount(); i++) {
                if (sinTildes(cbCategoria.getItemAt(i)).equals(sinTildes(categoria))) {
                    cbCategoria.setSelectedIndex(i);
                    break;
                }
            }
        } finally {
            actualizandoCategorias = false;
        }
        txtBuscarLibro.setText("");
        reconstruirCarrusel();
        mostrarCatalogo("");
        for (int i = 0; i < modeloLibros.getRowCount(); i++) {
            if ((int) modeloLibros.getValueAt(i, 0) == codigoLibro) {
                tablaLibros.setRowSelectionInterval(i, i);
                tablaLibros.scrollRectToVisible(tablaLibros.getCellRect(i, 0, true));
                break;
            }
        }
        carrusel.irA(l -> l.getCodigo() == codigoLibro);
        actualizarNavegador();
    }

    private void mostrarCatalogo(String filtro) {
        modeloLibros.setRowCount(0);
        ListaEnlazadaSimple<Libro> visibles = listaLibros.filtrar(
                l -> enCategoriaSeleccionada(l) && (filtro.isEmpty() || l.coincideCon(filtro)));
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

    private void modificarLibroSeleccionado() {
        int fila = tablaLibros.getSelectedRow();
        if (fila == -1) {
            mostrarMensaje("Seleccione un libro de la tabla para modificar.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int codigo = (int) modeloLibros.getValueAt(fila, 0);
        Libro libro = listaLibros.buscar(l -> l.getCodigo() == codigo);
        if (libro == null) return;
        final Libro libroAntes = libro.copiar();   // UNIDAD 3 · PILA: foto del libro ANTES de modificarlo

        JTextField fTit = new JTextField(libro.getTitulo());
        JTextField fAut = new JTextField(libro.getAutor());
        JComboBox<String> fCat = crearComboCategoria(libro.getCategoria());
        JTextField fCopTot = new JTextField(String.valueOf(libro.getCopiasTotales()));
        JTextField fCopDisp = new JTextField(String.valueOf(libro.getCopiasDisponibles()));
        JComboBox<String> fUbi = new JComboBox<>();
        fUbi.addItem("--");
        for (String f : FILAS) {
            for (int c = 1; c <= COLUMNAS; c++) {
                fUbi.addItem(f + c);
            }
        }
        if (libro.getUbicacion() != null && !libro.getUbicacion().isEmpty()) {
            fUbi.setSelectedItem(libro.getUbicacion().toUpperCase());
        }

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.add(new JLabel("Código (No editable):"));
        form.add(new JLabel(String.valueOf(codigo)));
        form.add(new JLabel("Título:")); form.add(fTit);
        form.add(new JLabel("Autor:")); form.add(fAut);
        form.add(new JLabel("Categoría:")); form.add(fCat);
        form.add(new JLabel("Copias totales:")); form.add(fCopTot);
        form.add(new JLabel("Copias disponibles:")); form.add(fCopDisp);

        int result = JOptionPane.showConfirmDialog(this, form, "Modificar Libro #" + codigo, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int copTot = Integer.parseInt(fCopTot.getText().trim());
                int copDisp = Integer.parseInt(fCopDisp.getText().trim());
                if (copDisp > copTot) {
                    mostrarMensaje("Las copias disponibles no pueden superar las copias totales.", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String nuevaUbi = fUbi.getSelectedItem().equals("--") ? null : (String) fUbi.getSelectedItem();
                final String catNueva = normalizarCategoria(textoCategoria(fCat));
                if (catNueva.isEmpty()) {
                    mostrarMensaje("Indique la categoría del libro.", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String sql = "UPDATE libro SET titulo = ?, autor = ?, categoria = ?, ubicacion_estante = ?, copias_totales = ?, copias_disponibles = ? WHERE codigo = ?";
                try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, fTit.getText().trim());
                    ps.setString(2, fAut.getText().trim());
                    ps.setString(3, catNueva);
                    ps.setString(4, nuevaUbi);
                    ps.setInt(5, copTot);
                    ps.setInt(6, copDisp);
                    ps.setInt(7, codigo);
                    ps.executeUpdate();

                    registrarAccionBitacora("MODIFICAR_LIBRO_" + codigo);
                    // UNIDAD 3 · PILA: se APILA la modificación (estado antes + estado después)
                    registrarAccionPila(new AccionCatalogo(AccionCatalogo.Tipo.MODIFICAR, libroAntes,
                            new Libro(codigo, fTit.getText().trim(), fAut.getText().trim(), catNueva, nuevaUbi, copTot, copDisp), null));
                    sincronizarListaDesdeBD();
                    irACategoria(catNueva, codigo);
                    cargarEstanteriaBidimensional();
                    mostrarMensaje("Libro modificado correctamente.", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                mostrarMensaje("Ingrese valores numéricos válidos en las copias.", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                mostrarMensaje("Error al actualizar en la BD: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        }
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
            // UNIDAD 3 · PILA: se APILA la eliminación guardando el libro completo para poder restaurarlo
            registrarAccionPila(new AccionCatalogo(AccionCatalogo.Tipo.ELIMINAR, libro.copiar(), null, null));
            refrescarCategorias();
            reconstruirCarrusel();
            mostrarCatalogo(txtBuscarLibro.getText().trim());
            cargarEstanteriaBidimensional();
            registrarAccionBitacora("ELIMINAR_LIBRO_" + codigo);
        } catch (SQLException e) {
            mostrarMensaje("Error al eliminar: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reconstruirCarrusel() {
        Libro enFoco = carrusel.actual();
        carrusel.vaciar();
        for (Libro l : listaLibros) {
            if (enCategoriaSeleccionada(l)) {
                carrusel.insertarFinal(l);
            }
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

    // Carga de estantería bidimensional con límite de libros dinámico por cada estante
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
                int limiteMax = capacidadesEstantes.getOrDefault(coord, 3);

                final boolean libre = (cant == 0);
                final boolean semiLleno = (cant > 0 && cant < limiteMax);
                final boolean lleno = (cant >= limiteMax);

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

                String textoEstado = libre ? "○ LIBRE (0/" + limiteMax + ")" : (semiLleno ? "● " + cant + "/" + limiteMax + " LIBROS" : "● LLENO (" + cant + "/" + limiteMax + ")");
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

    private void cambiarCapacidadEstanteDialogo() {
        String casillero = (String) cbCasilleros.getSelectedItem();
        if (casillero == null) return;

        int capActual = capacidadesEstantes.getOrDefault(casillero, 3);
        String resp = JOptionPane.showInputDialog(this, 
                "Ingrese la capacidad máxima de libros para el estante " + casillero + ":", 
                String.valueOf(capActual));

        if (resp != null && !resp.trim().isEmpty()) {
            try {
                int nuevaCap = Integer.parseInt(resp.trim());
                if (nuevaCap <= 0) {
                    mostrarMensaje("La capacidad debe ser mayor a 0.", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                capacidadesEstantes.put(casillero, nuevaCap);
                cargarEstanteriaBidimensional();
                mostrarMensaje("Capacidad del estante " + casillero + " actualizada a " + nuevaCap + " libros.", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                mostrarMensaje("Ingrese un número entero válido.", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void asignarLibroAEstante() {
        if (cbLibrosDisponibles.getSelectedItem() == null || cbCasilleros.getSelectedItem() == null) return;

        String itemLibro = (String) cbLibrosDisponibles.getSelectedItem();
        int cod = Integer.parseInt(itemLibro.split(" - ")[0]);
        String casillero = (String) cbCasilleros.getSelectedItem();
        int capMax = capacidadesEstantes.getOrDefault(casillero, 3);

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

            if (cantidadActual >= capMax) {
                mostrarMensaje("El casillero " + casillero + " está LLENO (capacidad máxima: " + capMax + " libros).\nLibere espacio, cambie su capacidad o elija otro estante.", JOptionPane.WARNING_MESSAGE);
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
            mostrarMensaje("Libro almacenado en " + casillero + " (" + (cantidadActual + 1) + "/" + capMax + ").", JOptionPane.INFORMATION_MESSAGE);

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
        Libro libroPrevio = listaLibros.buscar(l -> l.getCodigo() == codigo);
        final Libro antesPrestamo = (libroPrevio == null) ? null : libroPrevio.copiar();   // UNIDAD 3 · PILA
        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cambio);
            ps.setInt(2, codigo);
            ps.executeUpdate();

            // UNIDAD 3 · PILA: el préstamo/devolución también se puede deshacer
            if (antesPrestamo != null) {
                Libro despuesPrestamo = antesPrestamo.copiar();
                despuesPrestamo.setCopiasDisponibles(antesPrestamo.getCopiasDisponibles() + cambio);
                registrarAccionPila(new AccionCatalogo(AccionCatalogo.Tipo.MODIFICAR, antesPrestamo, despuesPrestamo,
                        cambio == -1 ? "préstamo" : "devolución"));
            }

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
        JComboBox<String> fCat = crearComboCategoria("");
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
                final String catNueva = normalizarCategoria(textoCategoria(fCat));
                if (catNueva.isEmpty()) {
                    mostrarMensaje("Indique la categoría del libro.", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String sql = "INSERT INTO libro (codigo, titulo, autor, categoria, ubicacion_estante, copias_totales, copias_disponibles) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, cod);
                    ps.setString(2, fTit.getText().trim());
                    ps.setString(3, fAut.getText().trim());
                    ps.setString(4, catNueva);
                    ps.setString(5, ubi);
                    ps.setInt(6, cop);
                    ps.setInt(7, cop);
                    ps.executeUpdate();

                    Libro nuevo = new Libro(cod, fTit.getText().trim(), fAut.getText().trim(), catNueva, ubi, cop, cop);
                    listaLibros.insertarOrdenado(nuevo, ordenActual);
                    // El catálogo salta directamente a la categoría del libro agregado
                    irACategoria(catNueva, cod);
                    // UNIDAD 3 · PILA: se APILA la acción para poder deshacerla
                    registrarAccionPila(new AccionCatalogo(AccionCatalogo.Tipo.AGREGAR, null, nuevo.copiar(), null));
                    cargarEstanteriaBidimensional();
                    registrarAccionBitacora("REGISTRO_LIBRO_" + cod);
                }
            } catch (Exception ex) {
                mostrarMensaje("Error: Código duplicado o datos incorrectos.", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    /** UNIDAD 3 · PILA — PUSH: guarda una acción nueva en la pila de deshacer. */
    private void registrarAccionPila(AccionCatalogo accion) {
        pilaDeshacer.apilar(accion);
        pilaRehacer = new Pila<>();          // una acción nueva invalida lo que se podía rehacer
        refrescarPanelPila();
    }

    /** UNIDAD 3 · PILA — POP: revierte la ÚLTIMA acción hecha sobre el catálogo. */
    private void deshacer() {
        AccionCatalogo accion = pilaDeshacer.desapilar();
        if (accion == null) {
            mostrarMensaje("No hay acciones para deshacer.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            aplicarAccion(accion, true);
            pilaRehacer.apilar(accion);
            registrarAccionBitacora("DESHACER_" + accion.getTipo() + "_LIBRO_" + accion.getCodigo());
            refrescarTrasPila(accion);
        } catch (SQLException ex) {
            pilaDeshacer.apilar(accion);     // no se pudo revertir: la acción vuelve a la pila
            mostrarMensaje("No se pudo deshacer: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        refrescarPanelPila();
    }

    /** UNIDAD 3 · PILA — vuelve a aplicar la última acción deshecha. */
    private void rehacer() {
        AccionCatalogo accion = pilaRehacer.desapilar();
        if (accion == null) {
            mostrarMensaje("No hay acciones para rehacer.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            aplicarAccion(accion, false);
            pilaDeshacer.apilar(accion);
            registrarAccionBitacora("REHACER_" + accion.getTipo() + "_LIBRO_" + accion.getCodigo());
            refrescarTrasPila(accion);
        } catch (SQLException ex) {
            pilaRehacer.apilar(accion);
            mostrarMensaje("No se pudo rehacer: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        refrescarPanelPila();
    }

    /** UNIDAD 3 · PILA — aplica (revertir=false) o revierte (revertir=true) una acción en la base de datos. */
    private void aplicarAccion(AccionCatalogo a, boolean revertir) throws SQLException {
        switch (a.getTipo()) {
            case AGREGAR:      // revertir un AGREGAR = eliminar el libro
                if (revertir) bdEliminar(a.getDespues().getCodigo()); else bdInsertar(a.getDespues());
                break;
            case ELIMINAR:     // revertir un ELIMINAR = volver a insertar el libro guardado
                if (revertir) bdInsertar(a.getAntes()); else bdEliminar(a.getAntes().getCodigo());
                break;
            default:           // MODIFICAR (incluye préstamos): se restaura el estado anterior / posterior
                bdActualizar(revertir ? a.getAntes() : a.getDespues());
                break;
        }
    }

    /** Recarga catálogo y estantería y se coloca en la categoría del libro afectado. */
    private void refrescarTrasPila(AccionCatalogo accion) {
        sincronizarListaDesdeBD();
        cargarEstanteriaBidimensional();
        final int codigo = accion.getCodigo();
        Libro afectado = listaLibros.buscar(l -> l.getCodigo() == codigo);
        if (afectado != null) {
            irACategoria(afectado.getCategoria(), codigo);
        } else {
            mostrarCatalogo(txtBuscarLibro.getText().trim());
        }
    }

    private void bdInsertar(Libro l) throws SQLException {
        String sql = "INSERT INTO libro (codigo, titulo, autor, categoria, ubicacion_estante, copias_totales, copias_disponibles) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, l.getCodigo());
            ps.setString(2, l.getTitulo());
            ps.setString(3, l.getAutor());
            ps.setString(4, l.getCategoria());
            ps.setString(5, l.getUbicacion());
            ps.setInt(6, l.getCopiasTotales());
            ps.setInt(7, l.getCopiasDisponibles());
            ps.executeUpdate();
        }
    }

    private void bdActualizar(Libro l) throws SQLException {
        String sql = "UPDATE libro SET titulo = ?, autor = ?, categoria = ?, ubicacion_estante = ?, copias_totales = ?, copias_disponibles = ? WHERE codigo = ?";
        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getCategoria());
            ps.setString(4, l.getUbicacion());
            ps.setInt(5, l.getCopiasTotales());
            ps.setInt(6, l.getCopiasDisponibles());
            ps.setInt(7, l.getCodigo());
            ps.executeUpdate();
        }
    }

    private void bdEliminar(int codigo) throws SQLException {
        try (Connection con = getConexion(); PreparedStatement ps = con.prepareStatement("DELETE FROM libro WHERE codigo = ?")) {
            ps.setInt(1, codigo);
            ps.executeUpdate();
        }
    }

    /** UNIDAD 3 · PILA — pestaña que muestra las dos pilas (tope arriba) con los botones Deshacer y Rehacer. */
    private JPanel panelHistorialPila() {
        JPanel card = new TarjetaRedondeada();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        top.setOpaque(false);
        JButton btnDes = crearBotonIOS("↶ Deshacer (pop)", COLOR_NARANJA_IOS);
        btnDes.addActionListener(e -> deshacer());
        top.add(btnDes);
        JButton btnReh = crearBotonIOS("↷ Rehacer", COLOR_AZUL_IOS);
        btnReh.addActionListener(e -> rehacer());
        top.add(btnReh);
        lblTopePila = new JLabel(" ");
        lblTopePila.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTopePila.setForeground(COLOR_AZUL_IOS);
        top.add(lblTopePila);

        JLabel info = new JLabel("Pila LIFO: la última acción hecha en el catálogo (agregar, modificar, eliminar, prestar, devolver) es la primera que se deshace.");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(COLOR_TEXTO_SECUNDARIO);
        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(top, BorderLayout.CENTER);
        norte.add(info, BorderLayout.SOUTH);
        card.add(norte, BorderLayout.NORTH);

        String[] columnas = {"Posición", "Acción", "Libro", "Detalle"};
        modeloPilaDeshacer = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        modeloPilaRehacer = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tDes = new JTable(modeloPilaDeshacer);
        JTable tReh = new JTable(modeloPilaRehacer);
        estilizarTabla(tDes);
        estilizarTabla(tReh);

        JPanel izq = new JPanel(new BorderLayout(0, 4));
        izq.setOpaque(false);
        izq.add(new JLabel("Pila DESHACER (tope arriba)"), BorderLayout.NORTH);
        izq.add(new JScrollPane(tDes), BorderLayout.CENTER);
        JPanel der = new JPanel(new BorderLayout(0, 4));
        der.setOpaque(false);
        der.add(new JLabel("Pila REHACER (tope arriba)"), BorderLayout.NORTH);
        der.add(new JScrollPane(tReh), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izq, der);
        split.setResizeWeight(0.5);
        split.setBorder(null);
        card.add(split, BorderLayout.CENTER);

        refrescarPanelPila();
        return card;
    }

    private void refrescarPanelPila() {
        if (modeloPilaDeshacer == null) return;
        llenarTablaPila(modeloPilaDeshacer, pilaDeshacer.aListaDesdeElTope());
        llenarTablaPila(modeloPilaRehacer, pilaRehacer.aListaDesdeElTope());
        AccionCatalogo tope = pilaDeshacer.verTope();
        lblTopePila.setText(tope == null ? "Pila vacía: nada que deshacer" : "Tope: " + tope + "   |   Acciones apiladas: " + pilaDeshacer.getTamanio());
    }

    private void llenarTablaPila(DefaultTableModel modelo, List<AccionCatalogo> acciones) {
        modelo.setRowCount(0);
        int pos = 1;
        for (AccionCatalogo a : acciones) {
            modelo.addRow(new Object[]{pos == 1 ? "1  ← TOPE" : String.valueOf(pos), a.getTipo(),
                "[" + a.getCodigo() + "] " + a.getTitulo(), a.getDetalle() == null ? "" : a.getDetalle()});
            pos++;
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

    // ╔════════════════════════════════════════════════════════════════════════════╗
    // ║ [AGREGADO UNIDAD 5 · GRAFOS - IMPLEMENTACIÓN DE GRAFO Y PANEL VISUAL]       ║
    // ║ Modela los pasillos entre el MOSTRADOR y los estantes (A1..C4) como un     ║
    // ║ grafo no dirigido ponderado con Dijkstra (ruta más corta), BFS y DFS.      ║
    // ╚════════════════════════════════════════════════════════════════════════════╝
    private static class GrafoEstantes {

        static class Arista {
            final String destino;
            final int peso;

            Arista(String destino, int peso) {
                this.destino = destino;
                this.peso = peso;
            }
        }

        static class ResultadoRuta {
            final List<String> camino;
            final int distanciaTotal;

            ResultadoRuta(List<String> camino, int distanciaTotal) {
                this.camino = camino;
                this.distanciaTotal = distanciaTotal;
            }
        }

        private final Map<String, List<Arista>> adyacencia = new LinkedHashMap<>();
        // [AGREGADO BLOQUEO TEMPORAL] Conjunto de estantes bloqueados por mantenimiento
        private final Set<String> bloqueados = new HashSet<>();

        void agregarVertice(String v) {
            adyacencia.putIfAbsent(v, new ArrayList<>());
        }

        void agregarArista(String origen, String destino, int peso) {
            agregarVertice(origen);
            agregarVertice(destino);
            adyacencia.get(origen).add(new Arista(destino, peso));
            adyacencia.get(destino).add(new Arista(origen, peso));
        }

        boolean estaBloqueado(String v) {
            return bloqueados.contains(v);
        }

        boolean alternarBloqueo(String v) {
            if ("MOSTRADOR".equals(v)) return false;
            if (bloqueados.contains(v)) {
                bloqueados.remove(v);
                return false;
            } else {
                bloqueados.add(v);
                return true;
            }
        }

        Set<String> getVertices() {
            return adyacencia.keySet();
        }

        List<Arista> getVecinos(String v) {
            return adyacencia.getOrDefault(v, Collections.emptyList());
        }

        /** Recorrido en Anchura (BFS) usando Cola (omite nodos bloqueados) */
        List<String> bfs(String inicio) {
            List<String> orden = new ArrayList<>();
            if (!adyacencia.containsKey(inicio) || estaBloqueado(inicio)) return orden;

            Set<String> visitados = new LinkedHashSet<>();
            Queue<String> cola = new LinkedList<>();
            visitados.add(inicio);
            cola.add(inicio);

            while (!cola.isEmpty()) {
                String actual = cola.poll();
                orden.add(actual);
                for (Arista a : getVecinos(actual)) {
                    if (!estaBloqueado(a.destino) && visitados.add(a.destino)) {
                        cola.add(a.destino);
                    }
                }
            }
            return orden;
        }

        /** Recorrido en Profundidad (DFS) (omite nodos bloqueados) */
        List<String> dfs(String inicio) {
            List<String> orden = new ArrayList<>();
            if (!adyacencia.containsKey(inicio) || estaBloqueado(inicio)) return orden;
            dfsRec(inicio, new HashSet<>(), orden);
            return orden;
        }

        private void dfsRec(String actual, Set<String> visitados, List<String> orden) {
            visitados.add(actual);
            orden.add(actual);
            for (Arista a : getVecinos(actual)) {
                if (!estaBloqueado(a.destino) && !visitados.contains(a.destino)) {
                    dfsRec(a.destino, visitados, orden);
                }
            }
        }

        /** Algoritmo de Dijkstra para calcular el camino más corto esquivando nodos bloqueados */
        ResultadoRuta dijkstra(String origen, String destino) {
            if (!adyacencia.containsKey(origen) || !adyacencia.containsKey(destino)
                    || estaBloqueado(origen) || estaBloqueado(destino)) {
                return new ResultadoRuta(Collections.emptyList(), -1);
            }

            Map<String, Integer> dist = new HashMap<>();
            Map<String, String> previo = new HashMap<>();
            for (String v : adyacencia.keySet()) {
                dist.put(v, Integer.MAX_VALUE);
            }
            dist.put(origen, 0);

            PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
            pq.add(origen);

            while (!pq.isEmpty()) {
                String u = pq.poll();
                if (u.equals(destino)) break;

                for (Arista a : getVecinos(u)) {
                    if (estaBloqueado(a.destino)) continue;
                    int nuevaDist = dist.get(u) + a.peso;
                    if (nuevaDist < dist.get(a.destino)) {
                        dist.put(a.destino, nuevaDist);
                        previo.put(a.destino, u);
                        pq.remove(a.destino);
                        pq.add(a.destino);
                    }
                }
            }

            if (dist.get(destino) == Integer.MAX_VALUE) {
                return new ResultadoRuta(Collections.emptyList(), -1);
            }

            LinkedList<String> camino = new LinkedList<>();
            for (String at = destino; at != null; at = previo.get(at)) {
                camino.addFirst(at);
            }
            return new ResultadoRuta(camino, dist.get(destino));
        }
    }

    private class PanelGrafos extends TarjetaRedondeada {

        private final Supplier<List<Libro>> proveedorLibros;
        private final GrafoEstantes grafo = new GrafoEstantes();
        private final Map<String, Point> posiciones = new LinkedHashMap<>();

        private JComboBox<String> cbOrigen;
        private JComboBox<String> cbDestino;
        private JComboBox<String> cbLibroBuscar;
        // [AGREGADO BLOQUEO TEMPORAL] Combo para elegir qué estante bloquear/desbloquear
        private JComboBox<String> cbEstanteBloqueo;
        private JLabel lblResultado;
        private LienzoGrafo lienzo;

        private List<String> rutaResaltada = Collections.emptyList();
        private List<String> recorridoResaltado = Collections.emptyList();

        // [AGREGADO ANIMACIÓN BFS / DFS] Variables de control para animar paso a paso
        private Timer timerAnimacion;
        private List<String> secuenciaAnimacion = Collections.emptyList();
        private int indiceAnimacion = 0;
        private Color colorAnimacion = COLOR_VERDE_IOS;

        PanelGrafos(Supplier<List<Libro>> proveedorLibros) {
            this.proveedorLibros = proveedorLibros;
            setLayout(new BorderLayout(12, 12));
            setBorder(new EmptyBorder(15, 15, 15, 15));
            construirMapaBiblioteca();
            iniciarUI();
            recargar();
        }

        private void construirMapaBiblioteca() {
            posiciones.put("MOSTRADOR", new Point(100, 250));

            int[] yFilas = {100, 250, 400};
            for (int f = 0; f < FILAS.length; f++) {
                for (int c = 1; c <= COLUMNAS; c++) {
                    String nodo = FILAS[f] + c;
                    posiciones.put(nodo, new Point(260 + (c - 1) * 195, yFilas[f]));
                }
            }

            // Conexiones desde el Mostrador hacia la columna 1 de estantes
            grafo.agregarArista("MOSTRADOR", "A1", 4);
            grafo.agregarArista("MOSTRADOR", "B1", 2);
            grafo.agregarArista("MOSTRADOR", "C1", 4);

            // Pasillos horizontales y verticales entre los estantes A1..C4
            for (int f = 0; f < FILAS.length; f++) {
                for (int c = 1; c <= COLUMNAS; c++) {
                    String actual = FILAS[f] + c;
                    if (c < COLUMNAS) {
                        grafo.agregarArista(actual, FILAS[f] + (c + 1), 3);
                    }
                    if (f < FILAS.length - 1) {
                        grafo.agregarArista(actual, FILAS[f + 1] + c, 5);
                    }
                }
            }
        }

        private void iniciarUI() {
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            top.setOpaque(false);

            String[] nodos = posiciones.keySet().toArray(new String[0]);
            cbOrigen = new JComboBox<>(nodos);
            cbDestino = new JComboBox<>(nodos);
            cbDestino.setSelectedItem("C4");

            top.add(new JLabel("Origen:"));
            top.add(cbOrigen);
            top.add(new JLabel("Destino:"));
            top.add(cbDestino);

            JButton btnDijkstra = crearBotonIOS("Ruta más corta (Dijkstra)", COLOR_AZUL_IOS);
            btnDijkstra.addActionListener(e -> calcularRuta());
            top.add(btnDijkstra);

            JButton btnBfs = crearBotonIOS("BFS", COLOR_VERDE_IOS);
            btnBfs.addActionListener(e -> ejecutarRecorrido(true));
            top.add(btnBfs);

            JButton btnDfs = crearBotonIOS("DFS", COLOR_NARANJA_IOS);
            btnDfs.addActionListener(e -> ejecutarRecorrido(false));
            top.add(btnDfs);

            JPanel subTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
            subTop.setOpaque(false);
            subTop.add(new JLabel("Guiar hacia libro asignado:"));
            cbLibroBuscar = new JComboBox<>();
            cbLibroBuscar.setPreferredSize(new Dimension(250, 28));
            subTop.add(cbLibroBuscar);

            JButton btnIrLibro = crearBotonIOS("Trazar ruta al libro", new Color(88, 86, 214));
            btnIrLibro.addActionListener(e -> trazarRutaHaciaLibro());
            subTop.add(btnIrLibro);

            // [AGREGADO BLOQUEO TEMPORAL] Controles para bloquear / desbloquear un estante
            subTop.add(new JLabel("Estante:"));
            cbEstanteBloqueo = new JComboBox<>();
            for (String f : FILAS) {
                for (int c = 1; c <= COLUMNAS; c++) {
                    cbEstanteBloqueo.addItem(f + c);
                }
            }
            cbEstanteBloqueo.setSelectedItem("B1");
            subTop.add(cbEstanteBloqueo);

            JButton btnBloquear = crearBotonIOS("Bloquear / Desbloquear", COLOR_ROJO_IOS);
            btnBloquear.addActionListener(e -> toggleBloqueoEstante());
            subTop.add(btnBloquear);

            JPanel panelNorte = new JPanel(new GridLayout(2, 1));
            panelNorte.setOpaque(false);
            panelNorte.add(top);
            panelNorte.add(subTop);
            add(panelNorte, BorderLayout.NORTH);

            lienzo = new LienzoGrafo();
            add(lienzo, BorderLayout.CENTER);

            lblResultado = new JLabel("Seleccione origen y destino para calcular la ruta óptima entre estantes.");
            lblResultado.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblResultado.setForeground(COLOR_TEXTO_TITULO);
            add(lblResultado, BorderLayout.SOUTH);
        }

        void recargar() {
            if (cbLibroBuscar == null) return;
            cbLibroBuscar.removeAllItems();
            for (Libro l : proveedorLibros.get()) {
                if (l.getUbicacion() != null && !l.getUbicacion().isBlank()) {
                    cbLibroBuscar.addItem(l.getCodigo() + " - " + l.getTitulo() + " [" + l.getUbicacion().toUpperCase() + "]");
                }
            }
            if (cbLibroBuscar.getItemCount() == 0) {
                cbLibroBuscar.addItem("Sin libros asignados a estantes");
            }
            lienzo.repaint();
        }

        private void detenerAnimacion() {
            if (timerAnimacion != null && timerAnimacion.isRunning()) {
                timerAnimacion.stop();
            }
        }

        // [AGREGADO BLOQUEO TEMPORAL] Bloquea o habilita el estante elegido y recalcula la ruta
        private void toggleBloqueoEstante() {
            detenerAnimacion();
            String estante = (String) cbEstanteBloqueo.getSelectedItem();
            if (estante == null) return;

            boolean ahoraBloqueado = grafo.alternarBloqueo(estante);
            rutaResaltada = Collections.emptyList();
            recorridoResaltado = Collections.emptyList();

            if (ahoraBloqueado) {
                lblResultado.setText("Estante " + estante + " BLOQUEADO temporalmente. Presione Dijkstra, BFS o DFS para ver la ruta alternativa.");
            } else {
                lblResultado.setText("Estante " + estante + " HABILITADO nuevamente.");
            }
            lienzo.repaint();
        }

        private void calcularRuta() {
            detenerAnimacion();
            String origen = (String) cbOrigen.getSelectedItem();
            String destino = (String) cbDestino.getSelectedItem();
            recorridoResaltado = Collections.emptyList();

            if (grafo.estaBloqueado(origen) || grafo.estaBloqueado(destino)) {
                rutaResaltada = Collections.emptyList();
                String msg = "El origen (" + origen + ") o el destino (" + destino + ") está bloqueado o cerrado temporalmente.";
                lblResultado.setText(msg);
                lienzo.repaint();
                mostrarMensaje(msg, JOptionPane.WARNING_MESSAGE);
                return;
            }

            GrafoEstantes.ResultadoRuta res = grafo.dijkstra(origen, destino);
            rutaResaltada = res.camino;

            if (res.camino.isEmpty()) {
                lblResultado.setText("No existe ruta disponible entre " + origen + " y " + destino + " con los bloqueos actuales.");
            } else {
                lblResultado.setText("Ruta óptima (" + res.distanciaTotal + " m): " + String.join(" → ", res.camino));
            }
            lienzo.repaint();
        }

        private void trazarRutaHaciaLibro() {
            String sel = (String) cbLibroBuscar.getSelectedItem();
            if (sel == null || !sel.contains("[")) return;
            String estante = sel.substring(sel.lastIndexOf('[') + 1, sel.lastIndexOf(']'));
            cbOrigen.setSelectedItem("MOSTRADOR");
            cbDestino.setSelectedItem(estante);
            calcularRuta();
        }

        // [MODIFICADO PARA ANIMACIÓN] Inicia un Timer que avanza nodo por nodo en BFS o DFS
        private void ejecutarRecorrido(boolean esBfs) {
            detenerAnimacion();
            String origen = (String) cbOrigen.getSelectedItem();
            rutaResaltada = Collections.emptyList();

            if (grafo.estaBloqueado(origen)) {
                recorridoResaltado = Collections.emptyList();
                String msg = "El nodo de origen (" + origen + ") está bloqueado o cerrado temporalmente.";
                lblResultado.setText(msg);
                lienzo.repaint();
                mostrarMensaje(msg, JOptionPane.WARNING_MESSAGE);
                return;
            }

            secuenciaAnimacion = esBfs ? grafo.bfs(origen) : grafo.dfs(origen);
            colorAnimacion = esBfs ? COLOR_VERDE_IOS : COLOR_NARANJA_IOS;
            final String tipo = esBfs ? "BFS (Anchura)" : "DFS (Profundidad)";

            if (secuenciaAnimacion.isEmpty()) return;

            indiceAnimacion = 1;
            recorridoResaltado = new ArrayList<>(secuenciaAnimacion.subList(0, indiceAnimacion));
            lblResultado.setText(tipo + " [Paso " + indiceAnimacion + "/" + secuenciaAnimacion.size() + "]: "
                    + String.join(" → ", recorridoResaltado));
            lienzo.repaint();

            timerAnimacion = new Timer(350, e -> {
                if (indiceAnimacion < secuenciaAnimacion.size()) {
                    indiceAnimacion++;
                    recorridoResaltado = new ArrayList<>(secuenciaAnimacion.subList(0, indiceAnimacion));
                    lblResultado.setText(tipo + " [Paso " + indiceAnimacion + "/" + secuenciaAnimacion.size() + "]: "
                            + String.join(" → ", recorridoResaltado));
                    lienzo.repaint();
                } else {
                    ((Timer) e.getSource()).stop();
                    lblResultado.setText(tipo + " desde " + origen + " (completado): "
                            + String.join(" → ", recorridoResaltado));
                    lienzo.repaint();
                }
            });
            timerAnimacion.start();
        }

        private class LienzoGrafo extends JPanel {
            LienzoGrafo() {
                setBackground(new Color(248, 248, 250));
                setBorder(BorderFactory.createLineBorder(COLOR_GRIS_BORDE, 1));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                double sx = getWidth() / 1000.0;
                double sy = getHeight() / 500.0;

                // 1. Dibujar aristas y distancias en metros
                Set<String> dibujadas = new HashSet<>();
                for (String u : grafo.getVertices()) {
                    Point p1 = posiciones.get(u);
                    for (GrafoEstantes.Arista a : grafo.getVecinos(u)) {
                        String clave = u.compareTo(a.destino) < 0 ? u + "-" + a.destino : a.destino + "-" + u;
                        if (!dibujadas.add(clave)) continue;

                        Point p2 = posiciones.get(a.destino);
                        int x1 = (int) (p1.x * sx), y1 = (int) (p1.y * sy);
                        int x2 = (int) (p2.x * sx), y2 = (int) (p2.y * sy);

                        boolean aristaBloqueada = grafo.estaBloqueado(u) || grafo.estaBloqueado(a.destino);
                        boolean enRuta = !aristaBloqueada && estaEnRuta(u, a.destino);
                        boolean enAnimacion = !aristaBloqueada && estaEnRecorridoAnimado(u, a.destino);

                        if (aristaBloqueada) {
                            g2.setColor(new Color(255, 59, 48, 90));
                            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                        } else if (enRuta) {
                            g2.setColor(COLOR_AZUL_IOS);
                            g2.setStroke(new BasicStroke(4f));
                        } else if (enAnimacion) {
                            g2.setColor(colorAnimacion);
                            g2.setStroke(new BasicStroke(3.5f));
                        } else {
                            g2.setColor(new Color(200, 200, 205));
                            g2.setStroke(new BasicStroke(2f));
                        }
                        g2.drawLine(x1, y1, x2, y2);

                        int mx = (x1 + x2) / 2;
                        int my = (y1 + y2) / 2;
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(mx - 14, my - 10, 28, 20, 8, 8);
                        g2.setColor(aristaBloqueada ? new Color(255, 59, 48, 130)
                                : (enRuta ? COLOR_AZUL_IOS : (enAnimacion ? colorAnimacion : COLOR_TEXTO_SECUNDARIO)));
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                        g2.drawString(a.peso + "m", mx - 10, my + 4);
                    }
                }

                // Nodo que está siendo visitado en este instante de la animación
                String nodoActualAnim = (!recorridoResaltado.isEmpty() && timerAnimacion != null && timerAnimacion.isRunning())
                        ? recorridoResaltado.get(recorridoResaltado.size() - 1) : null;

                // 2. Dibujar vértices (nodos)
                for (Map.Entry<String, Point> entry : posiciones.entrySet()) {
                    String nombre = entry.getKey();
                    int cx = (int) (entry.getValue().x * sx);
                    int cy = (int) (entry.getValue().y * sy);
                    boolean esMostrador = "MOSTRADOR".equals(nombre);
                    boolean bloqueado = grafo.estaBloqueado(nombre);
                    int w = esMostrador ? 96 : 54;
                    int h = 38;

                    Color relleno = Color.WHITE;
                    Color borde = COLOR_GRIS_BORDE;
                    Color texto = COLOR_TEXTO_TITULO;

                    int idxVisita = recorridoResaltado.indexOf(nombre);

                    if (bloqueado) {
                        relleno = new Color(255, 232, 230);
                        borde = COLOR_ROJO_IOS;
                        texto = COLOR_ROJO_IOS;
                    } else if (rutaResaltada.contains(nombre)) {
                        relleno = COLOR_AZUL_IOS;
                        borde = COLOR_AZUL_IOS;
                        texto = Color.WHITE;
                    } else if (idxVisita != -1) {
                        relleno = colorAnimacion;
                        borde = nombre.equals(nodoActualAnim) ? COLOR_TEXTO_TITULO : colorAnimacion.darker();
                        texto = Color.WHITE;
                    } else if (esMostrador) {
                        relleno = new Color(235, 245, 255);
                        borde = COLOR_AZUL_IOS;
                    }

                    // Halo de pulso en el nodo activo durante la animación
                    if (!bloqueado && nombre.equals(nodoActualAnim)) {
                        g2.setColor(new Color(colorAnimacion.getRed(), colorAnimacion.getGreen(), colorAnimacion.getBlue(), 80));
                        g2.fillRoundRect(cx - w / 2 - 6, cy - h / 2 - 6, w + 12, h + 12, 18, 18);
                    }

                    g2.setColor(relleno);
                    g2.fillRoundRect(cx - w / 2, cy - h / 2, w, h, 14, 14);
                    g2.setStroke(new BasicStroke((!bloqueado && nombre.equals(nodoActualAnim)) ? 3f : 2f));
                    g2.setColor(borde);
                    g2.drawRoundRect(cx - w / 2, cy - h / 2, w, h, 14, 14);

                    g2.setColor(texto);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, esMostrador ? 11 : 13));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(nombre, cx - fm.stringWidth(nombre) / 2, cy + fm.getAscent() / 2 - 2);

                    if (bloqueado) {
                        String badge = "BLOQ";
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                        FontMetrics fmB = g2.getFontMetrics();
                        int bw = fmB.stringWidth(badge) + 8;
                        int bx = cx - bw / 2;
                        int by = cy - h / 2 - 12;
                        g2.setColor(COLOR_ROJO_IOS);
                        g2.fillRoundRect(bx, by, bw, 14, 8, 8);
                        g2.setColor(Color.WHITE);
                        g2.drawString(badge, bx + 4, by + 10);
                    } else if (idxVisita != -1) {
                        // Etiqueta pequeña con el número de orden de visita (#1, #2, ...)
                        String badge = "#" + (idxVisita + 1);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        FontMetrics fmB = g2.getFontMetrics();
                        int bw = fmB.stringWidth(badge) + 8;
                        int bx = cx + w / 2 - bw / 2;
                        int by = cy - h / 2 - 12;
                        g2.setColor(COLOR_TEXTO_TITULO);
                        g2.fillRoundRect(bx, by, bw, 15, 8, 8);
                        g2.setColor(Color.WHITE);
                        g2.drawString(badge, bx + 4, by + 11);
                    }
                }
                g2.dispose();
            }

            private boolean estaEnRuta(String u, String v) {
                for (int i = 0; i < rutaResaltada.size() - 1; i++) {
                    String a = rutaResaltada.get(i);
                    String b = rutaResaltada.get(i + 1);
                    if ((a.equals(u) && b.equals(v)) || (a.equals(v) && b.equals(u))) return true;
                }
                return false;
            }

            private boolean estaEnRecorridoAnimado(String u, String v) {
                if (recorridoResaltado.size() < 2) return false;
                // Conecta cada nodo visitado con el primer nodo ya visitado que sea vecino suyo
                for (int i = 1; i < recorridoResaltado.size(); i++) {
                    String hijo = recorridoResaltado.get(i);
                    for (int j = 0; j < i; j++) {
                        String padre = recorridoResaltado.get(j);
                        boolean sonVecinos = false;
                        for (GrafoEstantes.Arista ar : grafo.getVecinos(padre)) {
                            if (ar.destino.equals(hijo)) {
                                sonVecinos = true;
                                break;
                            }
                        }
                        if (sonVecinos) {
                            if ((padre.equals(u) && hijo.equals(v)) || (padre.equals(v) && hijo.equals(u))) {
                                return true;
                            }
                            break;
                        }
                    }
                }
                return false;
            }
        }
    }
    // ═══════════════════ FIN DEL BLOQUE AGREGADO DE GRAFOS ═══════════════════

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        java.awt.EventQueue.invokeLater(() -> {
            new FrmPrincipal().setVisible(true);
        });
    }
}