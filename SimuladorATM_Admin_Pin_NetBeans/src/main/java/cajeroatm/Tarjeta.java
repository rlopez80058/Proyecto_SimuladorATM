package cajeroatm;

public class Tarjeta {

    private String numero;
    private String pin;
    private boolean bloqueada;
    private boolean vigente;

    public Tarjeta(String numero, String pin) {
        this(numero, pin, false, true);
    }

    public Tarjeta(String numero, String pin, boolean bloqueada, boolean vigente) {
        this.numero = numero;
        this.pin = pin;
        this.bloqueada = bloqueada;
        this.vigente = vigente;
    }

    // true solo si el pin coincide Y la tarjeta no esta bloqueada ni vencida
    public boolean verificarPIN(String pinIngresado) {
        return !bloqueada && vigente && pin.equals(pinIngresado);
    }

    public void bloquear() {
        bloqueada = true;
    }

    public void desbloquear() {
        bloqueada = false;
    }

    public boolean estaVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
    }

    public boolean estaBloqueada() {
        return bloqueada;
    }

    public String getNumero() {
        return numero;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
