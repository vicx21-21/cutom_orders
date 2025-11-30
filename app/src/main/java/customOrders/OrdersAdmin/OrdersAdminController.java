package customOrders.OrdersAdmin;

import customOrders.OrdersAdmin.OrdersAdminManager.AvailableProduct;
import customOrders.OrdersAdmin.OrdersAdminManager.Order;
import customOrders.OrdersAdmin.OrdersAdminManager.OrderItem;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * OrdersAdminController: Capa de Presentación y Control.
 * Maneja la interacción del usuario y actualiza la vista,
 * utilizando OrdersAdminManager para la lógica de negocio.
 *
 * NOTA: Usa los métodos getter (e.g., getOrderId()) de las clases tradicionales.
 */
public class OrdersAdminController {

    // --- Componentes FXML (Sincronizados con OrdersAdminView.fxml) ---

    // TABLA PRINCIPAL DE PEDIDOS
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdCol;
    @FXML private TableColumn<Order, String> customerCol;
    @FXML private TableColumn<Order, String> dateCol;
    @FXML private TableColumn<Order, String> statusCol;
    @FXML private TableColumn<Order, Void> actionsCol; // Columna de botones "Ver Detalles"

    // SECCIÓN DE DETALLES Y ACTUALIZACIÓN
    @FXML private TextArea detailsTextArea;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button updateStatusButton;

    // SECCIÓN DE CREACIÓN DE NUEVOS PEDIDOS
    @FXML private TextField newCustomerNameField;
    @FXML private TextField newShippingAddressField;
    @FXML private ComboBox<AvailableProduct> productComboBox;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private TableView<OrderItem> newOrderItemsTable;
    @FXML private TableColumn<OrderItem, String> newItemProductCol;
    @FXML private TableColumn<OrderItem, Integer> newItemQuantityCol;
    @FXML private TableColumn<OrderItem, Double> newItemPriceCol; // Esta columna muestra el subtotal
    @FXML private Label totalLabel;
    @FXML private Button placeOrderButton;

    // --- Propiedades y Servicios ---
    private final OrdersAdminManager manager;
    private ObservableList<Order> ordersList;
    private ObservableList<OrderItem> newOrderItemsList;

    public OrdersAdminController() {
        this.manager = new OrdersAdminManager();
    }

    // --- Inicialización ---

    @FXML
    public void initialize() {
        ordersList = FXCollections.observableArrayList();
        newOrderItemsList = FXCollections.observableArrayList();

        setupOrdersTable();
        setupNewOrderItemsTable();
        setupStatusComboBox();
        loadInitialData();
        setupListeners();
        setupProductCreationControls();
    }

    private void setupOrdersTable() {
        // Usamos getters de la clase Order del Manager
        orderIdCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOrderId()));
        customerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        // Columna de acciones (botón "Ver Detalles" que dispara el listener de selección)
        actionsCol.setCellFactory(param -> new TableCell<>() {
            final Button btn = new Button("Ver Detalles");
            {
                btn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    ordersTable.getSelectionModel().select(order); // Selecciona la fila y dispara el listener
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        ordersTable.setItems(ordersList);
    }

    private void setupNewOrderItemsTable() {
        // Usamos getters de la clase OrderItem del Manager
        newItemProductCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        newItemQuantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        // Muestra el subtotal del ítem
        newItemPriceCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalPrice()));
        newOrderItemsTable.setItems(newOrderItemsList);
    }

    private void setupStatusComboBox() {
        statusComboBox.getItems().addAll("Pendiente", "Procesando", "Enviado", "Completado", "Cancelado");
        statusComboBox.getSelectionModel().selectFirst(); // Estado por defecto
    }

    private void setupProductCreationControls() {
        // Cargar productos disponibles para el ComboBox (AvailableProduct usa getters)
        try {
            List<AvailableProduct> products = manager.loadAvailableProducts();
            productComboBox.getItems().addAll(products);
            // El ComboBox necesita un CellFactory si quieres mostrar el nombre del producto y no el objeto completo
            productComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(AvailableProduct item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " ($" + String.format("%.2f", item.getPrice()) + ")");
                }
            });
            productComboBox.setButtonCell(productComboBox.getCellFactory().call(null)); // Para la selección mostrada

            productComboBox.getSelectionModel().selectFirst();
        } catch (RuntimeException e) {
            showAlert("Error de DB", "No se pudieron cargar los productos disponibles: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        // Inicializar Spinner de Cantidad
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        // Listener para actualizar el total al añadir o quitar items
        newOrderItemsList.addListener((javafx.beans.InvalidationListener) observable -> updateTotal());
        updateTotal(); // Calcular el total inicial (0.0)
    }

    private void setupListeners() {
        // Listener para la selección de pedidos en la tabla
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showOrderDetails(newSelection);
            } else {
                detailsTextArea.clear();
            }
        });

        // Deshabilitar botón de actualizar si no hay pedido seleccionado
        updateStatusButton.disableProperty().bind(ordersTable.getSelectionModel().selectedItemProperty().isNull());
    }

    /**
     * Carga los datos iniciales de la base de datos (PostgreSQL).
     */
    private void loadInitialData() {
        try {
            List<Order> loadedOrders = manager.loadAllOrders();
            ordersList.setAll(loadedOrders);
            if (!loadedOrders.isEmpty()) {
                // Selecciona el primer pedido si existe
                ordersTable.getSelectionModel().selectFirst();
            }
        } catch (RuntimeException e) {
            showAlert("Error de Conexión", "No se pudo conectar a la base de datos o cargar los pedidos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // --- Lógica de Pedidos Existentes ---

    /**
     * Muestra los detalles de un pedido seleccionado en el TextArea.
     */
    private void showOrderDetails(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Pedido ID: %d\n", order.getOrderId()));
        sb.append(String.format("Cliente: %s\n", order.getCustomerName()));
        sb.append(String.format("Fecha: %s\n", order.getDate()));
        sb.append(String.format("Dirección: %s\n", order.getShippingAddress()));
        sb.append(String.format("Estado Actual: %s\n\n", order.getStatus()));
        sb.append("--- Ítems del Pedido ---\n");

        double total = 0.0;
        for (OrderItem item : order.getItems()) {
            sb.append(String.format("- %s (x%d) @ %.2f = %.2f\n",
                    item.getProductName(), item.getQuantity(), item.getUnitPrice(), item.getTotalPrice()));
            total += item.getTotalPrice();
        }
        sb.append(String.format("\nTOTAL DEL PEDIDO: $%.2f", total));
        detailsTextArea.setText(sb.toString());

        // Sincronizar ComboBox de estado con el estado actual del pedido
        statusComboBox.getSelectionModel().select(order.getStatus());
    }

    /**
     * Maneja la acción de actualizar el estado del pedido en la DB.
     * Método: handleUpdateStatus
     */
    @FXML
    private void handleUpdateStatus() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        String newStatus = statusComboBox.getSelectionModel().getSelectedItem();

        if (selectedOrder == null || newStatus == null) {
            showAlert("Error", "Selecciona un pedido y un nuevo estado.", Alert.AlertType.WARNING);
            return;
        }

        if (manager.updateOrderStatus(selectedOrder.getOrderId(), newStatus)) {
            // Recrear la orden con el nuevo estado
            Order updatedOrder = new Order(
                    selectedOrder.getOrderId(),
                    selectedOrder.getCustomerName(),
                    selectedOrder.getDate(),
                    newStatus, // Estado actualizado
                    selectedOrder.getShippingAddress(),
                    selectedOrder.getItems()
            );

            // Actualizar la lista observable y la selección
            int index = ordersList.indexOf(selectedOrder);
            if (index != -1) {
                ordersList.set(index, updatedOrder);
                ordersTable.getSelectionModel().select(updatedOrder);
                showOrderDetails(updatedOrder); // Refrescar detalles
            }
            showAlert("Éxito", "Estado del pedido actualizado a " + newStatus + ".", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error de DB", "No se pudo actualizar el estado del pedido.", Alert.AlertType.ERROR);
        }
    }

    // --- Lógica de Creación de Nuevos Pedidos ---

    /**
     * Maneja la acción de añadir un ítem a la tabla del nuevo pedido.
     * Método: handleAddItemToNewOrder
     */
    @FXML
    private void handleAddItemToNewOrder() {
        AvailableProduct selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
        Integer quantity = quantitySpinner.getValue();

        if (selectedProduct == null || quantity == null || quantity < 1) {
            showAlert("Advertencia", "Selecciona un producto y una cantidad válida.", Alert.AlertType.WARNING);
            return;
        }

        OrderItem newItem = new OrderItem(selectedProduct.getName(), quantity, selectedProduct.getPrice());

        // Lógica: Si el producto ya está, sumar la cantidad. Sino, añadir.
        boolean added = false;
        for (int i = 0; i < newOrderItemsList.size(); i++) {
            OrderItem existingItem = newOrderItemsList.get(i);
            if (existingItem.getProductName().equals(newItem.getProductName())) {
                OrderItem mergedItem = new OrderItem(
                        existingItem.getProductName(),
                        existingItem.getQuantity() + newItem.getQuantity(),
                        existingItem.getUnitPrice()
                );
                newOrderItemsList.set(i, mergedItem);
                added = true;
                break;
            }
        }

        if (!added) {
            newOrderItemsList.add(newItem);
        }

        newOrderItemsTable.refresh();
    }

    private void updateTotal() {
        double total = newOrderItemsList.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
        totalLabel.setText(String.format("TOTAL: $%.2f", total));
    }

    /**
     * Maneja la acción de crear el nuevo pedido en la DB.
     * Método: handlePlaceOrder
     */
    @FXML
    private void handlePlaceOrder() {
        String customerName = newCustomerNameField.getText().trim();
        String shippingAddress = newShippingAddressField.getText().trim();

        if (customerName.isEmpty() || shippingAddress.isEmpty() || newOrderItemsList.isEmpty()) {
            showAlert("Validación", "Debes ingresar el nombre del cliente, la dirección y añadir al menos un producto.", Alert.AlertType.WARNING);
            return;
        }

        String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        // Creamos una copia de la lista para pasarla al Manager
        List<OrderItem> itemsToSave = new ArrayList<>(newOrderItemsList);

        int newOrderId = manager.createNewOrder(customerName, currentDate, shippingAddress, itemsToSave);

        if (newOrderId != -1) {
            showAlert("Éxito", "El nuevo pedido (ID: " + newOrderId + ") ha sido creado y guardado en la DB.", Alert.AlertType.INFORMATION);
            // Recargar la lista de pedidos y limpiar el formulario de creación
            loadInitialData(); // Para ver el nuevo pedido en la tabla principal
            clearNewOrderForm();
        } else {
            showAlert("Error de DB", "Fallo al guardar el nuevo pedido en la base de datos. Revisa la consola.", Alert.AlertType.ERROR);
        }
    }

    private void clearNewOrderForm() {
        newCustomerNameField.clear();
        newShippingAddressField.clear();
        newOrderItemsList.clear();
        productComboBox.getSelectionModel().selectFirst();
        quantitySpinner.getValueFactory().setValue(1);
        updateTotal();
    }

    // --- Utilidades ---

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
