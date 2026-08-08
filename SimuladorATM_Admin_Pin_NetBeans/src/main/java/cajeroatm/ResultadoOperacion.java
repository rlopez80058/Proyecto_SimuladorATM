package cajeroatm;

public class ResultadoOperacion {
    private final boolean exito;
    private final String mensaje;

    public ResultadoOperacion(boolean exito, String mensaje) {
        this.exito = exito;
        this.mensaje = mensaje;
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }
}
