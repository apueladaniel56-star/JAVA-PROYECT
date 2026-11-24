package vista;

import datos.LibroDAO;
import estructuras.*;
import modelo.*;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class BibliotecaApp {

    private static Libro[] catalogo = new Libro[50];
    private static int totalLibros = 0;

    private static final ArbolBinarioBusqueda indiceCodigos = new ArbolBinarioBusqueda();
    private static final Pila<Operacion> historial = new Pila<>();
    private static final ListaEnlazadaSimple<Prestamo> prestamosActivos = new ListaEnlazadaSimple<>();
    private static final Map<Integer, Cola<String>> reservasPorLibro = new HashMap<>();

    // Matriz (estructura bidimensional): 6 estantes x 8 posiciones por estante.
    private static final EstanteMatriz estanteria = new EstanteMatriz(6, 8);

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        cargarDatosDemo();
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> registrarLibro();
                case 2 -> actualizarLibro();
                case 3 -> eliminarLibro();
                case 4 -> listarCatalogoOrdenado();
                case 5 -> buscarPorCodigo();
                case 6 -> buscarPorTitulo();
                case 7 -> prestarLibro();
                case 8 -> devolverLibro();
                case 9 -> deshacerUltimaOperacion();
                case 10 -> verHistorial();
                case 11 -> verPrestamosActivos();
                case 12 -> verReservas();
                case 13 -> asignarUbicacionEnEstante();
                case 14 -> System.out.println(estanteria.mostrarMapa());
                case 15 -> buscarEstanteDeLibro();
                case 0 -> System.out.println("Saliendo del sistema... Hasta pronto!");
                default -> System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println("\n===== SISTEMA DE BIBLIOTECA =====");
        System.out.println("1. Registrar nuevo libro");
        System.out.println("2. Actualizar datos de un libro");
        System.out.println("3. Eliminar libro del catalogo");
        System.out.println("4. Listar catalogo (ordenado por titulo)");
        System.out.println("5. Buscar libro por codigo (Arbol Binario)");
        System.out.println("6. Buscar libro por titulo (Busqueda Binaria)");
        System.out.println("7. Prestar libro");
        System.out.println("8. Devolver libro");
        System.out.println("9. Deshacer ultima operacion");
        System.out.println("10. Ver historial de operaciones");
        System.out.println("11. Ver prestamos activos");
        System.out.println("12. Ver reservas pendientes por libro");
        System.out.println("13. Asignar ubicacion en estante (Matriz)");
        System.out.println("14. Ver mapa de estanteria (Matriz)");
        System.out.println("15. Buscar estante de un libro (Matriz)");
        System.out.println("0. Salir");
    }

    // ---------------------- OPCIONES DEL MENÚ ----------------------

    private static void registrarLibro() {
        if (totalLibros == catalogo.length) {
            redimensionarCatalogo();
        }
        int codigo = leerEntero("Codigo del libro: ");
        if (Busqueda.secuencialPorCodigo(catalogo, totalLibros, codigo) != null) {
            System.out.println("Ya existe un libro con ese código.");
            return;
        }
        System.out.print("Titulo: ");
        String titulo = sc.nextLine();
        System.out.print("Autor: ");
        String autor = sc.nextLine();
        System.out.print("Categoria: ");
        String categoria = sc.nextLine();
        int copias = leerEntero("Numero de copias: ");

        Libro nuevo = new Libro(codigo, titulo, autor, categoria, copias);
        catalogo[totalLibros++] = nuevo;
        indiceCodigos.insertar(nuevo);
        reservasPorLibro.put(codigo, new Cola<>());
        LibroDAO.insertar(nuevo);
        System.out.println("Libro registrado correctamente:\n" + nuevo);
    }

    /** Actualización (CRUD) sobre el arreglo de objetos: modifica los datos de un libro existente. */
    private static void actualizarLibro() {
        int codigo = leerEntero("Codigo del libro a actualizar: ");
        Libro libro = indiceCodigos.buscarPorCodigo(codigo);
        if (libro == null) {
            System.out.println("No existe un libro con ese codigo.");
            return;
        }
        System.out.println("Deje en blanco (Enter) el campo que no desea modificar.");
        System.out.print("Nuevo titulo [" + libro.getTitulo() + "]: ");
        String titulo = sc.nextLine();
        if (!titulo.isBlank()) libro.setTitulo(titulo);

        System.out.print("Nuevo autor [" + libro.getAutor() + "]: ");
        String autor = sc.nextLine();
        if (!autor.isBlank()) libro.setAutor(autor);

        System.out.print("Nueva categoria [" + libro.getCategoria() + "]: ");
        String categoria = sc.nextLine();
        if (!categoria.isBlank()) libro.setCategoria(categoria);

        System.out.print("Nuevo total de copias [" + libro.getCopiasTotales() + "] (Enter para no cambiar): ");
        String copiasTexto = sc.nextLine();
        if (!copiasTexto.isBlank()) {
            libro.setCopiasTotales(Integer.parseInt(copiasTexto.trim()));
        }

        LibroDAO.actualizar(libro);
        System.out.println("Libro actualizado correctamente:\n" + libro);
    }

    /** Eliminación (CRUD) sobre el arreglo de objetos: retira un libro del catalogo por completo. */
    private static void eliminarLibro() {
        int codigo = leerEntero("Codigo del libro a eliminar: ");
        int posicion = -1;
        for (int i = 0; i < totalLibros; i++) {
            if (catalogo[i].getCodigo() == codigo) {
                posicion = i;
                break;
            }
        }
        if (posicion == -1) {
            System.out.println("No existe un libro con ese codigo.");
            return;
        }
        Libro libro = catalogo[posicion];
        // Compactar el arreglo desplazando los elementos posteriores una posicion a la izquierda.
        for (int i = posicion; i < totalLibros - 1; i++) {
            catalogo[i] = catalogo[i + 1];
        }
        catalogo[totalLibros - 1] = null;
        totalLibros--;

        indiceCodigos.eliminar(codigo);
        reservasPorLibro.remove(codigo);
        if (libro.tieneUbicacion()) {
            estanteria.liberar(libro.getFilaEstante(), libro.getColumnaEstante());
        }
        LibroDAO.eliminar(codigo);
        System.out.println("Libro eliminado correctamente:\n" + libro);
    }

    // ---------------------- OPCIONES DE LA MATRIZ (ESTANTERIA) ----------------------

    private static void asignarUbicacionEnEstante() {
        int codigo = leerEntero("Codigo del libro a ubicar: ");
        Libro libro = indiceCodigos.buscarPorCodigo(codigo);
        if (libro == null) {
            System.out.println("No existe un libro con ese codigo.");
            return;
        }
        int[] libre = estanteria.buscarPrimeraLibre();
        if (libre == null) {
            System.out.println("La estanteria esta completamente llena.");
            return;
        }
        if (libro.tieneUbicacion()) {
            estanteria.liberar(libro.getFilaEstante(), libro.getColumnaEstante());
        }
        estanteria.asignar(libre[0], libre[1], codigo);
        libro.asignarUbicacion(libre[0], libre[1]);
        System.out.printf("Libro [%03d] ubicado en Estante %d, Posicion %d.%n", codigo, libre[0], libre[1]);
    }

    private static void buscarEstanteDeLibro() {
        int codigo = leerEntero("Codigo del libro a ubicar en la estanteria: ");
        int[] ubicacion = estanteria.buscarPorCodigo(codigo);
        if (ubicacion == null) {
            System.out.println("Ese libro todavia no tiene una ubicacion asignada en la estanteria.");
            return;
        }
        System.out.printf("El libro [%03d] se encuentra en Estante %d, Posicion %d.%n", codigo, ubicacion[0], ubicacion[1]);
    }

    private static void listarCatalogoOrdenado() {
        if (totalLibros == 0) {
            System.out.println("El catalogo esta vacio.");
            return;
        }
        Libro[] copia = copiarCatalogo();
        Ordenamiento.burbujaPorTitulo(copia, totalLibros);
        System.out.println("\n--- Catalogo ordenado por titulo ---");
        for (int i = 0; i < totalLibros; i++) {
            System.out.println(copia[i]);
        }
    }

    private static void buscarPorCodigo() {
        int codigo = leerEntero("Ingrese el codigo a buscar: ");
        Libro encontrado = indiceCodigos.buscarPorCodigo(codigo);
        if (encontrado != null) {
            System.out.println("Encontrado (Arbol Binario de Busqueda):\n" + encontrado);
        } else {
            System.out.println("No existe un libro con ese codigo.");
        }
    }

    private static void buscarPorTitulo() {
        if (totalLibros == 0) {
            System.out.println("El catalogo está vacio.");
            return;
        }
        System.out.print("Ingrese el titulo exacto a buscar: ");
        String titulo = sc.nextLine();
        Libro[] copia = copiarCatalogo();
        Ordenamiento.burbujaPorTitulo(copia, totalLibros);
        Libro encontrado = Busqueda.binariaPorTitulo(copia, totalLibros, titulo);
        if (encontrado != null) {
            System.out.println("Encontrado (Busqueda Binaria):\n" + encontrado);
        } else {
            System.out.println("No se encontró un libro con ese titulo exacto.");
        }
    }

    private static void prestarLibro() {
        int codigo = leerEntero("Codigo del libro a prestar: ");
        Libro libro = indiceCodigos.buscarPorCodigo(codigo);
        if (libro == null) {
            System.out.println("No existe un libro con ese codigo.");
            return;
        }
        System.out.print("Nombre de la persona: ");
        String socio = sc.nextLine();
        String fecha = LocalDate.now().toString();

        if (libro.prestarCopia()) {
            Prestamo prestamo = new Prestamo(codigo, libro.getTitulo(), socio, fecha);
            prestamosActivos.insertarFinal(prestamo);
            historial.apilar(new Operacion(Operacion.Tipo.PRESTAMO, codigo, socio, fecha));
            LibroDAO.actualizar(libro);
            System.out.println("Prestamo registrado:\n" + prestamo);
        } else {
            reservasPorLibro.computeIfAbsent(codigo, k -> new Cola<>()).encolar(socio);
            System.out.println("No hay copias disponibles. " + socio + " fue agregado a la cola de reservas.");
        }
    }

    private static void devolverLibro() {
        int codigo = leerEntero("Codigo del libro a devolver: ");
        Libro libro = indiceCodigos.buscarPorCodigo(codigo);
        if (libro == null) {
            System.out.println("No existe un libro con ese codigo.");
            return;
        }
        Prestamo prestamo = prestamosActivos.buscar(p -> p.getCodigoLibro() == codigo);
        if (prestamo == null) {
            System.out.println("No hay un prestamo activo registrado para ese libro.");
            return;
        }
        prestamosActivos.eliminar(p -> p.getCodigoLibro() == codigo);
        libro.devolverCopia();
        historial.apilar(new Operacion(Operacion.Tipo.DEVOLUCION, codigo, prestamo.getSocio(), LocalDate.now().toString()));
        LibroDAO.actualizar(libro);
        System.out.println("Devolucion registrada para: " + prestamo);

        Cola<String> cola = reservasPorLibro.get(codigo);
        if (cola != null && !cola.estaVacia()) {
            String siguienteSocio = cola.desencolar();
            libro.prestarCopia();
            Prestamo nuevoPrestamo = new Prestamo(codigo, libro.getTitulo(), siguienteSocio, LocalDate.now().toString());
            prestamosActivos.insertarFinal(nuevoPrestamo);
            historial.apilar(new Operacion(Operacion.Tipo.PRESTAMO, codigo, siguienteSocio, LocalDate.now().toString()));
            LibroDAO.actualizar(libro);
            System.out.println("El libro fue asignado automáticamente al siguiente en la cola de reservas: " + siguienteSocio);
        }
    }

    private static void deshacerUltimaOperacion() {
        Operacion ultima = historial.desapilar();
        if (ultima == null) {
            System.out.println("No hay operaciones para deshacer.");
            return;
        }
        Libro libro = indiceCodigos.buscarPorCodigo(ultima.getCodigoLibro());
        if (libro == null) {
            System.out.println("No se pudo deshacer: el libro ya no existe.");
            return;
        }
        if (ultima.getTipo() == Operacion.Tipo.PRESTAMO) {
            libro.devolverCopia();
            prestamosActivos.eliminar(p -> p.getCodigoLibro() == ultima.getCodigoLibro()
                    && p.getSocio().equals(ultima.getSocio()));
            System.out.println("Se deshizo el prestamo: " + ultima);
        } else {
            libro.prestarCopia();
            prestamosActivos.insertarFinal(new Prestamo(ultima.getCodigoLibro(), libro.getTitulo(), ultima.getSocio(), ultima.getFecha()));
            System.out.println("Se deshizo la devolucion: " + ultima);
        }
    }

    private static void verHistorial() {
        List<Operacion> lista = historial.aListaDesdeElTope();
        if (lista.isEmpty()) {
            System.out.println("El historial está vacio.");
            return;
        }
        System.out.println("\n--- Historial de operaciones (mas reciente primero) ---");
        for (Operacion op : lista) System.out.println(op);
    }

    private static void verPrestamosActivos() {
        List<Prestamo> lista = prestamosActivos.aLista();
        if (lista.isEmpty()) {
            System.out.println("No hay prestamos activos.");
            return;
        }
        System.out.println("\n--- Prestamos activos ---");
        for (Prestamo p : lista) System.out.println(p);
    }

    private static void verReservas() {
        System.out.println("\n--- Reservas pendientes por libro ---");
        boolean hayReservas = false;
        for (Map.Entry<Integer, Cola<String>> entrada : reservasPorLibro.entrySet()) {
            if (!entrada.getValue().estaVacia()) {
                hayReservas = true;
                Libro libro = indiceCodigos.buscarPorCodigo(entrada.getKey());
                String titulo = (libro != null) ? libro.getTitulo() : "(desconocido)";
                System.out.println("Libro [" + entrada.getKey() + "] " + titulo + " -> " + entrada.getValue().aLista());
            }
        }
        if (!hayReservas) System.out.println("No hay reservas pendientes.");
    }

    // ---------------------- MÉTODOS DE APOYO ----------------------

    private static Libro[] copiarCatalogo() {
        Libro[] copia = new Libro[totalLibros];
        System.arraycopy(catalogo, 0, copia, 0, totalLibros);
        return copia;
    }

    private static void redimensionarCatalogo() {
        Libro[] nuevo = new Libro[catalogo.length * 2];
        System.arraycopy(catalogo, 0, nuevo, 0, catalogo.length);
        catalogo = nuevo;
    }

    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);
        while (!sc.hasNextInt()) {
            System.out.print("Ingrese un numero valido: ");
            sc.next();
        }
        int valor = sc.nextInt();
        sc.nextLine(); // limpiar salto de línea
        return valor;
    }

    /**
     * Carga el catálogo al iniciar el programa. Primero intenta leerlo desde MySQL
     * (persistencia real); si la base de datos no tiene libros o no está disponible,
     * carga un catálogo de ejemplo en memoria para poder probar el sistema igualmente.
     */
    private static void cargarDatosDemo() {
        List<Libro> desdeBD = LibroDAO.listarTodos();
        if (!desdeBD.isEmpty()) {
            for (Libro l : desdeBD) {
                registrarEnMemoria(l);
            }
            System.out.println("Catalogo cargado desde MySQL (" + desdeBD.size() + " libros).");
            return;
        }

        String[][] demo = {
                {"101", "Cien Anios de Soledad", "Gabriel Garcia Marquez", "Novela", "2"},
                {"102", "El Principito", "Antoine de Saint-Exupery", "Infantil", "3"},
                {"103", "Clean Code", "Robert C. Martin", "Tecnologia", "1"},
                {"104", "Estructuras de Datos en Java", "Frank Carrasco", "Tecnologia", "2"},
                {"105", "1984", "George Orwell", "Novela", "1"}
        };
        for (String[] d : demo) {
            Libro l = new Libro(Integer.parseInt(d[0]), d[1], d[2], d[3], Integer.parseInt(d[4]));
            registrarEnMemoria(l);
            LibroDAO.insertar(l); // se guardan también en MySQL para que persistan la próxima vez
        }
        System.out.println("MySQL sin datos previos: se cargo el catalogo de ejemplo y se guardo en la base de datos.");
    }

    private static void registrarEnMemoria(Libro libro) {
        if (totalLibros == catalogo.length) {
            redimensionarCatalogo();
        }
        catalogo[totalLibros++] = libro;
        indiceCodigos.insertar(libro);
        reservasPorLibro.put(libro.getCodigo(), new Cola<>());
    }
}
