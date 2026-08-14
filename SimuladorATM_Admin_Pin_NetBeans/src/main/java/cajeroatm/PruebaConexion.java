package cajeroatm;

import java.sql.Connection;

// clase aparte solo para probar rapido que la conexion a MySQL funciona, no se usa en el programa real
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