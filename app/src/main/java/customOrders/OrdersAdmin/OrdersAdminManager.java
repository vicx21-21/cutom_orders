package customOrders.OrdersAdmin;

import customOrders.PostgresConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrdersAdminManager: Capa de Lógica de Negocio que interactúa con PostgreSQL.
 * Este manager contiene las clases de datos y la lógica para interactuar con
 * la base de datos (carga, creación y actualización de pedidos/clientes).
 */
public class OrdersAdminManager {

    // --- Clases de Datos Internas ---

    public static class OrderItem {
        private String productName;
        private int quantity;
        private double unitPrice;

        public OrderItem(String productName, int quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        // Getters
        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public double getTotalPrice() {
            return quantity * unitPrice;
        }
    }

    public static class Order {
        private int orderId;
        private String customerName; // Concatenación de first_name y last_name
        private String date;
        private String status;
        private String shippingAddress;
        private List<OrderItem> items;

        public Order(int orderId, String customerName, String date, String status, String shippingAddress, List<OrderItem> items) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.date = date;
            this.status = status;
            this.shippingAddress = shippingAddress;
            this.items = items;
        }

        // Getters
        public int getOrderId() {
            return orderId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getDate() {
            return date;
        }

        public String getStatus() {
            return status;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public List<OrderItem> getItems() {
            return items;
        }
    }

    public static class AvailableProduct {
        private int id;
        private String name;
        private double price;

        public AvailableProduct(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        // Getters
        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return name + " ($" + String.format("%.2f", price) + ")";
        }
    }

    /**
     * Clase de datos para representar a un cliente.
     */
    public static class Customer {
        private int id;
        private String fullName;
        private String address;

        public Customer(int id, String fullName, String address) {
            this.id = id;
            this.fullName = fullName;
            this.address = address;
        }

        // Getters
        public int getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public String getAddress() {
            return address;
        }

        @Override
        public String toString() {
            return fullName;
        }
    }


    // --- Lógica de Carga de Datos ---

    /**
     * Carga productos disponibles.
     */
    public List<AvailableProduct> loadAvailableProducts() {
        List<AvailableProduct> products = new ArrayList<>();
        String SQL = "SELECT product_id, product_name, unit_price FROM products ORDER BY product_name";

        try (Connection conn = PostgresConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                products.add(new AvailableProduct(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("unit_price")));
            }
        } catch (SQLException ex) {
            System.err.println("ERROR Manager: Fallo al cargar productos. Mensaje de SQL: " + ex.getMessage());
            throw new RuntimeException("Fallo en la DB al cargar productos.", ex);
        }
        return products;
    }

    /**
     * Carga todos los clientes existentes.
     */
    public List<Customer> loadAvailableCustomers() {
        List<Customer> customers = new ArrayList<>();
        String SQL = "SELECT customer_id, first_name, last_name, address FROM customers ORDER BY last_name, first_name";

        try (Connection conn = PostgresConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                customers.add(new Customer(
                        rs.getInt("customer_id"),
                        fullName,
                        rs.getString("address")));
            }
        } catch (SQLException ex) {
            // IMPRESIÓN MEJORADA para diagnóstico
            System.err.println("ERROR Manager: Fallo crítico al cargar clientes. Revisa conexión o tabla 'customers'. Mensaje de SQL: " + ex.getMessage());
            throw new RuntimeException("Fallo en la DB al cargar clientes.", ex);
        }
        return customers;
    }

    /**
     * Carga todos los pedidos.
     */
    public List<Order> loadAllOrders() {
        List<Order> orders = new ArrayList<>();
        String SQL_ORDERS = "SELECT o.order_id, c.first_name, c.last_name, o.date_of_order, o.order_status, o.shipping_address " +
                "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                "ORDER BY o.date_of_order DESC";

        try (Connection conn = PostgresConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_ORDERS)) {

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String customerName = rs.getString("first_name") + " " + rs.getString("last_name");

                List<OrderItem> items = loadOrderItems(conn, orderId);

                orders.add(new Order(
                        orderId,
                        customerName,
                        rs.getString("date_of_order"),
                        rs.getString("order_status"),
                        rs.getString("shipping_address"),
                        items
                ));
            }
        } catch (SQLException ex) {
            System.err.println("ERROR Manager: Fallo al cargar pedidos. Mensaje de SQL: " + ex.getMessage());
            throw new RuntimeException("Fallo en la DB al cargar pedidos.", ex);
        }
        return orders;
    }

    /**
     * Carga los ítems asociados a un pedido específico.
     */
    private List<OrderItem> loadOrderItems(Connection conn, int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String SQL_ITEMS = "SELECT pio.product_quantity, p.unit_price, p.product_name " +
                "FROM products_in_the_order pio " +
                "JOIN products p ON pio.product_id = p.product_id " +
                "WHERE pio.order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQL_ITEMS)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new OrderItem(
                            rs.getString("product_name"),
                            rs.getInt("product_quantity"),
                            rs.getDouble("unit_price")
                    ));
                }
            }
        }
        return items;
    }


    // --- Lógica de Actualización y Creación ---

    /**
     * Actualiza el estado de un pedido en la DB.
     */
    public boolean updateOrderStatus(int orderId, String newStatus) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            System.err.println("ERROR Manager: El nuevo estado no puede estar vacío.");
            return false;
        }

        String statusToSend = newStatus.trim();

        System.out.println("DEBUG: Enviando estado a DB: " + statusToSend);

        String SQL = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, statusToSend);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("ERROR Manager al actualizar estado. Valor enviado: " + statusToSend + ". Mensaje SQL: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Crea un nuevo pedido y sus ítems en una transacción.
     */
    public int createNewOrder(String customerName, String date, String shippingAddress, List<OrderItem> items) {
        Connection conn = null;
        int newCustomerId = -1;
        int newOrderId = -1;

        // 1. Calcular el total_amount (total de la orden)
        double totalAmount = 0.0;
        for (OrderItem item : items) {
            totalAmount += item.getQuantity() * item.getUnitPrice();
        }

        System.out.println("DEBUG: Monto total calculado: " + totalAmount);

        try {
            conn = PostgresConnector.getConnection();
            conn.setAutoCommit(false); // Iniciar Transacción

            // 2. Insertar cliente (o encontrar ID)
            // Asume que el nombre está en formato "Nombre Apellido Apellido2"
            String[] parts = customerName.split(" ", 2);
            String firstName = parts[0];
            String lastName = (parts.length > 1) ? parts[1] : "";

            newCustomerId = findOrCreateCustomer(conn, firstName, lastName, shippingAddress);

            // 3. Insertar Pedido principal
            String SQL_ORDER = "INSERT INTO orders (customer_id, date_of_order, order_status, total_amount, shipping_address) VALUES (?, ?, ?, ?, ?) RETURNING order_id";
            try (PreparedStatement pstmt = conn.prepareStatement(SQL_ORDER)) {
                pstmt.setInt(1, newCustomerId);
                pstmt.setDate(2, Date.valueOf(date));
                pstmt.setString(3, "Pendiente"); // Estado inicial
                pstmt.setDouble(4, totalAmount);
                pstmt.setString(5, shippingAddress);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    newOrderId = rs.getInt(1);
                }
            }

            // 4. Insertar Ítems del Pedido
            if (newOrderId != -1) {
                String SQL_ITEMS = "INSERT INTO products_in_the_order (order_id, product_id, product_quantity, item_unit_price) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(SQL_ITEMS)) {
                    for (OrderItem item : items) {
                        int productId = getProductIdByName(conn, item.getProductName());

                        pstmt.setInt(1, newOrderId);
                        pstmt.setInt(2, productId);
                        pstmt.setInt(3, item.getQuantity());
                        pstmt.setDouble(4, item.getUnitPrice());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            } else {
                conn.rollback();
                return -1;
            }

            conn.commit(); // Confirmar Transacción
            return newOrderId;

        } catch (SQLException ex) {
            System.err.println("ERROR Manager al crear pedido (Transacción fallida). Mensaje SQL: " + ex.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    System.err.println("Error al hacer rollback: " + e.getMessage());
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    private int findOrCreateCustomer(Connection conn, String firstName, String lastName, String shippingAddress) throws SQLException {
        // 1. Buscar por nombre y apellido
        String SQL_FIND = "SELECT customer_id FROM customers WHERE first_name = ? AND last_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_FIND)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id");
                }
            }
        }

        // 2. Crear si no existe
        String SQL_CREATE = "INSERT INTO customers (first_name, last_name, address) VALUES (?, ?, ?) RETURNING customer_id";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_CREATE)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, shippingAddress);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Fallo al encontrar o crear cliente.");
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