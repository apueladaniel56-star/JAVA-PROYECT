package estructuras;
import modelo.Libro;

public class ArbolBinarioBusqueda {

    private class Nodo {
        Libro libro;
        Nodo izquierda, derecha;
        Nodo(Libro libro) { this.libro = libro; }
    }

    private Nodo raiz;

    public void insertar(Libro libro) {
        raiz = insertarRec(raiz, libro);
    }

    private Nodo insertarRec(Nodo actual, Libro libro) {
        if (actual == null) return new Nodo(libro);
        if (libro.getCodigo() < actual.libro.getCodigo()) {
            actual.izquierda = insertarRec(actual.izquierda, libro);
        } else if (libro.getCodigo() > actual.libro.getCodigo()) {
            actual.derecha = insertarRec(actual.derecha, libro);
        }
        // si el código ya existe, no se duplica
        return actual;
    }

    public Libro buscarPorCodigo(int codigo) {
        Nodo actual = raiz;
        while (actual != null) {
            if (codigo == actual.libro.getCodigo()) return actual.libro;
            actual = (codigo < actual.libro.getCodigo()) ? actual.izquierda : actual.derecha;
        }
        return null;
    }

    /** Recorrido en orden (In-Order): devuelve los libros ordenados por código ascendente. */
    public java.util.List<Libro> enOrden() {
        java.util.List<Libro> resultado = new java.util.ArrayList<>();
        enOrdenRec(raiz, resultado);
        return resultado;
    }

    private void enOrdenRec(Nodo nodo, java.util.List<Libro> resultado) {
        if (nodo == null) return;
        enOrdenRec(nodo.izquierda, resultado);
        resultado.add(nodo.libro);
        enOrdenRec(nodo.derecha, resultado);
    }

    public boolean eliminar(int codigo) {
        int tamanioAntes = enOrden().size();
        raiz = eliminarRec(raiz, codigo);
        return enOrden().size() < tamanioAntes;
    }

    private Nodo eliminarRec(Nodo actual, int codigo) {
        if (actual == null) return null;
        if (codigo < actual.libro.getCodigo()) {
            actual.izquierda = eliminarRec(actual.izquierda, codigo);
        } else if (codigo > actual.libro.getCodigo()) {
            actual.derecha = eliminarRec(actual.derecha, codigo);
        } else {
            if (actual.izquierda == null) return actual.derecha;
            if (actual.derecha == null) return actual.izquierda;
            Nodo sucesor = actual.derecha;
            while (sucesor.izquierda != null) sucesor = sucesor.izquierda;
            actual.libro = sucesor.libro;
            actual.derecha = eliminarRec(actual.derecha, sucesor.libro.getCodigo());
        }
        return actual;
    }
}
