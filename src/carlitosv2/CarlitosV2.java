package carlitosv2;

import javax.swing.UIManager;
import vista.FrmPrincipal;

/**
 * Punto de entrada del Sistema de Gestión de Préstamos para Biblioteca.
 *
 * Una sola aplicación: las pestañas de la Unidad 2 (listas enlazadas simples y dobles circulares)
 * y la pestaña del primer avance (arreglo, ordenamiento, búsqueda, árbol, pila, cola, matriz y
 * lista de préstamos) comparten el modelo Libro y la misma base de datos MySQL.
 */
public class CarlitosV2 {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        java.awt.EventQueue.invokeLater(() -> new FrmPrincipal().setVisible(true));
    }
}
