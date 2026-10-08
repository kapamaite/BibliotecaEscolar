import vista.LoginFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Swing debe iniciarse en su propio hilo de eventos
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}