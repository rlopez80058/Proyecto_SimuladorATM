package cajeroatm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Esta es la clase que habla con MySQL. Todo lo que sea leer/guardar datos pasa por aca,
// las demas clases (Cuenta, Cliente, etc) son solo el modelo en memoria.
public class Banco {

    // se guardan en memoria como cache de lo que hay en la BD, se recargan al abrir el programa
    private Map<String, Tarjeta> tarjetas;
    private Map<String, Cliente> clientesPorTarjeta;
    private Map<String, Cuenta> cuentas;

    public Banco() {
        tarjetas = new LinkedHashMap<>();
        clientesPorTarjeta = new LinkedHashMap<>();
        cuentas = new LinkedHashMap<>();
        cargarDatosDesdeBD(); // si esto falla el programa no arranca (ver el throw mas abajo)
    }

    // trae todo de MySQL (clientes, cuentas, tarjetas e historial) y arma los mapas en memoria
    private void cargarDatosDesdeBD() {
        tarjetas.clear();
        clientesPorTarjeta.clear();
        cuentas.clear();

        // mapa auxiliar solo para poder enlazar cuentas/tarjetas con su cliente mientras se cargan
        Map<String, Cliente> clientesPorIdentificacion = new LinkedHashMap<>();

        // el orden importa: primero clientes, porque cuentas y tarjetas dependen de ellos
        String sqlClientes = "SELECT identificacion, nombre, activo FROM clientes";
        String sqlCuentas = "SELECT numero, tipo, saldo, identificacion_cliente FROM cuentas";
        String sqlTarjetas = "SELECT numero, pin, bloqueada, vigente, identificacion_cliente FROM tarjetas";

        try (Connection conexion = ConexionBD.obtenerConexion()) {

            try (PreparedStatement ps = conexion.prepareStatement(sqlClientes);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Cliente cliente = new Cliente(
                            rs.getString("identificacion"),
                            rs.getString("nombre"),
                            rs.getBoolean("activo")
                    );

                    clientesPorIdentificacion.put(cliente.getIdentificacion(), cliente);
                }
            }

            try (PreparedStatement ps = conexion.prepareStatement(sqlCuentas);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Cuenta cuenta = new Cuenta(
                            rs.getString("numero"),
                            rs.getString("tipo"),
                            rs.getDouble("saldo")
                    );

                    String identificacion = rs.getString("identificacion_cliente");
                    Cliente cliente = clientesPorIdentificacion.get(identificacion);

                    if (cliente != null) {
                        cliente.agregarCuenta(cuenta);
                        cuentas.put(cuenta.getNumero(), cuenta);
                    }
                }
            }

            try (PreparedStatement ps = conexion.prepareStatement(sqlTarjetas);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Tarjeta tarjeta = new Tarjeta(
                            rs.getString("numero"),
                            rs.getString("pin"),
                            rs.getBoolean("bloqueada"),
                            rs.getBoolean("vigente")
                    );

                    String identificacion = rs.getString("identificacion_cliente");
                    Cliente cliente = clientesPorIdentificacion.get(identificacion);

                    if (cliente != null) {
                        tarjetas.put(tarjeta.getNumero(), tarjeta);
                        clientesPorTarjeta.put(tarjeta.getNumero(), cliente);
                    }
                }
            }

            cargarHistorialDesdeBD(conexion);

            System.out.println("Datos e historial cargados desde MySQL correctamente.");

        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible cargar los datos desde MySQL.", e);
        }
    }

    // reconstruye el historial de movimientos usando TransaccionHistorial
    // (esa clase no vuelve a aplicar el movimiento, solo sirve para mostrarlo)
    private void cargarHistorialDesdeBD(Connection conexion) throws SQLException {
        String sql = "SELECT id, tipo, monto, estado, fecha_hora, cuenta_origen, cuenta_destino "
                + "FROM transacciones ORDER BY fecha_hora, id";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cuenta cuentaOrigen = cuentas.get(rs.getString("cuenta_origen"));

                // Si la cuenta ya no existe, no podemos asociar el movimiento al historial.
                if (cuentaOrigen == null) {
                    continue;
                }

                String numeroDestino = rs.getString("cuenta_destino");
                Cuenta cuentaDestino = numeroDestino == null ? null : cuentas.get(numeroDestino);

                Timestamp timestamp = rs.getTimestamp("fecha_hora");

                TransaccionHistorial transaccion = new TransaccionHistorial(
                        rs.getInt("id"),
                        timestamp.toLocalDateTime(),
                        rs.getDouble("monto"),
                        rs.getString("estado"),
                        rs.getString("tipo"),
                        cuentaOrigen,
                        cuentaDestino
                );

                // Mantiene el mismo comportamiento del proyecto original:
                // el historial se muestra en la cuenta que originó la operación.
                cuentaOrigen.agregarTransaccion(transaccion);
            }
        }
    }

    public ResultadoOperacion procesarDeposito(Cuenta cuenta, double monto) {
        return ejecutarTransaccionPersistente(new Deposito(cuenta, monto), null);
    }

    public ResultadoOperacion procesarRetiro(Cuenta cuenta, double monto) {
        return ejecutarTransaccionPersistente(new Retiro(cuenta, monto), null);
    }

    public ResultadoOperacion procesarTransferencia(Cuenta cuentaOrigen, Cuenta cuentaDestino, double monto) {
        return ejecutarTransaccionPersistente(
                new Transferencia(cuentaOrigen, cuentaDestino, monto),
                cuentaDestino
        );
    }

    // Este es el metodo central de todo: valida, actualiza la BD y si sale bien recien ahi
    // actualiza el saldo en memoria. Usa commit/rollback para que si algo falla a la mitad
    // (ej. se cae la conexion) no quede el saldo de una cuenta actualizado y el de la otra no.
    private ResultadoOperacion ejecutarTransaccionPersistente(Transaccion transaccion, Cuenta cuentaDestino) {
        if (transaccion == null || transaccion.cuentaOrigen == null) {
            return new ResultadoOperacion(false, "Primero debe seleccionar una cuenta.");
        }

        // si no pasa validar() (saldo insuficiente, monto invalido, etc) igual se guarda
        // el intento en la BD como RECHAZADA, para que quede en el historial
        if (!transaccion.validar()) {
            transaccion.estado = "RECHAZADA";

            try (Connection conexion = ConexionBD.obtenerConexion()) {
                int idGenerado = insertarTransaccion(conexion, transaccion, cuentaDestino, "RECHAZADA");
                transaccion.id = idGenerado;
            } catch (SQLException e) {
                e.printStackTrace();
                transaccion.cuentaOrigen.agregarTransaccion(transaccion);
                return new ResultadoOperacion(
                        false,
                        "Operación rechazada: " + transaccion.getTipo()
                        + "\nNo fue posible guardar el intento en MySQL: " + e.getMessage()
                );
            }

            transaccion.cuentaOrigen.agregarTransaccion(transaccion);
            return new ResultadoOperacion(false, "Operación rechazada: " + transaccion.getTipo());
        }

        // autoCommit en false porque son varias sentencias (update de saldo + insert del
        // movimiento) y necesitamos que se apliquen juntas o ninguna
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                // dependiendo del tipo se actualiza 1 o 2 cuentas, por eso el if
                if (transaccion instanceof Deposito) {
                    actualizarSaldoDeposito(conexion, transaccion.cuentaOrigen, transaccion.monto);

                } else if (transaccion instanceof Retiro) {
                    actualizarSaldoRetiro(conexion, transaccion.cuentaOrigen, transaccion.monto);

                } else if (transaccion instanceof Transferencia) {
                    actualizarSaldoTransferencia(
                            conexion,
                            transaccion.cuentaOrigen,
                            cuentaDestino,
                            transaccion.monto
                    );

                } else {
                    throw new SQLException("Tipo de transacción no soportado.");
                }

                int idGenerado = insertarTransaccion(
                        conexion,
                        transaccion,
                        cuentaDestino,
                        "APROBADA"
                );

                conexion.commit();

                // El saldo en memoria se cambia únicamente después del COMMIT exitoso.
                transaccion.aplicar();
                transaccion.estado = "APROBADA";
                transaccion.id = idGenerado;
                transaccion.cuentaOrigen.agregarTransaccion(transaccion);

                return new ResultadoOperacion(true, transaccion.generarComprobante());

            } catch (SQLException e) {
                // algo fallo a mitad de camino, se revierte todo lo de este bloque
                conexion.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new ResultadoOperacion(
                    false,
                    "No se pudo procesar la operación en MySQL: " + e.getMessage()
            );
        }
    }

    private void actualizarSaldoDeposito(Connection conexion, Cuenta cuenta, double monto)
            throws SQLException {

        String sql = "UPDATE cuentas SET saldo = saldo + ? WHERE numero = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDouble(1, monto);
            ps.setString(2, cuenta.getNumero());

            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se encontró la cuenta del depósito.");
            }
        }
    }

    private void actualizarSaldoRetiro(Connection conexion, Cuenta cuenta, double monto)
            throws SQLException {

        // el "AND saldo >= ?" es a proposito, evita que dos retiros al mismo tiempo dejen
        // el saldo en negativo (si executeUpdate devuelve 0 es porque no alcanzaba el saldo)
        String sql = "UPDATE cuentas SET saldo = saldo - ? "
                + "WHERE numero = ? AND saldo >= ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDouble(1, monto);
            ps.setString(2, cuenta.getNumero());
            ps.setDouble(3, monto);

            if (ps.executeUpdate() != 1) {
                throw new SQLException("Saldo insuficiente o cuenta inexistente.");
            }
        }
    }

    private void actualizarSaldoTransferencia(
            Connection conexion,
            Cuenta cuentaOrigen,
            Cuenta cuentaDestino,
            double monto
    ) throws SQLException {

        if (cuentaDestino == null) {
            throw new SQLException("La cuenta destino no existe.");
        }

        // primero se debita la origen y despues se acredita la destino, en la misma transaccion
        String sqlDebitar = "UPDATE cuentas SET saldo = saldo - ? "
                + "WHERE numero = ? AND saldo >= ?";

        String sqlAcreditar = "UPDATE cuentas SET saldo = saldo + ? WHERE numero = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sqlDebitar)) {
            ps.setDouble(1, monto);
            ps.setString(2, cuentaOrigen.getNumero());
            ps.setDouble(3, monto);

            if (ps.executeUpdate() != 1) {
                throw new SQLException("Saldo insuficiente en la cuenta origen.");
            }
        }

        try (PreparedStatement ps = conexion.prepareStatement(sqlAcreditar)) {
            ps.setDouble(1, monto);
            ps.setString(2, cuentaDestino.getNumero());

            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se encontró la cuenta destino.");
            }
        }
    }

    // guarda el movimiento en la tabla transacciones y devuelve el id autoincrement que genero MySQL
    // (Statement.RETURN_GENERATED_KEYS es lo que permite leer ese id despues del insert)
    private int insertarTransaccion(
            Connection conexion,
            Transaccion transaccion,
            Cuenta cuentaDestino,
            String estado
    ) throws SQLException {

        String sql = "INSERT INTO transacciones "
                + "(tipo, monto, estado, fecha_hora, cuenta_origen, cuenta_destino) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, transaccion.getTipo());
            ps.setDouble(2, transaccion.monto);
            ps.setString(3, estado);
            ps.setTimestamp(4, Timestamp.valueOf(transaccion.fechaHora));
            ps.setString(5, transaccion.cuentaOrigen.getNumero());

            if (cuentaDestino == null) {
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(6, cuentaDestino.getNumero());
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("MySQL no devolvió el ID de la transacción.");
    }

    // agrega el cliente/tarjeta/cuentas a los mapas en memoria (no toca la BD, eso ya se hizo antes)
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

    // Crea un cliente + tarjeta + cuenta nuevos, todo junto. Se usa desde el modulo admin.
    // Son bastantes validaciones seguidas, ojo si se le agrega algo mas revisar que no rompa el orden
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

        String sqlCliente = "INSERT INTO clientes (identificacion, nombre, activo) VALUES (?, ?, ?)";
        String sqlTarjeta = "INSERT INTO tarjetas (numero, pin, bloqueada, vigente, identificacion_cliente) VALUES (?, ?, ?, ?, ?)";
        String sqlCuenta = "INSERT INTO cuentas (numero, tipo, saldo, identificacion_cliente) VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                // el cliente va primero porque tarjetas y cuentas tienen FK a identificacion_cliente
                try (PreparedStatement ps = conexion.prepareStatement(sqlCliente)) {
                    ps.setString(1, identificacion);
                    ps.setString(2, nombre);
                    ps.setBoolean(3, true);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conexion.prepareStatement(sqlTarjeta)) {
                    ps.setString(1, numeroTarjeta);
                    ps.setString(2, pin);
                    ps.setBoolean(3, false);
                    ps.setBoolean(4, true);
                    ps.setString(5, identificacion);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conexion.prepareStatement(sqlCuenta)) {
                    ps.setString(1, numeroCuenta);
                    ps.setString(2, tipoCuenta);
                    ps.setDouble(3, saldoInicial);
                    ps.setString(4, identificacion);
                    ps.executeUpdate();
                }

                conexion.commit();

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new ResultadoOperacion(false, "No se pudo guardar el usuario en MySQL: " + e.getMessage());
        }

        Cliente cliente = new Cliente(identificacion, nombre, true);
        Cuenta cuenta = new Cuenta(numeroCuenta, tipoCuenta, saldoInicial);
        Tarjeta tarjeta = new Tarjeta(numeroTarjeta, pin, false, true);

        cliente.agregarCuenta(cuenta);
        registrarCliente(tarjeta, cliente);

        return new ResultadoOperacion(true, "Usuario creado correctamente y guardado en MySQL.");
    }

    // edita nombre, pin (opcional) y estado activo de un cliente que ya existe.
    // OJO: no se puede cambiar el numero de tarjeta ni de cuenta desde aca, eso se decidio
    // asi porque si no habria que migrar el historial de transacciones tambien
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

        // el pin es opcional al editar, si viene vacio se deja el que ya tenia (se valida solo si viene algo)
        if (!nuevoPin.isEmpty() && (nuevoPin.length() != 4 || !nuevoPin.matches("\\d+"))) {
            return new ResultadoOperacion(false, "El nuevo PIN debe tener exactamente 4 números.");
        }

        Tarjeta tarjeta = obtenerTarjetaPorIdentificacion(identificacion);

        String sqlCliente = "UPDATE clientes SET nombre = ?, activo = ? WHERE identificacion = ?";
        String sqlPin = "UPDATE tarjetas SET pin = ? WHERE numero = ?";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conexion.prepareStatement(sqlCliente)) {
                    ps.setString(1, nuevoNombre);
                    ps.setBoolean(2, activo);
                    ps.setString(3, identificacion);
                    ps.executeUpdate();
                }

                if (tarjeta != null && !nuevoPin.isEmpty()) {
                    try (PreparedStatement ps = conexion.prepareStatement(sqlPin)) {
                        ps.setString(1, nuevoPin);
                        ps.setString(2, tarjeta.getNumero());
                        ps.executeUpdate();
                    }
                }

                conexion.commit();

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new ResultadoOperacion(false, "No se pudo actualizar el usuario en MySQL: " + e.getMessage());
        }

        cliente.setNombre(nuevoNombre);
        cliente.setActivo(activo);

        if (tarjeta != null && !nuevoPin.isEmpty()) {
            tarjeta.setPin(nuevoPin);
        }

        return new ResultadoOperacion(true, "Usuario actualizado correctamente en MySQL.");
    }

    // esto es lo que usa el boton "Desbloquear" del panel admin
    public ResultadoOperacion desbloquearTarjetaPorIdentificacion(String identificacion) {
        Tarjeta tarjeta = obtenerTarjetaPorIdentificacion(identificacion);

        if (tarjeta == null) {
            return new ResultadoOperacion(false, "No se encontró la tarjeta del usuario.");
        }

        String sql = "UPDATE tarjetas SET bloqueada = FALSE WHERE numero = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, tarjeta.getNumero());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return new ResultadoOperacion(false, "No se pudo desbloquear la tarjeta en MySQL: " + e.getMessage());
        }

        tarjeta.desbloquear();
        return new ResultadoOperacion(true, "Tarjeta desbloqueada correctamente.");
    }

    // este se llama tanto para bloquear (3 pines malos) como para desbloquear desde admin
    public ResultadoOperacion guardarBloqueoTarjeta(String numeroTarjeta, boolean bloqueada) {
        String sql = "UPDATE tarjetas SET bloqueada = ? WHERE numero = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setBoolean(1, bloqueada);
            ps.setString(2, numeroTarjeta);

            int filas = ps.executeUpdate();

            if (filas == 0) {
                return new ResultadoOperacion(false, "No se encontró la tarjeta para actualizar su bloqueo.");
            }

            return new ResultadoOperacion(true, "Estado de bloqueo actualizado.");

        } catch (SQLException e) {
            e.printStackTrace();
            return new ResultadoOperacion(false, "No se pudo guardar el bloqueo de la tarjeta: " + e.getMessage());
        }
    }

    public ResultadoOperacion eliminarUsuario(String identificacion) {
        identificacion = limpiarTexto(identificacion);

        Cliente cliente = buscarClientePorIdentificacion(identificacion);

        if (cliente == null) {
            return new ResultadoOperacion(false, "No se encontró el usuario seleccionado.");
        }

        // hay que borrar las transacciones primero por las FK, si no MySQL tira error de
        // integridad referencial al querer borrar clientes/cuentas que todavia tienen movimientos
        String sqlEliminarTransacciones =
                "DELETE FROM transacciones "
                + "WHERE cuenta_origen IN (SELECT numero FROM cuentas WHERE identificacion_cliente = ?) "
                + "OR cuenta_destino IN (SELECT numero FROM cuentas WHERE identificacion_cliente = ?)";

        // solo se borra de clientes, cuentas y tarjetas se van solas por el ON DELETE CASCADE
        // que tiene la tabla en MySQL (si alguien recrea la BD sin eso, esto va a fallar)
        String sqlEliminarCliente = "DELETE FROM clientes WHERE identificacion = ?";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conexion.prepareStatement(sqlEliminarTransacciones)) {
                    ps.setString(1, identificacion);
                    ps.setString(2, identificacion);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conexion.prepareStatement(sqlEliminarCliente)) {
                    ps.setString(1, identificacion);
                    ps.executeUpdate();
                }

                conexion.commit();

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new ResultadoOperacion(false, "No se pudo eliminar el usuario de MySQL: " + e.getMessage());
        }

        String numeroTarjeta = obtenerNumeroTarjetaPorIdentificacion(identificacion);

        if (numeroTarjeta != null) {
            tarjetas.remove(numeroTarjeta);
            clientesPorTarjeta.remove(numeroTarjeta);
        }

        for (Cuenta cuenta : cliente.obtenerCuentas()) {
            cuentas.remove(cuenta.getNumero());
        }

        return new ResultadoOperacion(true, "Usuario eliminado correctamente de MySQL.");
    }

    public Cliente buscarClientePorIdentificacion(String identificacion) {
        for (Cliente cliente : listarClientes()) {
            if (cliente.getIdentificacion().equalsIgnoreCase(identificacion)) {
                return cliente;
            }
        }

        return null;
    }

    // solo para no repetir el null check en cada validacion de arriba
    private String limpiarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }
}

