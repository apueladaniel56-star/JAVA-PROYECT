package vista;

import estructuras.ABB;
import estructuras.ArbolAVL;
import estructuras.ArbolBinario;
import estructuras.ArbolBinario.Recorrido;
import estructuras.NodoArbol;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import modelo.Libro;


public class PanelArboles extends JPanel {

    private final Supplier<List<Libro>> catalogo;

    // 1) Árbol binario
    private final VistaArbol<String> vistaJerarquia = new VistaArbol<>();

    // 2) ABB
    private ABB<Libro> abb;
    private final VistaArbol<Libro> vistaABB = new VistaArbol<>();
    private final JComboBox<String> cbIndice = new JComboBox<>(new String[]{"Código", "Título"});
    private final JTextField txtBuscarABB = EstiloUI.campo(14);
    private final JComboBox<String> cbLibrosABB = new JComboBox<>();
    private final JLabel lblResultadoABB = new JLabel(" ");
    private List<Libro> librosCombo = new ArrayList<>();

    // 3) AVL
    private final ArbolAVL<Integer> avl = new ArbolAVL<>(Comparator.naturalOrder(), String::valueOf);
    private final ABB<Integer> sinBalancear = new ABB<>(Comparator.naturalOrder()); // mismo contenido, SIN balancear (comparación)
    private final VistaArbol<Integer> vistaAVL = new VistaArbol<>();
    private final JTextField txtCodigoAVL = EstiloUI.campo(8);
    private final JTextArea bitacoraAVL = EstiloUI.areaTexto(10);
    private final JLabel lblEstadoAVL = new JLabel(" ");

    public PanelArboles(Supplier<List<Libro>> catalogo) {
        this.catalogo = catalogo;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        JTabbedPane sub = new JTabbedPane();
        sub.addTab("Árbol binario y recorridos", panelJerarquia());
        sub.addTab("ABB (búsqueda y orden)", panelABB());
        sub.addTab("AVL (balanceo)", panelAVL());
        add(sub, BorderLayout.CENTER);

        recargar();
        mostrarAVL();
    }

    /** Reconstruye el árbol binario y el ABB a partir del catálogo actual (se llama al abrir la pestaña). */
    public void recargar() {
        construirJerarquia();
        reconstruirABB();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UNIDAD 4 · ÁRBOL BINARIO Y RECORRIDOS (preorden, inorden, postorden)
    // ═══════════════════════════════════════════════════════════════════

    private JPanel panelJerarquia() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 8, 8, 8));
        JLabel info = EstiloUI.etiquetaInfo("<html><div style='width:880px'>Jerarquía <b>Biblioteca → Categorías → Libros</b> representada como árbol binario: "
                + "el <b>hijo izquierdo</b> es el primer elemento del nivel de abajo y el <b>hijo derecho</b> es el siguiente hermano "
                + "(la siguiente categoría o el siguiente libro de la misma categoría).</div></html>");
        p.add(info, BorderLayout.NORTH);
        p.add(vistaJerarquia, BorderLayout.CENTER);
        return p;
    }

    /** Arma el árbol binario Biblioteca -> categorías -> libros (hijo izquierdo / hermano derecho). */
    private void construirJerarquia() {
        Map<String, List<Libro>> porCategoria = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Libro l : catalogo.get()) {
            String cat = (l.getCategoria() == null || l.getCategoria().isBlank()) ? "Sin categoría" : l.getCategoria().trim();
            porCategoria.computeIfAbsent(cat, k -> new ArrayList<>()).add(l);
        }
        ArbolBinario<String> arbol = new ArbolBinario<>();
        NodoArbol<String> raiz = new NodoArbol<>("Biblioteca");
        arbol.setRaiz(raiz);

        NodoArbol<String> ultimaCategoria = null;
        for (Map.Entry<String, List<Libro>> e : porCategoria.entrySet()) {
            NodoArbol<String> nodoCat = new NodoArbol<>(e.getKey());
            if (ultimaCategoria == null) raiz.izq = nodoCat; else ultimaCategoria.der = nodoCat;
            ultimaCategoria = nodoCat;

            List<Libro> libros = e.getValue();
            libros.sort(Libro.POR_CODIGO);
            NodoArbol<String> ultimoLibro = null;
            for (Libro l : libros) {
                NodoArbol<String> nodoLibro = new NodoArbol<>(String.valueOf(l.getCodigo()));
                if (ultimoLibro == null) nodoCat.izq = nodoLibro; else ultimoLibro.der = nodoLibro;
                ultimoLibro = nodoLibro;
            }
        }
        vistaJerarquia.mostrar(arbol, s -> s, false);
        vistaJerarquia.setInfoExtra("Los libros se muestran por su código.");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UNIDAD 4 · ABB (almacenamiento indexado para búsquedas y ordenamientos)
    // ═══════════════════════════════════════════════════════════════════

    private JPanel panelABB() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controles.setOpaque(false);
        controles.add(new JLabel("Indexar por:"));
        controles.add(cbIndice);
        cbIndice.addActionListener(e -> reconstruirABB());

        controles.add(new JLabel("Valor:"));
        controles.add(txtBuscarABB);
        JButton btnBuscar = EstiloUI.boton("Buscar", EstiloUI.AZUL);
        btnBuscar.addActionListener(e -> buscarEnABB());
        controles.add(btnBuscar);
        JButton btnEliminar = EstiloUI.boton("Quitar del índice", EstiloUI.ROJO);
        btnEliminar.addActionListener(e -> eliminarDeABB());
        controles.add(btnEliminar);

        controles.add(cbLibrosABB);
        JButton btnInsertar = EstiloUI.boton("Insertar en índice", EstiloUI.VERDE);
        btnInsertar.addActionListener(e -> insertarEnABB());
        controles.add(btnInsertar);
        JButton btnReconstruir = EstiloUI.boton("Reconstruir", EstiloUI.GRIS);
        btnReconstruir.addActionListener(e -> reconstruirABB());
        controles.add(btnReconstruir);

        lblResultadoABB.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblResultadoABB.setForeground(EstiloUI.AZUL);
        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(controles, BorderLayout.CENTER);
        norte.add(lblResultadoABB, BorderLayout.SOUTH);

        p.add(norte, BorderLayout.NORTH);
        p.add(vistaABB, BorderLayout.CENTER);
        return p;
    }

    private boolean indicePorCodigo() { return cbIndice.getSelectedIndex() == 0; }

    private Function<Libro, String> etiquetaABB() {
        return indicePorCodigo() ? l -> String.valueOf(l.getCodigo()) : Libro::getTitulo;
    }

    /** Construye el ABB insertando cada libro del catálogo (la clave es el código o el título). */
    private void reconstruirABB() {
        Comparator<Libro> cmp = indicePorCodigo() ? Libro.POR_CODIGO : Libro.POR_TITULO;
        abb = new ABB<>(cmp);
        // Se insertan primero los libros del "medio" para que el ABB quede parejo (si se insertaran
        // ya ordenados se volvería una lista). El AVL de la otra pestaña logra esto SOLO, con rotaciones.
        List<Libro> ordenados = new ArrayList<>(catalogo.get());
        ordenados.sort(cmp);
        List<Libro> ordenInsercion = new ArrayList<>();
        medianaPrimero(ordenados, 0, ordenados.size() - 1, ordenInsercion);
        int repetidos = 0;
        for (Libro l : ordenInsercion) {
            if (!abb.insertar(l)) repetidos++;
        }
        vistaABB.mostrar(abb, etiquetaABB(), false);
        vistaABB.setInfoExtra(repetidos > 0 ? repetidos + " libro(s) con la misma clave no se indexan (el ABB no admite repetidos)." : "");
        lblResultadoABB.setText(" ");

        librosCombo = new ArrayList<>(catalogo.get());
        cbLibrosABB.removeAllItems();
        for (Libro l : librosCombo) cbLibrosABB.addItem("[" + l.getCodigo() + "] " + l.getTitulo());
        vistaABB.recorrer(Recorrido.INORDEN, false);
    }

    /** Orden de inserción "mediana primero": el elemento del medio, luego el medio de cada mitad, etc. */
    private void medianaPrimero(List<Libro> lista, int desde, int hasta, List<Libro> salida) {
        if (desde > hasta) return;
        int medio = (desde + hasta) / 2;
        salida.add(lista.get(medio));
        medianaPrimero(lista, desde, medio - 1, salida);
        medianaPrimero(lista, medio + 1, hasta, salida);
    }

    private Libro molde(String texto) {
        // "Libro molde": solo trae el campo que usa el comparador (código o título)
        if (indicePorCodigo()) {
            return new Libro(Integer.parseInt(texto.trim()), "", "", "", 0);
        }
        return new Libro(0, texto.trim(), "", "", 0);
    }

    // UNIDAD 4 · ABB — búsqueda: en cada nodo se descarta la mitad del árbol
    private void buscarEnABB() {
        String txt = txtBuscarABB.getText().trim();
        if (txt.isEmpty()) return;
        try {
            Libro clave = molde(txt);
            List<NodoArbol<Libro>> camino = abb.caminoDeBusqueda(clave);
            Libro hallado = abb.buscar(clave);
            Set<NodoArbol<Libro>> visitados = new HashSet<>(camino);
            NodoArbol<Libro> ultimo = camino.isEmpty() ? null : camino.get(camino.size() - 1);
            vistaABB.resaltar(visitados, hallado != null ? ultimo : null);

            StringBuilder ruta = new StringBuilder();
            for (NodoArbol<Libro> n : camino) {
                if (ruta.length() > 0) ruta.append(" → ");
                ruta.append(etiquetaABB().apply(n.dato));
            }
            int total = abb.tamano();
            if (hallado != null) {
                lblResultadoABB.setText("Encontrado: [" + hallado.getCodigo() + "] " + hallado.getTitulo() + " — " + camino.size()
                        + " comparaciones (una búsqueda secuencial haría hasta " + total + "). Ruta: " + ruta);
            } else {
                lblResultadoABB.setText("No encontrado tras " + camino.size() + " comparaciones. Ruta: " + ruta);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "En el índice por código escriba un número.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    // UNIDAD 4 · ABB — eliminación (3 casos: hoja, un hijo, dos hijos). Solo afecta al índice, no al catálogo.
    private void eliminarDeABB() {
        String txt = txtBuscarABB.getText().trim();
        if (txt.isEmpty()) return;
        try {
            boolean ok = abb.eliminar(molde(txt));
            lblResultadoABB.setText(ok ? "Quitado del índice: " + txt + " (el libro sigue en el catálogo)." : "No existe en el índice: " + txt);
            vistaABB.mostrar(abb, etiquetaABB(), false);
            vistaABB.recorrer(Recorrido.INORDEN, false);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "En el índice por código escriba un número.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    // UNIDAD 4 · ABB — inserción
    private void insertarEnABB() {
        int i = cbLibrosABB.getSelectedIndex();
        if (i < 0 || i >= librosCombo.size()) return;
        Libro l = librosCombo.get(i);
        boolean ok = abb.insertar(l);
        lblResultadoABB.setText(ok ? "Insertado en el índice: [" + l.getCodigo() + "] " + l.getTitulo() : "Ya estaba en el índice (clave repetida).");
        vistaABB.mostrar(abb, etiquetaABB(), false);
        vistaABB.recorrer(Recorrido.INORDEN, false);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UNIDAD 4 · AVL (inserción y borrado con balanceo y rotaciones)
    // ═══════════════════════════════════════════════════════════════════

    private JPanel panelAVL() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controles.setOpaque(false);
        controles.add(new JLabel("Código:"));
        controles.add(txtCodigoAVL);
        JButton btnIns = EstiloUI.boton("Insertar", EstiloUI.VERDE);
        btnIns.addActionListener(e -> operarAVL(true));
        controles.add(btnIns);
        JButton btnDel = EstiloUI.boton("Eliminar", EstiloUI.ROJO);
        btnDel.addActionListener(e -> operarAVL(false));
        controles.add(btnDel);
        JButton btnCat = EstiloUI.boton("Cargar códigos del catálogo", EstiloUI.AZUL);
        btnCat.addActionListener(e -> cargarCatalogoEnAVL());
        controles.add(btnCat);
        JButton btnEj = EstiloUI.boton("Ejemplo (10…50, 25)", EstiloUI.NARANJA);
        btnEj.addActionListener(e -> cargarEjemploAVL());
        controles.add(btnEj);
        JButton btnLimpiar = EstiloUI.boton("Limpiar", EstiloUI.GRIS);
        btnLimpiar.addActionListener(e -> limpiarAVL());
        controles.add(btnLimpiar);

        lblEstadoAVL.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstadoAVL.setForeground(EstiloUI.AZUL);
        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(controles, BorderLayout.CENTER);
        norte.add(lblEstadoAVL, BorderLayout.SOUTH);
        p.add(norte, BorderLayout.NORTH);

        p.add(vistaAVL, BorderLayout.CENTER);

        JScrollPane scrollLog = new JScrollPane(bitacoraAVL);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Bitácora del balanceo (evidencia de rotaciones)"));
        scrollLog.setPreferredSize(new Dimension(360, 100));
        p.add(scrollLog, BorderLayout.EAST);
        return p;
    }

    private void operarAVL(boolean insertar) {
        try {
            int cod = Integer.parseInt(txtCodigoAVL.getText().trim());
            if (insertar) {
                avl.insertar(cod);
                sinBalancear.insertar(cod);
            } else {
                avl.eliminar(cod);
                sinBalancear.eliminar(cod);
            }
            txtCodigoAVL.setText("");
            volcarBitacora();
            mostrarAVL();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Escriba un código numérico.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cargarCatalogoEnAVL() {
        List<Libro> libros = new ArrayList<>(catalogo.get());
        libros.sort(Libro.POR_CODIGO);   // en orden ascendente: el peor caso para un ABB, y el que más rotaciones provoca en el AVL
        for (Libro l : libros) {
            avl.insertar(l.getCodigo());
            sinBalancear.insertar(l.getCodigo());
        }
        volcarBitacora();
        mostrarAVL();
    }

    private void cargarEjemploAVL() {
        limpiarAVL();
        for (int c : new int[]{10, 20, 30, 40, 50, 25}) {   // provoca rotaciones simples y una doble
            avl.insertar(c);
            sinBalancear.insertar(c);
        }
        volcarBitacora();
        mostrarAVL();
    }

    private void limpiarAVL() {
        avl.vaciar();
        avl.limpiarBitacora();
        sinBalancear.vaciar();
        bitacoraAVL.setText("");
        mostrarAVL();
    }

    private void volcarBitacora() {
        for (String linea : avl.getBitacora()) bitacoraAVL.append(linea + "\n");
        avl.limpiarBitacora();
        bitacoraAVL.setCaretPosition(bitacoraAVL.getDocument().getLength());
    }

    private void mostrarAVL() {
        vistaAVL.mostrar(avl, String::valueOf, true);
        lblEstadoAVL.setText("Altura AVL: " + avl.altura() + "   |   Altura del mismo contenido en un ABB sin balancear: " + sinBalancear.altura()
                + "   |   Rotaciones: " + avl.getTotalRotaciones() + "   |   Balanceado: " + (avl.estaBalanceado() ? "sí" : "NO"));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Vista reutilizable: dibujo del árbol + botones de recorrido animados
    // ═══════════════════════════════════════════════════════════════════

    /**
     * UNIDAD 4 · RECORRIDOS — botones Preorden / Inorden / Postorden / Por niveles.
     * Cada recorrido se anima nodo por nodo y se escribe el resultado.
     */
    private static class VistaArbol<T> extends JPanel {
        private final PanelDibujoArbol<T> dibujo = new PanelDibujoArbol<>();
        private final JTextArea salida = EstiloUI.areaTexto(3);
        private final JLabel lblInfo = EstiloUI.etiquetaInfo(" ");
        private ArbolBinario<T> arbol;
        private Function<T, String> etiqueta = Object::toString;
        private String infoExtra = "";
        private Timer timer;

        VistaArbol() {
            setLayout(new BorderLayout(6, 6));
            setOpaque(false);
            JScrollPane scroll = new JScrollPane(dibujo);
            scroll.setBorder(BorderFactory.createLineBorder(EstiloUI.BORDE));
            add(scroll, BorderLayout.CENTER);

            // R = raíz, I = hijo izquierdo, D = hijo derecho (orden en que se visitan)
            JPanel botones = new JPanel(new GridLayout(1, 4, 8, 0));
            botones.setOpaque(false);
            botones.add(boton("Preorden (R-I-D)", Recorrido.PREORDEN, EstiloUI.AZUL));
            botones.add(boton("Inorden (I-R-D)", Recorrido.INORDEN, EstiloUI.VERDE));
            botones.add(boton("Postorden (I-D-R)", Recorrido.POSTORDEN, EstiloUI.NARANJA));
            botones.add(boton("Por niveles", Recorrido.POR_NIVELES, EstiloUI.GRIS));

            JPanel sur = new JPanel(new BorderLayout(0, 4));
            sur.setOpaque(false);
            sur.add(botones, BorderLayout.NORTH);
            JScrollPane scrollSalida = new JScrollPane(salida);
            scrollSalida.setPreferredSize(new Dimension(100, 78));
            sur.add(scrollSalida, BorderLayout.CENTER);
            sur.add(lblInfo, BorderLayout.SOUTH);
            add(sur, BorderLayout.SOUTH);
        }

        private JButton boton(String texto, Recorrido r, Color color) {
            JButton b = EstiloUI.boton(texto, color);
            b.addActionListener(e -> recorrer(r, true));
            return b;
        }

        void mostrar(ArbolBinario<T> arbol, Function<T, String> etiqueta, boolean mostrarFE) {
            detener();
            this.arbol = arbol;
            this.etiqueta = etiqueta;
            dibujo.setArbol(arbol, etiqueta, mostrarFE);
            salida.setText("");
            actualizarInfo();
        }

        void setInfoExtra(String extra) {
            infoExtra = extra;
            actualizarInfo();
        }

        void resaltar(Set<NodoArbol<T>> visitados, NodoArbol<T> actual) {
            detener();
            dibujo.resaltar(visitados, actual);
        }

        private void actualizarInfo() {
            lblInfo.setText("Nodos: " + arbol.tamano() + "   |   Altura: " + arbol.altura() + "   |   Hojas: " + arbol.hojas()
                    + (infoExtra.isEmpty() ? "" : "   |   " + infoExtra));
        }

        private void detener() {
            if (timer != null) timer.stop();
        }

        void recorrer(Recorrido tipo, boolean animado) {
            detener();
            String nombre;
            switch (tipo) {
                case PREORDEN:  nombre = "Preorden";   break;
                case INORDEN:   nombre = "Inorden";    break;
                case POSTORDEN: nombre = "Postorden";  break;
                default:        nombre = "Por niveles"; break;
            }
            List<NodoArbol<T>> nodos = arbol.recorrer(tipo);
            if (nodos.isEmpty()) {
                salida.setText("Árbol vacío.");
                return;
            }
            if (!animado) {
                dibujo.limpiarResaltado();
                salida.setText(nombre + ": " + unir(nodos, nodos.size()));
                return;
            }
            Set<NodoArbol<T>> visitados = new HashSet<>();
            int[] i = {0};
            timer = new Timer(550, null);
            timer.addActionListener(e -> {
                if (i[0] >= nodos.size()) {
                    timer.stop();
                    dibujo.resaltar(visitados, null);
                    return;
                }
                NodoArbol<T> n = nodos.get(i[0]++);
                dibujo.resaltar(visitados, n);
                visitados.add(n);
                salida.setText(nombre + ": " + unir(nodos, i[0]));
            });
            dibujo.limpiarResaltado();
            timer.setInitialDelay(0);
            timer.start();
        }

        private String unir(List<NodoArbol<T>> nodos, int hasta) {
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < hasta; k++) {
                if (k > 0) sb.append(" → ");
                sb.append(etiqueta.apply(nodos.get(k).dato));
            }
            return sb.toString();
        }
    }
}
