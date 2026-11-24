# Guía de las Unidades 3 y 4 (dónde está cada parte del código)

Todo el código de estas unidades está comentado con banners que empiezan con **UNIDAD 3** o **UNIDAD 4**.
En NetBeans: `Ctrl + Shift + F` (buscar en proyectos) y escribir el texto de la columna "Buscar".

## Unidad 3 — Pilas y Colas (4 puntos)

| Punto | Qué hace | Dónde está | Buscar |
|---|---|---|---|
| Pila LIFO (2 pts) | Historial para **deshacer / rehacer** agregar, modificar, eliminar, prestar y devolver libros. | `estructuras/Pila.java` (pila con arreglo que crece sola) · `modelo/AccionCatalogo.java` (lo que se apila) · `vista/FrmPrincipal.java` (métodos `registrarAccionPila`, `deshacer`, `rehacer`, `aplicarAccion`, pestaña **Pila: Deshacer (U3)** y botón **↶ Deshacer** del catálogo) | `UNIDAD 3 · PILA` |
| Cola FIFO (2 pts) | Turnos de atención en el mostrador: *Generar turno* = encolar, *Atender siguiente* = desencolar. | `estructuras/Cola.java` (cola circular) · `modelo/Turno.java` · `vista/PanelColaAtencion.java` (pestaña **Cola de Atención (U3)**) | `UNIDAD 3 · COLA` |
| Cola con prioridad | Prioritario / Preferente / Normal; mismo nivel = FIFO. | `estructuras/ColaPrioridad.java` (una cola FIFO por nivel) | `COLA CON PRIORIDAD` |

## Unidad 4 — Árboles (6 puntos)

Todo se ve en la pestaña **Árboles (U4)** (`vista/PanelArboles.java`), con tres sub-pestañas.

| Punto | Qué hace | Dónde está | Buscar |
|---|---|---|---|
| Árbol binario y recorridos (2 pts) | Jerarquía Biblioteca → Categorías → Libros. Botones Preorden, Inorden, Postorden y Por niveles, animados. | `estructuras/ArbolBinario.java` (los 4 recorridos) · `estructuras/NodoArbol.java` · `PanelArboles.construirJerarquia` | `UNIDAD 4 · ÁRBOL BINARIO` |
| ABB (2 pts) | Índice del catálogo por código o por título: buscar (muestra el camino y las comparaciones), insertar, quitar del índice (3 casos) y recorrido inorden = ordenado. | `estructuras/ABB.java` · `PanelArboles` (sub-pestaña ABB) | `UNIDAD 4 · ABB` |
| AVL (2 pts) | Insertar y eliminar con balanceo automático. Muestra el factor de equilibrio de cada nodo y una **bitácora** con cada rotación (LL, RR, LR, RL). Compara la altura con un ABB sin balancear. | `estructuras/ArbolAVL.java` (`rotarDerecha`, `rotarIzquierda`, `equilibrar`) · `PanelArboles` (sub-pestaña AVL) | `UNIDAD 4 · ÁRBOL AVL` |

## Cómo mostrarlo en la exposición

- **Pila:** en *Catálogo* agregue un libro, modifíquelo y elimínelo; vaya a *Pila: Deshacer (U3)* y vea las acciones apiladas (tope arriba). Pulse **Deshacer** varias veces: se revierten en orden inverso.
- **Cola:** en *Cola de Atención (U3)* pulse **Cargar ejemplo** y luego **Atender siguiente**: salen primero los prioritarios aunque hayan llegado después; los del mismo nivel salen por orden de llegada.
- **Recorridos:** en *Árboles → Árbol binario* pulse Preorden / Inorden / Postorden y observe el recorrido animado.
- **ABB:** en *Árboles → ABB* busque un código y observe el camino y el número de comparaciones; el inorden queda ordenado.
- **AVL:** en *Árboles → AVL* pulse **Ejemplo (10…50, 25)** y lea la bitácora; luego pruebe **Cargar códigos del catálogo** y compare la altura con la del ABB sin balancear. Pruebe también **Eliminar**.

## Pruebas sin base de datos

`estructuras.PruebaEstructuras` (clic derecho > Run File) incluye pruebas de la pila, la cola, la cola con prioridad, los recorridos, el ABB y el AVL (inserción y borrado con rotaciones, 1000 inserciones ascendentes).
