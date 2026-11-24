# Unidad 2: Estructuras de Datos Dinámicas - Listas

Proyecto: Sistema de Gestión de Biblioteca (CarlitosV3)

Este documento contiene la especificación de los TADs usados (informal y formal) y
la ubicación de cada requisito dentro del código.

---

## 1. Especificación de TADs (2 puntos)

### 1.1 TAD Lista (implementado con lista enlazada simple)

**Especificación informal**

- **Nombre:** Lista[T]
- **Descripción:** colección lineal de elementos del mismo tipo T, donde cada elemento ocupa una posición (0 a n-1). Se pueden agregar y quitar elementos en cualquier posición, buscar por un criterio y reordenar.
- **Uso en el sistema:** guarda los libros del catálogo (T = Libro). La tabla del catálogo se dibuja recorriendo esta lista.
- **Operaciones:**

| Operación | Entrada | Salida | Precondición | Postcondición |
|---|---|---|---|---|
| crear | - | Lista | - | lista vacía |
| estaVacia | Lista | Bool | - | true si no tiene elementos |
| tamano | Lista | Nat | - | cantidad de elementos |
| insertarInicio | Lista, T | Lista | - | el dato queda en la posición 0; tamaño + 1 |
| insertarFinal | Lista, T | Lista | - | el dato queda en la última posición; tamaño + 1 |
| insertarEn | Lista, Nat, T | Bool | 0 <= pos <= tamaño | el dato queda en pos; devuelve false sin cambios si pos es inválida |
| insertarOrdenado | Lista, T, Comparador | Lista | lista ordenada con ese comparador | el dato queda en su lugar y la lista sigue ordenada |
| buscar | Lista, Criterio | T o nulo | - | primer elemento que cumple el criterio, o nulo |
| indiceDe | Lista, Criterio | Int | - | posición del primer elemento que cumple, o -1 |
| eliminar | Lista, Criterio | Bool | - | quita el primer elemento que cumple; tamaño - 1 |
| eliminarEn | Lista, Nat | T o nulo | 0 <= pos < tamaño | quita y devuelve el elemento de pos |
| ordenar | Lista, Comparador | Lista | - | queda ordenada de menor a mayor (orden estable); mismo tamaño y mismos elementos |
| vaciar | Lista | Lista | - | lista vacía |

**Especificación formal (algebraica)**

```
TAD Lista[T]

Sorts:   Lista, T, Nat, Bool
Usa:     Criterio = T -> Bool        Comparador = T x T -> Int

Constructoras generadoras:
  crear          :                    -> Lista
  insertarInicio : Lista x T          -> Lista

Observadoras y modificadoras:
  esVacia        : Lista              -> Bool
  tamano         : Lista              -> Nat
  primero        : Lista              -> T          (parcial: solo si no es vacía)
  insertarFinal  : Lista x T          -> Lista
  insertarEn     : Lista x Nat x T    -> Lista      (parcial: Nat <= tamano)
  buscar         : Lista x Criterio   -> T u {nulo}
  eliminar       : Lista x Criterio   -> Lista
  ordenar        : Lista x Comparador -> Lista
  ordenada       : Lista x Comparador -> Bool

Axiomas (l: Lista; x, y: T; p: Criterio; c: Comparador; n: Nat):
  esVacia(crear)                       = verdadero
  esVacia(insertarInicio(l, x))        = falso

  tamano(crear)                        = 0
  tamano(insertarInicio(l, x))         = tamano(l) + 1

  primero(insertarInicio(l, x))        = x

  insertarFinal(crear, x)              = insertarInicio(crear, x)
  insertarFinal(insertarInicio(l, y), x) = insertarInicio(insertarFinal(l, x), y)

  insertarEn(l, 0, x)                  = insertarInicio(l, x)
  insertarEn(insertarInicio(l, y), n+1, x) = insertarInicio(insertarEn(l, n, x), y)

  buscar(crear, p)                     = nulo
  buscar(insertarInicio(l, x), p)      = si p(x) entonces x sino buscar(l, p)

  eliminar(crear, p)                   = crear
  eliminar(insertarInicio(l, x), p)    = si p(x) entonces l
                                         sino insertarInicio(eliminar(l, p), x)

  ordenada(crear, c)                   = verdadero
  ordenada(insertarInicio(l, x), c)    = (esVacia(l) o c(x, primero(l)) <= 0) y ordenada(l, c)

  ordenada(ordenar(l, c), c)           = verdadero
  tamano(ordenar(l, c))                = tamano(l)
```

### 1.2 TAD Lista Doble Circular con cursor

**Especificación informal**

- **Nombre:** ListaCircular[T]
- **Descripción:** secuencia de elementos donde, además del orden, existe un **cursor** que señala el elemento actual. El elemento que sigue al último es el primero y el que precede al primero es el último, por lo que el cursor puede avanzar o retroceder indefinidamente sin salirse de la lista.
- **Uso en el sistema:** pestaña "Navegador de Libros" (botones Anterior, Siguiente, Primero, Último, Ir al código).
- **Operaciones:**

| Operación | Salida | Precondición | Postcondición |
|---|---|---|---|
| crear | ListaCircular | - | vacía, sin cursor |
| insertarFinal(x) | ListaCircular | - | x queda antes de la cabeza (último); si era la primera inserción, el cursor queda en x |
| insertarInicio(x) | ListaCircular | - | x pasa a ser la cabeza |
| siguiente | T | no vacía | el cursor avanza una posición (del último pasa al primero) |
| anterior | T | no vacía | el cursor retrocede una posición (del primero pasa al último) |
| irAlInicio / irAlFinal | T | no vacía | el cursor queda en la cabeza / en la cola |
| irA(criterio) | T o nulo | - | el cursor queda en el primer elemento que cumple; si ninguno cumple, no se mueve |
| actual, verAnterior, verSiguiente | T | no vacía | consulta sin mover el cursor |
| posicionActual | Nat | - | posición del cursor desde 1 (0 si está vacía) |
| eliminarActual | T | no vacía | quita el elemento del cursor; el cursor pasa al siguiente |
| eliminar(criterio) | Bool | - | quita el primer elemento que cumple |

**Especificación formal (modelo)**

La lista se modela como un par `L = (s, k)`, con `s` una secuencia `<e1, ..., en>` y `k` el índice del cursor, `1 <= k <= n` cuando `n > 0`.

```
TAD ListaCircular[T]

Sorts:  ListaCircular, T, Nat

  crear        :                    -> ListaCircular
  insertarFinal: ListaCircular x T  -> ListaCircular
  siguiente    : ListaCircular      -> ListaCircular   (parcial: no vacía)
  anterior     : ListaCircular      -> ListaCircular   (parcial: no vacía)
  actual       : ListaCircular      -> T               (parcial: no vacía)
  tamano       : ListaCircular      -> Nat

Axiomas, para L = (s, k) con n = |s| > 0:
  tamano(insertarFinal(L, x))     = tamano(L) + 1
  actual(L)                       = s[k]
  siguiente(L)                    = (s, (k mod n) + 1)
  anterior(L)                     = (s, ((k - 2) mod n) + 1)

Propiedades (consecuencia de los axiomas):
  anterior(siguiente(L))          = L
  siguiente(anterior(L))          = L
  siguiente aplicada n veces a L  = L          (circularidad)
  si k = n:  actual(siguiente(L)) = s[1]       (del último al primero)
  si k = 1:  actual(anterior(L))  = s[n]       (del primero al último)
```

### 1.3 TAD auxiliar: Libro

Registro con: código (entero, único), título, autor, categoría, ubicación en estante (opcional), copias totales y copias disponibles. Es el tipo T con el que se instancian ambas listas.

---

## 2. Listas enlazadas simples (4 puntos)

**Archivos:** `src/estructuras/Nodo.java`, `TDALista.java`, `ListaEnlazadaSimple.java`

- **Nodo:** guarda `dato` y un puntero `siguiente`.
- **Lista:** punteros `cabeza` y `cola` (para insertar al final en O(1)) y un contador `tamano`.

| Operación | Método | Dónde se usa en la interfaz (pestaña Catálogo de Libros) |
|---|---|---|
| Inserción | `insertarInicio`, `insertarFinal`, `insertarEn`, `insertarOrdenado` | Botón "+ Añadir Libro": el libro se inserta en su lugar según el orden activo |
| Búsqueda | `buscar`, `indiceDe`, `filtrar` | Botón "Buscar" (filtra recorriendo la lista) y confirmación al eliminar |
| Eliminación | `eliminar`, `eliminarEn` | Botón "Eliminar Libro" |
| Ordenamiento | `ordenar` (merge sort sobre los nodos, O(n log n)) | Selector "Ordenar por" (Código, Título, Autor, Disponibles) y botón "Ordenar" |

Los libros se cargan desde MySQL a la lista (`sincronizarListaDesdeBD`) y la tabla de pantalla se dibuja recorriendo los nodos (`mostrarCatalogo`).

---

## 3. Listas complejas dobles o circulares (4 puntos)

**Archivos:** `src/estructuras/NodoDoble.java`, `ListaDobleCircular.java`

- **Nodo doble:** `dato`, `anterior` y `siguiente`.
- **Circular:** `cabeza.anterior` apunta a la cola y `cola.siguiente` apunta a la cabeza.
- **Módulo del sistema:** pestaña **Navegador de Libros**. Recorre el catálogo en ambas direcciones en O(1) por paso, sin comprobar límites: el último libro conecta con el primero. Muestra también los libros vecinos (anterior y siguiente) y permite saltar a un código.
- La lista circular se reconstruye con el mismo orden que la lista simple, por lo que al ordenar el catálogo cambia el orden de navegación.

---

## 4. Pruebas

`src/estructuras/PruebaEstructuras.java` verifica por consola todas las operaciones de ambas listas (sin base de datos). Ejecutar con clic derecho > Run File en NetBeans.

## 5. Ejecución

1. Importar `biblioteca_db` con `BdMySqlCarlitosV3.sql` en MySQL.
2. Agregar el conector JDBC de MySQL a Libraries.
3. Ejecutar `FrmPrincipal` (clic derecho > Run File).
