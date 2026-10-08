package vista;

import controlador.PrestamoController;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import modelo.Usuario;
import servicio.PrestamoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PrestamoPanel extends JPanel {

    private final PrestamoController controlador = new PrestamoController();
    private final Usuario usuario;
    private Estudiante fichaEstudiante;   // solo para el rol estudiante

    private final JComboBox<Estudiante> cmbEstudiante = new JComboBox<>();
    private final JComboBox<Libro> cmbLibro = new JComboBox<>();
    private final JButton btnPrestar = new JButton("Registrar préstamo");
    private final JProgressBar barraProgreso = new JProgressBar();
    private final JLabel lblEstado = new JLabel(" ");

    private final JCheckBox chkPendientes = new JCheckBox("Mostrar solo pendientes");
    private final JButton btnDevolver = new JButton("Registrar devolución");
    private final JButton btnActualizar = new JButton("Actualizar");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Estudiante", "Libro", "Fecha préstamo", "Vence", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    // Los préstamos atrasados se pintan en rojo
    private final JTable tabla = new JTable(modeloTabla) {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int fila, int columna) {
            Component c = super.prepareRenderer(renderer, fila, columna);
            if (!isRowSelected(fila)) {
                String estado = String.valueOf(getValueAt(fila, 5));
                c.setForeground(estado.startsWith("ATRASADO") ? new Color(180, 0, 0) : getForeground());
            }
            return c;
        }
    };

    public PrestamoPanel(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // El estudiante solo opera con su propia ficha
        if (!usuario.esBibliotecario()) {
            try {
                fichaEstudiante = controlador.buscarEstudiantePorCorreo(usuario.getCorreo());
            } catch (SQLException ex) {
                mostrarErrorBD(ex);
            }
        }

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);

        add(crearPanelNuevoPrestamo(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearBarraInferior(), BorderLayout.SOUTH);

        recargar();
    }

    private boolean puedePrestar() {
        return usuario.esBibliotecario() || fichaEstudiante != null;
    }

    // ---------- Construcción de la pantalla ----------

    private JPanel crearPanelNuevoPrestamo() {
        cmbEstudiante.setPreferredSize(new Dimension(230, 26));
        cmbLibro.setPreferredSize(new Dimension(270, 26));

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila.add(new JLabel("Estudiante:"));
        fila.add(cmbEstudiante);
        fila.add(new JLabel("Libro:"));
        fila.add(cmbLibro);
        fila.add(btnPrestar);

        String vence = LocalDate.now().plusDays(PrestamoService.DIAS_PRESTAMO)
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        JLabel lblInfo = new JLabel("  Los préstamos duran " + PrestamoService.DIAS_PRESTAMO
                + " días. Si se registra hoy, vence el " + vence + ".");

        barraProgreso.setIndeterminate(true);
        barraProgreso.setVisible(false);
        JPanel estado = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        estado.add(barraProgreso);
        estado.add(lblEstado);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(lblInfo, BorderLayout.NORTH);
        centro.add(estado, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Nuevo préstamo"));
        panel.add(fila, BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);

        btnPrestar.addActionListener(e -> registrarPrestamo());
        return panel;
    }

    private JPanel crearBarraInferior() {
        chkPendientes.addActionListener(e -> recargarTabla());
        btnDevolver.addActionListener(e -> registrarDevolucion());
        btnActualizar.addActionListener(e -> recargar());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        barra.add(chkPendientes);
        barra.add(btnDevolver);
        barra.add(btnActualizar);
        return barra;
    }

    // ---------- Carga de datos ----------

    public void recargar() {
        try {
            cargarCombos();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void recargarTabla() {
        try {
            cargarTabla();
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void cargarCombos() throws SQLException {
        cmbEstudiante.removeAllItems();
        if (usuario.esBibliotecario()) {
            for (Estudiante e : controlador.listarEstudiantes()) {
                cmbEstudiante.addItem(e);
            }
        } else if (fichaEstudiante != null) {
            cmbEstudiante.addItem(fichaEstudiante);
            cmbEstudiante.setEnabled(false);   // el estudiante solo puede pedir para sí mismo
        }

        cmbLibro.removeAllItems();
        for (Libro l : controlador.listarLibros()) {
            cmbLibro.addItem(l);
        }

        btnPrestar.setEnabled(puedePrestar());
        if (!puedePrestar()) {
            lblEstado.setText("Tu cuenta no tiene una ficha de estudiante asociada.");
        }
    }

    private void cargarTabla() throws SQLException {
        Map<Integer, String> nombres = new HashMap<>();
        for (Estudiante e : controlador.listarEstudiantes()) {
            nombres.put(e.getId(), e.getNombre());
        }
        Map<Integer, String> titulos = new HashMap<>();
        for (Libro l : controlador.listarLibros()) {
            titulos.put(l.getId(), l.getTitulo());
        }

        // Bibliotecario: todos los préstamos. Estudiante: solo los suyos.
        Integer filtro = null;
        if (!usuario.esBibliotecario()) {
            filtro = (fichaEstudiante != null) ? fichaEstudiante.getId() : -1;
        }

        modeloTabla.setRowCount(0);
        for (Prestamo p : controlador.listarPrestamos(filtro, chkPendientes.isSelected())) {
            modeloTabla.addRow(new Object[]{
                    p.getId(),
                    nombres.getOrDefault(p.getIdEstudiante(), "?"),
                    titulos.getOrDefault(p.getIdLibro(), "?"),
                    p.getFechaPrestamo(),
                    p.getFechaDevolucion(),
                    textoEstado(p)
            });
        }
    }

    private String textoEstado(Prestamo p) {
        if (p.isDevuelto()) return "Devuelto";
        if (p.estaAtrasado()) return "ATRASADO (" + p.diasAtraso() + " días)";
        return "Pendiente";
    }

    // ---------- Acciones ----------

    private void registrarPrestamo() {
        Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem();
        Libro libro = (Libro) cmbLibro.getSelectedItem();
        if (estudiante == null || libro == null) {
            mostrarAdvertencia("Selecciona un estudiante y un libro.");
            return;
        }

        // Mientras el hilo trabaja: botón bloqueado y barra de progreso visible
        btnPrestar.setEnabled(false);
        barraProgreso.setVisible(true);
        lblEstado.setText("Registrando préstamo en segundo plano...");

        // Estos bloques se ejecutan en el HILO, por eso la interfaz se actualiza con invokeLater
        controlador.registrarPrestamo(estudiante.getId(), libro.getId(),
                prestamo -> SwingUtilities.invokeLater(() -> {
                    terminarOperacion();
                    mostrarMensaje("Préstamo registrado correctamente.\nFecha de vencimiento: "
                            + prestamo.getFechaDevolucion());
                    recargar();
                }),
                mensaje -> SwingUtilities.invokeLater(() -> {
                    terminarOperacion();
                    mostrarAdvertencia(mensaje);
                    recargar();
                }));
    }

    private void terminarOperacion() {
        barraProgreso.setVisible(false);
        lblEstado.setText(" ");
        btnPrestar.setEnabled(puedePrestar());
    }

    private void registrarDevolucion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarAdvertencia("Selecciona un préstamo de la tabla.");
            return;
        }
        int m = tabla.convertRowIndexToModel(fila);
        int idPrestamo = (Integer) modeloTabla.getValueAt(m, 0);

        try {
            long diasAtraso = controlador.registrarDevolucion(idPrestamo);
            if (diasAtraso > 0) {
                JOptionPane.showMessageDialog(this,
                        "Devolución registrada con ATRASO de " + diasAtraso + " día(s).\n"
                                + "Se dejó constancia en el archivo registro_atrasos.txt.",
                        "Devolución con atraso", JOptionPane.WARNING_MESSAGE);
            } else {
                mostrarMensaje("Devolución registrada a tiempo.");
            }
            recargar();
        } catch (IllegalStateException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Mensajes ----------

    private void mostrarMensaje(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAdvertencia(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarErrorBD(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}