package estructuras;

public class Pila<T> {
    private Object[] datos;
    private int tope; // índice del próximo espacio libre

    public Pila() {
        datos = new Object[10];
        tope = 0;
    }

    private void redimensionar() {
        Object[] nuevo = new Object[datos.length * 2];
        System.arraycopy(datos, 0, nuevo, 0, datos.length);
        datos = nuevo;
    }

    public void apilar(T elemento) {
        if (tope == datos.length) redimensionar();
        datos[tope++] = elemento;
    }

    @SuppressWarnings("unchecked")
    public T desapilar() {
        if (estaVacia()) return null;
        T elemento = (T) datos[--tope];
        datos[tope] = null;
        return elemento;
    }

    @SuppressWarnings("unchecked")
    public T verTope() {
        if (estaVacia()) return null;
        return (T) datos[tope - 1];
    }

    public boolean estaVacia() { return tope == 0; }
    public int getTamanio() { return tope; }

    /** Devuelve los elementos del tope hacia la base, para mostrar el historial. */
    @SuppressWarnings("unchecked")
    public java.util.List<T> aListaDesdeElTope() {
        java.util.List<T> resultado = new java.util.ArrayList<>();
        for (int i = tope - 1; i >= 0; i--) {
            resultado.add((T) datos[i]);
        }
        return resultado;
    }
}
