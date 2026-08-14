package cajeroatm;

public class Transferencia extends Transaccion {
    private Cuenta cuentaDestino;

    public Transferencia(Cuenta cuentaOrigen, Cuenta cuentaDestino, double monto) {
        super(cuentaOrigen, monto);
        this.cuentaDestino = cuentaDestino;
    }

    @Override
    public boolean validar() {
        // no se puede transferir a la misma cuenta ni superar el saldo disponible
        return cuentaOrigen != null
                && cuentaDestino != null
                && monto > 0
                && !cuentaOrigen.getNumero().equals(cuentaDestino.getNumero())
                && cuentaOrigen.consultarSaldo() >= monto;
    }

    @Override
    protected void aplicar() {
        cuentaOrigen.debitar(monto);
        cuentaDestino.acreditar(monto);
    }

    @Override
    public String getTipo() {
        return "Transferencia";
    }

    public Cuenta getCuentaDestino() {
        return cuentaDestino;
    }

    @Override
    public String generarComprobante() {
        return super.generarComprobante()
                + "\nCuenta destino: " + cuentaDestino.getNumero();
    }
}
