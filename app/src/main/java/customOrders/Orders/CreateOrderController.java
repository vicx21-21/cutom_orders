package customOrders.Orders;

import customOrders.Products.ProductManager;
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
 */
public class CreateOrderController implements Initializable {

    // --- Componentes FXML de la VISTA DE GALERÍA ---
    @FXML private ScrollPane productScrollPane;
    @FXML private TilePane productTilePane;

    // Dejamos quantityField y productMessageLabel para compatibilidad FXML
    @FXML private TextField quantityField;
    @FXML private Label productMessageLabel;

    // Componentes FXML del Carrito (Se mantienen)
    @FXML private TableView<ProductInOrder> cartTable;
    @FXML private TableColumn<ProductInOrder, String> cartNameCol;
    @FXML private TableColumn<ProductInOrder, Double> cartPriceCol;
    @FXML private TableColumn<ProductInOrder, Integer> cartQuantityCol;
    @FXML private TableColumn<ProductInOrder, Double> cartTotalCol;
    @FXML private TableColumn<ProductInOrder, Void> cartRemoveCol;
    @FXML private Label totalItemsLabel;
    @FXML private Label grandTotalLabel;
    @FXML private Label orderMessageLabel;
    @FXML private Button placeOrderButton;

    // Lógica de Negocio
    private final ProductManager productManager = new ProductManager();
    private final CreateOrderManager orderManager = new CreateOrderManager();
    private final ObservableList<ProductInOrder> cartItems = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupProductGallery();
        setupCartTable();

        cartTable.setItems(cartItems);

        cartItems.addListener((javafx.collections.ListChangeListener<ProductInOrder>) c -> calculateTotals());
        calculateTotals();
    }

    /**
     * Configura la vista de galería cargando dinámicamente las tarjetas de producto.
     */
    private void setupProductGallery() {
        try {
            ObservableList<Product> products = FXCollections.observableArrayList(productManager.getAllProducts());
            productTilePane.getChildren().clear();

            // Rutas a intentar, de más específica a más genérica
            List<String> pathsToTry = Arrays.asList(
                    // 1. Ruta absoluta exacta que ha fallado (para depuración)
                    "/customOrders/modules/customer/ProductCard.fxml",
                    // 2. Ruta relativa dentro del paquete customOrders.Orders (si FXML está cerca)
                    "ProductCard.fxml",
                    // 3. Ruta más corta, asumiendo que "modules" es el punto de partida en resources
                    "/modules/customer/ProductCard.fxml"
            );

            URL fxmlUrl = null;
            String foundPath = null;

            // Intentar cargar el recurso con las rutas definidas
            for (String path : pathsToTry) {
                fxmlUrl = getClass().getResource(path);
                if (fxmlUrl != null) {
                    foundPath = path;
                    break;
                }
            }

            // --- BLOQUE DE VERIFICACIÓN FINAL ---
            if (fxmlUrl == null) {
                System.err.println("---------------------------------------------------------");
                System.err.println("--- ERROR DE RUTA CLASSPATH: FXML NO ENCONTRADO ---");
                System.err.println("El archivo 'ProductCard.fxml' NO FUE ENCONTRADO en el classpath.");
                System.err.println("Rutas FXML intentadas:");
                for (String path : pathsToTry) {
                    System.err.println(" - " + path);
                }
                System.err.println("Por favor, asegúrate de que el archivo existe en:");
                System.err.println("  src/main/resources/customOrders/modules/customer/ProductCard.fxml");
                System.err.println("  (O la ruta que corresponda a la clase ProductCardController)");
                System.err.println("---------------------------------------------------------");
                // Lanzamos la excepción para detener la ejecución y diagnosticar
                throw new IllegalStateException("Location is not set. FXML not found after checking multiple paths.");
            }
            // System.out.println("Ruta FXML ENCONTRADA con éxito: " + foundPath);
            // --- FIN BLOQUE DE VERIFICACIÓN ---


            for (Product product : products) {
                try {
                    // Cargar el FXML de la tarjeta usando la URL encontrada
                    FXMLLoader loader = new FXMLLoader(fxmlUrl);
                    VBox productCard = loader.load();

                    // Obtener el controlador de la tarjeta (campos @FXML ya inyectados)
                    ProductCardController controller = loader.getController();

                    // 1. Establecer la referencia al controlador principal
                    controller.setMainController(this);

                    // 2. Inicializar la tarjeta con los datos del producto (seguro de llamar)
                    controller.setProductData(product);

                    // Añadir la tarjeta al TilePane
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
        double grandTotal = 0.0;

        for (ProductInOrder item : cartItems) {
            totalItems += item.getQuantity();
            grandTotal += item.getTotalPrice();
        }

        totalItemsLabel.setText(String.valueOf(totalItems));
        grandTotalLabel.setText(String.format("%.2f €", grandTotal));

        // Habilita/Deshabilita el botón de ordenar si el carrito está vacío
        placeOrderButton.setDisable(cartItems.isEmpty());
    }

    // =========================================================================
    // === MÉTODOS REQUERIDOS POR ProductCardController
    // =========================================================================

    /**
     * Muestra un mensaje de notificación al usuario en la etiqueta de mensaje.
     * @param message El mensaje a mostrar.
     */
    public void showMessage(String message) {
        if (orderMessageLabel != null) {
            orderMessageLabel.setText(message);
        } else {
            System.out.println("MENSAJE DE PEDIDO: " + message);
        }
    }

    /**
     * Wrapper para permitir que ProductCardController use un nombre de método simple.
     * Reenvía la llamada a la lógica principal de manejo del carrito.
     * @param product Producto a añadir.
     * @param quantity Cantidad.
     */
    public void addToCart(Product product, int quantity) {
        // Llama al método que contiene la lógica real de añadir al carrito.
        addToCartFromCard(product, quantity);
    }

    // =========================================================================
    // === FIN DE MÉTODOS REQUERIDOS
    // =========================================================================


    // --- MANEJO DE EVENTOS ---

    /**
     * Añade o actualiza un producto en el carrito.
     * Este método es llamado por el ProductCardController (a través del wrapper addToCart).
     */
    public void addToCartFromCard(Product selectedProduct, int quantity) {

        // La validación de cantidad > 0 ya la hizo la tarjeta, pero se deja aquí por seguridad.
        if (quantity <= 0) {
            Dialogs.showWarningDialog("Advertencia de Cantidad", "Cantidad Mínima", "La cantidad a añadir debe ser mayor a cero.");
            return;
        }

        // Obtener el stock disponible real
        int availableStock = selectedProduct.getQuantity();

        // Buscar si el producto ya está en el carrito
        Optional<ProductInOrder> existingItem = cartItems.stream()
                .filter(item -> item.getProduct().getProduct_id().equals(selectedProduct.getProduct_id()))
                .findFirst();

        if (existingItem.isPresent()) {
            ProductInOrder item = existingItem.get();
            int currentQuantityInCart = item.getQuantity();
            int newQuantity = currentQuantityInCart + quantity;

            // Re-checar el stock total si se agrega más
            if (newQuantity > availableStock) {
                // Usamos showMessage para el feedback
                showMessage("El total de unidades (" + newQuantity + ") excede el stock máximo disponible (" + availableStock + ").");
                // Mantenemos el diálogo por si el showMessage solo actualiza una etiqueta
                Dialogs.showWarningDialog("Stock Excedido", "Stock No Disponible",
                        "El total de unidades (" + newQuantity + ") excede el stock máximo disponible (" + availableStock + ").");
                return;
            }
            item.setQuantity(newQuantity);
            cartTable.refresh(); // Refrescar la tabla para actualizar los totales del item

        } else {
            // Si es un producto nuevo, checar stock (aunque la tarjeta lo valida)
            if (quantity > availableStock) {
                showMessage("Solo hay " + availableStock + " unidades en stock de este producto.");
                Dialogs.showWarningDialog("Stock Excedido", "Stock No Disponible",
                        "Solo hay " + availableStock + " unidades en stock de este producto.");
                return;
            }
            // Agregar nuevo item al carrito
            cartItems.add(new ProductInOrder(selectedProduct, quantity));
        }

        calculateTotals();
        // El mensaje de éxito lo maneja ProductCardController llamando a showMessage()
    }


    /**
     * Elimina un item del carrito.
     */
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
        if (cartItems.isEmpty()) {
            Dialogs.showWarningDialog("Carrito Vacío", "No se puede continuar.", "Debes añadir al menos un producto para crear la orden.");
            return;
        }

        // Usar el método showConfirmationDialog
        Optional<ButtonType> result = Dialogs.showConfirmationDialog("Confirmar Pedido",
                "Revisión Final",
                "¿Estás seguro de que quieres crear el pedido por el monto total de " + grandTotalLabel.getText() + "?");

        if (result.isPresent() && result.get() == ButtonType.OK) {

            // Parsear el monto total
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

            // Intentar crear el pedido y actualizar el stock
            boolean success = orderManager.createOrderAndUpdateStock(cartItems, totalAmount);

            if (success) {
                // Si la transacción fue exitosa, limpia el carrito y refresca la lista de productos
                cartItems.clear();
                calculateTotals();
                setupProductGallery(); // Vuelve a cargar la GALERÍA para reflejar el stock reducido
                showMessage("¡Pedido creado con éxito! Stock actualizado y carrito reseteado.");
            } else {
                showMessage("ERROR: No se pudo completar el pedido. Revisa los logs de la DB.");
            }
        }
    }

    // El handleAddToCart original se deja para evitar errores de inyección FXML,
    // aunque la lógica real se mueve a las tarjetas.
    @FXML
    private void handleAddToCart() {
        Dialogs.showWarningDialog("Añadir Global", "Método Obsoleto", "Por favor, usa los botones 'Añadir al Carrito' que se encuentran debajo de cada producto en la galería.");
    }
}