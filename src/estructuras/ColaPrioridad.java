package estructuras;

import java.util.ArrayList;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  UNIDAD 3 · COLA CON PRIORIDAD (FIFO dentro de cada nivel)
 * ═══════════════════════════════════════════════════════════════════════════
 * Se implementa con UNA cola FIFO (la clase {@link Cola}, arreglo circular) por cada
 * nivel de prioridad:
 *
 *   nivel 0 (más urgente)  ->  Cola FIFO
 *   nivel 1                ->  Cola FIFO
 *   nivel 2 (normal)       ->  Cola FIFO
 *
 *  - encolar(x, nivel):  x entra al FINAL de la cola de su nivel          (FIFO)
 *  - desencolar():       sale el FRENTE de la primera cola no vacía        (prioridad)
 *
 * Así, entre dos personas del mismo nivel gana la que llegó primero, y una persona de
 * un nivel más urgente siempre se atiende antes que las de niveles inferiores.
 */
public class ColaPrioridad<T> {

    private final Cola<T>[] niveles;

    @SuppressWarnings("unchecked")
    public ColaPrioridad(int cantidadNiveles) {
        niveles = (Cola<T>[]) new Cola[cantidadNiveles];
        for (int i = 0; i < cantidadNiveles; i++) {
            niveles[i] = new Cola<>();
        }
    }

    /** Encola al final de la cola del nivel indicado (0 = más prioritario). */
    public void encolar(T elemento, int nivel) {
        int n = Math.max(0, Math.min(nivel, niveles.length - 1));
        niveles[n].encolar(elemento);
    }

    /** Saca al siguiente a atender: el frente de la cola no vacía más prioritaria. */
    public T desencolar() {
        for (Cola<T> cola : niveles) {
            if (!cola.estaVacia()) {
                return cola.desencolar();
            }
        }
        return null;
    }

    /** Ve quién sigue sin sacarlo. */
    public T verFrente() {
        for (Cola<T> cola : niveles) {
            if (!cola.estaVacia()) {
                return cola.verFrente();
            }
        }
        return null;
    }

    public boolean estaVacia() {
        for (Cola<T> cola : niveles) {
            if (!cola.estaVacia()) return false;
        }
        return true;
    }

    public int getTamanio() {
        int total = 0;
        for (Cola<T> cola : niveles) total += cola.getTamanio();
        return total;
    }

    public int getTamanioNivel(int nivel) {
        return niveles[nivel].getTamanio();
    }

    /** Elementos en el orden exacto en que serán atendidos. */
    public List<T> aListaEnOrdenDeAtencion() {
        List<T> resultado = new ArrayList<>();
        for (Cola<T> cola : niveles) {
            resultado.addAll(cola.aLista());
        }
        return resultado;
    }
}
