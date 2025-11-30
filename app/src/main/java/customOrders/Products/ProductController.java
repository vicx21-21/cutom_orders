package customOrders.Products;

// Importamos las clases FK anidadas que definiste en ProductManager
import customOrders.Products.ProductManager.ProductTypeFK;
import customOrders.Products.ProductManager.SupplierFK;

import customOrders.Products.Product;
import customOrders.util.Dialogs;
import customOrders.util.Validator;
// AÑADIDO: Importación de ImageUtil para cargar imágenes
import customOrders.util.ImageUtil;

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
// Nuevas importaciones para manejo de archivos
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;


public class ProductController {

    // RUTA LÓGICA (DENTRO DEL CLASSPATH) - Solo se usa para determinar la carpeta de guardado.
    // La ruta física de destino será: src/main/resources/ + esta ruta.
    private static final String RESOURCE_PATH_PREFIX = "customOrders/resources/product_images/";

    // *************************************************************
    // ** Campos FXML de la Tabla **
    // *************************************************************
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> idColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> typeColumn;
    @FXML
    private TableColumn<Product, String> supplierColumn;
    @FXML
    private TableColumn<Product, Double> priceColumn;
    @FXML
    private TableColumn<Product, Integer> quantityColumn;
    @FXML
    private TableColumn<Product, Boolean> activeColumn;
    @FXML
    private Label messageLabel;

    // *************************************************************
    // ** Campos FXML del Formulario **
    // *************************************************************
    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<ProductTypeFK> productTypeCombo;
    @FXML
    private ComboBox<SupplierFK> supplierCombo;
    @FXML
    private TextField priceField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField reorderLevelField;
    @FXML
    private TextField reorderQuantityField;
    @FXML
    private TextField weightField;
    @FXML
    private TextField otherDetailsField;
    @FXML
    private TextField descriptionField;
    @FXML
    private CheckBox isActiveCheck;
    @FXML
    private Button deleteButton;
    @FXML
    private Button saveButton;

    // *************************************************************
    // ** Campos FXML para Imagen (VERIFICA ESTOS EN TU FXML) **
    // *************************************************************
    @FXML
    private TextField imageUrlField;
    @FXML
    private ImageView previewImageView;


    private final ProductManager productManager = new ProductManager();
    private ObservableList<Product> productData;
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
    // ** Método showProductDetails - CORREGIDO **
    // *************************************************************

    /**
     * Rellena todos los campos del formulario con los datos del producto o los limpia.
     *
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

            // Establecer la URL de la imagen
            String imageUrl = product.getImage_url();
            imageUrlField.setText(imageUrl);

            // APLICACIÓN DE LA CORRECCIÓN: Usar ImageUtil para cargar la imagen,
            // ya que maneja URLs web, rutas absolutas y nombres de archivo de Classpath.
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Image image = ImageUtil.loadImageFromProductUrl(imageUrl);
                previewImageView.setImage(image);
            } else {
                previewImageView.setImage(null);
            }

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
    // ** Método para cargar la imagen y actualizar el ImageView - SIMPLIFICADO **
    // *************************************************************

    /**
     * Carga la imagen desde la ruta o URL especificada en el campo de texto y la muestra en el ImageView.
     * Ahora utiliza el ImageUtil centralizado.
     */
    @FXML
    private void updateImagePreview() {
        String urlString = imageUrlField.getText();
        if (urlString == null || urlString.trim().isEmpty()) {
            previewImageView.setImage(null);
            return;
        }

        // Usamos el ImageUtil centralizado para previsualizar cualquier tipo de ruta (web, absoluta, classpath)
        Image image = ImageUtil.loadImageFromProductUrl(urlString);
        previewImageView.setImage(image);
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

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);

        if (deleteButton == null || deleteButton.getScene() == null) {
            System.err.println("Error: No se pudo obtener la ventana (Stage).");
            return;
        }
        Stage stage = (Stage) deleteButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            // Se establece la RUTA ABSOLUTA para que se guarde en el campo de texto.
            // Esto es importante para el método handleSave, que detectará que es un archivo local
            // y lo copiará a la carpeta de recursos, guardando solo el nombre.
            imageUrlField.setText(file.getAbsolutePath());
            updateImagePreview(); // Mostrar la vista previa
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
    // ** Método handleSave (CON LÓGICA DE COPIA DE ARCHIVO) **
    // *************************************************************
    @FXML
    private void handleSave() {
        String idText = idField.getText();
        String name = nameField.getText();
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

        // Validaciones...
        if (name == null || name.trim().isEmpty()) {
            errorMessage += "Nombre no válido.\n";
        }
        if (type == null) {
            errorMessage += "Debe seleccionar un Tipo de Producto.\n";
        }
        if (supplier == null) {
            errorMessage += "Debe seleccionar un Proveedor.\n";
        }

        if (priceText.trim().isEmpty() || !Validator.isValidDouble(priceText)) {
            errorMessage += "Precio Unitario no válido.\n";
        }
        if (quantityText.trim().isEmpty() || !Validator.isValidInteger(quantityText)) {
            errorMessage += "Stock no válido.\n";
        }
        if (reorderLevelText.trim().isEmpty() || !Validator.isValidInteger(reorderLevelText)) {
            errorMessage += "Nivel de Reorden no válido.\n";
        }
        if (reorderQuantityText.trim().isEmpty() || !Validator.isValidInteger(reorderQuantityText)) {
            errorMessage += "Cantidad de Reorden no válida.\n";
        }
        if (weightText.trim().isEmpty() || !Validator.isValidDouble(weightText)) {
            errorMessage += "Peso no válido.\n";
        }

        if (errorMessage.isEmpty()) {
            try {
                // **********************************************************
                // ** LÓGICA DE MANEJO DE IMAGEN **
                // **********************************************************
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    File sourceFile = new File(imageUrl);

                    // Comprobar si es un archivo local existente y no una URL web
                    if (sourceFile.exists() && !imageUrl.startsWith("http") && !imageUrl.startsWith("https")) {
                        try {
                            String fileName = sourceFile.getName();

                            // RUTA DE DESTINO CORREGIDA: Apunta a la ruta real de tu fuente de recursos
                            // (src/main/resources/customOrders/resources/product_images)
                            Path destinationDir = Paths.get("src", "main", "resources", "customOrders", "resources", "product_images");

                            // Asegurar que el directorio de destino existe
                            if (!Files.exists(destinationDir)) {
                                Files.createDirectories(destinationDir);
                            }

                            Path destination = destinationDir.resolve(fileName);

                            // Copiar el archivo
                            Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                            // GUARDAR SOLO EL NOMBRE DEL ARCHIVO EN LA BASE DE DATOS
                            imageUrl = fileName;

                        } catch (IOException e) {
                            Dialogs.showErrorDialog("Error de Archivo", "Fallo al copiar la imagen.",
                                    "La imagen no pudo ser copiada a la carpeta de recursos. Se guardará el producto sin URL de imagen.", e);
                            imageUrl = null;
                        }
                    } else if (imageUrl.startsWith("http") || imageUrl.startsWith("https")) {
                        // Es una URL web: se guarda la URL completa.
                        // imageUrl ya tiene el valor correcto.
                    } else {
                        // Podría ser solo el nombre del archivo ya guardado, lo mantenemos (ej: "OIP.jpg")
                        // Esto asegura que la edición de un producto que ya tiene una imagen local no borre el nombre.
                    }
                }
                // **********************************************************
                // ** FIN DE LÓGICA DE MANEJO DE IMAGEN **
                // **********************************************************


                // Parsear los valores numéricos del formulario
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
                        // Usamos el 'imageUrl' procesado (contiene solo el nombre del archivo o la URL web)
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
    // ** MÉTODOS ADICIONALES (handleDelete) **
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