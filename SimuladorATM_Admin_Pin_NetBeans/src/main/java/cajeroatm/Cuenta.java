package cajeroatm;

import java.util.ArrayList;
import java.util.List;

public class Cuenta {
    private String numero;
    private String tipo;
    private double saldo;
    private List<Transaccion> historial;

    public Cuenta(String numero, String tipo, double saldoInicial) {
        this.numero = numero;
        this.tipo = tipo;
        this.saldo = saldoInicial;
        this.historial = new ArrayList<>();
    }

    public double consultarSaldo() {
        return saldo;
    }

    // suma al saldo (depositos), false si el monto no es valido
    public boolean acreditar(double monto) {
        if (monto <= 0) return false;
        saldo += monto;
        return true;
    }

    // resta del saldo (retiros/transferencias), no deja dejar el saldo negativo
    public boolean debitar(double monto) {
        if (monto <= 0 || monto > saldo) return false;
        saldo -= monto;
        return true;
    }

    public void agregarTransaccion(Transaccion transaccion) {
        historial.add(transaccion);
    }

    public List<Transaccion> getHistorial() {
        return historial;
    }

    public String getNumero() {
        return numero;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return numero + " - " + tipo + " - Saldo: ₡" + String.format("%,.2f", saldo);
    }
}
