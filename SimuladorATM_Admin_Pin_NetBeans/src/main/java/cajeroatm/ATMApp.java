package cajeroatm;

import javax.swing.SwingUtilities;

public class ATMApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazATM ventana = new InterfazATM();
            ventana.setVisible(true);
        });
    }
}
