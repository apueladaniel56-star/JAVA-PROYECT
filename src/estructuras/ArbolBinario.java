package estructuras;

import java.util.ArrayList;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  UNIDAD 4 · ÁRBOL BINARIO Y RECORRIDOS (preorden, inorden, postorden)
 * ═══════════════════════════════════════════════════════════════════════════
 * Clase base de {@link ABB} y {@link ArbolAVL}. Guarda la raíz y ofrece los
 * recorridos fundamentales (todos recursivos salvo "por niveles", que usa una cola):
 *
 *   PREORDEN   : raíz  -> izquierdo -> derecho
 *   INORDEN    : izquierdo -> raíz  -> derecho   (en un ABB sale ORDENADO)
 *   POSTORDEN  : izquierdo -> derecho -> raíz
 *   POR NIVELES: de arriba hacia abajo, de izquierda a derecha (usa Cola)
 */
public class ArbolBinario<T> {

    public enum Recorrido { PREORDEN, INORDEN, POSTORDEN, POR_NIVELES }

    protected NodoArbol<T> raiz;

    public NodoArbol<T> getRaiz() { return raiz; }
    public void setRaiz(NodoArbol<T> raiz) { this.raiz = raiz; }
    public boolean estaVacio() { return raiz == null; }
    public void vaciar() { raiz = null; }

    // ---------------------------- RECORRIDOS ----------------------------

    /** Devuelve los NODOS en el orden del recorrido pedido (sirve para animarlos en pantalla). */
    public List<NodoArbol<T>> recorrer(Recorrido tipo) {
        List<NodoArbol<T>> resultado = new ArrayList<>();
        switch (tipo) {
            case PREORDEN:   preOrden(raiz, resultado);   break;
            case INORDEN:    inOrden(raiz, resultado);    break;
            case POSTORDEN:  postOrden(raiz, resultado);  break;
            default:         porNiveles(resultado);       break;
        }
        return resultado;
    }

    /** Igual que {@link #recorrer} pero devolviendo solo los datos. */
    public List<T> datos(Recorrido tipo) {
        List<T> resultado = new ArrayList<>();
        for (NodoArbol<T> n : recorrer(tipo)) resultado.add(n.dato);
        return resultado;
    }

    // PREORDEN: visita la raíz ANTES que sus hijos
    private void preOrden(NodoArbol<T> n, List<NodoArbol<T>> out) {
        if (n == null) return;
        out.add(n);
        preOrden(n.izq, out);
        preOrden(n.der, out);
    }

    // INORDEN: visita la raíz ENTRE sus hijos (en un ABB produce el orden ascendente)
    private void inOrden(NodoArbol<T> n, List<NodoArbol<T>> out) {
        if (n == null) return;
        inOrden(n.izq, out);
        out.add(n);
        inOrden(n.der, out);
    }

    // POSTORDEN: visita la raíz DESPUÉS de sus hijos
    private void postOrden(NodoArbol<T> n, List<NodoArbol<T>> out) {
        if (n == null) return;
        postOrden(n.izq, out);
        postOrden(n.der, out);
        out.add(n);
    }

    // POR NIVELES (anchura): usa una COLA FIFO de nodos pendientes
    private void porNiveles(List<NodoArbol<T>> out) {
        if (raiz == null) return;
        Cola<NodoArbol<T>> pendientes = new Cola<>();
        pendientes.encolar(raiz);
        while (!pendientes.estaVacia()) {
            NodoArbol<T> n = pendientes.desencolar();
            out.add(n);
            if (n.izq != null) pendientes.encolar(n.izq);
            if (n.der != null) pendientes.encolar(n.der);
        }
    }

    // ---------------------------- MEDIDAS ----------------------------

    /** Altura calculada recorriendo el árbol (0 si está vacío, 1 si solo hay raíz). */
    public static <T> int alturaCalculada(NodoArbol<T> n) {
        if (n == null) return 0;
        return 1 + Math.max(alturaCalculada(n.izq), alturaCalculada(n.der));
    }

    public int altura() { return alturaCalculada(raiz); }

    public int tamano() { return contar(raiz); }

    private int contar(NodoArbol<T> n) {
        return n == null ? 0 : 1 + contar(n.izq) + contar(n.der);
    }

    public int hojas() { return hojas(raiz); }

    private int hojas(NodoArbol<T> n) {
        if (n == null) return 0;
        if (n.izq == null && n.der == null) return 1;
        return hojas(n.izq) + hojas(n.der);
    }
}
