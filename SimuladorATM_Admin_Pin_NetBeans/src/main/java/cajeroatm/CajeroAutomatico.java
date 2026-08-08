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
        return sesion.validarPIN(tarjeta, cliente, pin);
    }

    public void seleccionarCuenta(Cuenta cuenta) {
        sesion.seleccionarCuenta(cuenta);
    }

    public ResultadoOperacion retirar(double monto) {
        return new Retiro(sesion.getCuentaSeleccionada(), monto).ejecutar();
    }

    public ResultadoOperacion depositar(double monto) {
        return new Deposito(sesion.getCuentaSeleccionada(), monto).ejecutar();
    }

    public ResultadoOperacion transferir(String numeroCuentaDestino, double monto) {
        Cuenta destino = banco.buscarCuenta(numeroCuentaDestino);
        return new Transferencia(sesion.getCuentaSeleccionada(), destino, monto).ejecutar();
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
