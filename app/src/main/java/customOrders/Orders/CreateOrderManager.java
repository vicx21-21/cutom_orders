package customOrders.Orders;

// Importar el conector original del usuario

import customOrders.PostgresConnector;
import customOrders.util.Dialogs;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Gestiona las operaciones de base de datos relacionadas con la creación de pedidos
 * y la actualización del stock de productos.
 */
public class CreateOrderManager {

    // Nota: customer_id debe venir de la sesión de la aplicación, usamos default 1 por ahora
    private static final int DEFAULT_CUSTOMER_ID = 1;
    private static final String DEFAULT_ORDER_STATUS = "Pendiente";
    private static final String DEFAULT_SHIPPING_ADDRESS = "Dirección de recolección"; // Usando 'Dirección de recolección' como default


    /**

     */
    private int insertOrderHeader(double totalAmount) throws SQLException {
        // La columna aquí DEBE COINCIDIR con la base de datos: "shipping_address"
        String sql = "INSERT INTO orders (" +
                "customer_id, order_date, total_amount, status, shipping_address" + // <-- ¡CORRECCIÓN CRÍTICA AQUÍ!
                ") VALUES (" +
                "?, ?, ?, ?, ?" +
                ") RETURNING order_id";

        // NOTA: Reemplaza estos valores simulados (1 y la cadena "...") con los
        // valores reales obtenidos del usuario o de la sesión de tu aplicación.
        int simulatedCustomerId = 1;
        String actualShippingAddress = "Dirección de Envío del Cliente";

        int orderId = -1;

        // Uso de try-with-resources para asegurar el cierre de recursos
        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Timestamp now = new Timestamp(System.currentTimeMillis());

            // 1. customer_id
            stmt.setInt(1, simulatedCustomerId);
            // 2. order_date
            stmt.setTimestamp(2, now);
            // 3. total_amount
            stmt.setDouble(3, totalAmount);
            // 4. status
            stmt.setString(4, "PENDIENTE");
            // 5. shipping_address
            stmt.setString(5, actualShippingAddress);

            // Se usa executeQuery porque la sentencia SQL incluye RETURNING order_id
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
        }
        return orderId;
    }

    public boolean createOrderAndUpdateStock(List<ProductInOrder> cartItems, double totalAmount) {
        if (cartItems.isEmpty()) {
            // DIALOGO DE ADVERTENCIA (showWarningDialog)
            Dialogs.showWarningDialog(
                    "Carrito Vacío",
                    "No se puede crear un pedido sin productos.",
                    "Añade productos al carrito para continuar."
            );
            return false;
        }

        Connection conn = null;
        try {
            // Usar el conector original del usuario
            conn = PostgresConnector.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Crear el Encabezado del Pedido (Order)
            int orderId = insertOrderHeader(conn, totalAmount);
            if (orderId == -1) {
                conn.rollback();
                return false;
            }

            // 2. Insertar los Productos del Pedido y Actualizar Stock (por cada item)
            for (ProductInOrder item : cartItems) {
                // Validación adicional de stock antes de intentar la DB
                if (item.getQuantity() > item.getProduct().getQuantity()) {
                    // DIALOGO DE ADVERTENCIA (showWarningDialog)
                    Dialogs.showWarningDialog(
                            "Stock Insuficiente",
                            "El producto '" + item.getProduct_name() + "' ya no tiene suficiente stock disponible (" + item.getProduct().getQuantity() + ").",
                            "Por favor, ajusta la cantidad en el carrito."
                    );
                    conn.rollback();
                    return false;
                }

                if (!insertProductInOrder(conn, orderId, item) || !updateProductStock(conn, item)) {
                    conn.rollback(); // Rollback si falla alguna inserción o actualización de stock
                    return false;
                }
            }

            conn.commit(); // Confirmar la transacción

            // DIALOGO DE INFORMACIÓN (showInformationDialog)
            Dialogs.showInformationDialog(
                    "Éxito en la Creación",
                    "Pedido creado exitosamente.",
                    "Pedido #" + orderId + " ha sido registrado y el stock actualizado."
            );
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
            // DIALOGO DE ERROR CON EXCEPCIÓN (showErrorDialog con 4 parámetros)
            Dialogs.showErrorDialog(
                    "Error Crítico de DB",
                    "Fallo al crear el pedido.",
                    "Ocurrió un error en la base de datos que impidió completar la transacción. Detalles en el error expandido.",
                    e
            );
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    // --- Métodos Privados para la Transacción ---

    private int insertOrderHeader(Connection conn, double totalAmount) throws SQLException {
        // USO DE LOS NOMBRES DE COLUMNA TAL CUAL SE MOSTRARON EN LA IMAGEN: 'shipping_addres'
        String sql = "INSERT INTO orders (customer_id, date_of_order, order_status, total_amount, shipping_address) VALUES (?, ?, ?, ?, ?)";
        int orderId = -1;

        try (PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, DEFAULT_CUSTOMER_ID); // Usar ID de cliente
            pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.setString(3, DEFAULT_ORDER_STATUS);
            pstmt.setDouble(4, totalAmount);
            pstmt.setString(5, DEFAULT_SHIPPING_ADDRESS);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Fallo al crear el pedido, no se insertaron filas.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1); // Obtener el ID generado
                } else {
                    throw new SQLException("Fallo al crear el pedido, no se obtuvo ID.");
                }
            }
            return orderId;
        }
    }

    private boolean insertProductInOrder(Connection conn, int orderId, ProductInOrder item) throws SQLException {
        // USO DE LOS NOMBRES DE COLUMNA TAL CUAL SE MOSTRARON EN LA IMAGEN: 'product_quantity'
        String sql = "INSERT INTO products_in_the_order (order_id, product_id, product_quantity) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, item.getProduct().getProduct_id());
            pstmt.setInt(3, item.getQuantity()); // Cantidad a insertar en la columna product_quantity

            return pstmt.executeUpdate() > 0;
        }
    }

    private boolean updateProductStock(Connection conn, ProductInOrder item) throws SQLException {
        // La actualización de stock se hace en la tabla 'products' usando la columna 'quantity'
        String sql = "UPDATE products SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, item.getQuantity()); // Cantidad a restar
            pstmt.setInt(2, item.getProduct().getProduct_id());
            pstmt.setInt(3, item.getQuantity()); // Comprobar stock suficiente

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                // DIALOGO DE ADVERTENCIA (showWarningDialog)
                Dialogs.showWarningDialog(
                        "Error de Concurrencia de Stock",
                        "Stock Agotado durante la Transacción.",
                        "El producto '" + item.getProduct_name() + "' no pudo ser actualizado. Otro proceso agotó el stock o el producto fue eliminado."
                );
                throw new SQLException("Fallo al actualizar stock, stock insuficiente o producto no encontrado.");
            }
            return true;
        }
    }
}