package com.customOrders.Products;

// Importaciones corregidas que ahora apuntan a las clases internas públicas y estáticas de ProductManager
import com.customOrders.Products.ProductManager.ProductTypeFK;
import com.customOrders.Products.ProductManager.SupplierFK;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    // Nota: Las clases auxiliares ProductTypeFK y SupplierFK han sido movidas al ProductManager
    // y son importadas arriba.

    // --- Referencias a Clases Auxiliares ---
    private final ProductManager manager = new ProductManager();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private Product selectedProduct;

    // --- Componentes FXML de la Tabla ---
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> typeColumn;
    @FXML private TableColumn<Product, Integer> supplierColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, Boolean> activeColumn;

    // --- Componentes FXML del Formulario de Edición ---
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private TextField reorderLevelField;
    @FXML private TextField reorderQuantityField;
    @FXML private TextField weightField;
    @FXML private TextField otherDetailsField;
    @FXML private TextField descriptionField;
    @FXML private CheckBox isActiveCheck;

    // Componentes FXML para las claves foráneas (ComboBox)
    // Usamos las clases importadas: ProductTypeFK y SupplierFK
    @FXML private ComboBox<ProductTypeFK> productTypeCombo;
    @FXML private ComboBox<SupplierFK> supplierCombo;

    @FXML private Label messageLabel;
    @FXML private Button deleteButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurar las columnas.
        idColumn.setCellValueFactory(new PropertyValueFactory<>("product_id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("product_type_code"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier_id"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("unit_price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("is_active"));

        // Vincular la TableView con la lista Observable
        productTable.setItems(productList);

        // Configurar el listener para la tabla
        productTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showProductDetails(newValue));

        // Cargar todos los datos
        loadAllData();

        messageLabel.setText("Módulo de Productos cargado. Conectado a PostgreSQL.");
        setEditableFields(false);
    }

    /**
     * Carga todos los productos y las claves foráneas de la DB.
     */
    private void loadAllData() {
        try {
            // Cargar lista principal
            productList.setAll(manager.getAllProducts());

            // Cargar ComboBoxes (Claves Foráneas)
            productTypeCombo.setItems(FXCollections.observableArrayList(manager.getAllProductTypesFK()));
            supplierCombo.setItems(FXCollections.observableArrayList(manager.getAllSupplierFKs()));

        } catch (SQLException e) {
            System.err.println("Error al cargar datos: " + e.getMessage());
            messageLabel.setText("ERROR de DB al cargar datos: " + e.getMessage());
        }
    }

    // --- Métodos de Control ---

    private void showProductDetails(Product product) {
        if (product != null) {
            selectedProduct = product;
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

            // Seleccionar los valores de las claves foráneas en los ComboBox
            productTypeCombo.getSelectionModel().select(
                    productTypeCombo.getItems().stream()
                            .filter(t -> t.code().equals(product.getProduct_type_code()))
                            .findFirst().orElse(null)
            );

            supplierCombo.getSelectionModel().select(
                    supplierCombo.getItems().stream()
                            .filter(s -> s.id().equals(product.getSupplier_id()))
                            .findFirst().orElse(null)
            );

            setEditableFields(true);
            deleteButton.setDisable(false);
            idField.setDisable(true);
        } else {
            handleNew();
        }
    }

    @FXML
    private void handleNew() {
        selectedProduct = null;

        idField.setText("Nuevo (Auto-generado)");
        nameField.setText("");
        priceField.setText("0.00");
        quantityField.setText("0");
        reorderLevelField.setText("0");
        reorderQuantityField.setText("0");
        weightField.setText("0.00");
        otherDetailsField.setText("");
        descriptionField.setText("");
        isActiveCheck.setSelected(true);
        productTypeCombo.getSelectionModel().clearSelection();
        supplierCombo.getSelectionModel().clearSelection();

        setEditableFields(true);
        deleteButton.setDisable(true);
        messageLabel.setText("Introduzca los datos para un nuevo producto.");
        idField.setDisable(true);
        nameField.requestFocus();
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty() || productTypeCombo.getSelectionModel().isEmpty() || supplierCombo.getSelectionModel().isEmpty()) {
            messageLabel.setText("ERROR: Nombre, Tipo y Proveedor son obligatorios.");
            return;
        }

        try {
            // 1. Validar y parsear campos numéricos
            double price = Double.parseDouble(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            int reorderLevel = Integer.parseInt(reorderLevelField.getText());
            int reorderQuantity = Integer.parseInt(reorderQuantityField.getText());
            double weight = Double.parseDouble(weightField.getText());

            // 2. Obtener datos de las FK
            String productTypeCode = productTypeCombo.getSelectionModel().getSelectedItem().code();
            int supplierId = supplierCombo.getSelectionModel().getSelectedItem().id();

            // 3. Crear el objeto Producto
            // El ID es el existente (para UPDATE) o 0/null (para INSERT, se ignora en la DB)
            Integer currentId = selectedProduct != null ? selectedProduct.getProduct_id() : 0;
            LocalDate dateAdded = selectedProduct != null ? selectedProduct.getDate_added() : LocalDate.now();

            Product productToSave = new Product(
                    currentId,
                    productTypeCode,
                    supplierId,
                    nameField.getText(),
                    price,
                    descriptionField.getText(),
                    reorderLevel,
                    reorderQuantity,
                    otherDetailsField.getText(),
                    weight,
                    dateAdded,
                    isActiveCheck.isSelected(),
                    quantity
            );

            boolean isNew = selectedProduct == null;
            Product savedProduct;

            if (isNew) {
                // INSERT
                savedProduct = manager.insertProduct(productToSave);
                messageLabel.setText("Producto '" + savedProduct.getProduct_name() + "' AGREGADO correctamente con ID: " + savedProduct.getProduct_id());
            } else {
                // UPDATE
                manager.updateProduct(productToSave);
                savedProduct = productToSave; // Usar el objeto con los datos actualizados
                messageLabel.setText("Producto con ID " + savedProduct.getProduct_id() + " ACTUALIZADO correctamente.");
            }

            // 4. Recargar datos y seleccionar el ítem guardado
            loadAllData();

            productTable.getSelectionModel().select(savedProduct);
            selectedProduct = savedProduct;
            idField.setText(String.valueOf(savedProduct.getProduct_id()));

        } catch (NumberFormatException e) {
            messageLabel.setText("ERROR de Formato: Asegúrese de que Precio, Stock, Nivel y Cantidad de Reorden sean números válidos.");
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("foreign key constraint")) {
                messageLabel.setText("ERROR de DB: Clave foránea inválida (Tipo de Producto o Proveedor no existe).");
            } else {
                messageLabel.setText("ERROR de DB: " + errorMsg);
            }
            System.err.println("SQL Exception during save: " + errorMsg);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedProduct != null) {
            Integer idToDelete = selectedProduct.getProduct_id();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("Eliminar Producto ID: " + idToDelete);
            alert.setContentText("¿Está seguro que desea eliminar el producto '" + selectedProduct.getProduct_name() + "'?");

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    if (manager.deleteProduct(idToDelete)) {
                        messageLabel.setText("Producto con ID " + idToDelete + " ELIMINADO correctamente.");
                        loadAllData(); // Recargar datos
                        handleNew(); // Resetear formulario
                    } else {
                        messageLabel.setText("ERROR: No se pudo eliminar el producto (no encontrado).");
                    }
                } catch (SQLException e) {
                    messageLabel.setText("ERROR de DB: " + e.getMessage());
                    System.err.println("SQL Exception during delete: " + e.getMessage());
                }
            }
        }
    }

    // --- Métodos Auxiliares ---

    private void setEditableFields(boolean editable) {
        nameField.setDisable(!editable);
        priceField.setDisable(!editable);
        quantityField.setDisable(!editable);
        reorderLevelField.setDisable(!editable);
        reorderQuantityField.setDisable(!editable);
        weightField.setDisable(!editable);
        otherDetailsField.setDisable(!editable);
        descriptionField.setDisable(!editable);
        isActiveCheck.setDisable(!editable);
        productTypeCombo.setDisable(!editable);
        supplierCombo.setDisable(!editable);

        deleteButton.setDisable(selectedProduct == null);
    }
}