package cajeroatm;

import java.util.ArrayList;
import java.util.List;

public class Cliente {

    private String identificacion;
    private String nombre;
    private boolean activo;
    private List<Cuenta> cuentas;

    public Cliente(String identificacion, String nombre) {
        this(identificacion, nombre, true);
    }

    public Cliente(String identificacion, String nombre, boolean activo) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.activo = activo;
        this.cuentas = new ArrayList<>();
    }

    public void agregarCuenta(Cuenta cuenta) {
        cuentas.add(cuenta);
    }

    public List<Cuenta> obtenerCuentas() {
        return cuentas;
    }

    public boolean estaActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIdentificacion() {
        return identificacion;
    }
}