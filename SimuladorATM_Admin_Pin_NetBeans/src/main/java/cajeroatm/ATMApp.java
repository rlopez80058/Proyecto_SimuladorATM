package cajeroatm;

import javax.swing.SwingUtilities;

// Punto de entrada del programa, solo levanta la ventana principal
public class ATMApp {
    public static void main(String[] args) {
        // invokeLater para que la interfaz corra en el hilo de Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            InterfazATM ventana = new InterfazATM();
            ventana.setVisible(true);
        });
    }
}
