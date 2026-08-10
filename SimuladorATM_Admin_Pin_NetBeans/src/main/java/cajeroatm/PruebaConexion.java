package cajeroatm;

import java.sql.Connection;

public class PruebaConexion {

    public static void main(String[] args) {

        try (Connection conexion = ConexionBD.obtenerConexion()) {

            if (conexion != null) {
                System.out.println("Conexión a MySQL exitosa.");
            }

        } catch (Exception e) {

            System.out.println("Error de conexión:");
            e.printStackTrace();

        }
    }
}