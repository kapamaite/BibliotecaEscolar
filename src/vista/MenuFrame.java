package vista;

import modelo.Usuario;

import javax.swing.*;
import java.awt.*;

public class MenuFrame extends JFrame {

    private final Usuario usuario;

    public MenuFrame(Usuario usuario) {
        this.usuario = usuario;

        setTitle("Biblioteca Escolar - " + usuario.getNombre() + " (" + usuario.getRol() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Pestañas: el bibliotecario ve todo; el estudiante solo libros y préstamos
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Libros", crearPanelTemporal("Gestión de libros"));
        if (usuario.esBibliotecario()) {
            pestanas.addTab("Estudiantes", crearPanelTemporal("Gestión de estudiantes"));
        }
        pestanas.addTab("Préstamos y devoluciones", crearPanelTemporal("Préstamos y devoluciones"));
        if (usuario.esBibliotecario()) {
            pestanas.addTab("Reportes", crearPanelTemporal("Reportes"));
        }

        // Barra superior con el usuario y el botón de cerrar sesión
        JButton btnSalir = new JButton("Cerrar sesión");
        btnSalir.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barra.add(new JLabel("Sesión: " + usuario.getNombre()));
        barra.add(btnSalir);

        add(barra, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
    }

    public Usuario getUsuario() { return usuario; }

    private JPanel crearPanelTemporal(String texto) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(texto + " (en construcción)", SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }
}