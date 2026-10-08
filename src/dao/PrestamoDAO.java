package dao;

import modelo.Prestamo;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    private static final String COLUMNAS =
            "id, id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto";

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public void insertar(Prestamo p) throws SQLException {
        String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaDevolucion()));
            ps.setBoolean(5, p.isDevuelto());
            ps.executeUpdate();
        }
    }

    public List<Prestamo> listar() throws SQLException {
        return consultar("SELECT " + COLUMNAS + " FROM prestamos", null);
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException {
        return consultar("SELECT " + COLUMNAS + " FROM prestamos WHERE id_estudiante = ?", idEstudiante);
    }

    public List<Prestamo> listarPendientes() throws SQLException {
        return consultar("SELECT " + COLUMNAS + " FROM prestamos WHERE devuelto = 0", null);
    }

    public Prestamo buscarPorId(int id) throws SQLException {
        List<Prestamo> r = consultar("SELECT " + COLUMNAS + " FROM prestamos WHERE id = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    public void marcarDevuelto(int idPrestamo) throws SQLException {
        String sql = "UPDATE prestamos SET devuelto = 1 WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            ps.executeUpdate();
        }
    }

    public void actualizar(Prestamo p) throws SQLException {
        String sql = "UPDATE prestamos SET id_estudiante=?, id_libro=?, fecha_prestamo=?, "
                + "fecha_devolucion=?, devuelto=? WHERE id=?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaDevolucion()));
            ps.setBoolean(5, p.isDevuelto());
            ps.setInt(6, p.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM prestamos WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Método de apoyo: ejecuta una consulta con (o sin) un parámetro entero
    private List<Prestamo> consultar(String sql, Integer parametro) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Prestamo(
                            rs.getInt("id"),
                            rs.getInt("id_estudiante"),
                            rs.getInt("id_libro"),
                            rs.getDate("fecha_prestamo").toLocalDate(),
                            rs.getDate("fecha_devolucion").toLocalDate(),
                            rs.getBoolean("devuelto")
                    ));
                }
            }
        }
        return lista;
    }
}