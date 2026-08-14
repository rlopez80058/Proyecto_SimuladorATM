package cajeroatm;

public class Deposito extends Transaccion {
    public Deposito(Cuenta cuentaOrigen, double monto) {
        super(cuentaOrigen, monto);
    }

    @Override
    public boolean validar() {
        return cuentaOrigen != null && monto > 0; // deposito no tiene mas restricciones que el monto
    }

    @Override
    protected void aplicar() {
        cuentaOrigen.acreditar(monto);
    }

    @Override
    public String getTipo() {
        return "Depósito";
    }
}
