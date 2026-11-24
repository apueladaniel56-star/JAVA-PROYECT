package unidad1;

public class Cola<T> {
    private Object[] datos;
    private int frente;
    private int fin;
    private int cantidad;

    public Cola() {
        datos = new Object[10];
        frente = 0;
        fin = -1;
        cantidad = 0;
    }

    private void redimensionar() {
        Object[] nuevo = new Object[datos.length * 2];
        for (int i = 0; i < cantidad; i++) {
            nuevo[i] = datos[(frente + i) % datos.length];
        }
        datos = nuevo;
        frente = 0;
        fin = cantidad - 1;
    }

    public void encolar(T elemento) {
        if (cantidad == datos.length) redimensionar();
        fin = (fin + 1) % datos.length;
        datos[fin] = elemento;
        cantidad++;
    }

    @SuppressWarnings("unchecked")
    public T desencolar() {
        if (estaVacia()) return null;
        T elemento = (T) datos[frente];
        datos[frente] = null;
        frente = (frente + 1) % datos.length;
        cantidad--;
        return elemento;
    }

    public boolean estaVacia() { return cantidad == 0; }
    public int getTamanio() { return cantidad; }

    @SuppressWarnings("unchecked")
    public java.util.List<T> aLista() {
        java.util.List<T> resultado = new java.util.ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            resultado.add((T) datos[(frente + i) % datos.length]);
        }
        return resultado;
    }
}
