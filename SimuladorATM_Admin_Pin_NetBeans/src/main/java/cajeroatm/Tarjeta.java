package cajeroatm;

public class Tarjeta {

    private String numero;
    private String pin;
    private boolean bloqueada;
    private boolean vigente;

    public Tarjeta(String numero, String pin) {
        this.numero = numero;
        this.pin = pin;
        this.bloqueada = false;
        this.vigente = true;
    }

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
