package cajeroatm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class AdminUsuariosFrame extends JFrame {

    private Banco banco;

    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    private JTextField txtIdentificacion;
    private JTextField txtNombre;
    private JTextField txtTarjeta;
    private JPasswordField txtPin;
    private JTextField txtCuenta;
    private JComboBox<String> cmbTipoCuenta;
    private JTextField txtSaldo;
    private JCheckBox chkActivo;

    private final Color AZUL = new Color(18, 65, 110);
    private final Color VERDE = new Color(0, 145, 88);
    private final Color ROJO = new Color(190, 50, 45);
    private final Color FONDO = new Color(235, 241, 245);

    public AdminUsuariosFrame(JFrame parent, Banco banco) {
        this.banco = banco;
        configurarVentana(parent);
        construirInterfaz();
        cargarTabla();
    }

    private void configurarVentana(JFrame parent) {
        setTitle("Módulo Administrador - Usuarios ATM");
        setSize(950, 620);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
    }

    private void construirInterfaz() {
        JPanel principal = new JPanel(new BorderLayout(15, 15));
        principal.setBackground(FONDO);
        principal.setBorder(new EmptyBorder(18, 18, 18, 18));
        setContentPane(principal);

        JLabel titulo = new JLabel("ADMINISTRACIÓN DE USUARIOS", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(AZUL);
        principal.add(titulo, BorderLayout.NORTH);

        principal.add(crearPanelFormulario(), BorderLayout.WEST);
        principal.add(crearPanelTabla(), BorderLayout.CENTER);
        principal.add(crearPanelBotones(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(330, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 205, 215), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        txtIdentificacion = crearCampoTexto();
        txtNombre = crearCampoTexto();
        txtTarjeta = crearCampoTexto();

        txtPin = new JPasswordField();
        txtPin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPin.setPreferredSize(new Dimension(240, 34));

        txtCuenta = crearCampoTexto();
        cmbTipoCuenta = new JComboBox<>(new String[]{"Ahorros", "Corriente"});
        cmbTipoCuenta.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        txtSaldo = crearCampoTexto();

        chkActivo = new JCheckBox("Usuario activo");
        chkActivo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkActivo.setBackground(Color.WHITE);
        chkActivo.setSelected(true);

        agregarCampo(panel, gbc, "Identificación:", txtIdentificacion, 0);
        agregarCampo(panel, gbc, "Nombre completo:", txtNombre, 2);
        agregarCampo(panel, gbc, "Número de tarjeta:", txtTarjeta, 4);
        agregarCampo(panel, gbc, "PIN:", txtPin, 6);
        agregarCampo(panel, gbc, "Número de cuenta:", txtCuenta, 8);
        agregarCampo(panel, gbc, "Tipo de cuenta:", cmbTipoCuenta, 10);
        agregarCampo(panel, gbc, "Saldo inicial:", txtSaldo, 12);

        gbc.gridy = 14;
        panel.add(chkActivo, gbc);

        JLabel nota = new JLabel("<html><b>Nota:</b> Para editar, seleccione un usuario de la tabla. Se edita nombre, PIN y estado. La cuenta y tarjeta no se modifican desde edición.</html>");
        nota.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nota.setForeground(new Color(80, 80, 80));

        gbc.gridy = 15;
        gbc.insets = new Insets(18, 6, 6, 6);
        panel.add(nota, gbc);

        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 205, 215), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        modeloTabla = new DefaultTableModel(
                new Object[]{"Identificación", "Nombre", "Tarjeta", "Cuentas", "Activo", "Bloqueada"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaUsuarios.setRowHeight(28);
        tablaUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaUsuarios.setSelectionBackground(new Color(200, 225, 245));
        tablaUsuarios.setSelectionForeground(Color.BLACK);

        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarUsuarioSeleccionado();
            }
        });

        JScrollPane scroll = new JScrollPane(tablaUsuarios);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 10, 10));
        panel.setOpaque(false);

        panel.add(crearBoton("Nuevo", AZUL, e -> limpiarFormulario()));
        panel.add(crearBoton("Crear", VERDE, e -> crearUsuario()));
        panel.add(crearBoton("Editar", AZUL, e -> editarUsuario()));
        panel.add(crearBoton("Eliminar", ROJO, e -> eliminarUsuario()));
        panel.add(crearBoton("Desbloquear", new Color(120, 80, 160), e -> desbloquearTarjeta()));
        panel.add(crearBoton("Cerrar", new Color(80, 80, 80), e -> dispose()));

        return panel;
    }

    private JTextField crearCampoTexto() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setPreferredSize(new Dimension(240, 34));
        return campo;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, String etiqueta, java.awt.Component campo, int fila) {
        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(30, 50, 70));

        gbc.gridy = fila;
        panel.add(label, gbc);

        gbc.gridy = fila + 1;
        panel.add(campo, gbc);
    }

    private JButton crearBoton(String texto, Color color, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(color);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        boton.addActionListener(accion);
        return boton;
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);

        for (Cliente cliente : banco.listarClientes()) {
            String numeroTarjeta = banco.obtenerNumeroTarjetaPorIdentificacion(cliente.getIdentificacion());
            Tarjeta tarjeta = banco.obtenerTarjetaPorIdentificacion(cliente.getIdentificacion());

            StringBuilder cuentas = new StringBuilder();
            for (Cuenta cuenta : cliente.obtenerCuentas()) {
                if (cuentas.length() > 0) {
                    cuentas.append(", ");
                }

                cuentas.append(cuenta.getNumero())
                        .append(" (")
                        .append(cuenta.getTipo())
                        .append(")");
            }

            modeloTabla.addRow(new Object[]{
                cliente.getIdentificacion(),
                cliente.getNombre(),
                numeroTarjeta,
                cuentas.toString(),
                cliente.estaActivo() ? "Sí" : "No",
                tarjeta != null && tarjeta.estaBloqueada() ? "Sí" : "No"
            });
        }
    }

    private void cargarUsuarioSeleccionado() {
        int fila = tablaUsuarios.getSelectedRow();

        if (fila < 0) {
            return;
        }

        txtIdentificacion.setText(modeloTabla.getValueAt(fila, 0).toString());
        txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
        txtTarjeta.setText(modeloTabla.getValueAt(fila, 2).toString());
        txtPin.setText("");
        chkActivo.setSelected("Sí".equals(modeloTabla.getValueAt(fila, 4).toString()));

        txtIdentificacion.setEditable(false);
        txtTarjeta.setEditable(false);
        txtCuenta.setEditable(false);
        txtSaldo.setEditable(false);
        txtCuenta.setText("No editable");
        txtSaldo.setText("No editable");
    }

    private void crearUsuario() {
        Double saldo = obtenerSaldo();

        if (saldo == null) {
            return;
        }

        ResultadoOperacion resultado = banco.crearUsuario(
                txtIdentificacion.getText(),
                txtNombre.getText(),
                txtTarjeta.getText(),
                new String(txtPin.getPassword()),
                txtCuenta.getText(),
                cmbTipoCuenta.getSelectedItem().toString(),
                saldo
        );

        mostrarResultado(resultado);

        if (resultado.isExito()) {
            limpiarFormulario();
            cargarTabla();
        }
    }

    private void editarUsuario() {
        if (txtIdentificacion.getText().trim().isEmpty()) {
            mostrarMensaje("Seleccione un usuario de la tabla para editar.");
            return;
        }

        ResultadoOperacion resultado = banco.actualizarUsuario(
                txtIdentificacion.getText(),
                txtNombre.getText(),
                new String(txtPin.getPassword()),
                chkActivo.isSelected()
        );

        mostrarResultado(resultado);

        if (resultado.isExito()) {
            limpiarFormulario();
            cargarTabla();
        }
    }

    private void eliminarUsuario() {
        if (txtIdentificacion.getText().trim().isEmpty()) {
            mostrarMensaje("Seleccione un usuario de la tabla para eliminar.");
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que desea eliminar este usuario?\nEsta acción también eliminará sus tarjetas y cuentas.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        ResultadoOperacion resultado = banco.eliminarUsuario(txtIdentificacion.getText());
        mostrarResultado(resultado);

        if (resultado.isExito()) {
            limpiarFormulario();
            cargarTabla();
        }
    }

    private void desbloquearTarjeta() {
        if (txtIdentificacion.getText().trim().isEmpty()) {
            mostrarMensaje("Seleccione un usuario de la tabla para desbloquear la tarjeta.");
            return;
        }

        ResultadoOperacion resultado = banco.desbloquearTarjetaPorIdentificacion(txtIdentificacion.getText());
        mostrarResultado(resultado);

        if (resultado.isExito()) {
            cargarTabla();
        }
    }

    private Double obtenerSaldo() {
        try {
            String texto = txtSaldo.getText().trim();

            if (texto.isEmpty()) {
                mostrarMensaje("Debe ingresar el saldo inicial.");
                return null;
            }

            double saldo = Double.parseDouble(texto);

            if (saldo < 0) {
                mostrarMensaje("El saldo no puede ser negativo.");
                return null;
            }

            return saldo;
        } catch (NumberFormatException ex) {
            mostrarMensaje("El saldo debe ser un número válido.");
            return null;
        }
    }

    private void limpiarFormulario() {
        txtIdentificacion.setText("");
        txtNombre.setText("");
        txtTarjeta.setText("");
        txtPin.setText("");
        txtCuenta.setText("");
        txtSaldo.setText("");
        cmbTipoCuenta.setSelectedIndex(0);
        chkActivo.setSelected(true);

        txtIdentificacion.setEditable(true);
        txtTarjeta.setEditable(true);
        txtCuenta.setEditable(true);
        txtSaldo.setEditable(true);

        tablaUsuarios.clearSelection();
        txtIdentificacion.requestFocusInWindow();
    }

    private void mostrarResultado(ResultadoOperacion resultado) {
        mostrarMensaje(resultado.getMensaje());
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Administrador", JOptionPane.INFORMATION_MESSAGE);
    }
}
