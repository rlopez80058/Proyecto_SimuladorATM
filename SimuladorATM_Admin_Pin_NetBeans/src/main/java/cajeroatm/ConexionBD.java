package cajeroatm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Clase para manejar la conexión a la base de datos MySQL local
public class ConexionBD {

    // ojo: esto asume MySQL corriendo en localhost con la BD ya creada
    private static final String URL =
            "jdbc:mysql://localhost:3306/simulador_atm";

    // usuario y contraseña por defecto de MySQL en local, si en su compu es distinto hay que cambiarlo aca
    private static final String USUARIO = "root";

    private static final String PASSWORD = "root";

    // abre una conexion nueva cada vez que se llama, se cierra con try-with-resources en donde se usa
    public static Connection obtenerConexion() throws SQLException {

        return DriverManager.getConnection(
                URL,
                USUARIO,
                PASSWORD
        );
    }
}
