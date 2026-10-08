package dao;

import modelo.Estudiante;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public void insertar(Estudiante e) throws SQLException {
        String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.executeUpdate();
        }
    }

    public List<Estudiante> listar() throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes";
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(armarEstudiante(rs));
            }
        }
        return lista;
    }

    public Estudiante buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return armarEstudiante(rs);
                }
            }
        }
        return null;
    }

    // Sirve para vincular un usuario que inicia sesión con su ficha de estudiante
    public Estudiante buscarPorCorreo(String correo) throws SQLException {
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes WHERE correo = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return armarEstudiante(rs);
                }
            }
        }
        return null;
    }

    public void actualizar(Estudiante e) throws SQLException {
        String sql = "UPDATE estudiantes SET nombre=?, rut=?, curso=?, correo=? WHERE id=?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.setInt(5, e.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Estudiante armarEstudiante(ResultSet rs) throws SQLException {
        return new Estudiante(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("rut"),
                rs.getString("curso"),
                rs.getString("correo")
        );
    }
}