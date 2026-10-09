package dao;

import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Reporte 1: libros más prestados (top 10)
    public List<Object[]> librosMasPrestados() throws SQLException {
        String sql = "SELECT l.titulo, l.autor, COUNT(p.id) AS total "
                + "FROM prestamos p JOIN libros l ON l.id = p.id_libro "
                + "GROUP BY l.id, l.titulo, l.autor "
                + "ORDER BY total DESC, l.titulo "
                + "LIMIT 10";
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new Object[]{
                        rs.getString("titulo"), rs.getString("autor"), rs.getInt("total")
                });
            }
        }
        return filas;
    }

    // Reporte 2: historial de préstamos de un estudiante
    public List<Object[]> historialPorEstudiante(int idEstudiante) throws SQLException {
        String sql = "SELECT l.titulo, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
                + "DATEDIFF(CURDATE(), p.fecha_devolucion) AS dias "
                + "FROM prestamos p JOIN libros l ON l.id = p.id_libro "
                + "WHERE p.id_estudiante = ? "
                + "ORDER BY p.fecha_prestamo DESC";
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{
                            rs.getString("titulo"),
                            rs.getDate("fecha_prestamo"),
                            rs.getDate("fecha_devolucion"),
                            estado(rs.getBoolean("devuelto"), rs.getInt("dias"))
                    });
                }
            }
        }
        return filas;
    }

    // Reporte 3: libros que están actualmente en préstamo (no devueltos)
    public List<Object[]> librosEnPrestamo() throws SQLException {
        String sql = "SELECT l.titulo, e.nombre, e.curso, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
                + "DATEDIFF(CURDATE(), p.fecha_devolucion) AS dias "
                + "FROM prestamos p "
                + "JOIN libros l ON l.id = p.id_libro "
                + "JOIN estudiantes e ON e.id = p.id_estudiante "
                + "WHERE p.devuelto = 0 "
                + "ORDER BY p.fecha_devolucion";
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new Object[]{
                        rs.getString("titulo"),
                        rs.getString("nombre"),
                        rs.getString("curso"),
                        rs.getDate("fecha_prestamo"),
                        rs.getDate("fecha_devolucion"),
                        estado(rs.getBoolean("devuelto"), rs.getInt("dias"))
                });
            }
        }
        return filas;
    }

    // "dias" = días transcurridos desde el vencimiento (positivo si ya venció)
    private String estado(boolean devuelto, int dias) {
        if (devuelto) return "Devuelto";
        if (dias > 0) return "ATRASADO (" + dias + " días)";
        return "Pendiente";
    }
}