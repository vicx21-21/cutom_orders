package customOrders.ProductTypes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import customOrders.ProductTypes.ProductType;
import customOrders.ProductTypes.ProductTypeManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ProductsTypesController implements Initializable {

    // Componentes FXML de la tabla
    @FXML private TableView productTypeTable;
    @FXML private TableColumn codeColumn;
    @FXML private TableColumn nameColumn;
    @FXML private TableColumn parentCodeColumn;

    // Componentes FXML del formulario
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private ComboBox  parentTypeComboBox;
    @FXML private Label messageLabel;
    @FXML private Button deleteButton;

    private ProductTypeManager manager = new ProductTypeManager();
    private ObservableList<ProductType> productTypeList = FXCollections.observableArrayList();

    // Variable para rastrear el elemento seleccionado para edición/eliminación
    private ProductType selectedType;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Configurar las columnas de la tabla para enlazar a las propiedades del modelo
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("productTypeCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productTypeName"));
        parentCodeColumn.setCellValueFactory(new PropertyValueFactory<>("parentProductTypeCode"));

        productTypeTable.setItems(productTypeList);

        // 2. Escuchar la selección de la tabla
        productTypeTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTypeDetails((ProductType) newValue));

        // 3. Cargar datos iniciales
        loadProductTypes();

        // Inicializar el ComboBox con una opción para 'Ninguno/Nulo'
        parentTypeComboBox.setPlaceholder(new Label("Seleccione un padre..."));

        // El botón de eliminar se deshabilita hasta que se seleccione algo
        deleteButton.setDisable(true);
    }

    /**
     * Carga todos los tipos de producto de la base de datos y actualiza la tabla.
     */
    private void loadProductTypes() {
        try {
            List<ProductType> list = manager.getAllProductTypes();
            productTypeList.setAll(list);

            // 4. Actualiza los ítems del ComboBox (tipos disponibles como padre)
            // Se usa una copia de la lista para no interferir con la lista principal de la tabla
            ObservableList<ProductType> parentOptions = FXCollections.observableArrayList(list);

            // Agregamos una opción 'Null' al inicio para indicar que no hay padre
            parentOptions.add(0, new ProductType("", "Ninguno (Top Level)", null));
            parentTypeComboBox.setItems(parentOptions);

            messageLabel.setText("Datos cargados correctamente. Total: " + list.size());

        } catch (SQLException e) {
            messageLabel.setText("Error al cargar los datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra los detalles del tipo de producto seleccionado en el formulario.
     */
    private void showTypeDetails(ProductType type) {
        selectedType = type;
        if (type != null) {
            codeField.setText(type.getProductTypeCode());
            nameField.setText(type.getProductTypeName());

            // Habilitar la edición del nombre y la selección de padre
            nameField.setDisable(false);
            parentTypeComboBox.setDisable(false);
            deleteButton.setDisable(false);

            // Seleccionar el padre en el ComboBox
            String parentCode = type.getParentProductTypeCode();
            if (parentCode.isEmpty()) {
                parentTypeComboBox.getSelectionModel().selectFirst(); // Selecciona "Ninguno"
            } else {
                // Busca y selecciona el tipo de padre en la lista
                parentTypeComboBox.getItems().stream()
                        .filter(pt -> pt.getClass().equals(parentCode))
                        .findFirst()
                        .ifPresent(parentTypeComboBox.getSelectionModel()::select);
            }

            // El código no se puede editar después de la creación
            codeField.setDisable(true);

        } else {
            handleNew(); // Si se deselecciona, limpia el formulario
        }
    }

    /**
     * Limpia el formulario para crear un nuevo registro.
     */
    @FXML
    private void handleNew() {
        selectedType = null;
        codeField.setText("");
        nameField.setText("");
        parentTypeComboBox.getSelectionModel().selectFirst(); // Selecciona "Ninguno"

        // Habilitar campos para la nueva creación
        codeField.setDisable(false);
        nameField.setDisable(false);
        parentTypeComboBox.setDisable(false);
        deleteButton.setDisable(true);
        messageLabel.setText("Listo para crear un nuevo Tipo de Producto. Ingrese el Código.");
    }

    /**
     * Guarda (inserta o actualiza) el tipo de producto.
     */
    @FXML
    private void handleSave() {
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        ProductType selectedParent = (ProductType) parentTypeComboBox.getSelectionModel().getSelectedItem();
        String parentCode = (selectedParent != null && !selectedParent.getProductTypeCode().isEmpty()) ?
                selectedParent.getProductTypeCode() : "";

        if (code.isEmpty() || name.isEmpty()) {
            messageLabel.setText("Error: El Código y el Nombre son obligatorios.");
            return;
        }

        // Evitar que un tipo de producto sea su propio padre
        if (code.equals(parentCode)) {
            messageLabel.setText("Error: Un Tipo de Producto no puede ser su propio padre.");
            return;
        }

        try {
            if (selectedType == null) {
                // INSERCIÓN (Nuevo registro)
                ProductType newType = new ProductType(code, name, parentCode);
                boolean success = manager.insertProductType(newType);

                if (success) {
                    messageLabel.setText("Tipo de Producto '" + name + "' creado exitosamente.");
                } else {
                    messageLabel.setText("Error al crear el Tipo de Producto.");
                }
            } else {
                // ACTUALIZACIÓN (Registro existente)
                selectedType.setProductTypeName(name);
                selectedType.setParentProductTypeCode(parentCode);
                boolean success = manager.updateProductType(selectedType);

                if (success) {
                    messageLabel.setText("Tipo de Producto '" + name + "' actualizado exitosamente.");
                } else {
                    messageLabel.setText("Error al actualizar el Tipo de Producto.");
                }
            }

            loadProductTypes(); // Recargar la tabla
            handleNew(); // Limpiar el formulario

        } catch (SQLException e) {
            messageLabel.setText("Error de DB al guardar: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Elimina el tipo de producto seleccionado.
     */
    @FXML
    private void handleDelete() {
        if (selectedType == null) {
            messageLabel.setText("Error: Debe seleccionar un tipo de producto para eliminar.");
            return;
        }

        // Confirmación de la eliminación (usando un Alert simple)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("Eliminar Tipo de Producto: " + selectedType.getProductTypeCode());
        alert.setContentText("¿Está seguro de que desea eliminar '" + selectedType.getProductTypeName() + "'?\n" +
                "Esto podría causar errores si hay productos asociados.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = manager.deleteProductType(selectedType.getProductTypeCode());
                    if (success) {
                        messageLabel.setText("Tipo de Producto '" + selectedType.getProductTypeName() + "' eliminado.");
                    } else {
                        messageLabel.setText("Error al eliminar. Verifique las dependencias (FK).");
                    }
                    loadProductTypes(); // Recargar la tabla
                    handleNew(); // Limpiar el formulario
                } catch (SQLException e) {
                    messageLabel.setText("Error de DB al eliminar: Podría haber productos o subtipos asociados.");
                    e.printStackTrace();
                }
            }
        });
    }
}