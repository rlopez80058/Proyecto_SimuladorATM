package cajeroatm;

public class Sesion {

    private Cliente clienteActual;
    private Tarjeta tarjetaActual;
    private Cuenta cuentaSeleccionada;
    private int intentosPIN;
    private boolean autenticada;

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
