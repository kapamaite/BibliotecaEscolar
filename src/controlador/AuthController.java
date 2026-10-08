package controlador;

import dao.UsuarioDAO;
import modelo.Usuario;

import java.sql.SQLException;

public class AuthController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Devuelve el usuario si las credenciales son correctas; si no, null
    public Usuario iniciarSesion(String correo, String contrasena) throws SQLException {
        return usuarioDAO.autenticar(correo.trim(), contrasena);
    }
}