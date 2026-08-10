package cajeroatm;

import java.util.List;

public class CajeroAutomatico {
    private Banco banco;
    private Sesion sesion;

    public CajeroAutomatico(Banco banco) {
        this.banco = banco;
        this.sesion = new Sesion();
    }

    public ResultadoOperacion iniciarSesion(String numeroTarjeta, String pin) {
        Tarjeta tarjeta = banco.buscarTarjeta(numeroTarjeta);
        Cliente cliente = banco.obtenerClientePorTarjeta(numeroTarjeta);

        boolean estabaBloqueada = tarjeta != null && tarjeta.estaBloqueada();

        ResultadoOperacion resultado = sesion.validarPIN(tarjeta, cliente, pin);

        if (tarjeta != null && !estabaBloqueada && tarjeta.estaBloqueada()) {
            ResultadoOperacion guardado = banco.guardarBloqueoTarjeta(numeroTarjeta, true);

            if (!guardado.isExito()) {
                System.err.println(guardado.getMensaje());
            }
        }

        return resultado;
    }

    public void seleccionarCuenta(Cuenta cuenta) {
        sesion.seleccionarCuenta(cuenta);
    }

    public ResultadoOperacion retirar(double monto) {
        return banco.procesarRetiro(sesion.getCuentaSeleccionada(), monto);
    }

    public ResultadoOperacion depositar(double monto) {
        return banco.procesarDeposito(sesion.getCuentaSeleccionada(), monto);
    }

    public ResultadoOperacion transferir(String numeroCuentaDestino, double monto) {
        Cuenta destino = banco.buscarCuenta(numeroCuentaDestino);
        return banco.procesarTransferencia(sesion.getCuentaSeleccionada(), destino, monto);
    }

    public double consultarSaldo() {
        return sesion.getCuentaSeleccionada().consultarSaldo();
    }

    public List<Cuenta> obtenerCuentasCliente() {
        return sesion.getClienteActual().obtenerCuentas();
    }

    public Cliente getClienteActual() {
        return sesion.getClienteActual();
    }

    public void cerrarSesion() {
        sesion.finalizar();
    }
}
