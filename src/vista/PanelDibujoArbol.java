package vista;

import estructuras.ArbolBinario;
import estructuras.NodoArbol;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.swing.JPanel;


class PanelDibujoArbol<T> extends JPanel {

    private static final int DX = 98, DY = 66, ANCHO = 88, ALTO = 30, MARGEN = 28;

    private ArbolBinario<T> arbol;
    private Function<T, String> etiqueta = Object::toString;
    private boolean mostrarFE;
    private final Set<NodoArbol<T>> visitados = new HashSet<>();
    private NodoArbol<T> actual;

    private final Map<NodoArbol<T>, Point> posiciones = new IdentityHashMap<>();
    private int contador;

    PanelDibujoArbol() {
        setBackground(Color.WHITE);
    }

    void setArbol(ArbolBinario<T> arbol, Function<T, String> etiqueta, boolean mostrarFE) {
        this.arbol = arbol;
        this.etiqueta = etiqueta;
        this.mostrarFE = mostrarFE;
        limpiarResaltado();
    }

    void resaltar(Set<NodoArbol<T>> visitados, NodoArbol<T> actual) {
        this.visitados.clear();
        this.visitados.addAll(visitados);
        this.actual = actual;
        repaint();
    }

    void limpiarResaltado() {
        visitados.clear();
        actual = null;
        refrescar();
    }

    void refrescar() {
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (arbol == null || arbol.estaVacio()) return new Dimension(400, 200);
        return new Dimension(MARGEN * 2 + arbol.tamano() * DX, MARGEN * 2 + arbol.altura() * DY + 20);
    }

    private void calcularPosiciones() {
        posiciones.clear();
        contador = 0;
        asignar(arbol.getRaiz(), 0);
    }

    private void asignar(NodoArbol<T> n, int nivel) {
        if (n == null) return;
        asignar(n.izq, nivel + 1);
        posiciones.put(n, new Point(MARGEN + contador * DX + ANCHO / 2, MARGEN + nivel * DY + ALTO / 2));
        contador++;
        asignar(n.der, nivel + 1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (arbol == null || arbol.estaVacio()) {
            g2.setColor(EstiloUI.TEXTO_SEC);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            g2.drawString("Árbol vacío", 20, 30);
            g2.dispose();
            return;
        }
        calcularPosiciones();
        dibujarAristas(g2, arbol.getRaiz());
        dibujarNodos(g2, arbol.getRaiz());
        g2.dispose();
    }

    private void dibujarAristas(Graphics2D g2, NodoArbol<T> n) {
        if (n == null) return;
        Point p = posiciones.get(n);
        g2.setColor(new Color(170, 170, 178));
        g2.setStroke(new BasicStroke(1.6f));
        if (n.izq != null) {
            Point h = posiciones.get(n.izq);
            g2.drawLine(p.x, p.y + ALTO / 2, h.x, h.y - ALTO / 2);
        }
        if (n.der != null) {
            Point h = posiciones.get(n.der);
            g2.drawLine(p.x, p.y + ALTO / 2, h.x, h.y - ALTO / 2);
        }
        dibujarAristas(g2, n.izq);
        dibujarAristas(g2, n.der);
    }

    private void dibujarNodos(Graphics2D g2, NodoArbol<T> n) {
        if (n == null) return;
        Point p = posiciones.get(n);
        Color relleno = (n == actual) ? EstiloUI.AZUL : visitados.contains(n) ? EstiloUI.NARANJA : Color.WHITE;
        Color texto = (n == actual || visitados.contains(n)) ? Color.WHITE : EstiloUI.TEXTO;

        RoundRectangle2D caja = new RoundRectangle2D.Float(p.x - ANCHO / 2f, p.y - ALTO / 2f, ANCHO, ALTO, 14, 14);
        g2.setColor(relleno);
        g2.fill(caja);
        g2.setColor(EstiloUI.GRIS);
        g2.setStroke(new BasicStroke(1.4f));
        g2.draw(caja);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        String t = etiqueta.apply(n.dato);
        while (fm.stringWidth(t) > ANCHO - 8 && t.length() > 2) {
            t = t.substring(0, t.length() - 2) + "…";
        }
        g2.setColor(texto);
        g2.drawString(t, p.x - fm.stringWidth(t) / 2, p.y + fm.getAscent() / 2 - 1);

        if (mostrarFE) {
            int fe = ArbolBinario.alturaCalculada(n.izq) - ArbolBinario.alturaCalculada(n.der);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(Math.abs(fe) > 1 ? EstiloUI.ROJO : EstiloUI.TEXTO_SEC);
            String s = "FE=" + (fe > 0 ? "+" : "") + fe;
            g2.drawString(s, p.x - g2.getFontMetrics().stringWidth(s) / 2, p.y + ALTO / 2 + 12);
        }
        dibujarNodos(g2, n.izq);
        dibujarNodos(g2, n.der);
    }
}
