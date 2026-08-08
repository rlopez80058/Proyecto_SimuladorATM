package cajeroatm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Banco {

    private Map<String, Tarjeta> tarjetas;
    private Map<String, Cliente> clientesPorTarjeta;
    private Map<String, Cuenta> cuentas;

    public Banco() {
        tarjetas = new LinkedHashMap<>();
        clientesPorTarjeta = new LinkedHashMap<>();
        cuentas = new LinkedHashMap<>();
        cargarDatosDePrueba();
    }

    private void cargarDatosDePrueba() {
        Cliente cliente1 = new Cliente("1-1111-1111", "Nelson Rodríguez");
        Cuenta cuenta1 = new Cuenta("1001", "Ahorros", 150000);
        Cuenta cuenta2 = new Cuenta("1002", "Corriente", 85000);
        cliente1.agregarCuenta(cuenta1);
        cliente1.agregarCuenta(cuenta2);
        Tarjeta tarjeta1 = new Tarjeta("1111222233334444", "1234");

        Cliente cliente2 = new Cliente("2-2222-2222", "Rocío López");
        Cuenta cuenta3 = new Cuenta("2001", "Ahorros", 250000);
        cliente2.agregarCuenta(cuenta3);
        Tarjeta tarjeta2 = new Tarjeta("5555666677778888", "4321");

        registrarCliente(tarjeta1, cliente1);
        registrarCliente(tarjeta2, cliente2);
    }

    public void registrarCliente(Tarjeta tarjeta, Cliente cliente) {
        tarjetas.put(tarjeta.getNumero(), tarjeta);
        clientesPorTarjeta.put(tarjeta.getNumero(), cliente);

        for (Cuenta cuenta : cliente.obtenerCuentas()) {
            cuentas.put(cuenta.getNumero(), cuenta);
        }
    }

    public Tarjeta buscarTarjeta(String numeroTarjeta) {
        return tarjetas.get(numeroTarjeta);
    }

    public Cliente obtenerClientePorTarjeta(String numeroTarjeta) {
        return clientesPorTarjeta.get(numeroTarjeta);
    }

    public Cuenta buscarCuenta(String numeroCuenta) {
        return cuentas.get(numeroCuenta);
    }

    public List<Cliente> listarClientes() {
        List<Cliente> clientes = new ArrayList<>();

        for (Cliente cliente : clientesPorTarjeta.values()) {
            if (!clientes.contains(cliente)) {
                clientes.add(cliente);
            }
        }

        return clientes;
    }

    public String obtenerNumeroTarjetaPorIdentificacion(String identificacion) {
        for (Map.Entry<String, Cliente> entry : clientesPorTarjeta.entrySet()) {
            if (entry.getValue().getIdentificacion().equalsIgnoreCase(identificacion)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public Tarjeta obtenerTarjetaPorIdentificacion(String identificacion) {
        String numeroTarjeta = obtenerNumeroTarjetaPorIdentificacion(identificacion);

        if (numeroTarjeta == null) {
            return null;
        }

        return tarjetas.get(numeroTarjeta);
    }

    public boolean existeIdentificacion(String identificacion) {
        for (Cliente cliente : listarClientes()) {
            if (cliente.getIdentificacion().equalsIgnoreCase(identificacion)) {
                return true;
            }
        }

        return false;
    }

    public boolean existeTarjeta(String numeroTarjeta) {
        return tarjetas.containsKey(numeroTarjeta);
    }

    public boolean existeCuenta(String numeroCuenta) {
        return cuentas.containsKey(numeroCuenta);
    }

    public ResultadoOperacion crearUsuario(
            String identificacion,
            String nombre,
            String numeroTarjeta,
            String pin,
            String numeroCuenta,
            String tipoCuenta,
            double saldoInicial
    ) {
        identificacion = limpiarTexto(identificacion);
        nombre = limpiarTexto(nombre);
        numeroTarjeta = limpiarTexto(numeroTarjeta);
        pin = limpiarTexto(pin);
        numeroCuenta = limpiarTexto(numeroCuenta);
        tipoCuenta = limpiarTexto(tipoCuenta);

        if (identificacion.isEmpty() || nombre.isEmpty() || numeroTarjeta.isEmpty()
                || pin.isEmpty() || numeroCuenta.isEmpty() || tipoCuenta.isEmpty()) {
            return new ResultadoOperacion(false, "Debe completar todos los campos.");
        }

        if (pin.length() != 4 || !pin.matches("\\d+")) {
            return new ResultadoOperacion(false, "El PIN debe tener exactamente 4 números.");
        }

        if (!numeroTarjeta.matches("\\d+") || numeroTarjeta.length() < 12) {
            return new ResultadoOperacion(false, "La tarjeta debe contener solo números y tener al menos 12 dígitos.");
        }

        if (!numeroCuenta.matches("\\d+") || numeroCuenta.length() < 3) {
            return new ResultadoOperacion(false, "La cuenta debe contener solo números y tener al menos 3 dígitos.");
        }

        if (saldoInicial < 0) {
            return new ResultadoOperacion(false, "El saldo inicial no puede ser negativo.");
        }

        if (existeIdentificacion(identificacion)) {
            return new ResultadoOperacion(false, "Ya existe un usuario con esa identificación.");
        }

        if (existeTarjeta(numeroTarjeta)) {
            return new ResultadoOperacion(false, "Ya existe una tarjeta registrada con ese número.");
        }

        if (existeCuenta(numeroCuenta)) {
            return new ResultadoOperacion(false, "Ya existe una cuenta registrada con ese número.");
        }

        Cliente cliente = new Cliente(identificacion, nombre);
        Cuenta cuenta = new Cuenta(numeroCuenta, tipoCuenta, saldoInicial);
        Tarjeta tarjeta = new Tarjeta(numeroTarjeta, pin);

        cliente.agregarCuenta(cuenta);
        registrarCliente(tarjeta, cliente);

        return new ResultadoOperacion(true, "Usuario creado correctamente. Ya puede iniciar sesión en el ATM.");
    }

    public ResultadoOperacion actualizarUsuario(
            String identificacion,
            String nuevoNombre,
            String nuevoPin,
            boolean activo
    ) {
        identificacion = limpiarTexto(identificacion);
        nuevoNombre = limpiarTexto(nuevoNombre);
        nuevoPin = limpiarTexto(nuevoPin);

        Cliente cliente = buscarClientePorIdentificacion(identificacion);

        if (cliente == null) {
            return new ResultadoOperacion(false, "No se encontró el usuario seleccionado.");
        }

        if (nuevoNombre.isEmpty()) {
            return new ResultadoOperacion(false, "El nombre no puede quedar vacío.");
        }

        if (!nuevoPin.isEmpty() && (nuevoPin.length() != 4 || !nuevoPin.matches("\\d+"))) {
            return new ResultadoOperacion(false, "El nuevo PIN debe tener exactamente 4 números.");
        }

        cliente.setNombre(nuevoNombre);
        cliente.setActivo(activo);

        Tarjeta tarjeta = obtenerTarjetaPorIdentificacion(identificacion);
        if (tarjeta != null && !nuevoPin.isEmpty()) {
            tarjeta.setPin(nuevoPin);
        }

        return new ResultadoOperacion(true, "Usuario actualizado correctamente.");
    }

    public ResultadoOperacion desbloquearTarjetaPorIdentificacion(String identificacion) {
        Tarjeta tarjeta = obtenerTarjetaPorIdentificacion(identificacion);

        if (tarjeta == null) {
            return new ResultadoOperacion(false, "No se encontró la tarjeta del usuario.");
        }

        tarjeta.desbloquear();
        return new ResultadoOperacion(true, "Tarjeta desbloqueada correctamente.");
    }

    public ResultadoOperacion eliminarUsuario(String identificacion) {
        identificacion = limpiarTexto(identificacion);

        Cliente cliente = buscarClientePorIdentificacion(identificacion);

        if (cliente == null) {
            return new ResultadoOperacion(false, "No se encontró el usuario seleccionado.");
        }

        String numeroTarjeta = obtenerNumeroTarjetaPorIdentificacion(identificacion);

        if (numeroTarjeta != null) {
            tarjetas.remove(numeroTarjeta);
            clientesPorTarjeta.remove(numeroTarjeta);
        }

        for (Cuenta cuenta : cliente.obtenerCuentas()) {
            cuentas.remove(cuenta.getNumero());
        }

        return new ResultadoOperacion(true, "Usuario eliminado correctamente.");
    }

    public Cliente buscarClientePorIdentificacion(String identificacion) {
        for (Cliente cliente : listarClientes()) {
            if (cliente.getIdentificacion().equalsIgnoreCase(identificacion)) {
                return cliente;
            }
        }

        return null;
    }

    private String limpiarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
