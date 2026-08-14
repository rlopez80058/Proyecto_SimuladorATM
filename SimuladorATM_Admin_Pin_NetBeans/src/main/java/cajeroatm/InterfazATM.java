package cajeroatm;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

// Ventana principal del cajero. Usa CardLayout para cambiar entre pantalla de login y menu
// sin abrir ventanas nuevas (como un cajero real que solo tiene una pantalla).
public class InterfazATM extends JFrame {

    private Banco banco;
    private CajeroAutomatico cajero;

    private CardLayout cardLayout;
    private JPanel pantallaATM; // contiene las dos "cards": login y menu

    private JTextField txtTarjeta;
    private JPasswordField txtPin;
    private JTextField campoActivo;

    private JLabel lblBienvenida;
    private JLabel lblEstado;
    private JComboBox<Cuenta> cmbCuentas;
    private JTextArea txtSalida;

    private String pantallaActual = "login";
    private static final String PIN_ADMIN = "1234";

    private final Color COLOR_CARCASA = new Color(42, 47, 50);
    private final Color COLOR_CARCASA_CLARA = new Color(70, 76, 80);
    private final Color COLOR_AZUL = new Color(18, 65, 110);
    private final Color COLOR_VERDE = new Color(0, 145, 88);
    private final Color COLOR_ROJO = new Color(190, 50, 45);
    private final Color COLOR_AMARILLO = new Color(230, 185, 35);
    private final Color COLOR_PANTALLA = new Color(226, 238, 245);
    private final Color COLOR_TEXTO_OSCURO = new Color(20, 40, 70);

    public InterfazATM() {
        banco = new Banco();
        cajero = new CajeroAutomatico(banco);
        configurarVentana();
        construirInterfaz();
    }

    private void configurarVentana() {
        setTitle("Simulador de Cajero Automático - ATM");
        setSize(1050, 720);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
    }

    private void construirInterfaz() {
        JPanel carcasa = new JPanel(new BorderLayout(20, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, COLOR_CARCASA_CLARA,
                        0, getHeight(), COLOR_CARCASA
                );

                g2.setPaint(gp);
                g2.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 30, 30);

                g2.setColor(new Color(20, 22, 24));
                g2.setStroke(new BasicStroke(4));
                g2.drawRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 30, 30);
            }
        };

        carcasa.setBorder(new EmptyBorder(20, 25, 25, 25));
        setContentPane(carcasa);

        carcasa.add(crearEncabezadoBanco(), BorderLayout.NORTH);
        carcasa.add(crearCentroATM(), BorderLayout.CENTER);
        carcasa.add(crearParteInferiorATM(), BorderLayout.SOUTH);
    }

    private JPanel crearEncabezadoBanco() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel marca = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        marca.setBackground(new Color(24, 37, 50));
        marca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(10, 15, 20), 2),
                new EmptyBorder(8, 25, 8, 25)
        ));

        JLabel icono = new JLabel("▦");
        icono.setFont(new Font("Segoe UI Symbol", Font.BOLD, 34));
        icono.setForeground(Color.WHITE);

        JLabel texto = new JLabel(
                "<html><div style='text-align:left;'>"
                + "<span style='font-size:24px; font-weight:bold;'>BANCO SEGURO</span><br>"
                + "<span style='font-size:12px;'>Su confianza, nuestro compromiso</span>"
                + "</div></html>"
        );
        texto.setForeground(Color.WHITE);

        marca.add(icono);
        marca.add(texto);

        header.add(marca, BorderLayout.CENTER);
        return header;
    }

    private JPanel crearCentroATM() {
        JPanel centro = new JPanel(new BorderLayout(18, 0));
        centro.setOpaque(false);

        centro.add(crearPanelBotonesIzquierda(), BorderLayout.WEST);
        centro.add(crearMonitorATM(), BorderLayout.CENTER);
        centro.add(crearPanelBotonesDerecha(), BorderLayout.EAST);

        return centro;
    }

    private JPanel crearMonitorATM() {
        JPanel marcoMonitor = new JPanel(new BorderLayout());
        marcoMonitor.setBackground(new Color(22, 25, 27));
        marcoMonitor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(10, 10, 10), 4),
                new EmptyBorder(12, 12, 12, 12)
        ));

        pantallaATM = new JPanel();
        cardLayout = new CardLayout();
        pantallaATM.setLayout(cardLayout);
        pantallaATM.setBackground(COLOR_PANTALLA);

        // las dos pantallas se crean ambas al inicio, cardLayout solo muestra/oculta, no las recrea
        pantallaATM.add(crearPantallaLogin(), "login");
        pantallaATM.add(crearPantallaMenu(), "menu");

        marcoMonitor.add(pantallaATM, BorderLayout.CENTER);

        cardLayout.show(pantallaATM, "login");
        pantallaActual = "login";

        return marcoMonitor;
    }

    private JPanel crearPantallaLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PANTALLA);
        panel.setBorder(new EmptyBorder(25, 35, 25, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("BIENVENIDO AL CAJERO AUTOMÁTICO", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(COLOR_TEXTO_OSCURO);

        JLabel subtitulo = new JLabel("Ingrese su tarjeta y PIN para continuar", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitulo.setForeground(new Color(40, 55, 70));

        JPanel tarjetaVisual = crearTarjetaVisual();

        JLabel lblTarjeta = new JLabel("Número de tarjeta:");
        lblTarjeta.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTarjeta.setForeground(COLOR_TEXTO_OSCURO);

        txtTarjeta = new JTextField();
        txtTarjeta.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        txtTarjeta.setPreferredSize(new Dimension(320, 38));
        txtTarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(130, 145, 155), 1),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel lblPin = new JLabel("PIN:");
        lblPin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPin.setForeground(COLOR_TEXTO_OSCURO);

        txtPin = new JPasswordField();
        txtPin.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        txtPin.setPreferredSize(new Dimension(320, 38));
        txtPin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(130, 145, 155), 1),
                new EmptyBorder(4, 10, 4, 10)
        ));

        registrarCampoActivo(txtTarjeta);
        registrarCampoActivo(txtPin);
        campoActivo = txtTarjeta;

        SwingUtilities.invokeLater(() -> txtTarjeta.requestFocusInWindow());

        JButton btnIngresar = crearBotonPrincipal("INGRESAR  ›", COLOR_VERDE);
        btnIngresar.addActionListener(e -> iniciarSesion());

        JLabel ayuda = new JLabel(
                "<html><div style='text-align:center;'>"
                + "<b>Tarjetas de prueba:</b> 1111222233334444 / 5555666677778888<br>"
                + "<b>PIN de prueba:</b> 1234 / 4321<br>"
                + "<b>Admin:</b> botón ADMIN USUARIOS al lado derecho"
                + "</div></html>",
                SwingConstants.CENTER
        );
        ayuda.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ayuda.setForeground(COLOR_TEXTO_OSCURO);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titulo, gbc);

        gbc.gridy++;
        panel.add(subtitulo, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 8, 8, 8);

        JPanel zonaFormulario = new JPanel(new GridBagLayout());
        zonaFormulario.setOpaque(false);

        GridBagConstraints f = new GridBagConstraints();
        f.insets = new Insets(8, 12, 8, 12);
        f.fill = GridBagConstraints.HORIZONTAL;

        f.gridx = 0;
        f.gridy = 0;
        f.gridheight = 4;
        zonaFormulario.add(tarjetaVisual, f);

        f.gridheight = 1;
        f.gridx = 1;
        f.gridy = 0;
        zonaFormulario.add(lblTarjeta, f);

        f.gridy = 1;
        zonaFormulario.add(txtTarjeta, f);

        f.gridy = 2;
        zonaFormulario.add(lblPin, f);

        f.gridy = 3;
        zonaFormulario.add(txtPin, f);

        panel.add(zonaFormulario, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(15, 8, 8, 8);
        panel.add(ayuda, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(btnIngresar, gbc);

        return panel;
    }

    private JPanel crearPantallaMenu() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(COLOR_PANTALLA);
        panel.setBorder(new EmptyBorder(22, 28, 22, 28));

        lblBienvenida = new JLabel("Bienvenido(a)", SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblBienvenida.setForeground(COLOR_TEXTO_OSCURO);

        lblEstado = new JLabel("Seleccione una cuenta y luego elija una operación.", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblEstado.setForeground(new Color(55, 70, 85));

        JPanel encabezado = new JPanel(new GridLayout(2, 1, 4, 4));
        encabezado.setOpaque(false);
        encabezado.add(lblBienvenida);
        encabezado.add(lblEstado);

        JPanel centro = new JPanel(new BorderLayout(12, 12));
        centro.setOpaque(false);

        JPanel selectorCuenta = new JPanel(new BorderLayout(8, 8));
        selectorCuenta.setOpaque(false);

        JLabel lblCuenta = new JLabel("Cuenta activa:");
        lblCuenta.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCuenta.setForeground(COLOR_TEXTO_OSCURO);

        cmbCuentas = new JComboBox<>();
        cmbCuentas.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbCuentas.setPreferredSize(new Dimension(400, 38));

        selectorCuenta.add(lblCuenta, BorderLayout.NORTH);
        selectorCuenta.add(cmbCuentas, BorderLayout.CENTER);

        txtSalida = new JTextArea();
        txtSalida.setEditable(false);
        txtSalida.setLineWrap(true);
        txtSalida.setWrapStyleWord(true);
        txtSalida.setFont(new Font("Consolas", Font.PLAIN, 15));
        txtSalida.setForeground(new Color(10, 40, 65));
        txtSalida.setBackground(new Color(245, 250, 252));
        txtSalida.setBorder(new EmptyBorder(15, 15, 15, 15));
        txtSalida.setText("Sistema listo.\n\nUse los botones laterales para consultar saldo, retirar, depositar, transferir o revisar el historial.");

        JScrollPane scroll = new JScrollPane(txtSalida);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(120, 145, 160), 1));

        centro.add(selectorCuenta, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelBotonesIzquierda() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 12, 18));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(185, 0));

        panel.add(crearBotonLateral("▤  CONSULTAR\nSALDO", e -> consultarSaldo()));
        panel.add(crearBotonLateral("▣  RETIRAR\nEFECTIVO", e -> retirar()));
        panel.add(crearBotonLateral("▥  DEPOSITAR", e -> depositar()));
        panel.add(crearBotonLateral("⇄  TRANSFERIR", e -> transferir()));

        return panel;
    }

    private JPanel crearPanelBotonesDerecha() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 12));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(185, 0));

        panel.add(crearBotonLateral("▦  HISTORIAL\nMOVIMIENTOS", e -> verHistorial()));
        panel.add(crearBotonLateral("⌫  LIMPIAR", e -> limpiarCampos()));
        panel.add(crearBotonLateral("⚙  ADMIN\nUSUARIOS", e -> abrirAdminUsuarios()));
        panel.add(crearBotonLateral("ⓘ  AYUDA", e -> mostrarAyuda()));
        panel.add(crearBotonLateral("↪  SALIR", e -> cerrarSesion()));

        return panel;
    }

    private JPanel crearParteInferiorATM() {
        JPanel inferior = new JPanel(new BorderLayout(25, 0));
        inferior.setOpaque(false);
        inferior.setPreferredSize(new Dimension(0, 170));

        inferior.add(crearRanuraTarjeta(), BorderLayout.WEST);
        inferior.add(crearTecladoNumerico(), BorderLayout.CENTER);
        inferior.add(crearRanuraComprobante(), BorderLayout.EAST);

        return inferior;
    }

    private JPanel crearRanuraTarjeta() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(230, 150));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel label = new JLabel("INSERTE SU TARJETA", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel ranura = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(15, 18, 20));
                g2.fillRoundRect(8, 18, getWidth() - 16, 32, 12, 12);

                g2.setColor(new Color(35, 255, 80));
                g2.fillRoundRect(25, 31, getWidth() - 50, 6, 6, 6);
            }
        };

        ranura.setOpaque(false);
        ranura.setPreferredSize(new Dimension(170, 70));

        panel.add(label, BorderLayout.NORTH);
        panel.add(ranura, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearRanuraComprobante() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(230, 150));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel label = new JLabel("RETIRE SU COMPROBANTE", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel ranura = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(15, 18, 20));
                g2.fillRoundRect(8, 18, getWidth() - 16, 32, 12, 12);

                g2.setColor(new Color(120, 125, 130));
                g2.fillRect(45, 55, getWidth() - 90, 35);

                g2.setColor(Color.WHITE);
                g2.drawLine(58, 68, getWidth() - 58, 68);
                g2.drawLine(58, 78, getWidth() - 80, 78);
            }
        };

        ranura.setOpaque(false);
        ranura.setPreferredSize(new Dimension(170, 90));

        panel.add(label, BorderLayout.NORTH);
        panel.add(ranura, BorderLayout.CENTER);

        return panel;
    }

    // teclado numerico simulado en pantalla, escribe sobre el campo que tenga el foco (campoActivo)
    // los botones vacios de en medio son solo para que quede parecido a un cajero real (0-9)
    private JPanel crearTecladoNumerico() {
        JPanel contenedorTeclado = new JPanel(new BorderLayout(12, 0));
        contenedorTeclado.setOpaque(false);
        contenedorTeclado.setBorder(new EmptyBorder(10, 60, 10, 60));

        JPanel numeros = new JPanel(new GridLayout(4, 3, 10, 10));
        numeros.setOpaque(false);

        for (int i = 1; i <= 9; i++) {
            String numero = String.valueOf(i);
            numeros.add(crearBotonTeclado(numero, e -> escribirNumero(numero)));
        }

        numeros.add(crearBotonTeclado("", null));
        numeros.add(crearBotonTeclado("0", e -> escribirNumero("0")));
        numeros.add(crearBotonTeclado("", null));

        JPanel acciones = new JPanel(new GridLayout(3, 1, 10, 10));
        acciones.setOpaque(false);

        acciones.add(crearBotonAccionTeclado("CANCELAR  X", COLOR_ROJO, e -> limpiarCampos()));
        acciones.add(crearBotonAccionTeclado("CORREGIR  ←", COLOR_AMARILLO, e -> borrarUltimo()));
        acciones.add(crearBotonAccionTeclado("ACEPTAR  ○", COLOR_VERDE, e -> aceptarTeclado()));

        contenedorTeclado.add(numeros, BorderLayout.CENTER);
        contenedorTeclado.add(acciones, BorderLayout.EAST);

        return contenedorTeclado;
    }

    private JPanel crearTarjetaVisual() {
        JPanel tarjeta = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 55, 95));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

                g2.setColor(new Color(220, 235, 245));
                g2.fillRoundRect(16, 22, 55, 36, 8, 8);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString("ATM CARD", 16, 90);

                g2.setStroke(new BasicStroke(4));
                g2.drawLine(18, 110, 55, 110);
                g2.drawLine(72, 110, 110, 110);
                g2.drawLine(128, 110, 166, 110);
            }
        };

        tarjeta.setOpaque(false);
        tarjeta.setPreferredSize(new Dimension(190, 130));

        return tarjeta;
    }

    private JButton crearBotonLateral(String texto, ActionListener accion) {
        JButton boton = new JButton("<html><center>" + texto.replace("\n", "<br>") + "</center></html>");
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(COLOR_AZUL);
        boton.setFocusPainted(false);
        boton.setFocusable(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(5, 25, 45), 2),
                new EmptyBorder(12, 10, 12, 10)
        ));

        if (accion != null) {
            boton.addActionListener(accion);
        }

        return boton;
    }

    private JButton crearBotonPrincipal(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(300, 46));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        boton.setForeground(Color.WHITE);
        boton.setBackground(color);
        boton.setFocusPainted(false);
        boton.setFocusable(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                new EmptyBorder(8, 20, 8, 20)
        ));

        return boton;
    }

    private JButton crearBotonTeclado(String texto, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 22));
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(38, 42, 45));
        boton.setFocusPainted(false);
        boton.setFocusable(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createLineBorder(new Color(10, 10, 10), 2));

        if (texto == null || texto.trim().isEmpty()) {
            boton.setEnabled(false);
            boton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            boton.setBackground(new Color(30, 32, 34));
        }

        if (accion != null) {
            boton.addActionListener(accion);
        }

        return boton;
    }

    private JButton crearBotonAccionTeclado(String texto, Color color, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(160, 42));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        if (color.equals(COLOR_AMARILLO)) {
            boton.setForeground(new Color(40, 40, 40));
        } else {
            boton.setForeground(Color.WHITE);
        }

        boton.setBackground(color);
        boton.setFocusPainted(false);
        boton.setFocusable(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createLineBorder(color.darker(), 2));

        if (accion != null) {
            boton.addActionListener(accion);
        }

        return boton;
    }

    // se llama tanto desde el boton INGRESAR como desde el teclado numerico (ACEPTAR en login)
    private void iniciarSesion() {
        String tarjeta = txtTarjeta.getText().trim();
        String pin = new String(txtPin.getPassword()).trim();

        if (tarjeta.isEmpty() || pin.isEmpty()) {
            mostrarMensaje("Debe ingresar el número de tarjeta y el PIN.");
            return;
        }

        ResultadoOperacion resultado = cajero.iniciarSesion(tarjeta, pin);
        mostrarMensaje(resultado.getMensaje());

        if (resultado.isExito()) {
            lblBienvenida.setText("Bienvenido(a), " + cajero.getClienteActual().getNombre());

            // llena el combo con las cuentas del cliente que acaba de entrar
            cmbCuentas.setModel(new DefaultComboBoxModel<>(
                    cajero.obtenerCuentasCliente().toArray(new Cuenta[0])
            ));

            if (cmbCuentas.getItemCount() > 0) {
                cmbCuentas.setSelectedIndex(0);
                obtenerCuentaSeleccionada(); // deja la primera cuenta ya seleccionada en el cajero
            }

            txtSalida.setText(
                    "Inicio de sesión exitoso.\n\n"
                    + "Seleccione una cuenta y elija una operación desde los botones laterales.\n\n"
                    + "Operaciones disponibles:\n"
                    + "• Consultar saldo\n"
                    + "• Retirar efectivo\n"
                    + "• Depositar dinero\n"
                    + "• Transferir fondos\n"
                    + "• Ver historial de movimientos"
            );

            cardLayout.show(pantallaATM, "menu");
            pantallaActual = "menu";
        }
    }

    // chequeo rapido que se repite antes de cada operacion (retirar, depositar, etc)
    private boolean validarSesionActiva() {
        if (!"menu".equals(pantallaActual) || cmbCuentas == null || cmbCuentas.getSelectedItem() == null) {
            mostrarMensaje("Primero debe iniciar sesión y seleccionar una cuenta.");
            return false;
        }

        return true;
    }

    // toma lo que esta elegido en el combo y se lo pasa al objeto CajeroAutomatico
    private Cuenta obtenerCuentaSeleccionada() {
        Cuenta cuenta = (Cuenta) cmbCuentas.getSelectedItem();

        if (cuenta != null) {
            cajero.seleccionarCuenta(cuenta);
        }

        return cuenta;
    }

    private void consultarSaldo() {
        if (!validarSesionActiva()) {
            return;
        }

        Cuenta cuenta = obtenerCuentaSeleccionada();

        String mensaje = "CONSULTA DE SALDO\n"
                + "-----------------------------\n"
                + "Cuenta: " + cuenta.getNumero() + "\n"
                + "Tipo: " + cuenta.getTipo() + "\n"
                + "Saldo disponible: ₡" + String.format("%,.2f", cajero.consultarSaldo());

        txtSalida.setText(mensaje);
        lblEstado.setText("Saldo consultado correctamente.");
        cmbCuentas.repaint();
    }

    private void retirar() {
        if (!validarSesionActiva()) {
            return;
        }

        obtenerCuentaSeleccionada();

        Double monto = pedirMonto("Ingrese el monto a retirar:");

        if (monto == null) {
            return;
        }

        ResultadoOperacion resultado = cajero.retirar(monto);

        txtSalida.setText(resultado.getMensaje());
        lblEstado.setText(resultado.isExito() ? "Retiro procesado." : "Retiro rechazado.");
        cmbCuentas.repaint();
    }

    private void depositar() {
        if (!validarSesionActiva()) {
            return;
        }

        obtenerCuentaSeleccionada();

        Double monto = pedirMonto("Ingrese el monto a depositar:");

        if (monto == null) {
            return;
        }

        ResultadoOperacion resultado = cajero.depositar(monto);

        txtSalida.setText(resultado.getMensaje());
        lblEstado.setText(resultado.isExito() ? "Depósito procesado." : "Depósito rechazado.");
        cmbCuentas.repaint();
    }

    private void transferir() {
        if (!validarSesionActiva()) {
            return;
        }

        obtenerCuentaSeleccionada();

        String destino = JOptionPane.showInputDialog(
                this,
                "Ingrese la cuenta destino:",
                "Transferencia",
                JOptionPane.QUESTION_MESSAGE
        );

        if (destino == null || destino.trim().isEmpty()) {
            return;
        }

        Double monto = pedirMonto("Ingrese el monto a transferir:");

        if (monto == null) {
            return;
        }

        ResultadoOperacion resultado = cajero.transferir(destino.trim(), monto);

        txtSalida.setText(resultado.getMensaje());
        lblEstado.setText(resultado.isExito() ? "Transferencia procesada." : "Transferencia rechazada.");
        cmbCuentas.repaint();
    }

    private void verHistorial() {
        if (!validarSesionActiva()) {
            return;
        }

        Cuenta cuenta = obtenerCuentaSeleccionada();

        StringBuilder sb = new StringBuilder();

        sb.append("HISTORIAL DE MOVIMIENTOS\n");
        sb.append("-----------------------------\n");
        sb.append("Cuenta: ").append(cuenta.getNumero()).append("\n\n");

        if (cuenta.getHistorial().isEmpty()) {
            sb.append("No hay movimientos registrados.");
        } else {
            for (Transaccion t : cuenta.getHistorial()) {
                sb.append(t.toString()).append("\n");
            }
        }

        txtSalida.setText(sb.toString());
        lblEstado.setText("Historial actualizado.");
    }

    // popup generico para pedir un monto, se usa en retirar/depositar/transferir
    // devuelve null si el usuario cancelo o si el numero no era valido (asi el que llama
    // solo necesita revisar "if (monto == null) return;")
    private Double pedirMonto(String mensaje) {
        String texto = JOptionPane.showInputDialog(
                this,
                mensaje,
                "Monto de operación",
                JOptionPane.QUESTION_MESSAGE
        );

        if (texto == null) {
            return null;
        }

        try {
            double monto = Double.parseDouble(texto.trim());

            if (monto <= 0) {
                mostrarMensaje("El monto debe ser mayor a cero.");
                return null;
            }

            return monto;
        } catch (NumberFormatException ex) {
            mostrarMensaje("Debe ingresar un número válido.");
            return null;
        }
    }

    // este metodo hace dos cosas distintas dependiendo de en que pantalla este:
    // si esta en el menu, cierra sesion y vuelve al login. Si esta en el login, pregunta si quiere salir del programa.
    // (es el mismo boton "SALIR" en las dos pantallas, por eso el if)
    private void cerrarSesion() {
        if ("menu".equals(pantallaActual)) {
            cajero.cerrarSesion();

            txtPin.setText("");
            txtTarjeta.setText("");
            campoActivo = txtTarjeta;

            if (txtSalida != null) {
                txtSalida.setText("");
            }

            cardLayout.show(pantallaATM, "login");
            pantallaActual = "login";

            SwingUtilities.invokeLater(() -> txtTarjeta.requestFocusInWindow());

            mostrarMensaje("Sesión cerrada correctamente.");
        } else {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea salir del simulador ATM?",
                    "Salir",
                    JOptionPane.YES_NO_OPTION
            );

            if (opcion == JOptionPane.YES_OPTION) {
                dispose();
            }
        }
    }

    private void limpiarCampos() {
        if ("login".equals(pantallaActual)) {
            txtTarjeta.setText("");
            txtPin.setText("");
            campoActivo = txtTarjeta;
            SwingUtilities.invokeLater(() -> txtTarjeta.requestFocusInWindow());
        } else if (txtSalida != null) {
            txtSalida.setText("Pantalla limpia.\n\nSeleccione una operación.");
            lblEstado.setText("Pantalla limpia.");
        }
    }

    private void mostrarAyuda() {
        String ayuda = "SIMULADOR ATM\n\n"
                + "Datos de prueba:\n"
                + "Tarjeta 1: 1111222233334444 | PIN: 1234\n"
                + "Tarjeta 2: 5555666677778888 | PIN: 4321\n\n"
                + "Cuentas disponibles para transferencias:\n"
                + "1001, 1002, 2001\n\n"
                + "Módulo admin:\n"
                + "Use ADMIN USUARIOS para crear, editar, eliminar usuarios y desbloquear tarjetas.\n"
                + "PIN admin: 1234";

        if ("menu".equals(pantallaActual) && txtSalida != null) {
            txtSalida.setText(ayuda);
        } else {
            mostrarMensaje(ayuda);
        }
    }

    // pide el pin de admin (hardcodeado arriba en PIN_ADMIN) antes de dejar entrar al panel de gestion
    private void abrirAdminUsuarios() {
        JPasswordField campoPinAdmin = new JPasswordField();
        campoPinAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JLabel("Ingrese el PIN de administrador:"), BorderLayout.NORTH);
        panel.add(campoPinAdmin, BorderLayout.CENTER);

        int opcion = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Acceso administrador",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        String pinIngresado = new String(campoPinAdmin.getPassword()).trim();

        if (!pinIngresado.matches("\\d{4}")) {
            mostrarMensaje("El PIN de administrador debe tener exactamente 4 dígitos.");
            return;
        }

        if (!PIN_ADMIN.equals(pinIngresado)) {
            mostrarMensaje("PIN de administrador incorrecto. Acceso denegado.");
            return;
        }

        AdminUsuariosFrame admin = new AdminUsuariosFrame(this, banco);
        admin.setVisible(true);
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Simulador ATM",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void registrarCampoActivo(JTextField campo) {
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                campoActivo = campo;
            }
        });
    }

    // el teclado en pantalla solo funciona en login (tarjeta/pin), en el menu no hace nada
    private void escribirNumero(String numero) {
        if (!"login".equals(pantallaActual)) {
            return;
        }

        if (campoActivo == null) {
            campoActivo = txtTarjeta;
        }

        campoActivo.requestFocusInWindow();
        campoActivo.replaceSelection(numero);
    }

    private void borrarUltimo() {
        if (!"login".equals(pantallaActual)) {
            return;
        }

        if (campoActivo == null) {
            campoActivo = txtTarjeta;
        }

        String texto = campoActivo.getText();

        if (!texto.isEmpty()) {
            campoActivo.setText(texto.substring(0, texto.length() - 1));
        }

        campoActivo.requestFocusInWindow();
    }

    // el boton ACEPTAR del teclado cambia de funcion segun la pantalla:
    // en login hace login, en el menu consulta el saldo (para que siempre haga algo util)
    private void aceptarTeclado() {
        if ("login".equals(pantallaActual)) {
            iniciarSesion();
        } else {
            consultarSaldo();
        }
    }
}
