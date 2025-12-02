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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * OrdersAdminController: Capa de Presentación y Control.
 * Maneja la interacción del usuario y actualiza la vista,
 * utilizando OrdersAdminManager para la lógica de negocio.
 * * Incluye lógica de ordenación personalizada: Pendientes más antiguos primero.
 */
public class OrdersAdminController {

    // --- Componentes FXML ---

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
    // ComboBox para el autocompletado de clientes
    @FXML private ComboBox<String> newCustomerNameComboBox;
    @FXML private TextField newShippingAddressField;
    @FXML private ComboBox<AvailableProduct> productComboBox;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private TableView<OrderItem> newOrderItemsTable;
    @FXML private TableColumn<OrderItem, String> newItemProductCol;
    @FXML private TableColumn<OrderItem, Integer> newItemQuantityCol;
    @FXML private TableColumn<OrderItem, Double> newItemPriceCol;
    @FXML private Label totalLabel;
    @FXML private Button placeOrderButton;

    // --- Propiedades y Servicios ---
    private final OrdersAdminManager manager;
    private ObservableList<Order> ordersList;
    private ObservableList<OrderItem> newOrderItemsList;
    // Lista completa de nombres de clientes para el autocompletado
    private ObservableList<String> allCustomerNames;

    public OrdersAdminController() {
        this.manager = new OrdersAdminManager();
    }

    // --- Inicialización ---

    @FXML
    public void initialize() {
        ordersList = FXCollections.observableArrayList();
        newOrderItemsList = FXCollections.observableArrayList();
        allCustomerNames = FXCollections.observableArrayList();

        setupOrdersTable();
        setupNewOrderItemsTable();
        setupStatusComboBox();
        loadInitialData();
        setupListeners();
        setupProductCreationControls();
        setupCustomerAutocomplete();
    }

    /**
     * Mapea el estado de la DB (Ej: 'Pendiente') a la visualización en español.
     * Mapeamos 'Entregado' de la DB a 'Completado' para la UI.
     */
    private String mapDbStatusToDisplayStatus(String dbStatus) {
        if (dbStatus == null) return "Desconocido";

        // El mapeo de la UI debe coincidir con los valores mostrados en el ComboBox y la tabla
        return switch (dbStatus.trim()) {
            case "Pendiente" -> "Pendiente";
            case "Procesando" -> "Procesando";
            case "Enviado" -> "Enviado";
            case "Entregado" -> "Completado"; // Mapeamos 'Entregado' de DB a 'Completado' para la UI
            case "Cancelado" -> "Cancelado";
            default -> dbStatus;
        };
    }

    /**
     * Mapea el estado de la visualización en español al valor EXACTO requerido por la DB.
     * Mapeamos 'Completado' de la UI al valor de la DB 'Entregado'.
     */
    private String mapDisplayStatusToDbStatus(String displayStatus) {
        if (displayStatus == null) return "Pendiente";

        // Debe coincidir EXACTAMENTE con los valores de la restricción CHECK.
        return switch (displayStatus.trim()) {
            case "Pendiente" -> "Pendiente";
            case "Procesando" -> "Procesando";
            case "Enviado" -> "Enviado";
            case "Completado" -> "Entregado"; // El valor que espera la DB
            case "Cancelado" -> "Cancelado";
            default -> "Pendiente";
        };
    }


    private void setupOrdersTable() {
        orderIdCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOrderId()));
        customerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));

        // La columna de estado ahora usa el mapeo para mostrar el estado en español
        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(mapDbStatusToDisplayStatus(cellData.getValue().getStatus()))
        );

        // Columna de acciones (botón "Ver Detalles")
        actionsCol.setCellFactory(param -> new TableCell<>() {
            final Button btn = new Button("Ver Detalles");
            {
                btn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    ordersTable.getSelectionModel().select(order);
                });
                btn.setStyle("-fx-font-size: 10px; -fx-padding: 3 5; -fx-background-color: #00796B; -fx-text-fill: white;");
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
        newItemProductCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        newItemQuantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        // Muestra el subtotal del ítem
        newItemPriceCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalPrice()));
        newOrderItemsTable.setItems(newOrderItemsList);
    }

    private void setupStatusComboBox() {
        // Los valores en el ComboBox son en español para la UI
        statusComboBox.getItems().addAll("Pendiente", "Procesando", "Enviado", "Completado", "Cancelado");
        statusComboBox.getSelectionModel().selectFirst();
    }

    private void setupProductCreationControls() {
        if (productComboBox == null || quantitySpinner == null) {
            System.err.println("FATAL ERROR FXML: Componentes de producto/cantidad no inicializados.");
            return;
        }

        // Cargar productos disponibles para el ComboBox
        try {
            List<AvailableProduct> products = manager.loadAvailableProducts();
            productComboBox.getItems().addAll(products);

            // Configurar la visualización del producto (Nombre y Precio)
            productComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(AvailableProduct item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName() + " ($" + String.format("%.2f", item.getPrice()) + ")");
                }
            });
            productComboBox.setButtonCell(productComboBox.getCellFactory().call(null));

            if (!products.isEmpty()) {
                productComboBox.getSelectionModel().selectFirst();
            }
        } catch (RuntimeException e) {
            System.err.println("Error al cargar datos del Manager: " + e.getMessage());
            showAlert("Error de Carga de Datos", "No se pudieron cargar los productos disponibles: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        // Inicializar Spinner de Cantidad
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        // Listener para actualizar el total al añadir o quitar items
        newOrderItemsList.addListener((javafx.beans.InvalidationListener) observable -> updateTotal());
        updateTotal(); // Calcular el total inicial (0.0)
    }

    /**
     * Configura el ComboBox del cliente para tener funcionalidad de autocompletado.
     */
    private void setupCustomerAutocomplete() {
        // 1. Cargar la lista completa de nombres de clientes
        try {
            List<String> names = manager.loadAllCustomerNames();
            allCustomerNames.setAll(names); // Usamos setAll para manejar la recarga
        } catch (RuntimeException e) {
            System.err.println("Error al cargar nombres de clientes para autocompletar: " + e.getMessage());
        }

        newCustomerNameComboBox.setItems(allCustomerNames);
        newCustomerNameComboBox.setEditable(true);

        // 2. Implementar el filtro de autocompletado
        newCustomerNameComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                // Si el campo está vacío, restaurar todos los ítems y ocultar.
                newCustomerNameComboBox.setItems(allCustomerNames);
                newCustomerNameComboBox.hide();
                return;
            }

            // Crear una lista filtrada (no sensible a mayúsculas/minúsculas)
            List<String> filteredList = allCustomerNames.stream()
                    .filter(name -> name.toLowerCase().startsWith(newValue.toLowerCase()))
                    .toList();

            // Actualizar la lista visible en el ComboBox
            newCustomerNameComboBox.setItems(FXCollections.observableArrayList(filteredList));

            // Mostrar el ComboBox si hay ítems filtrados
            if (!filteredList.isEmpty() && !newCustomerNameComboBox.isShowing()) {
                newCustomerNameComboBox.show();
            } else if (filteredList.isEmpty()) {
                newCustomerNameComboBox.hide();
            }
        });
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
     * Carga los datos iniciales de la base de datos y los ordena según los criterios:
     * 1. Pendientes primero.
     * 2. Fecha más antigua primero.
     */
    private void loadInitialData() {
        try {
            List<Order> loadedOrders = manager.loadAllOrders();

            // --- Lógica de Ordenación Solicitada ---
            loadedOrders.sort(getOrderComparator());
            // ----------------------------------------

            ordersList.setAll(loadedOrders);
            if (!loadedOrders.isEmpty()) {
                ordersTable.getSelectionModel().selectFirst();
            }
        } catch (RuntimeException e) {
            System.err.println("Error al cargar datos del Manager: " + e.getMessage());
            showAlert("Error de Conexión", "No se pudo conectar a la base de datos o cargar los pedidos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Define el comparador personalizado para ordenar los pedidos:
     * 1. Prioridad para estado "Pendiente" (true = aparece primero).
     * 2. Luego, por Fecha (Ascendente: más antigua primero).
     */
    private Comparator<Order> getOrderComparator() {
        return Comparator
                // 1. Ordenar por Estado (Pendiente primero)
                // Corregido: Eliminado el argumento de tipo <Order> que causaba el error de compilación.
                .comparing((Order order) -> Objects.equals(order.getStatus(), "Pendiente"), Comparator.reverseOrder())
                // 2. Luego, por Fecha (Ascendente: más antigua primero)
                .thenComparing(order -> {
                    try {
                        // Convertir la fecha String a LocalDate para una comparación adecuada (ISO_DATE: YYYY-MM-DD)
                        return LocalDate.parse(order.getDate(), DateTimeFormatter.ISO_DATE);
                    } catch (Exception e) {
                        // Manejar error de parseo, asignando la fecha mínima para que aparezca al inicio
                        System.err.println("Advertencia: Error al parsear fecha del pedido ID " + order.getOrderId() + ". Usando fecha mínima.");
                        return LocalDate.MIN;
                    }
                });
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
        // Mapear el estado de la DB a español para la vista
        sb.append(String.format("Estado Actual: %s\n\n", mapDbStatusToDisplayStatus(order.getStatus())));
        sb.append("--- Ítems del Pedido ---\n");

        double total = 0.0;
        for (OrderItem item : order.getItems()) {
            sb.append(String.format("- %s (x%d) @ %.2f = %.2f\n",
                    item.getProductName(), item.getQuantity(), item.getUnitPrice(), item.getTotalPrice()));
            total += item.getTotalPrice();
        }
        sb.append(String.format("\nTOTAL DEL PEDIDO: $%.2f", total));
        detailsTextArea.setText(sb.toString());

        // Sincronizar ComboBox de estado con el estado traducido
        statusComboBox.getSelectionModel().select(mapDbStatusToDisplayStatus(order.getStatus()));
    }

    /**
     * Maneja la acción de actualizar el estado del pedido en la DB.
     */
    @FXML
    private void handleUpdateStatus() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        String displayStatus = statusComboBox.getSelectionModel().getSelectedItem(); // Valor en español de la UI ("Completado")

        if (selectedOrder == null || displayStatus == null) {
            showAlert("Error", "Selecciona un pedido y un nuevo estado.", Alert.AlertType.WARNING);
            return;
        }

        // Se traduce el estado de la UI ('Completado') al valor de la DB ('Entregado')
        String dbStatus = mapDisplayStatusToDbStatus(displayStatus);
        System.out.println("DEBUG: Enviando estado a DB (Valor de DB): " + dbStatus); // Para verificar

        if (manager.updateOrderStatus(selectedOrder.getOrderId(), dbStatus)) {

            int index = ordersList.indexOf(selectedOrder);
            if (index != -1) {
                // Crear una copia inmutable con el nuevo estado (en formato DB)
                Order updatedOrder = new Order(
                        selectedOrder.getOrderId(),
                        selectedOrder.getCustomerName(),
                        selectedOrder.getDate(),
                        dbStatus, // Estado actualizado (en el formato de la DB)
                        selectedOrder.getShippingAddress(),
                        selectedOrder.getItems()
                );

                // 1. Actualizar el objeto Order en la lista observable.
                ordersList.set(index, updatedOrder);

                // 2. Reordenar toda la lista. Esto moverá el pedido si su estado Pendiente cambió.
                ordersList.sort(getOrderComparator());

                // 3. Volver a seleccionar el pedido actualizado.
                ordersTable.getSelectionModel().select(updatedOrder);
                showOrderDetails(updatedOrder); // Refrescar detalles
            }
            // Notificar usando el término de la UI
            showAlert("Éxito", "Estado del pedido actualizado a " + displayStatus + " correctamente.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error de DB", "No se pudo actualizar el estado del pedido. Revisa los logs del Manager para detalles.", Alert.AlertType.ERROR);
        }
    }

    // --- Lógica de Creación de Nuevos Pedidos ---

    /**
     * Maneja la acción de añadir un ítem a la tabla del nuevo pedido.
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
     */
    @FXML
    private void handlePlaceOrder() {
        // OBTENEMOS EL VALOR DEL COMBOBOX/EDITOR
        String customerName = newCustomerNameComboBox.getValue() != null ? newCustomerNameComboBox.getValue().trim() : "";
        String shippingAddress = newShippingAddressField.getText().trim();

        // 1. Validación de Campos
        if (customerName.isEmpty()) {
            showAlert("Validación", "Debes ingresar el nombre del cliente.", Alert.AlertType.WARNING);
            return;
        }

        if (shippingAddress.isEmpty()) {
            showAlert("Validación", "Debes ingresar la dirección de envío.", Alert.AlertType.WARNING);
            return;
        }

        if (newOrderItemsList.isEmpty()) {
            showAlert("Validación", "Debes añadir al menos un producto.", Alert.AlertType.WARNING);
            return;
        }

        String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        List<OrderItem> itemsToSave = new ArrayList<>(newOrderItemsList);

        // El Manager se encarga de findOrCreateCustomer con el string del nombre
        int newOrderId = manager.createNewOrder(customerName, currentDate, shippingAddress, itemsToSave);

        if (newOrderId != -1) {
            showAlert("Éxito", "El nuevo pedido (ID: " + newOrderId + ") ha sido creado y guardado en la DB.", Alert.AlertType.INFORMATION);
            loadInitialData(); // Recargar y reordenar toda la lista, incluyendo el nuevo pedido
            clearNewOrderForm();
        } else {
            showAlert("Error de DB", "Fallo al guardar el nuevo pedido en la base de datos. Revisa la consola para detalles.", Alert.AlertType.ERROR);
        }

        // Tras crear un pedido, es posible que se haya añadido un nuevo cliente,
        // por lo que recargamos la lista de autocompletado.
        setupCustomerAutocomplete();
    }

    /**
     * Limpia los campos del formulario de creación de nuevos pedidos.
     */
    private void clearNewOrderForm() {
        // Limpiar ComboBox
        newCustomerNameComboBox.getSelectionModel().clearSelection();
        newCustomerNameComboBox.setValue(null);
        newShippingAddressField.clear();
        newOrderItemsList.clear();
        if (productComboBox.getSelectionModel().getSelectedItem() != null) {
            productComboBox.getSelectionModel().selectFirst();
        }
        quantitySpinner.getValueFactory().setValue(1);
        updateTotal();
    }

    /**
     * Método auxiliar para mostrar alertas de forma consistente.
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}