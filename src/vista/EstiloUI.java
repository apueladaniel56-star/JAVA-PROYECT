package vista;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Estilo visual compartido por los paneles de las Unidades 3 y 4 (mismos colores y botones
 * redondeados que FrmPrincipal), para que todas las pestañas se vean iguales.
 */
final class EstiloUI {

    static final Color FONDO = new Color(242, 242, 247);
    static final Color TEXTO = new Color(28, 28, 30);
    static final Color TEXTO_SEC = new Color(142, 142, 147);
    static final Color AZUL = new Color(0, 122, 255);
    static final Color VERDE = new Color(52, 199, 89);
    static final Color NARANJA = new Color(255, 149, 0);
    static final Color ROJO = new Color(255, 59, 48);
    static final Color GRIS = new Color(110, 110, 115);
    static final Color BORDE = new Color(229, 229, 234);

    private EstiloUI() { }

    static JButton boton(String texto, Color fondo) {
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
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    static void tabla(JTable tabla) {
        tabla.setRowHeight(30);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setGridColor(BORDE);
        tabla.setSelectionBackground(new Color(230, 240, 255));
        tabla.setSelectionForeground(TEXTO);
        tabla.setShowVerticalLines(false);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(248, 248, 250));
        tabla.getTableHeader().setForeground(TEXTO_SEC);
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 32));
    }

    static JTextField campo(int columnas) {
        JTextField t = new JTextField(columnas);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true), new EmptyBorder(5, 8, 5, 8)));
        return t;
    }

    static JTextArea areaTexto(int filas) {
        JTextArea a = new JTextArea(filas, 40);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(new Font("Consolas", Font.PLAIN, 13));
        a.setBorder(new EmptyBorder(6, 8, 6, 8));
        return a;
    }

    static JLabel etiquetaInfo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXTO_SEC);
        return l;
    }
}
