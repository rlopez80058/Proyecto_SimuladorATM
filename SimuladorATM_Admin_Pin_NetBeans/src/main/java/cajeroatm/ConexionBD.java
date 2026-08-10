package cajeroatm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
            "jdbc:mysql://localhost:3306/simulador_atm";

    private static final String USUARIO = "root";

    private static final String PASSWORD = "root";

    public static Connection obtenerConexion() throws SQLException {

        return DriverManager.getConnection(
                URL,
                USUARIO,
                PASSWORD
        );
    }
}
