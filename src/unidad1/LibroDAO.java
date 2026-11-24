package unidad1;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos (DAO) para la entidad Libro sobre MySQL.
 *
 * El arreglo "catalogo" en BibliotecaApp sigue siendo la estructura de datos que se
 * recorre, ordena y busca en memoria (tal como pide el curso), pero cada operación de
 * alta, actualización o baja se refleja también en la base de datos para que el
 * catálogo persista entre ejecuciones del programa, algo que la versión anterior
 * basada en archivos .txt / memoria no garantizaba.
 *
 * Si la base de datos no está disponible, cada método simplemente avisa por consola
 * y el programa continúa funcionando en memoria, para no bloquear las pruebas del
 * resto de estructuras del curso.
 */
public class LibroDAO {

    private LibroDAO() { }

    public static void insertar(Libro libro) {
        String sql = "INSERT INTO libro (codigo, titulo, autor, categoria, copias_totales, copias_disponibles) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = Conexion.obtener();
        if (con == null) return;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, libro.getCodigo());
            ps.setString(2, libro.getTitulo());
            ps.setString(3, libro.getAutor());
            ps.setString(4, libro.getCategoria());
            ps.setInt(5, libro.getCopiasTotales());
            ps.setInt(6, libro.getCopiasDisponibles());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Aviso: no se pudo guardar el libro en MySQL (" + e.getMessage() + ").");
        }
    }

    public static void actualizar(Libro libro) {
        String sql = "UPDATE libro SET titulo = ?, autor = ?, categoria = ?, "
                + "copias_totales = ?, copias_disponibles = ? WHERE codigo = ?";
        Connection con = Conexion.obtener();
        if (con == null) return;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getCategoria());
            ps.setInt(4, libro.getCopiasTotales());
            ps.setInt(5, libro.getCopiasDisponibles());
            ps.setInt(6, libro.getCodigo());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Aviso: no se pudo actualizar el libro en MySQL (" + e.getMessage() + ").");
        }
    }

    public static void eliminar(int codigo) {
        String sql = "DELETE FROM libro WHERE codigo = ?";
        Connection con = Conexion.obtener();
        if (con == null) return;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codigo);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Aviso: no se pudo eliminar el libro en MySQL (" + e.getMessage() + ").");
        }
    }

    /** Carga todo el catálogo guardado en MySQL (se usa al iniciar el programa). */
    public static List<Libro> listarTodos() {
        List<Libro> resultado = new ArrayList<>();
        String sql = "SELECT codigo, titulo, autor, categoria, copias_totales, copias_disponibles FROM libro";
        Connection con = Conexion.obtener();
        if (con == null) return resultado;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Libro libro = new Libro(
                        rs.getInt("codigo"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("categoria"),
                        rs.getInt("copias_totales"));
                int disponibles = rs.getInt("copias_disponibles");
                // Ajusta las copias disponibles reales (pueden ser menores al total si hay préstamos activos).
                while (libro.getCopiasDisponibles() > disponibles) {
                    libro.prestarCopia();
                }
                resultado.add(libro);
            }
        } catch (SQLException e) {
            System.out.println("Aviso: no se pudo leer el catalogo desde MySQL (" + e.getMessage() + ").");
        }
        return resultado;
    }
}
