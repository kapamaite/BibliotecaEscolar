package vista;

import controlador.EstudianteController;
import modelo.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class EstudiantePanel extends JPanel {

    private final EstudianteController controlador = new EstudianteController();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Nombre", "RUT", "Curso", "Correo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField txtBuscar = new JTextField(20);
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtRut = new JTextField();
    private final JTextField txtCurso = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();

    private int idSeleccionado = 0;   // 0 = ningún estudiante seleccionado

    public EstudiantePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Buscador ---
        JPanel barraBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraBusqueda.add(new JLabel("Buscar:"));
        barraBusqueda.add(txtBuscar);
        add(barraBusqueda, BorderLayout.NORTH);

        // --- Tabla ---
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(sorter);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String texto = txtBuscar.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null
                        : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
            }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFormularioDesdeFila();
            }
        });

        add(crearFormulario(), BorderLayout.SOUTH);
        recargar();
    }

    public void recargar() {
        try {
            modeloTabla.setRowCount(0);
            for (Estudiante e : controlador.listar()) {
                modeloTabla.addRow(new Object[]{
                        e.getId(), e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo()
                });
            }
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private JPanel crearFormulario() {
        JPanel campos = new JPanel(new GridLayout(3, 4, 8, 6));
        campos.add(new JLabel("Nombre:"));     campos.add(txtNombre);
        campos.add(new JLabel("RUT:"));        campos.add(txtRut);
        campos.add(new JLabel("Curso:"));      campos.add(txtCurso);
        campos.add(new JLabel("Correo:"));     campos.add(txtCorreo);
        campos.add(new JLabel("Contraseña:")); campos.add(txtClave);
        campos.add(new JLabel(""));
        campos.add(new JLabel("<html><i>Al modificar, déjala vacía para no cambiarla</i></html>"));

        JButton btnAgregar = new JButton("Registrar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnAgregar.addActionListener(e -> registrar());
        btnModificar.addActionListener(e -> modificar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnAgregar);
        botones.add(btnModificar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel formulario = new JPanel(new BorderLayout(0, 8));
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del estudiante"));
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);
        return formulario;
    }

    private void cargarFormularioDesdeFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        int m = tabla.convertRowIndexToModel(fila);

        idSeleccionado = (Integer) modeloTabla.getValueAt(m, 0);
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(m, 1)));
        txtRut.setText(String.valueOf(modeloTabla.getValueAt(m, 2)));
        txtCurso.setText(String.valueOf(modeloTabla.getValueAt(m, 3)));
        txtCorreo.setText(String.valueOf(modeloTabla.getValueAt(m, 4)));
        txtCorreo.setEditable(false);   // el correo no se puede cambiar
        txtClave.setText("");
    }

    // ---------- Acciones ----------

    private void registrar() {
        if (idSeleccionado != 0) {
            mostrarAdvertencia("Presiona «Limpiar» antes de registrar un estudiante nuevo.");
            return;
        }
        try {
            controlador.registrar(txtNombre.getText(), txtRut.getText(), txtCurso.getText(),
                    txtCorreo.getText(), new String(txtClave.getPassword()));
            mostrarMensaje("Estudiante registrado correctamente.\nYa puede iniciar sesión con su correo.");
            limpiar();
            recargar();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void modificar() {
        if (idSeleccionado == 0) {
            mostrarAdvertencia("Selecciona un estudiante de la tabla para modificarlo.");
            return;
        }
        try {
            controlador.modificar(idSeleccionado, txtNombre.getText(), txtRut.getText(),
                    txtCurso.getText(), new String(txtClave.getPassword()));
            mostrarMensaje("Estudiante modificado correctamente.");
            limpiar();
            recargar();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void eliminar() {
        if (idSeleccionado == 0) {
            mostrarAdvertencia("Selecciona un estudiante de la tabla para eliminarlo.");
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Seguro que quieres eliminar a este estudiante?\nTambién se eliminará su cuenta de usuario.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) return;

        try {
            controlador.eliminar(idSeleccionado);
            mostrarMensaje("Estudiante eliminado correctamente.");
            limpiar();
            recargar();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void limpiar() {
        txtNombre.setText("");
        txtRut.setText("");
        txtCurso.setText("");
        txtCorreo.setText("");
        txtCorreo.setEditable(true);
        txtClave.setText("");
        tabla.clearSelection();
        idSeleccionado = 0;
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