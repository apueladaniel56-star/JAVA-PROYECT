package estructuras;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * TAD Lista: contrato (especificacion informal) de una coleccion lineal
 * de elementos del mismo tipo, con acceso por posicion (0 .. tamano-1).
 *
 * La especificacion formal (sorts, operaciones y axiomas) esta en
 * docs/TAD_Lista.md. La implementacion concreta queda oculta tras esta
 * interfaz: quien usa la lista solo conoce las operaciones, no los nodos.
 *
 * @param <T> tipo de los elementos
 */
public interface TDALista<T> extends Iterable<T> {

    /** Pre: ninguna. Post: devuelve true si y solo si la lista no tiene elementos. */
    boolean estaVacia();

    /** Pre: ninguna. Post: devuelve la cantidad de elementos. */
    int tamano();

    /** Pre: ninguna. Post: dato queda como primer elemento; tamano aumenta en 1. */
    void insertarInicio(T dato);

    /** Pre: ninguna. Post: dato queda como ultimo elemento; tamano aumenta en 1. */
    void insertarFinal(T dato);

    /**
     * Pre: 0 <= posicion <= tamano.
     * Post: dato queda en la posicion indicada; devuelve false (sin cambios) si la posicion es invalida.
     */
    boolean insertarEn(int posicion, T dato);

    /**
     * Pre: ninguna.
     * Post: devuelve el primer elemento que cumple el criterio, o null si no existe.
     */
    T buscar(Predicate<? super T> criterio);

    /** Post: devuelve la posicion del primer elemento que cumple el criterio, o -1 si no existe. */
    int indiceDe(Predicate<? super T> criterio);

    /** Pre: 0 <= posicion < tamano. Post: devuelve el elemento de esa posicion o null si es invalida. */
    T obtener(int posicion);

    /**
     * Post: elimina el primer elemento que cumple el criterio y devuelve true;
     * devuelve false si ninguno lo cumple.
     */
    boolean eliminar(Predicate<? super T> criterio);

    /** Pre: 0 <= posicion < tamano. Post: elimina y devuelve el elemento, o null si la posicion es invalida. */
    T eliminarEn(int posicion);

    /** Post: la lista queda ordenada de menor a mayor segun el comparador (orden estable). */
    void ordenar(Comparator<? super T> comparador);

    /** Post: la lista queda vacia. */
    void vaciar();
}
