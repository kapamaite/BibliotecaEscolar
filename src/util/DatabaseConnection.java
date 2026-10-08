package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Datos de conexión (ajusta USER y PASSWORD si tu MySQL los tiene distintos)
    private static final String URL =
            "jdbc:mysql://localhost:3306/biblioteca?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // La única instancia (Singleton)
    private static DatabaseConnection instance;
    private Connection connection;

    // Constructor privado: nadie fuera de esta clase puede crear objetos
    private DatabaseConnection() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Método estático que entrega siempre la misma instancia
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // Entrega la conexión (la reabre si se hubiera cerrado)
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
}