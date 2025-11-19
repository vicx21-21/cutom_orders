package com.customOrders;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresConnector {

    // -Parámetros de Conexión
    private static final String URL = "jdbc:postgresql://localhost:5432/data_model_with_custom_orders";
    private static final String USUARIO = "developer";
    private static final String CONTRASENA = "210521";


    public static Connection getConnection() throws SQLException {
        // Usa DriverManager para establecer la conexión con los parámetros definidos
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }

    public static void main(String[] args) {

        System.out.println("Intentando conectar a la base de datos PostgreSQL...");

        // Usamos try-with-resources para asegurar el cierre automático de la conexión y el statement.
        try (Connection connection = getConnection(); // <-- Usamos el nuevo método getConnection()
             Statement statement = connection.createStatement()) {

            if (connection != null) {
                System.out.println(" ¡Conexión exitosa!");

                // --- Ejemplo de Consulta SQL ---
                String sql = "SELECT current_database() AS db_name, now() AS server_time;";

                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    if (resultSet.next()) {
                        String dbName = resultSet.getString("db_name");
                        String time = resultSet.getString("server_time");

                        System.out.println("\n--- Resultado de la Prueba ---");
                        System.out.println("Base de Datos Conectada: " + dbName);
                        System.out.println("Hora del Servidor: " + time);
                        System.out.println("-----------------------------\n");
                    }
                }

            } else {
                // Esta parte es difícil de alcanzar si getConnection lanza una excepción en caso de fallo.
                System.out.println("Falló la conexión (El objeto Connection es nulo).");
            }

        } catch (SQLException e) {
            System.out.println(" Error de Conexión o de Consulta SQL.");
            System.out.println("Verifica los parámetros de conexión, el driver y que el servidor PostgreSQL esté en ejecución.");
            System.out.println("Detalle del Error: " + e.getMessage());
        }
    }
}