package dao;

import modelo.Libro;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // CREATE
    public void insertar(Libro l) throws SQLException {
        String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getStock());
            ps.setInt(6, l.getIdCategoria());
            ps.executeUpdate();
        }
    }

    // READ (todos)
    public List<Libro> listar() throws SQLException {
        List<Libro> lista = new ArrayList<>();
        String sql = "SELECT id, titulo, autor, isbn, editorial, stock, id_categoria FROM libros";
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(armarLibro(rs));
            }
        }
        return lista;
    }

    // READ (uno por id)
    public Libro buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, titulo, autor, isbn, editorial, stock, id_categoria "
                + "FROM libros WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return armarLibro(rs);
                }
            }
        }
        return null; // no existe
    }

    // UPDATE
    public void actualizar(Libro l) throws SQLException {
        String sql = "UPDATE libros SET titulo=?, autor=?, isbn=?, editorial=?, stock=?, id_categoria=? "
                + "WHERE id=?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getStock());
            ps.setInt(6, l.getIdCategoria());
            ps.setInt(7, l.getId());
            ps.executeUpdate();
        }
    }

    // DELETE
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM libros WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
    // Descuenta 1 de stock SOLO si queda stock. Devuelve true si lo logró.
    public boolean descontarStock(int idLibro) throws SQLException {
        String sql = "UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() > 0;
        }
    }

    // Suma 1 al stock (cuando se devuelve un libro)
    public void aumentarStock(int idLibro) throws SQLException {
        String sql = "UPDATE libros SET stock = stock + 1 WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            ps.executeUpdate();
        }
    }
    // Convierte una fila de la tabla en un objeto Libro
    private Libro armarLibro(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("isbn"),
                rs.getString("editorial"),
                rs.getInt("stock"),
                rs.getInt("id_categoria")
        );
    }
}