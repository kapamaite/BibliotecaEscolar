package controlador;

import dao.EstudianteDAO;
import dao.UsuarioDAO;
import modelo.Estudiante;
import modelo.Usuario;
import servicio.PrestamoService;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class EstudianteController {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Estudiante> listar() throws SQLException {
        return estudianteDAO.listar();
    }

    // Registra al estudiante Y su cuenta de usuario, todo o nada
    public void registrar(String nombre, String rut, String curso, String correo, String contrasena)
            throws SQLException {
        nombre = nombre.trim();
        rut = rut.trim();
        curso = curso.trim();
        correo = correo.trim();
        validarDatos(nombre, rut, curso, correo);
        if (contrasena.isBlank()) {
            throw new IllegalArgumentException("La contraseña inicial es obligatoria.");
        }
        if (existeRut(rut, 0)) {
            throw new IllegalStateException("Ya existe un estudiante con ese RUT.");
        }
        if (estudianteDAO.buscarPorCorreo(correo) != null || usuarioDAO.buscarPorCorreo(correo) != null) {
            throw new IllegalStateException("Ya existe un usuario con ese correo.");
        }

        // Mismo candado que los préstamos: comparten la conexión y no deben mezclar transacciones
        synchronized (PrestamoService.class) {
            Connection con = DatabaseConnection.getInstance().getConnection();
            try {
                con.setAutoCommit(false);
                estudianteDAO.insertar(new Estudiante(0, nombre, rut, curso, correo));
                usuarioDAO.insertar(new Usuario(0, nombre, rut, correo, contrasena, "estudiante"));
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // El correo no se modifica (es el vínculo con la cuenta de usuario).
    // Si "contrasenaNueva" viene vacía, se mantiene la actual.
    public void modificar(int id, String nombre, String rut, String curso, String contrasenaNueva)
            throws SQLException {
        Estudiante actual = estudianteDAO.buscarPorId(id);
        if (actual == null) {
            throw new IllegalStateException("El estudiante no existe.");
        }
        nombre = nombre.trim();
        rut = rut.trim();
        curso = curso.trim();
        validarDatos(nombre, rut, curso, actual.getCorreo());
        if (existeRut(rut, id)) {
            throw new IllegalStateException("Ya existe otro estudiante con ese RUT.");
        }

        synchronized (PrestamoService.class) {
            Connection con = DatabaseConnection.getInstance().getConnection();
            try {
                con.setAutoCommit(false);
                estudianteDAO.actualizar(new Estudiante(id, nombre, rut, curso, actual.getCorreo()));

                Usuario u = usuarioDAO.buscarPorCorreo(actual.getCorreo());
                if (u != null && "estudiante".equals(u.getRol())) {
                    u.setNombre(nombre);
                    u.setRut(rut);
                    if (!contrasenaNueva.isBlank()) {
                        u.setContrasena(contrasenaNueva);
                    }
                    usuarioDAO.actualizar(u);
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void eliminar(int id) throws SQLException {
        Estudiante actual = estudianteDAO.buscarPorId(id);
        if (actual == null) {
            throw new IllegalStateException("El estudiante no existe.");
        }

        synchronized (PrestamoService.class) {
            Connection con = DatabaseConnection.getInstance().getConnection();
            try {
                con.setAutoCommit(false);
                estudianteDAO.eliminar(id);   // falla si el estudiante tiene préstamos

                Usuario u = usuarioDAO.buscarPorCorreo(actual.getCorreo());
                if (u != null && "estudiante".equals(u.getRol())) {
                    usuarioDAO.eliminar(u.getId());
                }
                con.commit();
            } catch (SQLIntegrityConstraintViolationException e) {
                con.rollback();
                throw new IllegalStateException(
                        "No se puede eliminar: el estudiante tiene préstamos en su historial.");
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // ---------- Validaciones ----------

    private void validarDatos(String nombre, String rut, String curso, String correo) {
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (!rut.matches("\\d{7,8}-[\\dkK]")) {
            throw new IllegalArgumentException("El RUT debe tener el formato 12345678-9 (sin puntos y con guion).");
        }
        if (curso.isBlank()) {
            throw new IllegalArgumentException("El curso es obligatorio.");
        }
        if (!correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El correo no es válido.");
        }
    }

    // idExcluido: al modificar, el propio estudiante no cuenta como duplicado
    private boolean existeRut(String rut, int idExcluido) throws SQLException {
        for (Estudiante e : estudianteDAO.listar()) {
            if (e.getRut().equalsIgnoreCase(rut) && e.getId() != idExcluido) {
                return true;
            }
        }
        return false;
    }
}