package controlador;

import dao.CategoriaDAO;
import dao.LibroDAO;
import modelo.Categoria;
import modelo.Libro;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class LibroController {

    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Libro> listarLibros() throws SQLException {
        return libroDAO.listar();
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.listar();
    }

    public void agregar(String titulo, String autor, String isbn, String editorial,
                        String stockTexto, Categoria categoria) throws SQLException {
        Libro libro = validar(0, titulo, autor, isbn, editorial, stockTexto, categoria);
        libroDAO.insertar(libro);
    }

    public void modificar(int id, String titulo, String autor, String isbn, String editorial,
                          String stockTexto, Categoria categoria) throws SQLException {
        Libro libro = validar(id, titulo, autor, isbn, editorial, stockTexto, categoria);
        libroDAO.actualizar(libro);
    }

    public void eliminar(int id) throws SQLException {
        try {
            libroDAO.eliminar(id);
        } catch (SQLIntegrityConstraintViolationException e) {
            // La base de datos protege los libros que tienen préstamos asociados
            throw new IllegalStateException("No se puede eliminar: el libro tiene préstamos asociados.");
        }
    }

    // Revisa los datos y arma el objeto Libro. Si algo está mal, lanza un error con un mensaje claro.
    private Libro validar(int id, String titulo, String autor, String isbn, String editorial,
                          String stockTexto, Categoria categoria) {
        if (titulo.isBlank())    throw new IllegalArgumentException("El título es obligatorio.");
        if (autor.isBlank())     throw new IllegalArgumentException("El autor es obligatorio.");
        if (isbn.isBlank())      throw new IllegalArgumentException("El ISBN es obligatorio.");
        if (editorial.isBlank()) throw new IllegalArgumentException("La editorial es obligatoria.");
        if (categoria == null)   throw new IllegalArgumentException("Selecciona una categoría.");

        int stock;
        try {
            stock = Integer.parseInt(stockTexto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El stock debe ser un número entero.");
        }
        if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");

        return new Libro(id, titulo.trim(), autor.trim(), isbn.trim(), editorial.trim(), stock, categoria.getId());
    }
}