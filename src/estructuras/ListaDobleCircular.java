package estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Lista doblemente enlazada y circular con un cursor (elemento "actual").
 *
 * - Doble: cada nodo conoce a su anterior y a su siguiente, por lo que se
 *   puede avanzar y retroceder en O(1).
 * - Circular: el siguiente de la cola es la cabeza y el anterior de la
 *   cabeza es la cola, por lo que la navegacion nunca "se sale" de la lista.
 *
 * Se usa para navegar el catalogo de libros sin comprobar limites.
 *
 * @param <T> tipo de los elementos
 */
public class ListaDobleCircular<T> {

    private NodoDoble<T> cabeza;
    private NodoDoble<T> actual;
    private int tamano;

    public ListaDobleCircular() {
        vaciar();
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int tamano() {
        return tamano;
    }

    // ------------------------------------------------------------ INSERCION

    /** Inserta al final (entre la cola y la cabeza). Si es el primer nodo, queda como actual. */
    public void insertarFinal(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        if (cabeza == null) {
            nuevo.siguiente = nuevo;
            nuevo.anterior = nuevo;
            cabeza = nuevo;
            actual = nuevo;
        } else {
            NodoDoble<T> cola = cabeza.anterior;
            nuevo.anterior = cola;
            nuevo.siguiente = cabeza;
            cola.siguiente = nuevo;
            cabeza.anterior = nuevo;
        }
        tamano++;
    }

    /** Inserta al inicio: el nuevo nodo pasa a ser la cabeza. */
    public void insertarInicio(T dato) {
        insertarFinal(dato);
        cabeza = cabeza.anterior;
    }

    // ------------------------------------------------------------ NAVEGACION

    /** Mueve el cursor al siguiente (desde el ultimo salta al primero) y devuelve su dato. */
    public T siguiente() {
        if (actual == null) {
            return null;
        }
        actual = actual.siguiente;
        return actual.dato;
    }

    /** Mueve el cursor al anterior (desde el primero salta al ultimo) y devuelve su dato. */
    public T anterior() {
        if (actual == null) {
            return null;
        }
        actual = actual.anterior;
        return actual.dato;
    }

    public T irAlInicio() {
        actual = cabeza;
        return actual == null ? null : actual.dato;
    }

    public T irAlFinal() {
        actual = (cabeza == null) ? null : cabeza.anterior;
        return actual == null ? null : actual.dato;
    }

    /** Recorre como maximo una vuelta; si encuentra el criterio deja el cursor ahi y devuelve el dato. */
    public T irA(Predicate<? super T> criterio) {
        if (cabeza == null) {
            return null;
        }
        NodoDoble<T> nodo = cabeza;
        do {
            if (criterio.test(nodo.dato)) {
                actual = nodo;
                return nodo.dato;
            }
            nodo = nodo.siguiente;
        } while (nodo != cabeza);
        return null;
    }

    public T actual() {
        return actual == null ? null : actual.dato;
    }

    /** Dato que esta a la izquierda del cursor, sin moverlo. */
    public T verAnterior() {
        return actual == null ? null : actual.anterior.dato;
    }

    /** Dato que esta a la derecha del cursor, sin moverlo. */
    public T verSiguiente() {
        return actual == null ? null : actual.siguiente.dato;
    }

    /** Posicion del cursor contando desde 1 (0 si la lista esta vacia). */
    public int posicionActual() {
        if (actual == null) {
            return 0;
        }
        int pos = 1;
        NodoDoble<T> nodo = cabeza;
        while (nodo != actual) {
            nodo = nodo.siguiente;
            pos++;
        }
        return pos;
    }

    // ------------------------------------------------------------ ELIMINACION

    /** Elimina el nodo del cursor; el cursor pasa al siguiente. Devuelve el dato eliminado. */
    public T eliminarActual() {
        if (actual == null) {
            return null;
        }
        NodoDoble<T> borrar = actual;
        T dato = borrar.dato;
        if (tamano == 1) {
            vaciar();
            return dato;
        }
        borrar.anterior.siguiente = borrar.siguiente;
        borrar.siguiente.anterior = borrar.anterior;
        if (borrar == cabeza) {
            cabeza = borrar.siguiente;
        }
        actual = borrar.siguiente;
        borrar.siguiente = null;
        borrar.anterior = null;
        tamano--;
        return dato;
    }

    /** Elimina el primer elemento que cumple el criterio. El cursor se conserva si no era ese nodo. */
    public boolean eliminar(Predicate<? super T> criterio) {
        NodoDoble<T> cursorPrevio = actual;
        T encontrado = irA(criterio);
        if (encontrado == null) {
            return false;
        }
        boolean eraElCursor = (cursorPrevio == actual);
        eliminarActual();
        if (!eraElCursor && cursorPrevio != null && tamano > 0) {
            actual = cursorPrevio;
        }
        return true;
    }

    public void vaciar() {
        cabeza = null;
        actual = null;
        tamano = 0;
    }

    /** Recorre los elementos una sola vuelta, desde la cabeza. */
    public Iterator<T> recorrido() {
        return new Iterator<T>() {
            private NodoDoble<T> nodo = cabeza;
            private int restantes = tamano;

            @Override
            public boolean hasNext() {
                return restantes > 0;
            }

            @Override
            public T next() {
                if (restantes <= 0) {
                    throw new NoSuchElementException();
                }
                T dato = nodo.dato;
                nodo = nodo.siguiente;
                restantes--;
                return dato;
            }
        };
    }
}
