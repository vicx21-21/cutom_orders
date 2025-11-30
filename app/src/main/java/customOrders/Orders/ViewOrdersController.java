package customOrders.Orders;

// Importaciones necesarias para JavaFX
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// Importaciones para Base de Datos
import customOrders.PostgresConnector; // Se asume que esta clase proporciona la conexión
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de historial de órdenes del cliente.
 * La lógica de acceso a la base de datos (DB) se encuentra directamente en esta clase,
 * sin utilizar OrderManager.
 * Muestra el resumen de órdenes (Order) y el detalle de ítems (OrderItem).
 */
public class ViewOrdersController implements Initializable {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ID del cliente actual (fijo para prueba)
    private static final int CURRENT_CUSTOMER_ID = 101;

    // Lista de datos para la tabla principal
    private ObservableList<Order> orderData;

    // --- FXML Fields: Tabla de Resumen de Órdenes (Order) ---
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, LocalDate> dateColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, Double> totalColumn;

    // --- FXML Fields: Sección de Detalles del Pedido (OrderItem) ---
    @FXML private Label orderDetailsTitle;
    @FXML private Label orderIdDetailLabel;

    // Usando su clase OrderItem
    @FXML private TableView<OrderItem> itemsTable;
    @FXML private TableColumn<OrderItem, String> itemNameColumn;
    @FXML private TableColumn<OrderItem, Integer> itemQuantityColumn;
    @FXML private TableColumn<OrderItem, Double> itemUnitPriceColumn;
    @FXML private TableColumn<OrderItem, Double> itemSubtotalColumn;

    @FXML private Label messageLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // 1. Inicialización de datos
        orderData = FXCollections.observableArrayList();
        ordersTable.setItems(orderData);

        // 2. Configuración de Columnas de Resumen de Órdenes (Order)
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("order_id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_of_order"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("order_status"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total_amount"));

        // Formateadores de celda para fechas y moneda
        dateColumn.setCellFactory(column -> new FormattedTableCellFactory<>(dateFormatter));
        totalColumn.setCellFactory(column -> new CurrencyCellFactory<>());

        // 3. Configuración de Columnas de Detalle de Ítems (OrderItem)
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        itemUnitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        itemSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        itemUnitPriceColumn.setCellFactory(column -> new CurrencyCellFactory<>());
        itemSubtotalColumn.setCellFactory(column -> new CurrencyCellFactory<>());

        // 4. Listener de Selección: Cargar ítems cuando se selecciona una orden
        ordersTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showOrderDetails(newValue));

        // 5. Carga inicial de datos para el cliente por defecto
        loadCustomerOrders(CURRENT_CUSTOMER_ID);

        orderDetailsTitle.setText("Seleccione un Pedido en la tabla superior.");
    }

    /**
     * Obtiene el listado de todos los pedidos (Order) de un cliente específico.
     * MÉTODOS DE DB MOVIDOS DIRECTAMENTE AL CONTROLADOR.
     */
    private List<Order> getOrdersByCustomer(int customerId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, date_of_order, order_status, total_amount, shipping_address " +
                "FROM orders " +
                "WHERE customer_id = ? " +
                "ORDER BY date_of_order DESC";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    LocalDate dateOfOrder = rs.getDate("date_of_order").toLocalDate();
                    String status = rs.getString("order_status");
                    String address = rs.getString("shipping_address");

                    // Order requiere una lista de ítems en el constructor. Para la vista de resumen,
                    // la dejamos vacía y confiamos en el total_amount de la BD.
                    Order order = new Order(
                            orderId,
                            customerId,
                            dateOfOrder,
                            status,
                            address,
                            new ArrayList<>()
                    );
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    /**
     * Obtiene los ítems detallados (OrderItem) para una orden específica.
     * MÉTODOS DE DB MOVIDOS DIRECTAMENTE AL CONTROLADOR.
     */
    private List<OrderItem> getItemsForOrder(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();

        // Une products_in_the_order con products para obtener el nombre y el precio unitario
        String sql = "SELECT pio.product_quantity, p.product_name, p.unit_price " +
                "FROM products_in_the_order pio " +
                "JOIN products p ON pio.product_id = p.product_id " +
                "WHERE pio.order_id = ?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("product_name");
                    int quantityOrdered = rs.getInt("product_quantity");
                    double unitPrice = rs.getDouble("unit_price");

                    // Crea OrderItem (el subtotal se calcula dentro de OrderItem)
                    OrderItem item = new OrderItem(productName, quantityOrdered, unitPrice);
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * Carga todas las órdenes del cliente especificado llamando al método DB interno.
     */
    public void loadCustomerOrders(int customerId) {
        try {
            orderData.setAll(getOrdersByCustomer(customerId));
            setMessage("Pedidos cargados para el Cliente ID: " + customerId + ". Total: " + orderData.size(), false);
        } catch (SQLException e) {
            setMessage("ERROR al cargar los pedidos. Verifique la conexión DB y OrderItem/Order.", true);
            e.printStackTrace();
            orderData.clear();
        }
    }

    /**
     * Carga y muestra los ítems de detalle (OrderItem) de la orden seleccionada.
     */
    private void showOrderDetails(Order order) {
        if (order != null) {
            try {
                // Llama al método DB interno
                List<OrderItem> items = getItemsForOrder(order.getOrder_id());
                itemsTable.getItems().setAll(items);

                // Actualizar etiquetas con detalles de la orden
                orderDetailsTitle.setText("Contenido del Pedido ID: " + order.getOrder_id());
                orderIdDetailLabel.setText(
                        String.format("Fecha: %s | Estado: %s | Dirección: %s | Total: € %.2f",
                                dateFormatter.format(order.getDate_of_order()),
                                order.getOrder_status(),
                                order.getShipping_address(),
                                order.getTotal_amount()));
                setMessage("Detalles del Pedido " + order.getOrder_id() + " cargados.", false);

            } catch (SQLException e) {
                setMessage("ERROR al cargar los detalles del pedido " + order.getOrder_id() + ": " + e.getMessage(), true);
                itemsTable.getItems().clear();
                e.printStackTrace();
            }

        } else {
            // Limpiar la vista si no hay selección
            orderDetailsTitle.setText("Seleccione un Pedido para ver el Contenido");
            orderIdDetailLabel.setText("Órdenes cargadas para el Cliente ID: " + CURRENT_CUSTOMER_ID);
            itemsTable.getItems().clear();
            setMessage("", false);
        }
    }

    /**
     * Muestra un mensaje de estado/error en la etiqueta inferior.
     */
    private void setMessage(String message, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            if (isError) {
                messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else {
                messageLabel.setStyle("-fx-text-fill: green;");
            }
        }
    }

    // Clase auxiliar estática para formatear celdas de TableView con fecha
    private static class FormattedTableCellFactory<S, T> extends TableCell<S, T> {
        private final DateTimeFormatter dateTimeFormatter;

        public FormattedTableCellFactory(DateTimeFormatter dateTimeFormatter) {
            this.dateTimeFormatter = dateTimeFormatter;
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                if (item instanceof LocalDate) {
                    setText(dateTimeFormatter.format((LocalDate) item));
                } else {
                    setText(item.toString());
                }
            }
        }
    }

    // Clase auxiliar estática para formatear celdas de TableView con moneda (€)
    private static class CurrencyCellFactory<S> extends TableCell<S, Double> {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(String.format("€ %.2f", item));
            }
        }
    }
}