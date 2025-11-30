package customOrders.Orders;

import customOrders.Products.ProductManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import customOrders.Orders.CreateOrderManager;
import customOrders.Orders.ProductInOrder;
import customOrders.Products.Product;
import customOrders.util.Dialogs;
import customOrders.util.Validator;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de creación de pedidos (carrito de compras).
 * Permite seleccionar productos, añadir cantidades al carrito, y confirmar la orden.
 */
public class CreateOrderController implements Initializable {

    // Componentes FXML de la Vista de Productos
    @FXML private ListView<Product> productList;
    @FXML private TextField quantityField;
    @FXML private Label productMessageLabel;

    // Componentes FXML del Carrito
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
        setupProductList();
        setupCartTable();

        // Conectar la lista del carrito a la tabla
        cartTable.setItems(cartItems);

        // Escucha cambios en el carrito para recalcular el total
        cartItems.addListener((javafx.collections.ListChangeListener<ProductInOrder>) c -> calculateTotals());
    }

    /**
     * Configura el ListView para mostrar los productos disponibles.
     */
    private void setupProductList() {
        try {
            // Cargar todos los productos activos
            ObservableList<Product> products = FXCollections.observableArrayList(productManager.getAllProducts());
            productList.setItems(products);

            // Personaliza cómo se muestra cada celda (Producto + Imagen + Stock)
            productList.setCellFactory(lv -> new ListCell<Product>() {
                @Override
                protected void updateItem(Product product, boolean empty) {
                    super.updateItem(product, empty);
                    if (empty || product == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle(null);
                    } else {
                        // Usamos un layout HBox para la celda
                        HBox hBox = new HBox(10);
                        hBox.setPadding(new javafx.geometry.Insets(10));
                        hBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-color: #ffffff;");
                        hBox.setSpacing(15);

                        // Generación de Placeholder de Imagen (debes reemplazar 'imageUrl' con tu URL real)
                        ImageView imageView = new ImageView();
                        String imageUrl = "https://placehold.co/80x80/007bff/white?text=P" + product.getProduct_id();

                        try {
                            Image image = new Image(imageUrl, 80, 80, true, true);
                            imageView.setImage(image);
                        } catch (Exception e) {
                            imageView.setImage(new Image("https://placehold.co/80x80/cccccc/000000?text=No+Img"));
                        }

                        VBox infoBox = new VBox(5);
                        Label nameLabel = new Label(product.getProduct_name());
                        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                        Label priceLabel = new Label(String.format("Precio: %.2f €", product.getUnit_price()));
                        Label stockLabel = new Label("Stock: " + product.getQuantity());

                        // Advertencia si el stock es bajo
                        if (product.getQuantity() < 5) {
                            stockLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        } else {
                            stockLabel.setStyle("-fx-text-fill: green;");
                        }

                        infoBox.getChildren().addAll(nameLabel, priceLabel, stockLabel);

                        hBox.getChildren().addAll(imageView, infoBox);
                        HBox.setHgrow(infoBox, Priority.ALWAYS); // Hacer que la caja de info crezca
                        setGraphic(hBox);
                    }
                }
            });

        } catch (SQLException e) {
            // El ProductManager ya muestra un diálogo de error
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
            }
        });
        cartTotalCol.setCellFactory(tc -> new TableCell<ProductInOrder, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty ? null : String.format("%.2f €", price));
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
                removeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
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

    // --- MANEJO DE EVENTOS ---

    @FXML
    private void handleAddToCart() {
        Product selectedProduct = productList.getSelectionModel().getSelectedItem();
        String quantityStr = quantityField.getText();

        productMessageLabel.setText(""); // Limpiar mensaje

        if (selectedProduct == null) {
            Dialogs.showWarningDialog("Advertencia de Producto", "Selección Requerida", "Selecciona un producto de la lista antes de añadirlo al carrito.");
            return;
        }

        if (!Validator.isValidInteger(quantityStr)) {
            Dialogs.showWarningDialog("Advertencia de Cantidad", "Formato Inválido", "La cantidad debe ser un número entero válido.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            // Esto solo ocurriría si Validator fallara, pero es una buena práctica defensiva.
            Dialogs.showWarningDialog("Advertencia de Cantidad", "Cantidad Inválida", "La cantidad ingresada no es válida.");
            return;
        }


        if (quantity <= 0) {
            Dialogs.showWarningDialog("Advertencia de Cantidad", "Cantidad Mínima", "La cantidad a añadir debe ser mayor a cero.");
            return;
        }

        // Obtener el stock disponible real (del objeto Product en la lista de productos)
        int availableStock = selectedProduct.getQuantity();

        // Buscar si el producto ya está en el carrito
        Optional<ProductInOrder> existingItem = cartItems.stream()
                .filter(item -> item.getProduct().getProduct_id().equals(selectedProduct.getProduct_id()))
                .findFirst();

        if (existingItem.isPresent()) {
            ProductInOrder item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            // Re-checar el stock total si se agrega más
            if (newQuantity > availableStock) {
                Dialogs.showWarningDialog("Stock Excedido", "Stock No Disponible",
                        "El total de unidades (" + newQuantity + ") excede el stock máximo disponible (" + availableStock + ").");
                return;
            }
            item.setQuantity(newQuantity);
            cartTable.refresh(); // Refrescar la tabla para actualizar los totales del item

        } else {
            // Si es un producto nuevo, checar stock
            if (quantity > availableStock) {
                Dialogs.showWarningDialog("Stock Excedido", "Stock No Disponible",
                        "Solo hay " + availableStock + " unidades en stock de este producto.");
                return;
            }
            // Agregar nuevo item al carrito
            cartItems.add(new ProductInOrder(selectedProduct, quantity));
        }

        // Opcional: limpiar el campo de cantidad
        quantityField.setText("1");
        calculateTotals();
        productMessageLabel.setText("Producto '" + selectedProduct.getProduct_name() + "' añadido/actualizado.");
    }

    /**
     * Elimina un item del carrito.
     */
    private void handleRemoveFromCart(ProductInOrder item) {
        cartItems.remove(item);
        calculateTotals();
        orderMessageLabel.setText("Producto '" + item.getProduct_name() + "' eliminado del carrito.");
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
                // Uso del diálogo de error con 4 parámetros (incluyendo la excepción)
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
                setupProductList(); // Vuelve a cargar la lista para reflejar el stock reducido
                orderMessageLabel.setText("¡Pedido creado con éxito! Stock y carrito reseteados.");
            } else {
                // El error ya fue manejado dentro del Manager.
                orderMessageLabel.setText("ERROR: No se pudo completar el pedido. Revisa los logs de la DB.");
            }
        }
    }
}