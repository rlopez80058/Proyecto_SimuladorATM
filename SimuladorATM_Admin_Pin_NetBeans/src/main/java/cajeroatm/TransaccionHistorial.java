package cajeroatm;

import java.time.LocalDateTime;

/**
 * Representa una transacción que ya existe en MySQL.
 * Se usa únicamente para reconstruir el historial al abrir el programa,
 * por lo que aplicar() no modifica ningún saldo.
 */
public class TransaccionHistorial extends Transaccion {

    private final String tipo;
    private final Cuenta cuentaDestino;

    public TransaccionHistorial(int id, LocalDateTime fechaHora, double monto,
                                String estado, String tipo,
                                Cuenta cuentaOrigen, Cuenta cuentaDestino) {
        super(id, fechaHora, monto, estado, cuentaOrigen);
        this.tipo = tipo;
        this.cuentaDestino = cuentaDestino;
    }

    @Override
    public boolean validar() {
        return true;
    }

    @Override
    protected void aplicar() {
        // No se aplica nada: esta transacción ya ocurrió y viene de MySQL.
    }

    @Override
    public String getTipo() {
        return tipo;
    }

    public Cuenta getCuentaDestino() {
        return cuentaDestino;
    }
}

