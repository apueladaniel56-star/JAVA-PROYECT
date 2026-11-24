# Primera presentación incluida en el proyecto

El código de la primera presentación (Sistema de Gestión de Préstamos para Biblioteca) está en el paquete `unidad1` (`src/unidad1/`). Se agregó sin cambiar su lógica; solo se añadió la línea `package unidad1;` y se ajustó `Conexion.java` (ver Notas).

## Contenido

| Archivo | Estructura / función |
|---|---|
| `Libro.java` | Entidad del catálogo (con ubicación fila/columna en la estantería) |
| `BibliotecaApp.java` | Versión de consola: arreglo de objetos con CRUD (registrar, actualizar, eliminar) y menú de 15 opciones |
| `BibliotecaGUI.java` | Versión gráfica Swing de lo mismo (tablas, préstamos, historial, reservas, estantería) |
| `Ordenamiento.java` | Burbuja por título y QuickSort por código |
| `Busqueda.java` | Búsqueda secuencial por código y búsqueda binaria por título |
| `ArbolBinarioBusqueda.java` | Índice de códigos (inserción, búsqueda, eliminación) |
| `Pila.java`, `Operacion.java` | Pila con el historial de operaciones y opción de deshacer |
| `Cola.java` | Cola circular de reservas por libro |
| `ListaEnlazada.java`, `Prestamo.java` | Lista enlazada de préstamos activos |
| `EstanteMatriz.java` | Matriz bidimensional de estantería (6 x 8) |
| `Conexion.java`, `LibroDAO.java` | Persistencia en MySQL (JDBC) |

`docs/schema_primera_presentacion.sql` es el script original de esa entrega. La base de datos de la segunda entrega (`BdMySqlCarlitosV3.sql`) lo reemplaza: tiene las mismas tablas y columnas más `ubicacion_estante`, y la primera presentación funciona con ella.

## Relación con la Unidad 2

`unidad1.ListaEnlazada` es la lista simple básica de la primera entrega (agregar, buscar, eliminar de los préstamos). `estructuras.ListaEnlazadaSimple` la amplía con inserción en cualquier posición, inserción ordenada, ordenamiento y TAD formal, y es la que usa `FrmPrincipal`.

## Cómo ejecutar cada parte

- Primera presentación, interfaz gráfica: `unidad1.BibliotecaGUI`.
- Primera presentación, consola: `unidad1.BibliotecaApp`.
- Segunda presentación (Unidad 2): `FrmPrincipal`.

Todas usan la base `biblioteca_db` (script `BdMySqlCarlitosV3.sql`) y el conector JDBC de MySQL en Libraries.

## Notas

- `unidad1/Conexion.java` ahora usa usuario `root`, clave `root` y `allowPublicKeyRetrieval=true`, igual que `FrmPrincipal`, para que ambas partes se conecten a la misma base. Si tu clave es otra, cámbiala en las dos clases.
- Sin base de datos disponible, la primera presentación sigue funcionando en memoria con su catálogo de ejemplo.
