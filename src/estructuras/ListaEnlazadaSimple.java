package estructuras;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Lista enlazada simple: cada nodo apunta solo al siguiente.
 * Mantiene punteros a la cabeza y a la cola para que insertar al final sea O(1).
 *
 * Costos: insertarInicio O(1), insertarFinal O(1), insertarEn O(n),
 * buscar O(n), eliminar O(n), ordenar O(n log n) (merge sort sobre nodos).
 *
 * @param <T> tipo de los elementos
 */
public class ListaEnlazadaSimple<T> implements TDALista<T> {

    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamano;

    public ListaEnlazadaSimple() {
        vaciar();
    }

    @Override
    public boolean estaVacia() {
        return cabeza == null;
    }

    @Override
    public int tamano() {
        return tamano;
    }

    // ------------------------------------------------------------ INSERCION

    @Override
    public void insertarInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = cabeza;
        cabeza = nuevo;
        if (cola == null) {
            cola = nuevo;
        }
        tamano++;
    }

    @Override
    public void insertarFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cola == null) {
            cabeza = nuevo;
        } else {
            cola.siguiente = nuevo;
        }
        cola = nuevo;
        tamano++;
    }

    @Override
    public boolean insertarEn(int posicion, T dato) {
        if (posicion < 0 || posicion > tamano) {
            return false;
        }
        if (posicion == 0) {
            insertarInicio(dato);
            return true;
        }
        if (posicion == tamano) {
            insertarFinal(dato);
            return true;
        }
        Nodo<T> previo = cabeza;
        for (int i = 0; i < posicion - 1; i++) {
            previo = previo.siguiente;
        }
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = previo.siguiente;
        previo.siguiente = nuevo;
        tamano++;
        return true;
    }

    /**
     * Inserta el dato en la posicion que le corresponde para que una lista ya
     * ordenada con el mismo comparador siga ordenada (inserta despues de los iguales).
     */
    public void insertarOrdenado(T dato, Comparator<? super T> comparador) {
        if (cabeza == null || comparador.compare(dato, cabeza.dato) < 0) {
            insertarInicio(dato);
            return;
        }
        Nodo<T> actual = cabeza;
        while (actual.siguiente != null && comparador.compare(actual.siguiente.dato, dato) <= 0) {
            actual = actual.siguiente;
        }
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = actual.siguiente;
        actual.siguiente = nuevo;
        if (nuevo.siguiente == null) {
            cola = nuevo;
        }
        tamano++;
    }

    // ------------------------------------------------------------- BUSQUEDA

    @Override
    public T buscar(Predicate<? super T> criterio) {
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            if (criterio.test(actual.dato)) {
                return actual.dato;
            }
        }
        return null;
    }

    @Override
    public int indiceDe(Predicate<? super T> criterio) {
        int i = 0;
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente, i++) {
            if (criterio.test(actual.dato)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public T obtener(int posicion) {
        if (posicion < 0 || posicion >= tamano) {
            return null;
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < posicion; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    /** Devuelve una lista nueva con todos los elementos que cumplen el criterio (mismo orden). */
    public ListaEnlazadaSimple<T> filtrar(Predicate<? super T> criterio) {
        ListaEnlazadaSimple<T> resultado = new ListaEnlazadaSimple<>();
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            if (criterio.test(actual.dato)) {
                resultado.insertarFinal(actual.dato);
            }
        }
        return resultado;
    }

    // ------------------------------------------------------------ ELIMINACION

    @Override
    public boolean eliminar(Predicate<? super T> criterio) {
        Nodo<T> previo = null;
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (criterio.test(actual.dato)) {
                desenlazar(previo, actual);
                return true;
            }
            previo = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    @Override
    public T eliminarEn(int posicion) {
        if (posicion < 0 || posicion >= tamano) {
            return null;
        }
        Nodo<T> previo = null;
        Nodo<T> actual = cabeza;
        for (int i = 0; i < posicion; i++) {
            previo = actual;
            actual = actual.siguiente;
        }
        desenlazar(previo, actual);
        return actual.dato;
    }

    /** Saca el nodo 'actual' de la cadena; 'previo' es su antecesor (null si es la cabeza). */
    private void desenlazar(Nodo<T> previo, Nodo<T> actual) {
        if (previo == null) {
            cabeza = actual.siguiente;
        } else {
            previo.siguiente = actual.siguiente;
        }
        if (actual == cola) {
            cola = previo;
        }
        actual.siguiente = null;
        tamano--;
    }

    // ------------------------------------------------------------ ORDENAMIENTO

    @Override
    public void ordenar(Comparator<? super T> comparador) {
        cabeza = mergeSort(cabeza, comparador);
        cola = cabeza;
        while (cola != null && cola.siguiente != null) {
            cola = cola.siguiente;
        }
    }

    private Nodo<T> mergeSort(Nodo<T> inicio, Comparator<? super T> cmp) {
        if (inicio == null || inicio.siguiente == null) {
            return inicio;
        }
        // Se parte la cadena por la mitad con dos punteros (lento y rapido).
        Nodo<T> lento = inicio;
        Nodo<T> rapido = inicio.siguiente;
        while (rapido != null && rapido.siguiente != null) {
            lento = lento.siguiente;
            rapido = rapido.siguiente.siguiente;
        }
        Nodo<T> segundaMitad = lento.siguiente;
        lento.siguiente = null;
        return mezclar(mergeSort(inicio, cmp), mergeSort(segundaMitad, cmp), cmp);
    }

    private Nodo<T> mezclar(Nodo<T> a, Nodo<T> b, Comparator<? super T> cmp) {
        Nodo<T> centinela = new Nodo<>(null);
        Nodo<T> fin = centinela;
        while (a != null && b != null) {
            if (cmp.compare(a.dato, b.dato) <= 0) {
                fin.siguiente = a;
                a = a.siguiente;
            } else {
                fin.siguiente = b;
                b = b.siguiente;
            }
            fin = fin.siguiente;
        }
        fin.siguiente = (a != null) ? a : b;
        return centinela.siguiente;
    }

    // ------------------------------------------------------------------ OTROS

    @Override
    public void vaciar() {
        cabeza = null;
        cola = null;
        tamano = 0;
    }

    /** Copia los elementos, en orden, a una java.util.List (para llenar tablas). */
    public java.util.List<T> aLista() {
        java.util.List<T> resultado = new java.util.ArrayList<>();
        for (T dato : this) resultado.add(dato);
        return resultado;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Nodo<T> actual = cabeza; actual != null; actual = actual.siguiente) {
            sb.append(actual.dato);
            if (actual.siguiente != null) {
                sb.append(" -> ");
            }
        }
        return sb.append("]").toString();
    }
}
