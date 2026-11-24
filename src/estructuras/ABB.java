package estructuras;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  UNIDAD 4 · ÁRBOL BINARIO DE BÚSQUEDA (ABB)
 * ═══════════════════════════════════════════════════════════════════════════
 * Regla del ABB: para cada nodo, TODO lo que está a su izquierda es menor y TODO lo que
 * está a su derecha es mayor (según el comparador). Con eso:
 *
 *   - buscar:    en cada nodo se descarta la mitad del árbol  -> O(log n) si está equilibrado
 *   - ordenar:   el recorrido INORDEN entrega los datos ya ordenados
 *   - insertar / eliminar: mantienen la regla (eliminar tiene 3 casos)
 *
 * El ABB NO se balancea solo: si se insertan datos ya ordenados se vuelve una "lista"
 * (O(n)). Ese problema lo resuelve {@link ArbolAVL}.
 */
public class ABB<T> extends ArbolBinario<T> {

    private final Comparator<? super T> comparador;

    public ABB(Comparator<? super T> comparador) {
        this.comparador = comparador;
    }

    // ---------------------------- INSERTAR ----------------------------

    /** Inserta el dato. Devuelve false si ya existe uno igual (no se duplica). */
    public boolean insertar(T dato) {
        if (raiz == null) {
            raiz = new NodoArbol<>(dato);
            return true;
        }
        NodoArbol<T> actual = raiz;
        while (true) {
            int c = comparador.compare(dato, actual.dato);
            if (c == 0) return false;
            if (c < 0) {
                if (actual.izq == null) { actual.izq = new NodoArbol<>(dato); return true; }
                actual = actual.izq;
            } else {
                if (actual.der == null) { actual.der = new NodoArbol<>(dato); return true; }
                actual = actual.der;
            }
        }
    }

    // ---------------------------- BUSCAR ----------------------------

    /** Busca por clave (un dato "molde" que solo necesita el campo que usa el comparador). */
    public T buscar(T clave) {
        List<NodoArbol<T>> camino = caminoDeBusqueda(clave);
        if (camino.isEmpty()) return null;
        NodoArbol<T> ultimo = camino.get(camino.size() - 1);
        return comparador.compare(clave, ultimo.dato) == 0 ? ultimo.dato : null;
    }

    /**
     * Nodos visitados al buscar la clave (de la raíz hasta donde termina la búsqueda).
     * Su tamaño = número de comparaciones; sirve de evidencia de la búsqueda O(log n).
     */
    public List<NodoArbol<T>> caminoDeBusqueda(T clave) {
        List<NodoArbol<T>> camino = new ArrayList<>();
        NodoArbol<T> actual = raiz;
        while (actual != null) {
            camino.add(actual);
            int c = comparador.compare(clave, actual.dato);
            if (c == 0) break;
            actual = (c < 0) ? actual.izq : actual.der;
        }
        return camino;
    }

    public T minimo() {
        if (raiz == null) return null;
        NodoArbol<T> n = raiz;
        while (n.izq != null) n = n.izq;
        return n.dato;
    }

    public T maximo() {
        if (raiz == null) return null;
        NodoArbol<T> n = raiz;
        while (n.der != null) n = n.der;
        return n.dato;
    }

    // ---------------------------- ELIMINAR ----------------------------

    private boolean eliminado;

    /** Elimina la clave del ABB. Devuelve false si no existía. */
    public boolean eliminar(T clave) {
        eliminado = false;
        raiz = eliminarRec(raiz, clave);
        return eliminado;
    }

    private NodoArbol<T> eliminarRec(NodoArbol<T> n, T clave) {
        if (n == null) return null;
        int c = comparador.compare(clave, n.dato);
        if (c < 0) {
            n.izq = eliminarRec(n.izq, clave);
        } else if (c > 0) {
            n.der = eliminarRec(n.der, clave);
        } else {
            eliminado = true;
            // CASO 1 y 2: sin hijo izquierdo o sin hijo derecho (incluye hojas) -> lo reemplaza el otro hijo
            if (n.izq == null) return n.der;
            if (n.der == null) return n.izq;
            // CASO 3: dos hijos -> se copia el SUCESOR (el menor del subárbol derecho) y se borra ese
            NodoArbol<T> sucesor = n.der;
            while (sucesor.izq != null) sucesor = sucesor.izq;
            n.dato = sucesor.dato;
            boolean marca = eliminado;
            n.der = eliminarRec(n.der, sucesor.dato);
            eliminado = marca;
        }
        return n;
    }
}
