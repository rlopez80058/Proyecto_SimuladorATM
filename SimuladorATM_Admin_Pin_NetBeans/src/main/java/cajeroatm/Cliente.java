package cajeroatm;

import java.util.ArrayList;
import java.util.List;

// Representa al dueño de una o mas cuentas/tarjetas
public class Cliente {

    private String identificacion;
    private String nombre;
    private boolean activo; // si esta en false no puede iniciar sesion (lo controla Sesion.validarPIN)
    private List<Cuenta> cuentas;

    // constructor corto, por defecto el cliente se crea activo
    public Cliente(String identificacion, String nombre) {
        this(identificacion, nombre, true);
    }

    // este es el que se usa cuando se carga desde la BD, ahi si puede venir activo=false
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