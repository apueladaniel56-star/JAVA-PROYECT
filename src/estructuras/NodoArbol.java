package estructuras;

/**
 * UNIDAD 4 · ÁRBOLES — nodo de un árbol binario (hijo izquierdo, hijo derecho).
 * El campo "altura" lo mantiene el árbol AVL (altura de un nodo hoja = 1).
 */
public class NodoArbol<T> {
    public T dato;
    public NodoArbol<T> izq;
    public NodoArbol<T> der;
    public int altura = 1;

    public NodoArbol(T dato) {
        this.dato = dato;
    }
}
