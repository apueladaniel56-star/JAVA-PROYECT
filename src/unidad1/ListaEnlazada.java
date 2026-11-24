package unidad1;

public class ListaEnlazada<T> {

    private class Nodo {
        T dato;
        Nodo siguiente;
        Nodo(T dato) { this.dato = dato; }
    }

    private Nodo cabeza;
    private int tamanio;

    public void agregar(T dato) {
        Nodo nuevo = new Nodo(dato);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Nodo actual = cabeza;
            while (actual.siguiente != null) actual = actual.siguiente;
            actual.siguiente = nuevo;
        }
        tamanio++;
    }

    /** Elimina el primer nodo cuyo dato cumpla la condición dada. */
    public boolean eliminarSi(java.util.function.Predicate<T> condicion) {
        Nodo actual = cabeza;
        Nodo anterior = null;
        while (actual != null) {
            if (condicion.test(actual.dato)) {
                if (anterior == null) {
                    cabeza = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                tamanio--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    public T buscarSi(java.util.function.Predicate<T> condicion) {
        Nodo actual = cabeza;
        while (actual != null) {
            if (condicion.test(actual.dato)) return actual.dato;
            actual = actual.siguiente;
        }
        return null;
    }

    public java.util.List<T> aLista() {
        java.util.List<T> resultado = new java.util.ArrayList<>();
        Nodo actual = cabeza;
        while (actual != null) {
            resultado.add(actual.dato);
            actual = actual.siguiente;
        }
        return resultado;
    }

    public int getTamanio() { return tamanio; }
    public boolean estaVacia() { return tamanio == 0; }
}
