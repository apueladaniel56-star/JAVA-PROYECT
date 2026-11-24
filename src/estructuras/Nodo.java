package estructuras;

/**
 * Nodo de una lista enlazada simple.
 * Guarda un dato y un puntero (referencia) al nodo siguiente.
 *
 * @param <T> tipo del dato almacenado
 */
public class Nodo<T> {

    T dato;
    Nodo<T> siguiente;

    public Nodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    public T getDato() {
        return dato;
    }

    public Nodo<T> getSiguiente() {
        return siguiente;
    }
}
