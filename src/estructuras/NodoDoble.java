package estructuras;

/**
 * Nodo de una lista doblemente enlazada.
 * Guarda un dato y dos punteros: al nodo anterior y al nodo siguiente.
 *
 * @param <T> tipo del dato almacenado
 */
public class NodoDoble<T> {

    T dato;
    NodoDoble<T> anterior;
    NodoDoble<T> siguiente;

    public NodoDoble(T dato) {
        this.dato = dato;
        this.anterior = null;
        this.siguiente = null;
    }

    public T getDato() {
        return dato;
    }

    public NodoDoble<T> getAnterior() {
        return anterior;
    }

    public NodoDoble<T> getSiguiente() {
        return siguiente;
    }
}
