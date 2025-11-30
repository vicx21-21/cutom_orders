package customOrders.OrdersAdmin;

// Importamos las clases de modelo definidas estáticamente en el Manager
import customOrders.PostgresConnector;

import static customOrders.OrdersAdmin.OrdersAdminManager.Order;
import static customOrders.OrdersAdmin.OrdersAdminManager.OrderItem;
import static customOrders.OrdersAdmin.OrdersAdminManager.AvailableProduct;
import static customOrders.OrdersAdmin.OrdersAdminManager.Customer; // Importar la nueva clase Customer

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrdersDataService: Contiene la lógica para interactuar con la base de datos
 * utilizando JDBC, a través del PostgresConnector.
 *
 * - Se ha añadido el método getAvailableCustomers().
 */
public class OrdersDataService {

    /**
     * Obtiene todos los pedidos de la base de datos, incluyendo sus items.
     * @return Una lista de objetos Order.
     */
    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String SQL_SELECT_ORDERS = "SELECT o.order_id, c.first_name, c.last_name, o.date_of_order, o.order_status, o.shipping_address " +
                "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                "ORDER BY o.date_of_order DESC";

        try (Connection conn = PostgresConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_ORDERS)) {

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String customerName = rs.getString("first_name") + " " + rs.getString("last_name");

                String date = rs.getString("date_of_order");
                String status = rs.getString("order_status");
                String shippingAddress = rs.getString("shipping_address");

                List<OrderItem> items = getOrderItems(orderId, conn);

                orders.add(new Order(orderId, customerName, date, status, shippingAddress, items));
            }
        }

        return orders;
    }

    /**
     * Consulta auxiliar para obtener los ítems de un pedido dado su ID.
     */
    private List<OrderItem> getOrderItems(int orderId, Connection conn) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String SQL_SELECT_ITEMS = "SELECT p.product_name, pio.product_quantity, p.unit_price " +
                "FROM products_in_the_order pio " +
                "JOIN products p ON pio.product_id = p.product_id " +
                "WHERE pio.order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ITEMS)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("product_name");
                    int quantity = rs.getInt("product_quantity");
                    double unitPrice = rs.getDouble("unit_price");
                    items.add(new OrderItem(productName, quantity, unitPrice));
                }
            }
        }

        return items;
    }

    /**
     * Obtiene la lista de todos los productos disponibles (ID, nombre y precio).
     */
    public List<AvailableProduct> getAvailableProducts() throws SQLException {
        List<AvailableProduct> products = new ArrayList<>();
        String SQL_SELECT_PRODUCTS = "SELECT product_id, product_name, unit_price FROM products ORDER BY product_name ASC";

        try (Connection conn = PostgresConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_PRODUCTS)) {

            while (rs.next()) {
                int id = rs.getInt("product_id");
                String name = rs.getString("product_name");
                double price = (double) rs.getFloat("unit_price");

                products.add(new AvailableProduct(id, name, price));
            }
        }
        return products;
    }

    /**
     * Nuevo: Obtiene la lista de todos los clientes existentes.
     */
    public List<Customer> getAvailableCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        // Consulta SQL para obtener todos los clientes
        String SQL_SELECT_CUSTOMERS = "SELECT customer_id, first_name, last_name, address FROM customers ORDER BY last_name, first_name";

        try (Connection conn = PostgresConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_CUSTOMERS)) {

            while (rs.next()) {
                int id = rs.getInt("customer_id");
                // Concatenamos nombre y apellido para el nombre completo
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                String address = rs.getString("address");

                customers.add(new Customer(id, fullName, address));
            }
        }
        return customers;
    }


    /**
     * Actualiza el campo de estado de un pedido.
     */
    public boolean updateOrderStatus(int orderId, String newStatus) throws SQLException {
        String SQL_UPDATE_STATUS = "UPDATE orders SET order_status = ? WHERE order_id = ?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {

            ps.setString(1, newStatus);
            ps.setInt(2, orderId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Inserta un nuevo pedido y sus ítems asociados usando transacciones.
     */
    public int createNewOrder(String customerName, String date, String shippingAddress, List<OrderItem> items) throws SQLException {
        int newOrderId = -1;
        int assumedCustomerId = 1;

        String SQL_INSERT_ORDER = "INSERT INTO orders (customer_id, date_of_order, order_status, shipping_address) VALUES (?, ?, ?, ?)";
        String SQL_INSERT_ITEM = "INSERT INTO products_in_the_order (order_id, product_id, product_quantity, unit_price) VALUES (?, ?, ?, ?)";

        Connection conn = PostgresConnector.getConnection();
        conn.setAutoCommit(false);

        try (PreparedStatement psOrder = conn.prepareStatement(SQL_INSERT_ORDER, Statement.RETURN_GENERATED_KEYS)) {
            // 1. Insertar el Pedido principal
            psOrder.setInt(1, assumedCustomerId);
            psOrder.setString(2, date);
            psOrder.setString(3, "Pendiente");
            psOrder.setString(4, shippingAddress);
            psOrder.executeUpdate();

            // Obtener el ID generado
            try (ResultSet rs = psOrder.getGeneratedKeys()) {
                if (rs.next()) {
                    newOrderId = rs.getInt(1);
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(SQL_INSERT_ITEM)) {
                for (OrderItem item : items) {
                    int productId = getProductIdByName(conn, item.getProductName());

                    psItem.setInt(1, newOrderId);
                    psItem.setInt(2, productId);
                    psItem.setInt(3, item.getQuantity());
                    psItem.setDouble(4, item.getUnitPrice());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            conn.commit();
            return newOrderId;

        } catch (SQLException e) {
            System.err.println("Error al crear nuevo pedido en OrdersDataService: " + e.getMessage());
            conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private int getProductIdByName(Connection conn, String productName) throws SQLException {
        String SQL = "SELECT product_id FROM products WHERE product_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, productName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("product_id");
                }
            }
        }
        throw new SQLException("Producto no encontrado: " + productName);
    }
}