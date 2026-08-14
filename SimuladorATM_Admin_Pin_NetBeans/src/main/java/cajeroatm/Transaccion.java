package cajeroatm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Clase base para Deposito/Retiro/Transferencia, cada una implementa su propia validacion
// Nota: el id que se pone aqui con el consecutivo es temporal, en Banco.java se sobreescribe
// con el id real que devuelve MySQL despues del INSERT (por eso lo de "consecutivo" casi no importa)
public abstract class Transaccion {
    private static int consecutivo = 1;
    protected int id;
    protected LocalDateTime fechaHora;
    protected double monto;
    protected String estado;
    protected Cuenta cuentaOrigen;

    public Transaccion(Cuenta cuentaOrigen, double monto) {
        this.id = consecutivo++;
        this.fechaHora = LocalDateTime.now();
        this.monto = monto;
        this.cuentaOrigen = cuentaOrigen;
        this.estado = "PENDIENTE";
    }

    // Constructor usado al reconstruir el historial guardado en MySQL.
    protected Transaccion(int id, LocalDateTime fechaHora, double monto,
                          String estado, Cuenta cuentaOrigen) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.monto = monto;
        this.estado = estado;
        this.cuentaOrigen = cuentaOrigen;

        // Evita que las transacciones nuevas reutilicen un consecutivo ya cargado.
        if (id >= consecutivo) {
            consecutivo = id + 1;
        }
    }

    public abstract boolean validar();   // revisa si la operacion se puede hacer (saldo, montos, etc)
    protected abstract void aplicar();    // hace el cambio real sobre la cuenta (solo en memoria)
    public abstract String getTipo();

    // Este metodo casi no se usa ya, Banco.ejecutarTransaccionPersistente hace lo mismo
    // pero contra la base de datos. Se dejo por si se necesita probar sin BD.
    public ResultadoOperacion ejecutar() {
        if (!validar()) {
            estado = "RECHAZADA";
            if (cuentaOrigen != null) {
                cuentaOrigen.agregarTransaccion(this);
            }
            return new ResultadoOperacion(false, "Operación rechazada: " + getTipo());
        }

        aplicar();
        estado = "APROBADA";
        cuentaOrigen.agregarTransaccion(this);
        return new ResultadoOperacion(true, generarComprobante());
    }

    public String generarComprobante() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return "Comprobante ATM\n"
                + "---------------------------\n"
                + "Transacción: " + id + "\n"
                + "Tipo: " + getTipo() + "\n"
                + "Fecha: " + fechaHora.format(formato) + "\n"
                + "Cuenta: " + cuentaOrigen.getNumero() + "\n"
                + "Monto: ₡" + String.format("%,.2f", monto) + "\n"
                + "Estado: " + estado + "\n"
                + "Saldo actual: ₡" + String.format("%,.2f", cuentaOrigen.consultarSaldo());
    }

    @Override
    public String toString() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return fechaHora.format(formato) + " | " + getTipo() + " | ₡"
                + String.format("%,.2f", monto) + " | " + estado;
    }
}
