package controlador;

import dao.EstudianteDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import servicio.PrestamoService;
import servicio.PrestamoTask;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PrestamoController {

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public List<Estudiante> listarEstudiantes() throws SQLException {
        return estudianteDAO.listar();
    }

    public List<Libro> listarLibros() throws SQLException {
        return libroDAO.listar();
    }

    public Estudiante buscarEstudiantePorCorreo(String correo) throws SQLException {
        return estudianteDAO.buscarPorCorreo(correo);
    }

    // idEstudiante == null => todos los préstamos (bibliotecario)
    public List<Prestamo> listarPrestamos(Integer idEstudiante, boolean soloPendientes) throws SQLException {
        List<Prestamo> todos = (idEstudiante == null)
                ? prestamoDAO.listar()
                : prestamoDAO.listarPorEstudiante(idEstudiante);
        if (!soloPendientes) {
            return todos;
        }
        List<Prestamo> pendientes = new ArrayList<>();
        for (Prestamo p : todos) {
            if (!p.isDevuelto()) {
                pendientes.add(p);
            }
        }
        return pendientes;
    }

    // Lanza el préstamo en un HILO INDEPENDIENTE: la interfaz no se bloquea
    public void registrarPrestamo(int idEstudiante, int idLibro,
                                  Consumer<Prestamo> alExito, Consumer<String> alError) {
        Thread hilo = new Thread(new PrestamoTask(idEstudiante, idLibro, alExito, alError), "hilo-prestamo");
        hilo.start();
    }

    // Devuelve los días de atraso (0 si llegó a tiempo) y deja constancia si hubo atraso
    public long registrarDevolucion(int idPrestamo) throws Exception {
        Prestamo p = prestamoDAO.buscarPorId(idPrestamo);
        long diasAtraso = PrestamoService.registrarDevolucion(idPrestamo);
        if (diasAtraso > 0 && p != null) {
            registrarConstancia(p, diasAtraso);
        }
        return diasAtraso;
    }

    // Constancia del atraso: una línea en el archivo registro_atrasos.txt
    private void registrarConstancia(Prestamo p, long diasAtraso) {
        try {
            Estudiante e = estudianteDAO.buscarPorId(p.getIdEstudiante());
            Libro l = libroDAO.buscarPorId(p.getIdLibro());
            String nombre = (e != null) ? e.getNombre() : "id " + p.getIdEstudiante();
            String titulo = (l != null) ? l.getTitulo() : "id " + p.getIdLibro();

            String linea = LocalDate.now() + " | Préstamo " + p.getId()
                    + " | Estudiante: " + nombre
                    + " | Libro: " + titulo
                    + " | Vencía: " + p.getFechaDevolucion()
                    + " | Atraso: " + diasAtraso + " día(s)";

            try (PrintWriter pw = new PrintWriter(new FileWriter("registro_atrasos.txt", true))) {
                pw.println(linea);
            }
        } catch (Exception ex) {
            System.err.println("No se pudo guardar la constancia: " + ex.getMessage());
        }
    }
}