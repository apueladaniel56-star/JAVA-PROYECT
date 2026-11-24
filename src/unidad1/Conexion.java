package unidad1;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Encapsula la conexión JDBC a la base de datos MySQL "biblioteca_db".
 *
 * Antes, el sistema guardaba la información en archivos .txt: esto generaba dificultad
 * para buscar datos, falta de integridad, riesgo de pérdida de información y poca
 * escalabilidad. Al migrar a MySQL se gana organización, seguridad, integridad y
 * rapidez en el acceso a los datos, además de permitir relacionar entidades
 * (libros, préstamos, historial) de forma mucho más sencilla que con archivos planos.
 *
 * IMPORTANTE (configuración local):
 * 1. Ejecutar el script schema.sql incluido en este proyecto sobre un servidor MySQL.
 * 2. Agregar la librería "MySQL Connector/J" al proyecto (NetBeans: clic derecho en
 *    Libraries -> Add Library... o Add JAR/Folder, apuntando al .jar del conector).
 * 3. Ajustar USUARIO y CLAVE con las credenciales de tu instalación local de MySQL.
 */
public class Conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/biblioteca_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String CLAVE = "root";

    private static Connection conexion;

    private Conexion() { }

    public static Connection obtener() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
            }
        } catch (SQLException e) {
            System.out.println("No se pudo conectar a la base de datos MySQL: " + e.getMessage());
            conexion = null;
        }
        return conexion;
    }
}
