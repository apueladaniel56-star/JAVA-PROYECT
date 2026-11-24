package datos;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Encapsula la conexión JDBC a la base de datos MySQL "biblioteca_db".
 *
 * Antes (primer avance), el sistema guardaba la información en archivos .txt: esto generaba dificultad
 * para buscar datos, falta de integridad, riesgo de pérdida de información y poca
 * escalabilidad. Al migrar a MySQL se gana organización, seguridad, integridad y
 * rapidez en el acceso a los datos, además de permitir relacionar entidades
 * (libros, préstamos, historial) de forma mucho más sencilla que con archivos planos.
 *
 * IMPORTANTE (configuración local):
 * 1. Ejecutar el script BdMySqlCarlitosV3.sql sobre un servidor MySQL.
 * 2. Agregar la librería "MySQL Connector/J" al proyecto (NetBeans: clic derecho en
 *    Libraries -> Add Library... o Add JAR/Folder, apuntando al .jar del conector).
 * 3. Ajustar USUARIO y CLAVE con las credenciales de tu instalación local de MySQL.
 */
public class Conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/biblioteca_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String CLAVE = "Danielapuela.200609";

    private static Connection conexion;

    private Conexion() { }

    /** Conexión nueva (para quien la cierra con try-with-resources, como FrmPrincipal). */
    public static Connection nueva() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("El conector de MySQL no se encuentra en Libraries.");
        }
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    /** Conexión compartida y reutilizable (la usan los DAO). Devuelve null si no hay base de datos. */
    public static Connection obtener() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = nueva();
            }
        } catch (SQLException e) {
            System.out.println("No se pudo conectar a la base de datos MySQL: " + e.getMessage());
            conexion = null;
        }
        return conexion;
    }
}
