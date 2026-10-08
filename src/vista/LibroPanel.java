package vista;

import controlador.LibroController;
import modelo.Categoria;
import modelo.Libro;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LibroPanel extends JPanel {

    private final LibroController controlador = new LibroController();
    private final Map<Integer, String> nombresCategoria = new HashMap<>();

    // Tabla (las celdas no se pueden editar directamente)
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Título", "Autor", "ISBN", "Editorial", "Stock", "Categoría"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    // Campos del formulario
    private final JTextField txtBuscar = new JTextField(20);
    private final JTextField txtTitulo = new JTextField();
    private final JTextField txtAutor = new JTextField();
    private final JTextField txtIsbn = new JTextField();
    private final JTextField txtEditorial = new JTextField();
    private final JTextField txtStock = new JTextField();
    private final JComboBox<Categoria> cmbCategoria = new JComboBox<>();

    private int idSeleccionado = 0;   // 0 = ningún libro seleccionado

    public LibroPanel(Usuario usuario) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Barra de búsqueda (arriba) ---
        JPanel barraBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraBusqueda.add(new JLabel("Buscar:"));
        barraBusqueda.add(txtBuscar);
        add(barraBusqueda, BorderLayout.NORTH);

        // --- Tabla (centro) ---
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(sorter);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Filtra la tabla mientras se escribe en el buscador
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String texto = txtBuscar.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null
                        : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
            }
        });

        // Al hacer clic en una fila, sus datos pasan al formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFormularioDesdeFila();
            }
        });

        // --- Formulario (abajo): solo para el bibliotecario ---
        if (usuario.esBibliotecario()) {
            add(crearFormulario(), BorderLayout.SOUTH);
        } else {
            barraBusqueda.add(new JLabel("   (Modo consulta)"));
        }

        recargar();
    }

    // Vuelve a leer categorías y libros desde la base de datos
    public void recargar() {
        cargarCategorias();
        cargarTabla();
    }

    private JPanel crearFormulario() {
        JPanel campos = new JPanel(new GridLayout(3, 4, 8, 6));
        campos.add(new JLabel("Título:"));    campos.add(txtTitulo);
        campos.add(new JLabel("Autor:"));     campos.add(txtAutor);
        campos.add(new JLabel("ISBN:"));      campos.add(txtIsbn);
        campos.add(new JLabel("Editorial:")); campos.add(txtEditorial);
        campos.add(new JLabel("Stock:"));     campos.add(txtStock);
        campos.add(new JLabel("Categoría:")); campos.add(cmbCategoria);

        JButton btnAgregar = new JButton("Agregar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnAgregar.addActionListener(e -> agregar());
        btnModificar.addActionListener(e -> modificar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnAgregar);
        botones.add(btnModificar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel formulario = new JPanel(new BorderLayout(0, 8));
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del libro"));
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);
        return formulario;
    }

    // ---------- Carga de datos ----------

    private void cargarCategorias() {
        try {
            cmbCategoria.removeAllItems();
            nombresCategoria.clear();
            for (Categoria c : controlador.listarCategorias()) {
                cmbCategoria.addItem(c);
                nombresCategoria.put(c.getId(), c.getNombre());
            }
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void cargarTabla() {
        try {
            modeloTabla.setRowCount(0);   // vacía la tabla
            for (Libro l : controlador.listarLibros()) {
                modeloTabla.addRow(new Object[]{
                        l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(), l.getEditorial(),
                        l.getStock(), nombresCategoria.getOrDefault(l.getIdCategoria(), "—")
                });
            }
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void cargarFormularioDesdeFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        int m = tabla.convertRowIndexToModel(fila);   // por si la tabla está filtrada

        idSeleccionado = (Integer) modeloTabla.getValueAt(m, 0);
        txtTitulo.setText(String.valueOf(modeloTabla.getValueAt(m, 1)));
        txtAutor.setText(String.valueOf(modeloTabla.getValueAt(m, 2)));
        txtIsbn.setText(String.valueOf(modeloTabla.getValueAt(m, 3)));
        txtEditorial.setText(String.valueOf(modeloTabla.getValueAt(m, 4)));
        txtStock.setText(String.valueOf(modeloTabla.getValueAt(m, 5)));

        String nombreCategoria = String.valueOf(modeloTabla.getValueAt(m, 6));
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getNombre().equals(nombreCategoria)) {
                cmbCategoria.setSelectedIndex(i);
                break;
            }
        }
    }

    // ---------- Acciones de los botones ----------

    private void agregar() {
        try {
            controlador.agregar(txtTitulo.getText(), txtAutor.getText(), txtIsbn.getText(),
                    txtEditorial.getText(), txtStock.getText(), (Categoria) cmbCategoria.getSelectedItem());
            mostrarMensaje("Libro agregado correctamente.");
            limpiar();
            cargarTabla();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void modificar() {
        if (idSeleccionado == 0) {
            mostrarAdvertencia("Selecciona un libro de la tabla para modificarlo.");
            return;
        }
        try {
            controlador.modificar(idSeleccionado, txtTitulo.getText(), txtAutor.getText(), txtIsbn.getText(),
                    txtEditorial.getText(), txtStock.getText(), (Categoria) cmbCategoria.getSelectedItem());
            mostrarMensaje("Libro modificado correctamente.");
            limpiar();
            cargarTabla();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void eliminar() {
        if (idSeleccionado == 0) {
            mostrarAdvertencia("Selecciona un libro de la tabla para eliminarlo.");
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Seguro que quieres eliminar este libro?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) return;

        try {
            controlador.eliminar(idSeleccionado);
            mostrarMensaje("Libro eliminado correctamente.");
            limpiar();
            cargarTabla();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (SQLException ex) {
            mostrarErrorBD(ex);
        }
    }

    private void limpiar() {
        txtTitulo.setText("");
        txtAutor.setText("");
        txtIsbn.setText("");
        txtEditorial.setText("");
        txtStock.setText("");
        if (cmbCategoria.getItemCount() > 0) cmbCategoria.setSelectedIndex(0);
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