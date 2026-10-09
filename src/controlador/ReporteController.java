package controlador;

import dao.EstudianteDAO;
import dao.ReporteDAO;
import modelo.Estudiante;

import java.sql.SQLException;
import java.util.List;

public class ReporteController {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public List<Object[]> librosMasPrestados() throws SQLException {
        return reporteDAO.librosMasPrestados();
    }

    public List<Object[]> historialPorEstudiante(int idEstudiante) throws SQLException {
        return reporteDAO.historialPorEstudiante(idEstudiante);
    }

    public List<Object[]> librosEnPrestamo() throws SQLException {
        return reporteDAO.librosEnPrestamo();
    }

    public List<Estudiante> listarEstudiantes() throws SQLException {
        return estudianteDAO.listar();
    }
}