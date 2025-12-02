package customOrders.Orders;

import customOrders.Customer;
import customOrders.CustomerAware;
import customOrders.Products.ProductManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import customOrders.Orders.CreateOrderManager;
import customOrders.Orders.ProductInOrder;
import customOrders.Products.Product;
import customOrders.util.Dialogs;
import customOrders.util.Validator;
import customOrders.util.ImageUtil;

import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Arrays;
import java.util.List;

/**
 * Controlador para la vista de creación de pedidos (carrito de compras) con vista de productos en Galería.
 * Implementa la solución de desconexión/reconexión del botón para manejar la doble instancia del controlador.
 */
public class CreateOrderController implements Initializable, CustomerAware {

    // --- Componentes FXML ---
    @FXML private ScrollPane productScrollPane;
    @FXML private TilePane productTilePane;
    @FXML private TextField quantityField;
    @FXML private Label productMessageLabel;
    @FXML private TableView<ProductInOrder> cartTable;
    @FXML private TableColumn<ProductInOrder, String> cartNameCol;
    @FXML private TableColumn<ProductInOrder, Double> cartPriceCol;
    @FXML private TableColumn<ProductInOrder, Integer> cartQuantityCol;
    @FXML private TableColumn<ProductInOrder, Double> cartTotalCol;
    @FXML private TableColumn<ProductInOrder, Void> cartRemoveCol;
    @FXML private Label totalItemsLabel;
    @FXML private Label grandTotalLabel;
    @FXML private Label orderMessageLabel;
    // El botón es crítico para la reconexión programática
    @FXML private Button placeOrderButton;

    // Lógica de Negocio
    // Uso de Property para un estado más robusto
    private ObjectProperty<Integer> currentCustomerIdProperty = new SimpleObjectProperty<>();
    private final ProductManager productManager = new ProductManager();
    private final CreateOrderManager orderManager = new CreateOrderManager();
    private final ObservableList<ProductInOrder> cartItems = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 🛑 SOLUCIÓN CRÍTICA DE INSTANCIA:
        // Desactivamos el manejo del botón en la inicialización (Instancia fantasma).
        if (placeOrderButton != null) {
            placeOrderButton.setOnAction(null);
        }

        setupProductGallery();
        setupCartTable();

        cartTable.setItems(cartItems);

        // Agregamos un listener para asegurar que los totales se calculan cada vez que el carrito cambia.
        cartItems.addListener((javafx.collections.ListChangeListener<ProductInOrder>) c -> calculateTotals());
    }

    // -------------------------------------------------------------------------
    // 🎯 CAMBIO CRÍTICO: RECONEXIÓN DEL BOTÓN SOLO CUANDO EL ID ESTÁ DISPONIBLE
    // -------------------------------------------------------------------------
    @Override
    public void setCustomer(Customer customer) {
        if (customer != null) {
            this.currentCustomerIdProperty.set(customer.getCustomerID());

            // 💡 DEBUG: Verifica si el ID llegó a la instancia correcta del controlador
            System.out.println("DEBUG (setCustomer): Cliente recibido y ID cargado: " + customer.getCustomerID());

            showMessage("Cliente ID: " + this.currentCustomerIdProperty.get() + " cargado exitosamente.");

            // ✅ RECONEXIÓN CRÍTICA: Solo esta instancia (la correcta)
            // puede conectar la acción del botón.
            if (placeOrderButton != null) {
                placeOrderButton.setOnAction(event -> handlePlaceOrder());
                System.out.println("DEBUG (setCustomer): Botón 'Place Order' reconectado programáticamente.");
            }

            // Recalculamos totales para habilitar el botón si ya hay algo en el carrito
            calculateTotals();

        } else {
            this.currentCustomerIdProperty.set(null);
            showMessage("Error: No se recibió objeto de cliente.");
        }
    }

    /**
     * Configura la vista de galería cargando dinámicamente las tarjetas de producto.
     */
    private void setupProductGallery() {
        try {
            ObservableList<Product> products = FXCollections.observableArrayList(productManager.getAllProducts());
            productTilePane.getChildren().clear();

            // Rutas de FXML (adaptar según sea necesario)
            List<String> pathsToTry = Arrays.asList(
                    "/customOrders/Orders/ProductCard.fxml",
                    "/modules/customer/ProductCard.fxml"
            );

            URL fxmlUrl = null;
            for (String path : pathsToTry) {
                fxmlUrl = getClass().getResource(path);
                if (fxmlUrl != null) {
                    break;
                }
            }

            if (fxmlUrl == null) {
                throw new IllegalStateException("FXML 'ProductCard.fxml' not found.");
            }

            for (Product product : products) {
                try {
                    FXMLLoader loader = new FXMLLoader(fxmlUrl);
                    VBox productCard = loader.load();
                    ProductCardController controller = loader.getController();
                    controller.setMainController(this);
                    controller.setProductData(product);
                    productTilePane.getChildren().add(productCard);

                } catch (IOException e) {
                    System.err.println("Error al cargar la tarjeta de producto para: " + product.getProduct_name());
                    Dialogs.showErrorDialog("Error de Carga FXML", "Fallo al crear tarjeta de producto.", "Asegúrate de que 'ProductCard.fxml' tiene el controlador asignado y está bien formado.", e);
                }
            }

        } catch (SQLException e) {
            Dialogs.showErrorDialog("Error de Carga", "Error al conectar con la base de datos.", "Fallo al cargar los productos iniciales.", e);
            orderMessageLabel.setText("ERROR: Fallo al cargar los productos iniciales.");
        }
    }


    /**
     * Configura las columnas de la TableView del carrito.
     */
    private void setupCartTable() {
        cartNameCol.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        cartPriceCol.setCellValueFactory(new PropertyValueFactory<>("unit_price"));
        cartQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // Formato de moneda
        cartPriceCol.setCellFactory(tc -> new TableCell<ProductInOrder, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("%.2f €", price));
                setStyle(empty ? null : "-fx-alignment: CENTER_RIGHT;");
            }
        });
        cartTotalCol.setCellFactory(tc -> new TableCell<ProductInOrder, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("%.2f €", price));
                setStyle(empty ? null : "-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");
            }
        });

        // Columna para el botón de eliminar del carrito
        cartRemoveCol.setCellFactory(param -> new TableCell<ProductInOrder, Void>() {
            private final Button removeButton = new Button("🗑️");

            {
                removeButton.setOnAction(event -> {
                    ProductInOrder item = getTableView().getItems().get(getIndex());
                    handleRemoveFromCart(item);
                });
                removeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 3 6 3 6; -fx-background-radius: 5;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(removeButton);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        cartTable.setItems(cartItems);
    }

    /**
     * Calcula y actualiza el total de items y el monto total del pedido.
     */
    private void calculateTotals() {
        int totalItems = 0;
        double subtotal = 0.0;
        final double TAX_RATE = 0.21;

        for (ProductInOrder item : cartItems) {
            totalItems += item.getQuantity();
            subtotal += item.getTotalPrice();
        }

        double tax = subtotal * TAX_RATE;
        double grandTotal = subtotal + tax;

        totalItemsLabel.setText(String.valueOf(totalItems));
        grandTotalLabel.setText(String.format("%.2f €", grandTotal));

        // 🚨 Habilitación del Botón: DOS condiciones
        if (placeOrderButton != null) {
            // El botón se habilita si NO está vacío AND el ID del cliente fue cargado (no es null)
            placeOrderButton.setDisable(cartItems.isEmpty() || currentCustomerIdProperty.get() == null);
        }
    }

    public void showMessage(String message) {
        if (orderMessageLabel != null) {
            orderMessageLabel.setText(message);
        } else {
            System.out.println("MENSAJE DE PEDIDO: " + message);
        }
    }
    private String requestShippingAddress() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Dirección de Envío");
        dialog.setHeaderText("Ingrese la Dirección de Envío para el Pedido");
        dialog.setContentText("Dirección:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public void addToCart(Product product, int quantity) {
        addToCartFromCard(product, quantity);
    }


    public void addToCartFromCard(Product selectedProduct, int quantity) {

        if (quantity <= 0) {
            Dialogs.showWarningDialog("Advertencia de Cantidad", "Cantidad Mínima", "La cantidad a añadir debe ser mayor a cero.");
            return;
        }

        int availableStock = selectedProduct.getQuantity();

        Optional<ProductInOrder> existingItem = cartItems.stream()
                .filter(item -> item.getProduct().getProduct_id().equals(selectedProduct.getProduct_id()))
                .findFirst();

        if (existingItem.isPresent()) {
            ProductInOrder item = existingItem.get();
            int currentQuantityInCart = item.getQuantity();
            int newQuantity = currentQuantityInCart + quantity;

            if (newQuantity > availableStock) {
                showMessage("El total de unidades (" + newQuantity + ") excede el stock máximo disponible (" + availableStock + ").");
                Dialogs.showWarningDialog("Stock Excedido", "Stock No Disponible",
                        "El total de unidades (" + newQuantity + ") excede el stock máximo disponible (" + availableStock + ").");
                return;
            }
            item.setQuantity(newQuantity);
            cartTable.refresh();

        } else {
            if (quantity > availableStock) {
                showMessage("Solo hay " + availableStock + " unidades en stock de este producto.");
                Dialogs.showWarningDialog("Stock Excedido", "Stock No Disponible",
                        "Solo hay " + availableStock + " unidades en stock de este producto.");
                return;
            }
            cartItems.add(new ProductInOrder(selectedProduct, quantity));
        }

        calculateTotals();
    }


    private void handleRemoveFromCart(ProductInOrder item) {
        cartItems.remove(item);
        calculateTotals();
        showMessage("Producto '" + item.getProduct_name() + "' eliminado del carrito.");
    }


    /**
     * Maneja la confirmación del pedido: llama al Manager para guardarlo en la DB y actualizar el stock.
     */
    @FXML
    private void handlePlaceOrder() {

        Integer customerId = this.currentCustomerIdProperty.get();

        // 💡 DEBUG: Esta es la línea que DEBE aparecer si el botón está correctamente conectado y pulsado.
        System.out.println("DEBUG: currentCustomerId al hacer click: " + customerId);

        // 1. VERIFICACIÓN CRÍTICA: ID del cliente
        if (customerId == null) {
            Dialogs.showErrorDialog("Error de Sesión", "Cliente no identificado.",
                    "No se puede crear la orden. El ID del cliente no se cargó correctamente.");
            return;
        }

        // 2. VERIFICACIÓN DEL CARRITO
        if (cartItems.isEmpty()) {
            Dialogs.showWarningDialog("Carrito Vacío", "No se puede continuar.",
                    "Debes añadir al menos un producto para crear la orden.");
            return;
        }

        // 3. RECOLECCIÓN DE DATOS (shippingAddress)
        String shippingAddress = requestShippingAddress();

        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            Dialogs.showWarningDialog("Dirección Requerida", "Falta Información",
                    "Debe ingresar una dirección de envío válida para continuar.");
            return;
        }

        Optional<ButtonType> result = Dialogs.showConfirmationDialog("Confirmar Pedido",
                "Revisión Final",
                "¿Estás seguro de que quieres crear el pedido por el monto total de " + grandTotalLabel.getText() + "?");

        if (result.isPresent() && result.get() == ButtonType.OK) {

            String totalText = grandTotalLabel.getText().replace(" €", "").replace(",", ".");
            double totalAmount;
            try {
                totalAmount = Double.parseDouble(totalText);
            } catch (NumberFormatException e) {
                Dialogs.showErrorDialog("Error de Cálculo", "Monto Total Inválido",
                        "El monto total del carrito es inválido. Intenta re-añadir los productos.",
                        e);
                return;
            }

            boolean success = orderManager.createOrderAndUpdateStock(
                    cartItems,
                    customerId,
                    shippingAddress,
                    totalAmount
            );

            if (success) {
                cartItems.clear();
                calculateTotals();
                setupProductGallery();
                showMessage("¡Pedido creado con éxito! Stock actualizado y carrito reseteado.");
            } else {
                showMessage("ERROR: No se pudo completar el pedido. Revisa los logs de la DB.");
            }
        }
    }

    @FXML
    private void handleAddToCart() {
        Dialogs.showWarningDialog("Añadir Global", "Método Obsoleto", "Por favor, usa los botones 'Añadir al Carrito' que se encuentran debajo de cada producto en la galería.");
    }

}