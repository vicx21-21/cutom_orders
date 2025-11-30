package customOrders.Products;

// Importamos las clases FK anidadas que definiste en ProductManager
import customOrders.Products.ProductManager.ProductTypeFK;
import customOrders.Products.ProductManager.SupplierFK;

import customOrders.Products.Product;
import customOrders.util.Dialogs;
import customOrders.util.Validator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;


public class ProductController {

    // *************************************************************
    // ** Campos FXML de la Tabla **
    // *************************************************************
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> typeColumn;
    @FXML private TableColumn<Product, String> supplierColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, Boolean> activeColumn;
    @FXML private Label messageLabel;

    // *************************************************************
    // ** Campos FXML del Formulario **
    // *************************************************************
    @FXML private TextField idField;
    @FXML private TextField nameField;
    // Usamos los records FK de ProductManager
    @FXML private ComboBox<ProductTypeFK> productTypeCombo;
    @FXML private ComboBox<SupplierFK> supplierCombo;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private TextField reorderLevelField;
    @FXML private TextField reorderQuantityField;
    @FXML private TextField weightField;
    @FXML private TextField otherDetailsField;
    @FXML private TextField descriptionField;
    @FXML private CheckBox isActiveCheck;
    @FXML private Button deleteButton;
    @FXML private Button saveButton; // Añadido si se usa el fx:id para guardar

    // *************************************************************
    // ** Campos FXML para Imagen (VERIFICA ESTOS EN TU FXML) **
    // *************************************************************
    @FXML private TextField imageUrlField;
    @FXML private ImageView previewImageView;


    private final ProductManager productManager = new ProductManager();
    private ObservableList<Product> productData;
    // Usamos los records FK de ProductManager
    private ObservableList<ProductTypeFK> productTypes;
    private ObservableList<SupplierFK> suppliers;

    // *************************************************************
    // ** Método initialize **
    // *************************************************************
    @FXML
    public void initialize() {
        // 1. Columna de ID (Integer)
        idColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getProduct_id()));

        // 2. Columna de Nombre (String)
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct_name()));

        // 3. Columna de Tipo (String) - Muestra el código de tipo
        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct_type_code()));

        // 4. Columna de Proveedor (String) - Muestra el ID como String
        supplierColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getSupplier_id())));

        // 5. Columna de Precio (Double)
        priceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getUnit_price()));

        // 6. Columna de Cantidad (Integer)
        quantityColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getQuantity()));

        // 7. Columna de Activo (Boolean)
        activeColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getIs_active()));

        // Iniciar la carga de datos
        loadForeignKeys();
        loadProductData();

        // Listener para la tabla (para mostrar detalles al seleccionar)
        productTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showProductDetails(newValue));
    }

    // Método auxiliar para cargar datos
    private void loadProductData() {
        try {
            // Llama al método correcto en ProductManager
            productData = FXCollections.observableArrayList(productManager.getAllProducts());
            productTable.setItems(productData);
            messageLabel.setText("Total de productos: " + productData.size());
        } catch (Exception e) {
            // Captura cualquier excepción durante la carga de datos
            Dialogs.showErrorDialog("Error de Carga", "No se pudo cargar la lista de productos.", e.getMessage(), e);
        }
    }

    // Método auxiliar para cargar claves foráneas
    private void loadForeignKeys() {
        try {
            // Llama a los métodos con el sufijo FK
            productTypes = FXCollections.observableArrayList(productManager.getAllProductTypesFK());
            productTypeCombo.setItems(productTypes);
            suppliers = FXCollections.observableArrayList(productManager.getAllSupplierFKs());
            supplierCombo.setItems(suppliers);
        } catch (Exception e) {
            // Captura cualquier excepción durante la carga de FK
            Dialogs.showErrorDialog("Error de Carga de Dependencias", "No se pudo cargar Tipos de Producto o Proveedores.", e.getMessage(), e);
        }
    }


    // *************************************************************
    // ** Método showProductDetails **
    // *************************************************************
    /**
     * Rellena todos los campos del formulario con los datos del producto o los limpia.
     * @param product el producto a mostrar, o null para limpiar los campos.
     */
    private void showProductDetails(Product product) {
        if (product != null) {
            // Rellenar campos existentes
            idField.setText(String.valueOf(product.getProduct_id()));
            nameField.setText(product.getProduct_name());
            priceField.setText(String.valueOf(product.getUnit_price()));
            quantityField.setText(String.valueOf(product.getQuantity()));
            reorderLevelField.setText(String.valueOf(product.getReorder_level()));
            reorderQuantityField.setText(String.valueOf(product.getReorder_quantity()));
            weightField.setText(String.valueOf(product.getWeight_kg()));
            otherDetailsField.setText(product.getOther_details());
            descriptionField.setText(product.getProduct_description());
            isActiveCheck.setSelected(product.getIs_active());

            // Seleccionar ComboBoxes usando los campos 'code' e 'id' de los Records FK
            productTypeCombo.getSelectionModel().select(
                    productTypes.stream()
                            .filter(pt -> pt.code().equals(product.getProduct_type_code()))
                            .findFirst().orElse(null));

            supplierCombo.getSelectionModel().select(
                    suppliers.stream()
                            .filter(s -> s.id().equals(product.getSupplier_id()))
                            .findFirst().orElse(null));

            // Campo de URL de Imagen
            imageUrlField.setText(product.getImageUrl());
            updateImagePreview();

            deleteButton.setDisable(false);
        } else {
            // Limpiar campos
            idField.setText("Auto-generado");
            nameField.setText("");
            priceField.setText("");
            quantityField.setText("");
            reorderLevelField.setText("");
            reorderQuantityField.setText("");
            weightField.setText("");
            otherDetailsField.setText("");
            descriptionField.setText("");
            isActiveCheck.setSelected(true);
            productTypeCombo.getSelectionModel().clearSelection();
            supplierCombo.getSelectionModel().clearSelection();

            // Limpiar el campo de URL e ImageView
            imageUrlField.setText("");
            previewImageView.setImage(null);

            deleteButton.setDisable(true);
        }
    }

    // *************************************************************
    // ** Método para cargar la imagen y actualizar el ImageView **
    // *************************************************************
    /**
     * Carga la imagen desde la ruta o URL especificada en el campo de texto y la muestra en el ImageView.
     */
    @FXML
    private void updateImagePreview() {
        String urlString = imageUrlField.getText();
        if (urlString == null || urlString.trim().isEmpty()) {
            previewImageView.setImage(null);
            return;
        }

        Image image = null;
        try {
            // Intentar cargar como URL web (http/https)
            if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
                image = new Image(urlString, true); // true para background loading
            } else {
                // Intentar cargar como ruta de archivo local
                File file = new File(urlString);
                if (file.exists()) {
                    image = new Image(new FileInputStream(file));
                } else {
                    // Mostrar una imagen de error/placeholder si no existe localmente
                    image = new Image(new URL("https://placehold.co/200x150/ef4444/ffffff?text=Imagen+No+Encontrada").toExternalForm());
                }
            }
        } catch (MalformedURLException | FileNotFoundException e) {
            // Error al intentar cargar la imagen (URL mal formada o archivo no encontrado)
            try {
                image = new Image(new URL("https://placehold.co/200x150/ef4444/ffffff?text=URL+Inválida").toExternalForm());
            } catch (MalformedURLException e2) {
                // Esto no debería suceder si la URL de placeholder es correcta
                System.err.println("Error al cargar placeholder.");
            }
        } catch (Exception e) {
            // Error general (ej. error de I/O)
            try {
                image = new Image(new URL("https://placehold.co/200x150/ef4444/ffffff?text=Error+de+Carga").toExternalForm());
            } catch (MalformedURLException e2) {
                System.err.println("Error al cargar placeholder.");
            }
        }

        // Si hay una imagen (incluso si es un placeholder de error), la establece
        if (image != null) {
            previewImageView.setImage(image);
        }
    }

    // *************************************************************
    // ** Método para buscar una imagen localmente **
    // *************************************************************
    /**
     * Abre un cuadro de diálogo FileChooser para seleccionar una imagen local
     * y establece la ruta absoluta en el imageUrlField.
     */
    @FXML
    private void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen del Producto");

        // Filtro para tipos de archivo comunes de imagen
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);

        // Obtener la Stage actual (ventana). Aseguramos que deleteButton no es null.
        if (deleteButton == null || deleteButton.getScene() == null) {
            System.err.println("Error: No se pudo obtener la ventana (Stage). Verifica la conexión FXML del deleteButton.");
            return;
        }
        Stage stage = (Stage) deleteButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            // Establecer la ruta absoluta del archivo
            imageUrlField.setText(file.getAbsolutePath());
            updateImagePreview(); // Mostrar la vista previa inmediatamente
        }
    }

    // *************************************************************
    // ** Método handleNew **
    // *************************************************************
    @FXML
    private void handleNew() {
        productTable.getSelectionModel().clearSelection();
        showProductDetails(null);
    }

    // *************************************************************
    // ** Método handleSave (Con fix para NumberFormatException) **
    // *************************************************************
    @FXML
    private void handleSave() {
        String idText = idField.getText();
        String name = nameField.getText();
        // Usamos ProductTypeFK y SupplierFK
        ProductTypeFK type = productTypeCombo.getSelectionModel().getSelectedItem();
        SupplierFK supplier = supplierCombo.getSelectionModel().getSelectedItem();
        String priceText = priceField.getText();
        String quantityText = quantityField.getText();
        String reorderLevelText = reorderLevelField.getText();
        String reorderQuantityText = reorderQuantityField.getText();
        String weightText = weightField.getText();
        String otherDetails = otherDetailsField.getText();
        String description = descriptionField.getText();
        boolean isActive = isActiveCheck.isSelected();
        String imageUrl = imageUrlField.getText();

        String errorMessage = "";

        // Validación de datos - Aseguramos que NO estén vacíos Y que sean válidos
        if (name == null || name.trim().isEmpty()) { errorMessage += "Nombre no válido.\n"; }
        if (type == null) { errorMessage += "Debe seleccionar un Tipo de Producto.\n"; }
        if (supplier == null) { errorMessage += "Debe seleccionar un Proveedor.\n"; }

        // Validaciones numéricas robustas, incluyendo la comprobación de vacío
        if (priceText.trim().isEmpty() || !Validator.isValidDouble(priceText)) { errorMessage += "Precio Unitario no válido.\n"; }
        if (quantityText.trim().isEmpty() || !Validator.isValidInteger(quantityText)) { errorMessage += "Stock no válido.\n"; }
        if (reorderLevelText.trim().isEmpty() || !Validator.isValidInteger(reorderLevelText)) { errorMessage += "Nivel de Reorden no válido.\n"; }
        if (reorderQuantityText.trim().isEmpty() || !Validator.isValidInteger(reorderQuantityText)) { errorMessage += "Cantidad de Reorden no válida.\n"; }
        if (weightText.trim().isEmpty() || !Validator.isValidDouble(weightText)) { errorMessage += "Peso no válido.\n"; }

        if (errorMessage.isEmpty()) {
            try {
                // Parsear los valores numéricos del formulario (Ahora sabemos que no son cadenas vacías)
                Double unitPrice = Double.parseDouble(priceText);
                Integer quantity = Integer.parseInt(quantityText);
                Integer reorderLevel = Integer.parseInt(reorderLevelText);
                Integer reorderQuantity = Integer.parseInt(reorderQuantityText);
                Double weight = Double.parseDouble(weightText);

                // Obtener el ID del proveedor y el código del tipo
                String productTypeCode = type.code();
                Integer supplierId = supplier.id();

                // Crear el objeto Product
                Product newProduct = new Product(
                        // Solo parsear idText si no es el valor por defecto
                        idText.equals("Auto-generado") ? null : Integer.parseInt(idText),
                        productTypeCode,
                        supplierId,
                        name,
                        unitPrice,
                        description,
                        reorderLevel,
                        reorderQuantity,
                        otherDetails,
                        weight,
                        LocalDate.now(),
                        isActive,
                        quantity,
                        imageUrl
                );

                if (idText.equals("Auto-generado")) {
                    // MODO NUEVO
                    productManager.insertProduct(newProduct);
                    Dialogs.showInformationDialog("Éxito", "Producto Creado", "El nuevo producto ha sido registrado correctamente.");
                } else {
                    // MODO EDICIÓN
                    productManager.updateProduct(newProduct);
                    Dialogs.showInformationDialog("Éxito", "Producto Actualizado", "Los detalles del producto han sido actualizados.");
                }

                // Recargar los datos de la tabla
                loadProductData();
                productTable.getSelectionModel().clearSelection();
                handleNew(); // Limpiar formulario

            } catch (NumberFormatException e) {
                Dialogs.showErrorDialog("Error de Formato", "Revisa los campos numéricos.", "Asegúrate de que no haya caracteres extraños en los campos de números.", e);
            } catch (Exception e) {
                Dialogs.showErrorDialog("Error de Base de Datos", "No se pudo guardar el producto.", e.getMessage(), e);
            }
        } else {
            Dialogs.showWarningDialog("Datos Inválidos", "Por favor, corrige los siguientes errores:", errorMessage);
        }
    }

    // *************************************************************
    // ** MÉTODOS PENDIENTES (Ejemplo: handleEdit y handleDelete) **
    // *************************************************************

    @FXML
    private void handleDelete() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Optional<ButtonType> result = Dialogs.showConfirmationDialog(
                    "Confirmar Eliminación",
                    "¿Estás seguro de que quieres eliminar el producto: " + selectedProduct.getProduct_name() + "?",
                    "Esta acción no se puede deshacer.");

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    productManager.deleteProduct(selectedProduct.getProduct_id());
                    Dialogs.showInformationDialog("Éxito", "Producto Eliminado", "El producto fue eliminado correctamente.");
                    loadProductData();
                    handleNew(); // Limpiar el formulario
                } catch (Exception e) {
                    Dialogs.showErrorDialog("Error de Eliminación", "No se pudo eliminar el producto.", e.getMessage(), e);
                }
            }
        } else {
            Dialogs.showWarningDialog("Advertencia", "No hay producto seleccionado.", "Por favor, selecciona un producto de la tabla para eliminar.");
        }
    }
}