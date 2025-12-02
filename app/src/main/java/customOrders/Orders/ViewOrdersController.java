package customOrders.Orders;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty; // Importación necesaria para SimpleStringProperty

// Importaciones para Base de Datos
import customOrders.PostgresConnector;
import customOrders.Products.Product; // Necesario para la entidad Producto
import customOrders.CustomerAware;
import customOrders.Customer;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.ResourceBundle;

// Importaciones para JavaFX
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell; // Necesario para CellFactory
import javafx.scene.control.cell.PropertyValueFactory;

// Clases de modelos (asumo que están en el mismo paquete o importadas correctamente)
import customOrders.Orders.Order;
import customOrders.Orders.ProductInOrder;

/**
 * Controlador para la vista de historial de órdenes del cliente.
 * Muestra el resumen de órdenes (Order) y el detalle de ítems (ProductInOrder).
 */
public class ViewOrdersController implements Initializable, CustomerAware {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ID del cliente actual (fijo para prueba)
    private int currentCustomerId;

    // Lista de datos para la tabla principal
    private ObservableList<Order> orderData;

    // --- FXML Fields: Tabla de Resumen de Órdenes (Order) ---
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, LocalDate> dateColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, Double> totalColumn;

    // --- FXML Fields: Sección de Detalles del Pedido (ProductInOrder) ---
    @FXML private Label orderDetailsTitle;
    @FXML private Label orderIdDetailLabel;

    // Usando su clase ProductInOrder para los detalles
    @FXML private TableView<ProductInOrder> itemsTable;
    @FXML private TableColumn<ProductInOrder, String> itemNameColumn;
    @FXML private TableColumn<ProductInOrder, Integer> itemQuantityColumn;
    @FXML private TableColumn<ProductInOrder, Double> itemUnitPriceColumn;
    @FXML private TableColumn<ProductInOrder, Double> itemSubtotalColumn;

    @FXML private Label messageLabel;

    /**
     * Implementación del método de la interfaz CustomerAware.
     * Este es el punto de entrada para inyectar el ID del cliente.
     */
    @Override
    public void setCustomer(Customer customer) {
        this.currentCustomerId = customer.getCustomerID(); // Getter corregido: getCustomerID()
        // 5. Carga inicial de datos para el cliente inyectado
        loadCustomerOrders(this.currentCustomerId);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // 1. Inicialización de datos
        orderData = FXCollections.observableArrayList();
        ordersTable.setItems(orderData);

        // 2. Configuración de Columnas de Resumen de Órdenes (Order)
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("order_id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_of_order"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("order_status"));
        // El PropertyValueFactory está bien, pero el objeto Order debe tener el valor correcto
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total_amount"));

        // Formateadores de celda para fechas y moneda
        dateColumn.setCellFactory(column -> new FormattedTableCellFactory<>(dateFormatter));
        totalColumn.setCellFactory(column -> new CurrencyCellFactory<>());

        // 3. Configuración de Columnas de Detalle de Ítems (ProductInOrder)

        // Mapeo del nombre del producto (requiere acceder a Product dentro de ProductInOrder)
        itemNameColumn.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getProduct().getProduct_name();
            return new SimpleStringProperty(name);
        });

        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // CORRECCIÓN: Usar getUnit_price() del modelo ProductInOrder
        itemUnitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unit_price"));

        // Mapeamos el subtotal a getTotalPrice() de ProductInOrder
        itemSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        itemUnitPriceColumn.setCellFactory(column -> new CurrencyCellFactory<>());
        itemSubtotalColumn.setCellFactory(column -> new CurrencyCellFactory<>());

        // 4. Listener de Selección: Cargar ítems cuando se selecciona una orden
        ordersTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showOrderDetails(newValue));

        // NOTA: La carga inicial de datos (loadCustomerOrders) se movió al método setCustomer(int customerId)
        // para asegurar que el ID del cliente haya sido inyectado antes de consultar la DB.

        orderDetailsTitle.setText("Seleccione un Pedido en la tabla superior.");
        orderIdDetailLabel.setText("Esperando ID de Cliente...");
    }

    /**
     * Obtiene el listado de todos los pedidos (Order) de un cliente específico.
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
                    // *** VALOR CLAVE: total_amount que viene de la DB ***
                    double totalAmountFromDB = rs.getDouble("total_amount");
                    String address = rs.getString("shipping_address");

                    // Llamando al constructor de 6 parámetros de Order: (id, customer_id, date, status, address, items)
                    Order order = new Order(
                            orderId,
                            customerId,
                            dateOfOrder,
                            status,
                            address,
                            new ArrayList<>() // Lista vacía de ProductInOrder
                    );

                    // *** CORRECCIÓN CRUCIAL: Inicializar el total_amount con el valor de la DB ***
                    // Asumimos que la clase Order tiene un setter llamado setTotal_amount(double)
                    // o un constructor alternativo que acepte este valor.
                    // Si no existe setTotal_amount(), esto podría fallar.
                    // Si existe el setter, es la solución más limpia:
                    order.setTotal_amount(totalAmountFromDB);

                    orders.add(order);
                }
            }
        }
        return orders;
    }

    /**
     * Obtiene los ítems detallados (ProductInOrder) para una orden específica.
     */
    private List<ProductInOrder> getItemsForOrder(int orderId) throws SQLException {
        List<ProductInOrder> items = new ArrayList<>();

        // Traemos el precio histórico (pio.item_unit_price) para garantizar precisión
        String sql = "SELECT pio.product_id, pio.product_quantity, pio.item_unit_price, p.product_name " +
                "FROM products_in_the_order pio " +
                "JOIN products p ON pio.product_id = p.product_id " +
                "WHERE pio.order_id = ?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    String productName = rs.getString("product_name");
                    int quantityOrdered = rs.getInt("product_quantity");
                    double historicalUnitPrice = rs.getDouble("item_unit_price"); // Precio registrado en la venta

                    // 1. Crear el objeto Producto TEMPORAL (con el constructor de 14 argumentos).
                    // Inyectamos el precio histórico en el campo unit_price para que ProductInOrder lo use.
                    // Los demás campos se dejan en null/placeholder.
                    Product product = new Product(
                            productId,              // product_id
                            null,                   // product_type_code
                            null,                   // supplier_id
                            productName,            // product_name
                            historicalUnitPrice,    // <<< ¡CLAVE! Usamos el precio histórico aquí
                            null, null, null, null, // description, reorder_level, reorder_quantity, other_details
                            null, null, null, null, // weight_kg, date_added, is_active, quantity
                            null                    // image_url
                    );

                    // 2. Crear ProductInOrder (usará el precio inyectado en el objeto Product).
                    ProductInOrder item = new ProductInOrder(product, quantityOrdered);
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
            orderIdDetailLabel.setText("Órdenes cargadas para el Cliente ID: " + customerId);
        } catch (SQLException e) {
            setMessage("ERROR al cargar los pedidos. Verifique la conexión DB y modelos de datos. Mensaje: " + e.getMessage(), true);
            e.printStackTrace();
            orderData.clear();
        }
    }

    /**
     * Carga y muestra los ítems de detalle (ProductInOrder) de la orden seleccionada.
     */
    private void showOrderDetails(Order order) {
        if (order != null) {
            try {
                // 1. Cargar ítems y actualizar el modelo Order
                List<ProductInOrder> items = getItemsForOrder(order.getOrder_id());
                order.setItems(items); // Esto asegura que el total_amount de la orden se recalcule correctamente
                itemsTable.getItems().setAll(items);

                // *** CORRECCIÓN ADICIONAL: Forzar el refresco de la tabla principal ***
                // Esto garantiza que el total_amount (que se recalcula en order.setItems())
                // se muestre inmediatamente en la columna totalColumn, resolviendo el síntoma
                // de tener que "picarle a la flecha" para que aparezca.
                ordersTable.refresh();

                // Actualizar etiquetas con detalles de la orden
                orderDetailsTitle.setText("Contenido del Pedido ID: " + order.getOrder_id());
                orderIdDetailLabel.setText(
                        String.format("Fecha: %s | Estado: %s | Dirección: %s | Total: € %.2f",
                                dateFormatter.format(order.getDate_of_order()),
                                order.getOrder_status(),
                                order.getShipping_address(),
                                // Llama a calculateTotal() dentro del modelo Order.
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
            orderIdDetailLabel.setText("Órdenes cargadas para el Cliente ID: " + currentCustomerId);
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