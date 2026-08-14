package cajeroatm;

// Maneja el estado del login (cliente logueado, cuenta elegida, intentos de pin, etc)
public class Sesion {

    private Cliente clienteActual;
    private Tarjeta tarjetaActual;
    private Cuenta cuentaSeleccionada;
    private int intentosPIN; // se reinicia cada vez que entra bien o cuando se llama finalizar()
    private boolean autenticada;

    // valida el pin y bloquea la tarjeta despues de 3 intentos fallidos (como un cajero real)
    public ResultadoOperacion validarPIN(Tarjeta tarjeta, Cliente cliente, String pin) {
        if (tarjeta == null || cliente == null) {
            return new ResultadoOperacion(false, "La tarjeta no existe.");
        }

        if (!cliente.estaActivo()) {
            return new ResultadoOperacion(false, "El usuario está inactivo. Contacte al administrador.");
        }

        if (tarjeta.estaBloqueada()) {
            return new ResultadoOperacion(false, "La tarjeta está bloqueada.");
        }

        if (!tarjeta.estaVigente()) {
            return new ResultadoOperacion(false, "La tarjeta no está vigente.");
        }

        if (tarjeta.verificarPIN(pin)) {
            this.tarjetaActual = tarjeta;
            this.clienteActual = cliente;
            this.autenticada = true;
            this.intentosPIN = 0;
            return new ResultadoOperacion(true, "Inicio de sesión exitoso.");
        }

        intentosPIN++;
        if (intentosPIN >= 3) {
            // esto solo bloquea el objeto en memoria, quien lo guarda en la BD
            // es CajeroAutomatico.iniciarSesion (llama a banco.guardarBloqueoTarjeta)
            tarjeta.bloquear();
            return new ResultadoOperacion(false, "PIN incorrecto. La tarjeta fue bloqueada por 3 intentos fallidos.");
        }

        return new ResultadoOperacion(false, "PIN incorrecto. Intentos restantes: " + (3 - intentosPIN));
    }

    public void seleccionarCuenta(Cuenta cuenta) {
        this.cuentaSeleccionada = cuenta;
    }

    public void finalizar() {
        clienteActual = null;
        tarjetaActual = null;
        cuentaSeleccionada = null;
        intentosPIN = 0;
        autenticada = false;
    }

    public boolean estaAutenticada() {
        return autenticada;
    }

    public Cliente getClienteActual() {
        return clienteActual;
    }

    public Cuenta getCuentaSeleccionada() {
        return cuentaSeleccionada;
    }
}
