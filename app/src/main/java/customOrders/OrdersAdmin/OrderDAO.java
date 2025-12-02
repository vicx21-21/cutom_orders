package customOrders.OrdersAdmin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Objeto de Acceso a Datos (DAO) para la entidad Order (Simulado).
 * Contiene la lógica para interactuar con la base de datos (DB).
 */
public class OrderDAO {

    // Simulación de la conexión a la base de datos
    private Connection getConnection() throws SQLException {
        // En una aplicación real, aquí iría la lógica para obtener la conexión (ej: DataSource, JDBC).
        // Usaremos una simulación simple.
        System.out.println("DEBUG: Conexión simulada a la DB.");
        return null; // Retorna null en la simulación
    }

    /**
     * Elimina TODOS los registros de pedidos y sus ítems asociados de la base de datos.
     * ADVERTENCIA: Esta operación es destructiva y permanente.
     * * @return El número total de filas eliminadas.
     * @throws SQLException Si ocurre un error al interactuar con la DB.
     */
    public int deleteAllOrders() throws SQLException {
        // En una DB real, necesitarías una transacción para asegurar ambas eliminaciones.

        // Contadores para el mensaje de éxito
        int deletedItems = 0;
        int deletedOrders = 0;

        // Simulación:
        if (getConnection() == null) {
            System.out.println("DEBUG: Simulando eliminación de TODOS los pedidos.");

            // Simulación de filas afectadas
            deletedItems = 150;
            deletedOrders = 50;

            System.out.println("DEBUG: Eliminación simulada exitosa.");
            System.out.printf("DEBUG: Ítems de pedido eliminados: %d, Pedidos eliminados: %d%n", deletedItems, deletedOrders);

            return deletedOrders;
        }

        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Iniciar transacción
            stmt = conn.createStatement();

            // 1. Eliminar ítems de pedidos primero (dependencias)
            String deleteItemsSQL = "DELETE FROM Order_Items";
            deletedItems = stmt.executeUpdate(deleteItemsSQL);

            // 2. Eliminar los pedidos
            String deleteOrdersSQL = "DELETE FROM Orders";
            deletedOrders = stmt.executeUpdate(deleteOrdersSQL);

            conn.commit(); // Confirmar la transacción

            System.out.println("Se eliminaron " + deletedItems + " ítems y " + deletedOrders + " pedidos.");
            return deletedOrders;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Deshacer en caso de error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new SQLException("Error al eliminar todos los pedidos: " + e.getMessage());

        } finally {
            // Cierre de recursos
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }

    // Otros métodos CRUD irían aquí... (ej: save, findById, updateStatus)

}
