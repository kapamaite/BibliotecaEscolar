package vista;

import controlador.ReporteController;
import modelo.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ReportePanel extends JPanel {

    private final ReporteController controlador = new ReporteController();

    private final DefaultTableModel modeloMasPrestados =
            nuevoModelo("Título", "Autor", "Veces prestado");
    private final DefaultTableModel modeloHistorial =
            nuevoModelo("Libro", "Fecha préstamo", "Vence", "Estado");
    private final DefaultTableModel modeloEnPrestamo =
            nuevoModelo("Libro", "Estudiante", "Curso", "Fecha préstamo", "Vence", "Estado");

    private final JComboBox<Estudiante> cmbEstudiante = new JComboBox<>();

    public ReportePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane subPestanas = new JTabbedPane();
        subPestanas.addTab("Libros más prestados", crearPestanaMasPrestados());
        subPestanas.addTab("Historial por estudiante", crearPestanaHistorial());
        subPestanas.addTab("Libros en préstamo", crearPestanaEnPrestamo());
        add(subPestanas, BorderLayout.CENTER);

        recargar();
    }

    // Vuelve a consultar los tres reportes
    public void recargar() {
        try {
            cargarEstudiantes();
            llenar(modeloMasPrestados, controlador.librosMasPrestados());
            llenar(modeloEnPrestamo, controlador.librosEnPrestamo());
            cargarHistorial();
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    // ---------- Construcción de las sub-pestañas ----------

    private JPanel crearPestanaMasPrestados() {
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> recargar());

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arriba.add(new JLabel("Top 10 de libros con más préstamos registrados."));
        arriba.add(btnActualizar);
        return panelConTabla(arriba, modeloMasPrestados);
    }

    private JPanel crearPestanaHistorial() {
        JButton btnConsultar = new JButton("Consultar");
        btnConsultar.addActionListener(e -> {
            try {
                cargarHistorial();
            } catch (SQLException ex) {
                mostrarErrorBD(ex);
            }
        });
        cmbEstudiante.setPreferredSize(new Dimension(260, 26));

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arriba.add(new JLabel("Estudiante:"));
        arriba.add(cmbEstudiante);
        arriba.add(btnConsultar);
        return panelConTabla(arriba, modeloHistorial);
    }

    private JPanel crearPestanaEnPrestamo() {
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> recargar());

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arriba.add(new JLabel("Libros que aún no han sido devueltos."));
        arriba.add(btnActualizar);
        return panelConTabla(arriba, modeloEnPrestamo);
    }

    // ---------- Carga de datos ----------

    private void cargarEstudiantes() throws SQLException {
        Estudiante seleccionado = (Estudiante) cmbEstudiante.getSelectedItem();
        cmbEstudiante.removeAllItems();
        for (Estudiante e : controlador.listarEstudiantes()) {
            cmbEstudiante.addItem(e);
            if (seleccionado != null && e.getId() == seleccionado.getId()) {
                cmbEstudiante.setSelectedItem(e);   // mantiene el estudiante que se estaba mirando
            }
        }
    }

    private void cargarHistorial() throws SQLException {
        Estudiante e = (Estudiante) cmbEstudiante.getSelectedItem();
        if (e == null) {
            modeloHistorial.setRowCount(0);
            return;
        }
        llenar(modeloHistorial, controlador.historialPorEstudiante(e.getId()));
    }

    private void llenar(DefaultTableModel modelo, List<Object[]> filas) {
        modelo.setRowCount(0);
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
    }

    // ---------- Utilidades ----------

    private static DefaultTableModel nuevoModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private JPanel panelConTabla(JPanel arriba, DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(arriba, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private void mostrarErrorBD(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}