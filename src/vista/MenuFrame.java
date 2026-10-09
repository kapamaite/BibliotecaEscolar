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
        setSize(1000, 650);
        setLocationRelativeTo(null);

        LibroPanel libroPanel = new LibroPanel(usuario);
        PrestamoPanel prestamoPanel = new PrestamoPanel(usuario);
        ReportePanel reportePanel = usuario.esBibliotecario() ? new ReportePanel() : null;

        // Pestañas: el bibliotecario ve todo; el estudiante solo libros y préstamos
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Libros", libroPanel);
        if (usuario.esBibliotecario()) {
            pestanas.addTab("Estudiantes", new EstudiantePanel());
        }
        pestanas.addTab("Préstamos y devoluciones", prestamoPanel);
        if (usuario.esBibliotecario()) {
            pestanas.addTab("Reportes", reportePanel);
        }

        // Al entrar a una pestaña, se actualizan sus datos
        pestanas.addChangeListener(e -> {
            Component seleccionado = pestanas.getSelectedComponent();
            if (seleccionado == libroPanel) libroPanel.recargar();
            if (seleccionado == prestamoPanel) prestamoPanel.recargar();
            if (reportePanel != null && seleccionado == reportePanel) reportePanel.recargar();
        });

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

    public Usuario getUsuario() {
        return usuario;
    }
}