# Estructura del proyecto (Avance 1 + Unidad 2 en una sola aplicación)

La Unidad 2 es la continuación del primer avance: ambos comparten el mismo modelo `Libro`, la misma
conexión a MySQL y se abren desde una sola ventana (`FrmPrincipal`), cuya última pestaña,
**Préstamos (Avance 1)**, contiene el sistema de la primera entrega.

## Paquetes

| Paquete | Contenido |
|---|---|
| `carlitosv2` | `CarlitosV2`: punto de entrada (abre `FrmPrincipal`). |
| `modelo` | `Libro` (única clase, usada por ambas unidades), `Prestamo`, `Operacion`. |
| `estructuras` | **Avance 1:** `Pila`, `Cola` (circular), `ArbolBinarioBusqueda`, `EstanteMatriz`, `Ordenamiento` (burbuja y QuickSort), `Busqueda` (secuencial y binaria). **Unidad 2:** `TDALista`, `ListaEnlazadaSimple`, `ListaDobleCircular`, `Nodo`, `NodoDoble`, `PruebaEstructuras`. |
| `datos` | `Conexion` (JDBC, una sola configuración) y `LibroDAO`. |
| `vista` | `FrmPrincipal` (ventana principal), `PanelAvance1` (pestaña del primer avance), `BibliotecaApp` (versión de consola del primer avance). |

## Cómo continúa el Avance 1 en la Unidad 2

- La lista de préstamos activos del avance 1 ahora es `ListaEnlazadaSimple<Prestamo>` (TAD formal en `docs/TAD_Lista.md`): ya no existe una lista enlazada aparte.
- El catálogo de `FrmPrincipal` se mantiene en una `ListaEnlazadaSimple<Libro>` (búsqueda, inserción, eliminación y ordenamiento) y el navegador Anterior/Siguiente usa una `ListaDobleCircular<Libro>`.
- La ubicación en estantería se guarda en MySQL como texto (`A1`, `B3`...). La matriz del avance 1 (6 x 8) usa la misma ubicación: la letra es la fila y el número la columna, así que un libro ubicado en una pestaña aparece en la otra.
- Al cambiar de una pestaña a otra, la aplicación vuelve a leer MySQL para que ambas vean los mismos libros.

## Ejecución

- Aplicación completa: `carlitosv2.CarlitosV2` (clase principal del proyecto en NetBeans).
- Consola del avance 1: `vista.BibliotecaApp`.
- Pruebas de las listas: `estructuras.PruebaEstructuras`.

Base de datos `biblioteca_db` (script `BdMySqlCarlitosV3.sql`; el script original del avance 1 está en `docs/schema_primera_presentacion.sql`) y conector JDBC de MySQL agregado en Libraries. Usuario y clave se configuran en un solo lugar: `datos/Conexion.java` (por defecto root / root).

Sin base de datos, el avance 1 funciona en memoria con su catálogo de ejemplo.

## Unidades 3 y 4

Se agregaron las pestañas **Pila: Deshacer (U3)**, **Cola de Atención (U3)** y **Árboles (U4)**.
Nuevas clases: `ColaPrioridad`, `NodoArbol`, `ArbolBinario`, `ABB`, `ArbolAVL` (paquete `estructuras`),
`AccionCatalogo`, `Turno` (paquete `modelo`) y `PanelColaAtencion`, `PanelArboles`, `PanelDibujoArbol`, `EstiloUI` (paquete `vista`).
Detalle y dónde buscar cada parte del código: `docs/Guia_Unidades_3_y_4.md`.

