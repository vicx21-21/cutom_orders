package customOrders.Orders;

import customOrders.PostgresConnector;
import customOrders.util.Dialogs;

import java.sql.*;
import java.util.List;

/**
 * Gestiona las operaciones de base de datos relacionadas con la creación de pedidos
 * y la actualización del stock de productos.
 */
public class CreateOrderManager {

    // Definimos un estado inicial constante que no es nulo
    // CORRECCIÓN DE CHECK CONSTRAINT: Cambiado de "PENDIENTE" a "pendiente" (minúsculas)
    private static final String DEFAULT_ORDER_STATUS = "Pendiente";

    /**
     * Método principal que gestiona la transacción de la orden.
     */
    public boolean createOrderAndUpdateStock(
            List<ProductInOrder> cartItems,
            Integer customerId,
            String shippingAddress,
            double totalAmount)
    {
        Connection conn = null;
        try {
            // 1. Obtener la conexión e iniciar la transacción
            conn = PostgresConnector.getConnection();
            conn.setAutoCommit(false); // **INICIAR TRANSACCIÓN** // --- PASO A: Crear la Orden Maestra (Order Header) ---

            // Inserción en la tabla Orders (que ya está correcta)
            String insertOrderSQL = "INSERT INTO Orders (customer_id, date_of_order, shipping_address, total_amount, order_status) VALUES (?, NOW(), ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);

            // 1. customer_id
            pstmt.setInt(1, customerId);
            // 2. shipping_address
            pstmt.setString(2, shippingAddress);
            // 3. total_amount
            pstmt.setDouble(3, totalAmount);
            // 4. order_status
            pstmt.setString(4, DEFAULT_ORDER_STATUS);

            pstmt.executeUpdate();

            // Obtener el ID de la nueva orden (necesario para products_in_the_order)
            int orderId = -1;
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
            rs.close();
            pstmt.close();

            if (orderId == -1) {
                throw new SQLException("Fallo crítico al obtener el ID de la orden generada.");
            }

            // --- PASO B: Insertar los Detalles de la Orden y Actualizar Stock ---

            // CORRECCIÓN FINAL DE COLUMNA: 'product_price' cambiado a 'item_unit_price' (Según el esquema de la imagen)
            String insertDetailSQL = "INSERT INTO products_in_the_order (order_id, product_id, product_quantity, item_unit_price) VALUES (?, ?, ?, ?)";
            String updateStockSQL = "UPDATE Products SET quantity = quantity - ? WHERE product_id = ?";

            PreparedStatement pstmtDetail = conn.prepareStatement(insertDetailSQL);
            PreparedStatement pstmtStock = conn.prepareStatement(updateStockSQL);

            for (ProductInOrder item : cartItems) {
                // 1. Insertar Detalle
                pstmtDetail.setInt(1, orderId);
                pstmtDetail.setInt(2, item.getProduct().getProduct_id());
                // product_quantity
                pstmtDetail.setInt(3, item.getQuantity());
                // item_unit_price
                pstmtDetail.setDouble(4, item.getUnit_price());
                pstmtDetail.addBatch();

                // 2. Actualizar Stock (Asumiendo que 'quantity' en Products sí existe y es correcto)
                pstmtStock.setInt(1, item.getQuantity());
                pstmtStock.setInt(2, item.getProduct().getProduct_id());
                pstmtStock.addBatch();
            }

            pstmtDetail.executeBatch();
            pstmtStock.executeBatch();

            // 3. Confirmar la transacción
            conn.commit(); // **CONFIRMAR CAMBIOS**
            return true;

        } catch (SQLException e) {
            System.err.println("Error SQL en la transacción de la orden. Haciendo ROLLBACK.");

            // Mostrar un diálogo amigable al usuario
            Dialogs.showErrorDialog("Error de Base de Datos", "Fallo al crear el pedido.", "Se ha producido un error crítico al intentar guardar el pedido en la base de datos. Se ha deshecho la operación. Error: " + e.getMessage(), e);
            e.printStackTrace();

            // 4. Revertir la transacción si algo falla
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Fallo al hacer ROLLBACK: " + ex.getMessage());
                }
            }
            return false;

        } finally {
            // 5. Cerrar la conexión y restablecer el autocommit
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }

    // --- Métodos Privados de soporte (Simplificados) ---
}