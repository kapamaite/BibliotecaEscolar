package dao;

import modelo.Usuario;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Devuelve el usuario si correo y contraseña son correctos; si no, null
    public Usuario autenticar(String correo, String contrasena) throws SQLException {
        String sql = "SELECT id, nombre, rut, correo, `contraseña`, rol "
                + "FROM usuarios WHERE correo = ? AND `contraseña` = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return armarUsuario(rs);
                }
            }
        }
        return null;
    }
    public Usuario buscarPorCorreo(String correo) throws SQLException {
        String sql = "SELECT id, nombre, rut, correo, `contraseña`, rol FROM usuarios WHERE correo = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return armarUsuario(rs);
                }
            }
        }
        return null;
    }
    public void insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, rut, correo, `contraseña`, rol) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol());
            ps.executeUpdate();
        }
    }

    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, rut, correo, `contraseña`, rol FROM usuarios";
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(armarUsuario(rs));
            }
        }
        return lista;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, rut, correo, `contraseña`, rol FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return armarUsuario(rs);
                }
            }
        }
        return null;
    }

    public void actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET nombre=?, rut=?, correo=?, `contraseña`=?, rol=? WHERE id=?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol());
            ps.setInt(6, u.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Usuario armarUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("rut"),
                rs.getString("correo"),
                rs.getString("contraseña"),
                rs.getString("rol")
        );
    }
}