package estructuras;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  UNIDAD 4 · ÁRBOL AVL (ABB que se BALANCEA solo con rotaciones)
 * ═══════════════════════════════════════════════════════════════════════════
 * Un AVL es un ABB donde, en TODOS los nodos, la diferencia de alturas entre el subárbol
 * izquierdo y el derecho (factor de equilibrio, FE) es -1, 0 o +1.
 *
 *     FE(n) = altura(n.izq) - altura(n.der)
 *
 * Después de insertar o eliminar se recalcula la altura de cada nodo del camino (de
 * abajo hacia arriba) y, si algún FE llega a +2 o -2, se corrige con ROTACIONES:
 *
 *     FE = +2 y FE(izq) >= 0  -> caso IZQUIERDA-IZQUIERDA : rotación simple a la derecha
 *     FE = +2 y FE(izq) <  0  -> caso IZQUIERDA-DERECHA   : rotación doble (izq. y luego der.)
 *     FE = -2 y FE(der) <= 0  -> caso DERECHA-DERECHA     : rotación simple a la izquierda
 *     FE = -2 y FE(der) >  0  -> caso DERECHA-IZQUIERDA   : rotación doble (der. y luego izq.)
 *
 * Cada paso queda anotado en la BITÁCORA (evidencia del algoritmo de balanceo).
 */
public class ArbolAVL<T> extends ArbolBinario<T> {

    private final Comparator<? super T> comparador;
    private final Function<T, String> etiqueta;
    private final List<String> bitacora = new ArrayList<>();
    private int totalRotaciones = 0;
    private boolean huboCambio;

    public ArbolAVL(Comparator<? super T> comparador, Function<T, String> etiqueta) {
        this.comparador = comparador;
        this.etiqueta = etiqueta;
    }

    public List<String> getBitacora() { return bitacora; }
    public void limpiarBitacora() { bitacora.clear(); }
    public int getTotalRotaciones() { return totalRotaciones; }

    @Override
    public void vaciar() {
        super.vaciar();
        totalRotaciones = 0;
    }

    // ---------------------------- ALTURA Y FACTOR DE EQUILIBRIO ----------------------------

    private int altura(NodoArbol<T> n) { return n == null ? 0 : n.altura; }

    private void actualizarAltura(NodoArbol<T> n) {
        n.altura = 1 + Math.max(altura(n.izq), altura(n.der));
    }

    /** Factor de equilibrio: altura izquierda - altura derecha. */
    public int factorEquilibrio(NodoArbol<T> n) {
        return n == null ? 0 : altura(n.izq) - altura(n.der);
    }

    // ---------------------------- ROTACIONES ----------------------------

    /**
     * Rotación simple a la DERECHA sobre y:
     *
     *         y                x
     *        / \              / \
     *       x   C    ==>     A   y
     *      / \                  / \
     *     A   B                B   C
     */
    private NodoArbol<T> rotarDerecha(NodoArbol<T> y) {
        NodoArbol<T> x = y.izq;
        NodoArbol<T> b = x.der;
        x.der = y;
        y.izq = b;
        actualizarAltura(y);   // primero y (ahora es hijo), luego x
        actualizarAltura(x);
        totalRotaciones++;
        return x;              // x pasa a ser la nueva raíz de este subárbol
    }

    /**
     * Rotación simple a la IZQUIERDA sobre x:
     *
     *       x                    y
     *      / \                  / \
     *     A   y      ==>       x   C
     *        / \              / \
     *       B   C            A   B
     */
    private NodoArbol<T> rotarIzquierda(NodoArbol<T> x) {
        NodoArbol<T> y = x.der;
        NodoArbol<T> b = y.izq;
        y.izq = x;
        x.der = b;
        actualizarAltura(x);
        actualizarAltura(y);
        totalRotaciones++;
        return y;
    }

    // ---------------------------- BALANCEO ----------------------------

    /** Recalcula la altura de n y, si está desbalanceado, aplica la rotación que corresponda. */
    private NodoArbol<T> equilibrar(NodoArbol<T> n) {
        actualizarAltura(n);
        int fe = factorEquilibrio(n);
        String nombre = etiqueta.apply(n.dato);

        if (fe > 1) {                                   // pesado a la IZQUIERDA
            if (factorEquilibrio(n.izq) >= 0) {
                registrar("   ⚠ Nodo " + nombre + " con FE=+2 -> caso Izquierda-Izquierda -> ROTACIÓN SIMPLE A LA DERECHA sobre " + nombre);
                return rotarDerecha(n);
            }
            registrar("   ⚠ Nodo " + nombre + " con FE=+2 -> caso Izquierda-Derecha -> ROTACIÓN DOBLE: izquierda sobre "
                    + etiqueta.apply(n.izq.dato) + " y derecha sobre " + nombre);
            n.izq = rotarIzquierda(n.izq);
            return rotarDerecha(n);
        }
        if (fe < -1) {                                  // pesado a la DERECHA
            if (factorEquilibrio(n.der) <= 0) {
                registrar("   ⚠ Nodo " + nombre + " con FE=-2 -> caso Derecha-Derecha -> ROTACIÓN SIMPLE A LA IZQUIERDA sobre " + nombre);
                return rotarIzquierda(n);
            }
            registrar("   ⚠ Nodo " + nombre + " con FE=-2 -> caso Derecha-Izquierda -> ROTACIÓN DOBLE: derecha sobre "
                    + etiqueta.apply(n.der.dato) + " e izquierda sobre " + nombre);
            n.der = rotarDerecha(n.der);
            return rotarIzquierda(n);
        }
        return n;                                       // FE en {-1,0,1}: no hace falta rotar
    }

    // ---------------------------- INSERTAR ----------------------------

    /** Inserta y rebalancea. Devuelve false si el dato ya existía. */
    public boolean insertar(T dato) {
        huboCambio = false;
        registrar("➕ Insertar " + etiqueta.apply(dato));
        raiz = insertarRec(raiz, dato);
        if (!huboCambio) registrar("   (ya existía, no se inserta)");
        else if (!eventoConRotacion) registrar("   sin rotaciones: el árbol sigue balanceado");
        eventoConRotacion = false;
        return huboCambio;
    }

    private boolean eventoConRotacion;

    private NodoArbol<T> insertarRec(NodoArbol<T> n, T dato) {
        if (n == null) {
            huboCambio = true;
            return new NodoArbol<>(dato);
        }
        int c = comparador.compare(dato, n.dato);
        if (c < 0) {
            n.izq = insertarRec(n.izq, dato);
        } else if (c > 0) {
            n.der = insertarRec(n.der, dato);
        } else {
            return n;                                   // duplicado
        }
        if (!huboCambio) return n;
        int antes = totalRotaciones;
        NodoArbol<T> nuevaRaiz = equilibrar(n);         // al volver de la recursión se rebalancea
        if (totalRotaciones != antes) eventoConRotacion = true;
        return nuevaRaiz;
    }

    // ---------------------------- ELIMINAR ----------------------------

    /** Elimina y rebalancea. Devuelve false si el dato no existía. */
    public boolean eliminar(T clave) {
        huboCambio = false;
        registrar("➖ Eliminar " + etiqueta.apply(clave));
        raiz = eliminarRec(raiz, clave);
        if (!huboCambio) registrar("   (no existe en el árbol)");
        else if (!eventoConRotacion) registrar("   sin rotaciones: el árbol sigue balanceado");
        eventoConRotacion = false;
        return huboCambio;
    }

    private NodoArbol<T> eliminarRec(NodoArbol<T> n, T clave) {
        if (n == null) return null;
        int c = comparador.compare(clave, n.dato);
        if (c < 0) {
            n.izq = eliminarRec(n.izq, clave);
        } else if (c > 0) {
            n.der = eliminarRec(n.der, clave);
        } else {
            huboCambio = true;
            if (n.izq == null || n.der == null) {       // 0 o 1 hijo: lo reemplaza su hijo
                return (n.izq != null) ? n.izq : n.der;
            }
            NodoArbol<T> sucesor = n.der;               // 2 hijos: se usa el sucesor inorden
            while (sucesor.izq != null) sucesor = sucesor.izq;
            registrar("   nodo con dos hijos: se reemplaza por su sucesor " + etiqueta.apply(sucesor.dato));
            n.dato = sucesor.dato;
            n.der = eliminarRec(n.der, sucesor.dato);
        }
        if (!huboCambio) return n;
        int antes = totalRotaciones;
        NodoArbol<T> nuevaRaiz = equilibrar(n);         // también se rebalancea al ELIMINAR
        if (totalRotaciones != antes) eventoConRotacion = true;
        return nuevaRaiz;
    }

    // ---------------------------- VERIFICACIÓN ----------------------------

    /** Comprueba que TODOS los nodos cumplan |FE| <= 1 y que las alturas guardadas sean correctas. */
    public boolean estaBalanceado() {
        return verificar(raiz);
    }

    private boolean verificar(NodoArbol<T> n) {
        if (n == null) return true;
        if (Math.abs(factorEquilibrio(n)) > 1) return false;
        if (n.altura != alturaCalculada(n)) return false;
        return verificar(n.izq) && verificar(n.der);
    }

    public T buscar(T clave) {
        NodoArbol<T> n = raiz;
        while (n != null) {
            int c = comparador.compare(clave, n.dato);
            if (c == 0) return n.dato;
            n = (c < 0) ? n.izq : n.der;
        }
        return null;
    }

    private void registrar(String linea) {
        bitacora.add(linea);
    }
}
