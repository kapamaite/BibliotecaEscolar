package vista;

import controlador.AuthController;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField txtCorreo = new JTextField(20);
    private final JPasswordField txtClave = new JPasswordField(20);
    private final AuthController controlador = new AuthController();

    public LoginFrame() {
        setTitle("Biblioteca Escolar - Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 240);
        setLocationRelativeTo(null);   // centra la ventana
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("Biblioteca Escolar", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0; panel.add(new JLabel("Correo:"), gbc);
        gbc.gridx = 1;                panel.add(txtCorreo, gbc);
        gbc.gridy = 2; gbc.gridx = 0; panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;                panel.add(txtClave, gbc);

        JButton btnIngresar = new JButton("Ingresar");
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnIngresar, gbc);

        add(panel);

        btnIngresar.addActionListener(e -> iniciarSesion());
        getRootPane().setDefaultButton(btnIngresar);   // Enter también ingresa
    }

    private void iniciarSesion() {
        String correo = txtCorreo.getText();
        String clave = new String(txtClave.getPassword());

        if (correo.isBlank() || clave.isBlank()) {
            JOptionPane.showMessageDialog(this, "Ingresa correo y contraseña.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario u = controlador.iniciarSesion(correo, clave);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Correo o contraseña incorrectos.",
                        "Error de acceso", JOptionPane.ERROR_MESSAGE);
                txtClave.setText("");
            } else {
                new MenuFrame(u).setVisible(true);   // abre el menú con el usuario
                dispose();                           // cierra el login
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar a la base de datos:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}