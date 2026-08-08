package cajeroatm;

public class Retiro extends Transaccion {
    public Retiro(Cuenta cuentaOrigen, double monto) {
        super(cuentaOrigen, monto);
    }

    @Override
    public boolean validar() {
        return cuentaOrigen != null && monto > 0 && cuentaOrigen.consultarSaldo() >= monto;
    }

    @Override
    protected void aplicar() {
        cuentaOrigen.debitar(monto);
    }

    @Override
    public String getTipo() {
        return "Retiro";
    }
}
