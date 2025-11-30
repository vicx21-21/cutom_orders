package customOrders.Orders;

import customOrders.Products.Product;
import customOrders.util.ImageUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la tarjeta individual de producto, maneja la adición al carrito.
 */
public class ProductCardController implements Initializable {

    @FXML
    private VBox productCard;
    @FXML
    private ImageView productImageView;
    @FXML
    private Label nameLabel; // <<< CORRECCIÓN FINAL: Debe ser 'nameLabel' para coincidir con el FXML
    @FXML
    private Label priceLabel;
    @FXML
    private Label stockLabel;
    @FXML
    private Spinner<Integer> quantitySpinner;
    @FXML
    private Button addButton;

    private Product product;
    // Referencia al controlador principal: CreateOrderController
    private CreateOrderController mainController;

    /**
     * Inyecta la referencia al controlador principal (CreateOrderController).
     */
    public void setMainController(CreateOrderController controller) {
        this.mainController = controller;
    }

    /**
     * Configura la vista de la tarjeta con los datos de un producto específico.
     */
    public void setProductData(Product product) {
        this.product = product;

        // Ahora 'nameLabel' NO debería ser null, ya que coincide con el fx:id="nameLabel"
        nameLabel.setText(product.getProduct_name());
        priceLabel.setText(String.format("%.2f €", product.getUnit_price()));
        stockLabel.setText(product.getQuantity() + " en stock");

        // 1. Configurar el Spinner: mínimo 1, máximo el stock disponible, valor inicial 1
        int maxQuantity = Math.max(0, product.getQuantity());
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxQuantity, 1);
        quantitySpinner.setValueFactory(valueFactory);

        // 2. Manejo de estado sin stock
        if (maxQuantity == 0) {
            addButton.setDisable(true);
            addButton.setText("Agotado");
            productCard.setOpacity(0.6);
            stockLabel.setText("Agotado");
            if (quantitySpinner.getValueFactory() != null) {
                quantitySpinner.getValueFactory().setValue(0);
            }
            quantitySpinner.setDisable(true);
        } else {
            addButton.setDisable(false);
            addButton.setText("Añadir");
            productCard.setOpacity(1.0);
            quantitySpinner.setDisable(false);
        }

        // 3. Cargar la imagen usando ImageUtil
        loadImage();
    }

    /**
     * Carga la imagen del producto usando la utilidad de Classpath.
     */
    private void loadImage() {
        if (product == null || product.getImage_url() == null || product.getImage_url().trim().isEmpty()) {
            System.err.println("ProductCardController: Producto o URL de imagen nulo/vacío. Usando placeholder.");
            Image fallback = ImageUtil.loadImageFromProductUrl("/images/placeholder.png");
            if (fallback != null) {
                productImageView.setImage(fallback);
            }
            return;
        }

        String imagePath = product.getImage_url();
        Image image = ImageUtil.loadImageFromProductUrl(imagePath);

        if (image != null) {
            productImageView.setImage(image);
        } else {
            System.err.println("Fallo al cargar la imagen para " + product.getProduct_name() +
                    ". Revisar la ruta en DB: " + imagePath);
            Image fallback = ImageUtil.loadImageFromProductUrl("/images/placeholder.png");
            if (fallback != null) {
                productImageView.setImage(fallback);
            }
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Validación para evitar NPE si se usa FXML
        if (addButton != null) {
            addButton.setOnAction(event -> handleAddToCart());
        }
    }

    /**
     * Maneja el evento de añadir el producto al carrito.
     */
    private void handleAddToCart() {
        if (mainController == null) {
            System.err.println("Error: CreateOrderController no está configurado.");
            return;
        }

        int quantity = quantitySpinner.getValue();

        if (quantity <= 0) {
            mainController.showMessage("La cantidad debe ser mayor a cero.");
            return;
        }

        if (quantity > product.getQuantity()) {
            mainController.showMessage("No hay suficiente stock disponible (" + product.getQuantity() + ").");
            return;
        }

        mainController.addToCart(this.product, quantity);

        mainController.showMessage(quantity + " x " + product.getProduct_name() + " añadido al carrito.");
    }
}