package servicio;

import dao.EstudianteDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;
import modelo.Libro;
import modelo.Prestamo;
import util.DatabaseConnection;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PrestamoService {

    public static final int DIAS_PRESTAMO = 7;

    private static final LibroDAO libroDAO = new LibroDAO();
    private static final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private static final EstudianteDAO estudianteDAO = new EstudianteDAO();

    /**
     * Registra un préstamo: valida, descuenta stock y guarda el préstamo
     * (que además queda como historial del estudiante).
     * "synchronized" => solo un hilo a la vez puede ejecutarlo.
     */
    public static synchronized Prestamo registrarPrestamo(int idEstudiante, int idLibro) throws Exception {
        Connection con = DatabaseConnection.getInstance().getConnection();
        try {
            con.setAutoCommit(false);   // inicia la transacción

            if (estudianteDAO.buscarPorId(idEstudiante) == null) {
                throw new IllegalStateException("El estudiante no existe.");
            }
            Libro libro = libroDAO.buscarPorId(idLibro);
            if (libro == null) {
                throw new IllegalStateException("El libro no existe.");
            }

            // Descuenta stock solo si hay disponible
            if (!libroDAO.descontarStock(idLibro)) {
                throw new IllegalStateException("No hay stock disponible de \"" + libro.getTitulo() + "\".");
            }

            // Fecha de vencimiento calculada automáticamente
            LocalDate hoy = LocalDate.now();
            Prestamo prestamo = new Prestamo(0, idEstudiante, idLibro, hoy, hoy.plusDays(DIAS_PRESTAMO), false);
            prestamoDAO.insertar(prestamo);

            con.commit();               // todo salió bien: se confirma
            return prestamo;

        } catch (Exception e) {
            con.rollback();             // algo falló: se deshace todo
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    /**
     * Registra la devolución. Devuelve los días de atraso (0 si llegó a tiempo).
     * También es synchronized porque modifica el stock.
     */
    public static synchronized long registrarDevolucion(int idPrestamo) throws Exception {
        Connection con = DatabaseConnection.getInstance().getConnection();
        try {
            con.setAutoCommit(false);

            Prestamo p = prestamoDAO.buscarPorId(idPrestamo);
            if (p == null) {
                throw new IllegalStateException("El préstamo no existe.");
            }
            if (p.isDevuelto()) {
                throw new IllegalStateException("Este préstamo ya fue devuelto.");
            }

            // Detección de atraso (la "constancia" que se mostrará al usuario)
            long diasAtraso = p.estaAtrasado()
                    ? ChronoUnit.DAYS.between(p.getFechaDevolucion(), LocalDate.now())
                    : 0;

            prestamoDAO.marcarDevuelto(idPrestamo);
            libroDAO.aumentarStock(p.getIdLibro());

            con.commit();
            return diasAtraso;

        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }
}