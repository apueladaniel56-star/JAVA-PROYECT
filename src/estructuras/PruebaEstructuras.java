package estructuras;

import java.util.Comparator;

/**
 * Pruebas por consola de las listas (no necesita base de datos).
 * Ejecutar: clic derecho > Run File, o java estructuras.PruebaEstructuras
 */
public class PruebaEstructuras {

    private static int fallos = 0;

    private static void verificar(String nombre, boolean condicion) {
        System.out.println((condicion ? "OK     " : "FALLO  ") + nombre);
        if (!condicion) {
            fallos++;
        }
    }

    public static void main(String[] args) {
        pruebaListaSimple();
        pruebaListaDobleCircular();
        pruebaPilaLIFO();          // UNIDAD 3
        pruebaColaFIFOyPrioridad(); // UNIDAD 3
        pruebaRecorridosYABB();    // UNIDAD 4
        pruebaAVL();               // UNIDAD 4
        System.out.println(fallos == 0 ? "\nTodas las pruebas pasaron." : "\nPruebas fallidas: " + fallos);
    }

    private static void pruebaListaSimple() {
        System.out.println("--- Lista enlazada simple ---");
        ListaEnlazadaSimple<Integer> l = new ListaEnlazadaSimple<>();
        verificar("nueva lista esta vacia", l.estaVacia() && l.tamano() == 0);

        l.insertarFinal(30);
        l.insertarFinal(10);
        l.insertarInicio(50);
        l.insertarEn(2, 20);
        verificar("insertar inicio/final/posicion -> [50, 30, 20, 10]", l.toString().equals("[50 -> 30 -> 20 -> 10]"));
        verificar("insertarEn posicion invalida devuelve false", !l.insertarEn(9, 1) && l.tamano() == 4);

        verificar("buscar existente", l.buscar(x -> x == 20) != null);
        verificar("buscar inexistente devuelve null", l.buscar(x -> x == 99) == null);
        verificar("indiceDe", l.indiceDe(x -> x == 10) == 3 && l.indiceDe(x -> x == 99) == -1);

        l.ordenar(Comparator.naturalOrder());
        verificar("ordenar ascendente", l.toString().equals("[10 -> 20 -> 30 -> 50]"));
        l.insertarFinal(5);
        l.ordenar(Comparator.reverseOrder());
        verificar("ordenar descendente", l.toString().equals("[50 -> 30 -> 20 -> 10 -> 5]"));
        l.ordenar(Comparator.naturalOrder());
        l.insertarFinal(60);
        verificar("la cola se recalcula tras ordenar", l.toString().endsWith("-> 50 -> 60]"));

        l.insertarOrdenado(25, Comparator.naturalOrder());
        l.insertarOrdenado(1, Comparator.naturalOrder());
        l.insertarOrdenado(100, Comparator.naturalOrder());
        verificar("insertarOrdenado mantiene el orden", l.toString().equals("[1 -> 5 -> 10 -> 20 -> 25 -> 30 -> 50 -> 60 -> 100]"));

        verificar("eliminar la cabeza", l.eliminar(x -> x == 1) && l.obtener(0) == 5);
        verificar("eliminar la cola", l.eliminar(x -> x == 100) && l.obtener(l.tamano() - 1) == 60);
        l.insertarFinal(70);
        verificar("insertarFinal tras eliminar la cola", l.obtener(l.tamano() - 1) == 70);
        verificar("eliminar un nodo intermedio", l.eliminar(x -> x == 25) && l.indiceDe(x -> x == 25) == -1);
        verificar("eliminar inexistente devuelve false", !l.eliminar(x -> x == 999));
        verificar("eliminarEn(0)", l.eliminarEn(0) == 5);
        verificar("eliminarEn posicion invalida", l.eliminarEn(50) == null);

        ListaEnlazadaSimple<Integer> pares = l.filtrar(x -> x % 20 == 0);
        verificar("filtrar crea una lista nueva", pares.toString().equals("[20 -> 60]") && l.tamano() == 6);

        int suma = 0;
        for (int x : l) {
            suma += x;
        }
        verificar("iterador recorre todos los nodos", suma == 10 + 20 + 30 + 50 + 60 + 70);

        while (!l.estaVacia()) {
            l.eliminarEn(0);
        }
        l.insertarFinal(1);
        verificar("lista vaciada y reutilizable", l.tamano() == 1 && l.obtener(0) == 1);
    }

    private static void pruebaListaDobleCircular() {
        System.out.println("--- Lista doble circular ---");
        ListaDobleCircular<String> c = new ListaDobleCircular<>();
        verificar("vacia: siguiente() devuelve null", c.siguiente() == null && c.estaVacia());

        c.insertarFinal("B");
        c.insertarFinal("C");
        c.insertarInicio("A");
        verificar("el primero insertado queda como actual", c.actual().equals("B"));
        c.irAlInicio();
        verificar("insertarInicio deja A como cabeza", c.actual().equals("A") && c.posicionActual() == 1);

        verificar("siguiente avanza", c.siguiente().equals("B"));
        c.siguiente();
        verificar("desde el ultimo, siguiente vuelve al primero", c.siguiente().equals("A"));
        verificar("desde el primero, anterior salta al ultimo", c.anterior().equals("C"));
        verificar("verAnterior/verSiguiente no mueven el cursor",
                c.verAnterior().equals("B") && c.verSiguiente().equals("A") && c.actual().equals("C"));

        verificar("irA encuentra un elemento", "B".equals(c.irA(s -> s.equals("B"))) && c.posicionActual() == 2);
        verificar("irA inexistente devuelve null y no mueve el cursor", c.irA(s -> s.equals("Z")) == null && c.actual().equals("B"));

        verificar("eliminarActual pasa al siguiente", c.eliminarActual().equals("B") && c.actual().equals("C") && c.tamano() == 2);
        verificar("circularidad tras eliminar", c.siguiente().equals("A") && c.siguiente().equals("C") && c.anterior().equals("A"));

        c.insertarFinal("D");
        c.irAlInicio();
        verificar("eliminar por criterio conserva el cursor", c.eliminar(s -> s.equals("D")) && c.actual().equals("A") && c.tamano() == 2);
        verificar("eliminar la cabeza actualiza la cabeza", c.eliminar(s -> s.equals("A")) && c.irAlInicio().equals("C"));
        c.eliminarActual();
        verificar("al eliminar el ultimo nodo la lista queda vacia", c.estaVacia() && c.actual() == null);
    }

    // ═══════════════ UNIDAD 3 · PILA (LIFO) ═══════════════
    private static void pruebaPilaLIFO() {
        System.out.println("--- UNIDAD 3: Pila (LIFO) ---");
        Pila<String> p = new Pila<>();
        verificar("pila nueva está vacía", p.estaVacia() && p.desapilar() == null);
        for (int i = 1; i <= 25; i++) p.apilar("accion" + i);   // más de 10: obliga a redimensionar
        verificar("apilar 25 elementos (crece sola)", p.getTamanio() == 25 && p.verTope().equals("accion25"));
        verificar("desapilar devuelve el ÚLTIMO apilado (LIFO)", p.desapilar().equals("accion25") && p.desapilar().equals("accion24"));
        verificar("verTope no saca el elemento", p.verTope().equals("accion23") && p.getTamanio() == 23);
        verificar("aListaDesdeElTope empieza por el tope", p.aListaDesdeElTope().get(0).equals("accion23"));
    }

    // ═══════════════ UNIDAD 3 · COLA (FIFO) Y COLA CON PRIORIDAD ═══════════════
    private static void pruebaColaFIFOyPrioridad() {
        System.out.println("--- UNIDAD 3: Cola (FIFO) y cola con prioridad ---");
        Cola<Integer> c = new Cola<>();
        for (int i = 1; i <= 15; i++) c.encolar(i);              // obliga a redimensionar
        verificar("desencolar devuelve el PRIMERO que entró (FIFO)", c.desencolar() == 1 && c.desencolar() == 2 && c.verFrente() == 3);
        verificar("tamaño tras 2 desencolar", c.getTamanio() == 13);

        ColaPrioridad<String> cp = new ColaPrioridad<>(3);
        cp.encolar("normal-1", 2);
        cp.encolar("normal-2", 2);
        cp.encolar("preferente-1", 1);
        cp.encolar("urgente-1", 0);
        cp.encolar("urgente-2", 0);
        verificar("cola con prioridad: orden de atención",
                cp.aListaEnOrdenDeAtencion().toString().equals("[urgente-1, urgente-2, preferente-1, normal-1, normal-2]"));
        verificar("sale primero el más urgente", cp.desencolar().equals("urgente-1"));
        cp.encolar("urgente-3", 0);
        verificar("mismo nivel: respeta FIFO", cp.desencolar().equals("urgente-2") && cp.desencolar().equals("urgente-3"));
        verificar("luego preferente y luego normales en orden de llegada",
                cp.desencolar().equals("preferente-1") && cp.desencolar().equals("normal-1") && cp.desencolar().equals("normal-2"));
        verificar("cola con prioridad vacía", cp.estaVacia() && cp.desencolar() == null);
    }

    // ═══════════════ UNIDAD 4 · ÁRBOL BINARIO, RECORRIDOS Y ABB ═══════════════
    private static void pruebaRecorridosYABB() {
        System.out.println("--- UNIDAD 4: Recorridos y ABB ---");
        //            50
        //          /    \
        //        30      70
        //       /  \    /  \
        //     20   40  60   80
        ABB<Integer> abb = new ABB<>(Comparator.naturalOrder());
        for (int v : new int[]{50, 30, 70, 20, 40, 60, 80}) abb.insertar(v);
        verificar("preorden", abb.datos(ArbolBinario.Recorrido.PREORDEN).toString().equals("[50, 30, 20, 40, 70, 60, 80]"));
        verificar("inorden sale ORDENADO", abb.datos(ArbolBinario.Recorrido.INORDEN).toString().equals("[20, 30, 40, 50, 60, 70, 80]"));
        verificar("postorden", abb.datos(ArbolBinario.Recorrido.POSTORDEN).toString().equals("[20, 40, 30, 60, 80, 70, 50]"));
        verificar("por niveles", abb.datos(ArbolBinario.Recorrido.POR_NIVELES).toString().equals("[50, 30, 70, 20, 40, 60, 80]"));
        verificar("altura 3, 7 nodos, 4 hojas", abb.altura() == 3 && abb.tamano() == 7 && abb.hojas() == 4);
        verificar("insertar repetido no duplica", !abb.insertar(40) && abb.tamano() == 7);
        verificar("buscar existente / inexistente", abb.buscar(60) == 60 && abb.buscar(65) == null);
        verificar("camino de búsqueda de 60: 50 -> 70 -> 60", abb.caminoDeBusqueda(60).size() == 3);
        verificar("mínimo y máximo", abb.minimo() == 20 && abb.maximo() == 80);

        verificar("eliminar hoja (20)", abb.eliminar(20) && abb.datos(ArbolBinario.Recorrido.INORDEN).toString().equals("[30, 40, 50, 60, 70, 80]"));
        verificar("eliminar nodo con un hijo (30)", abb.eliminar(30) && abb.datos(ArbolBinario.Recorrido.INORDEN).toString().equals("[40, 50, 60, 70, 80]"));
        verificar("eliminar nodo con dos hijos (70)", abb.eliminar(70) && abb.datos(ArbolBinario.Recorrido.INORDEN).toString().equals("[40, 50, 60, 80]"));
        verificar("eliminar la raíz (50) y eliminar inexistente", abb.eliminar(50) && !abb.eliminar(999)
                && abb.datos(ArbolBinario.Recorrido.INORDEN).toString().equals("[40, 60, 80]"));
    }

    // ═══════════════ UNIDAD 4 · AVL (BALANCEO CON ROTACIONES) ═══════════════
    private static void pruebaAVL() {
        System.out.println("--- UNIDAD 4: AVL ---");
        // Insertar 10,20,30 en un ABB normal da una "lista"; en el AVL provoca una rotación simple a la izquierda (RR)
        ArbolAVL<Integer> avl = new ArbolAVL<>(Comparator.naturalOrder(), String::valueOf);
        avl.insertar(10); avl.insertar(20); avl.insertar(30);
        verificar("RR: 10,20,30 -> raíz 20 con 1 rotación", avl.getRaiz().dato == 20 && avl.getTotalRotaciones() == 1 && avl.estaBalanceado());

        ArbolAVL<Integer> ll = new ArbolAVL<>(Comparator.naturalOrder(), String::valueOf);
        ll.insertar(30); ll.insertar(20); ll.insertar(10);
        verificar("LL: 30,20,10 -> raíz 20 (rotación a la derecha)", ll.getRaiz().dato == 20 && ll.estaBalanceado());

        ArbolAVL<Integer> lr = new ArbolAVL<>(Comparator.naturalOrder(), String::valueOf);
        lr.insertar(30); lr.insertar(10); lr.insertar(20);
        verificar("LR: 30,10,20 -> raíz 20 (rotación doble = 2 giros)", lr.getRaiz().dato == 20 && lr.getTotalRotaciones() == 2 && lr.estaBalanceado());

        ArbolAVL<Integer> rl = new ArbolAVL<>(Comparator.naturalOrder(), String::valueOf);
        rl.insertar(10); rl.insertar(30); rl.insertar(20);
        verificar("RL: 10,30,20 -> raíz 20 (rotación doble = 2 giros)", rl.getRaiz().dato == 20 && rl.getTotalRotaciones() == 2 && rl.estaBalanceado());

        // 1000 códigos en orden ascendente: un ABB tendría altura 1000; el AVL debe quedar <= 1.44*log2(n)
        ArbolAVL<Integer> grande = new ArbolAVL<>(Comparator.naturalOrder(), String::valueOf);
        ABB<Integer> sinBalance = new ABB<>(Comparator.naturalOrder());
        for (int i = 1; i <= 1000; i++) { grande.insertar(i); sinBalance.insertar(i); }
        verificar("1000 inserciones ascendentes: AVL balanceado y de altura <= 14 (ABB: 1000)",
                grande.estaBalanceado() && grande.altura() <= 14 && sinBalance.altura() == 1000);
        verificar("el inorden del AVL sigue ordenado", grande.datos(ArbolBinario.Recorrido.INORDEN).get(0) == 1
                && grande.datos(ArbolBinario.Recorrido.INORDEN).get(999) == 1000 && grande.tamano() == 1000);

        // Borrado con rebalanceo: se borran los de un lado hasta forzar rotaciones
        int antes = grande.getTotalRotaciones();
        boolean todoOk = true;
        for (int i = 1; i <= 500; i++) {
            todoOk &= grande.eliminar(i);
            todoOk &= grande.estaBalanceado();
        }
        verificar("borrar 500 nodos manteniendo el balance (con rotaciones)", todoOk && grande.tamano() == 500 && grande.getTotalRotaciones() > antes);
        verificar("eliminar inexistente devuelve false", !grande.eliminar(5));

        ArbolAVL<Integer> demo = new ArbolAVL<>(Comparator.naturalOrder(), String::valueOf);
        for (int v : new int[]{10, 20, 30, 40, 50, 25}) demo.insertar(v);
        verificar("ejemplo 10,20,30,40,50,25 queda balanceado con raíz 30", demo.estaBalanceado() && demo.getRaiz().dato == 30);
        verificar("la bitácora deja evidencia de las rotaciones", demo.getBitacora().stream().anyMatch(l -> l.contains("ROTACIÓN")));
    }
}
