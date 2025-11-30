package customOrders.Suppliers;

import customOrders.util.Dialogs;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class SuppliersController implements Initializable {

    // --- Instancia del Manager para interactuar con la DB ---
    // Usamos el nombre de clase completo por si hay ambigüedades
    private final SuppliersManager manager = new SuppliersManager();

    // --- Componentes FXML de la Tabla ---
    // Los nombres de fx:id deben coincidir EXACTAMENTE con el FXML
    @FXML private TableView<Suppliers> supplierTable;
    @FXML private TableColumn<Suppliers, String> idColumn;
    @FXML private TableColumn<Suppliers, String> nameColumn;
    @FXML private TableColumn<Suppliers, String> contactNameColumn;
    @FXML private TableColumn<Suppliers, String> phoneColumn;
    @FXML private TableColumn<Suppliers, String> addressColumn;

    // --- Componentes FXML del Formulario de Edición ---
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField contactNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField; // Asumimos que también tienes emailField en el FXML
    @FXML private TextField addressField;
    @FXML private Label messageLabel;
    @FXML private Button deleteButton;
    // No necesitamos fx:id para guardar/nuevo si solo usamos onAction

    private ObservableList<Suppliers> supplierList = FXCollections.observableArrayList();
    private Suppliers selectedSupplier;

    /**
     * Se llama automáticamente después de que el cargador FXML haya procesado todos los elementos.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Configurar las columnas para usar los nombres de propiedad del modelo Suppliers.
        idColumn.setCellValueFactory(new PropertyValueFactory<>("supplier_id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("supplier_name"));
        contactNameColumn.setCellValueFactory(new PropertyValueFactory<>("contact_name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address")); // Usamos el campo 'address'

        supplierTable.setItems(supplierList);

        // 2. Cargar datos reales de la DB
        loadData();

        // 3. Configurar el listener para la tabla
        supplierTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showSupplierDetails(newValue));

        messageLabel.setText("Módulo de Proveedores listo. Seleccione o cree uno nuevo.");
        // Al inicio, el formulario debe estar vacío
        showSupplierDetails(null);
    }

    /**
     * Carga los datos de la DB al ObservableList y actualiza la TableView.
     */
    private void loadData() {
        try {
            supplierList.setAll(manager.getAllSuppliers());
            messageLabel.setText("Proveedores cargados. Total: " + supplierList.size());
        } catch (SQLException e) {
            Dialogs.showErrorDialog("Error de Conexión o Carga",
                    "No se pudo cargar la lista de proveedores desde la base de datos.",
                    "Revise su configuración de PostgreSQL y el método getAllSuppliers().", e);
            messageLabel.setText("ERROR de DB al cargar proveedores.");
        }
    }

    /**
     * Rellena el formulario con los detalles del proveedor seleccionado.
     */
    private void showSupplierDetails(Suppliers supplier) {
        if (supplier != null) {
            selectedSupplier = supplier;
            idField.setText(supplier.getSupplier_id());
            nameField.setText(supplier.getSupplier_name());
            contactNameField.setText(supplier.getContact_name());
            phoneField.setText(supplier.getPhone());
            emailField.setText(supplier.getEmail());
            addressField.setText(supplier.getAddress());

            // Habilitar la edición y el botón de eliminar
            setEditableFields(true);
            deleteButton.setDisable(false);
            idField.setDisable(true); // Bloquear ID en modo edición
        } else {
            // Limpiar formulario y entrar en modo 'Nuevo'
            handleNew();
        }
    }

    // --- Métodos de Acción ---

    /**
     * Prepara el formulario para la creación de un nuevo proveedor.
     */
    @FXML
    private void handleNew() {
        selectedSupplier = null;
        supplierTable.getSelectionModel().clearSelection();

        idField.setText("");
        nameField.setText("");
        contactNameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");

        setEditableFields(true);
        deleteButton.setDisable(true);
        messageLabel.setText("Introduzca los datos para un nuevo proveedor.");
        idField.setDisable(false); // ID es editable solo para un nuevo registro
        nameField.requestFocus();
    }

    /**
     * Guarda un nuevo proveedor o actualiza uno existente.
     */
    @FXML
    private void handleSave() {
        // Validación básica
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            Dialogs.showWarningDialog("Datos Incompletos", "El nombre del proveedor es obligatorio.", "Por favor, introduzca un nombre fiscal.");
            return;
        }

        try {
            boolean isNew = selectedSupplier == null;

            // 1. Recopilar datos del formulario
            Suppliers supplierToSave;

            if (isNew) {
                // Si es NUEVO, usamos el constructor sin ID (el Manager lo genera)
                supplierToSave = new Suppliers(
                        nameField.getText(),
                        contactNameField.getText(),
                        phoneField.getText(),
                        addressField.getText(),
                        emailField.getText()
                );

                // 2. Insertar y obtener el nuevo ID
                String newId = manager.insertSupplier(supplierToSave);
                if (newId != null) {
                    messageLabel.setText("Proveedor AGREGADO correctamente. ID: " + newId);
                } else {
                    Dialogs.showErrorDialog("Error de Inserción", "No se pudo agregar el proveedor.", "Revise los logs de la consola para detalles de la base de datos.");
                    return;
                }

            } else {
                // Si es EDICIÓN, SÍ necesitamos el ID existente.
                String idText = idField.getText(); // Recuperamos el ID existente
                supplierToSave = new Suppliers(
                        idText, // Le pasamos el ID existente
                        nameField.getText(),
                        contactNameField.getText(),
                        phoneField.getText(),
                        addressField.getText(),
                        emailField.getText()
                );

                // 2. Actualizar
                if (manager.updateSupplier(supplierToSave)) {
                    messageLabel.setText("Proveedor con ID " + idText + " ACTUALIZADO correctamente.");
                } else {
                    Dialogs.showErrorDialog("Error de Actualización", "No se pudo actualizar el proveedor.", "El registro no fue encontrado o hubo un error de DB.");
                    return;
                }
            }

            // 3. Recargar datos y limpiar/seleccionar
            loadData();
            // Buscar y seleccionar el item guardado/actualizado en la tabla
            supplierList.stream()
                    .filter(s -> s.getSupplier_name().equals(supplierToSave.getSupplier_name()))
                    .findFirst()
                    .ifPresent(supplierTable.getSelectionModel()::select);

            idField.setDisable(true); // Bloquear ID después de guardar

        } catch (SQLException e) {
            Dialogs.showErrorDialog("Error de Base de Datos",
                    "Ocurrió un error al intentar guardar el proveedor.",
                    "Detalle: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina el proveedor actualmente seleccionado.
     */
    @FXML
    private void handleDelete() {
        if (selectedSupplier != null) {
            Optional<ButtonType> result = Dialogs.showConfirmationDialog(
                    "Confirmar Eliminación",
                    "Eliminar Proveedor ID: " + selectedSupplier.getSupplier_id(),
                    "¿Está seguro que desea eliminar el proveedor '" + selectedSupplier.getSupplier_name() + "'? Esta acción es irreversible."
            );

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (manager.deleteSupplier(selectedSupplier.getSupplier_id())) {
                        messageLabel.setText("Proveedor ELIMINADO correctamente.");
                        loadData();
                        handleNew(); // Limpiar el formulario
                    } else {
                        Dialogs.showWarningDialog("No Encontrado", "No se pudo eliminar el proveedor.", "El registro no fue encontrado en la base de datos.");
                    }
                } catch (SQLException e) {
                    Dialogs.showErrorDialog("Error de DB", "No se pudo eliminar el proveedor.", "Detalle: " + e.getMessage(), e);
                }
            }
        }
    }

    // --- Métodos Auxiliares ---

    /**
     * Controla la habilitación de los campos de texto del formulario.
     */
    private void setEditableFields(boolean editable) {
        // El ID siempre se deshabilita si selectedSupplier no es null (es decir, en modo edición)
        idField.setDisable(selectedSupplier != null);

        nameField.setDisable(!editable);
        contactNameField.setDisable(!editable);
        phoneField.setDisable(!editable);
        emailField.setDisable(!editable);
        addressField.setDisable(!editable);

        // Habilitar/deshabilitar el botón de eliminar basado en si hay algo seleccionado
        deleteButton.setDisable(selectedSupplier == null);
    }
}