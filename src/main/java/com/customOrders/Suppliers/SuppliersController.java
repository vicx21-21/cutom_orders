package com.customOrders.Suppliers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SuppliersController implements Initializable {

    // --- Instancia del Manager para interactuar con la DB ---
    private final SuppliersManager manager = new SuppliersManager();

    // --- Componentes FXML de la Tabla ---
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
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private Label messageLabel;
    @FXML private Button deleteButton;

    private ObservableList<Suppliers> supplierList = FXCollections.observableArrayList();
    private Suppliers selectedSupplier;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Asumiendo que las referencias FXML están correctamente definidas en su vista.

        // Configurar las columnas.
        idColumn.setCellValueFactory(new PropertyValueFactory<>("supplier_id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("supplier_name"));
        contactNameColumn.setCellValueFactory(new PropertyValueFactory<>("contact_name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        supplierTable.setItems(supplierList);

        // --- Cargar datos reales de la DB ---
        loadData();

        // Configurar el listener para la tabla
        supplierTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showSupplierDetails(newValue));

        messageLabel.setText("Módulo de Proveedores cargado. Conectado a PostgreSQL.");
        setEditableFields(false);
    }

    /**
     * Carga los datos de la DB al ObservableList.
     */
    private void loadData() {
        try {
            supplierList.setAll(manager.getAllSuppliers());
            messageLabel.setText("Proveedores cargados desde la base de datos.");
        } catch (SQLException e) {
            System.err.println("Error al cargar proveedores de la DB: " + e.getMessage());
            messageLabel.setText("ERROR de DB al cargar proveedores: " + e.getMessage() + ". Revise su tabla.");
        }
    }

    // --- Métodos de Control ---

    private void showSupplierDetails(Suppliers supplier) {
        if (supplier != null) {
            selectedSupplier = supplier;
            idField.setText(supplier.getSupplier_id());
            nameField.setText(supplier.getSupplier_name());
            contactNameField.setText(supplier.getContact_name());
            phoneField.setText(supplier.getPhone());
            emailField.setText(supplier.getEmail());
            addressField.setText(supplier.getAddress());

            setEditableFields(true);
            deleteButton.setDisable(false);
            idField.setDisable(true); // Bloquear ID en modo edición
        } else {
            handleNew();
        }
    }

    @FXML
    private void handleNew() {
        selectedSupplier = null;
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

    @FXML
    private void handleSave() {
        // Eliminamos la validación de idField.getText().isEmpty()
        if (nameField.getText().isEmpty()) {
            messageLabel.setText("ERROR: El Nombre del proveedor es obligatorio.");
            return;
        }

        try {
            boolean isNew = selectedSupplier == null;

            if (isNew) {
                // **PUNTO 1: Usar el constructor sin ID**
                // Si es NUEVO, no le pasamos el ID. El Manager lo generará.
                Suppliers supplierToSave = new Suppliers(
                        nameField.getText(),
                        contactNameField.getText(),
                        phoneField.getText(),
                        addressField.getText(),
                        emailField.getText()
                );

                // **PUNTO 2: Manejar el String de retorno (Opción A)**
                // El manager devuelve el ID (String) si tuvo éxito.
                String newId = manager.insertSupplier(supplierToSave);

                if (newId != null) {
                    messageLabel.setText("Proveedor '" + supplierToSave.getSupplier_name() + "' AGREGADO correctamente. ID: " + newId);
                    // Opcional: El objeto supplierToSave ya tiene el ID asignado por el Manager.

                } else {
                    messageLabel.setText("ERROR: No se pudo agregar el proveedor.");
                    return;
                }

            } else {
                // Si es EDICIÓN, SÍ necesitamos el ID existente.
                String idText = idField.getText(); // Recuperamos el ID existente
                Suppliers supplierToSave = new Suppliers(
                        idText, // Le pasamos el ID existente
                        nameField.getText(),
                        contactNameField.getText(),
                        phoneField.getText(),
                        addressField.getText(),
                        emailField.getText()
                );

                if (manager.updateSupplier(supplierToSave)) {
                    messageLabel.setText("Proveedor con ID " + idText + " ACTUALIZADO correctamente en la DB.");
                } else {
                    messageLabel.setText("ERROR: No se pudo actualizar el proveedor.");
                    return;
                }
            }

            // 3. Recargar datos de la DB para actualizar la TableView
            loadData();

            // 4. Seleccionar el proveedor guardado
            // Nota: Si es nuevo, el objeto supplierToSave ya tiene el ID de la DB.
            supplierTable.getSelectionModel().select(selectedSupplier);
            idField.setDisable(true);

        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            messageLabel.setText("ERROR de DB al guardar: " + errorMsg);
            System.err.println("SQL Exception during save: " + errorMsg);
            e.printStackTrace();
        }
    }
    @FXML
    private void handleDelete() {
        if (selectedSupplier != null) {
            String idToDelete = selectedSupplier.getSupplier_id();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("Eliminar Proveedor ID: " + idToDelete);
            alert.setContentText("¿Está seguro que desea eliminar el proveedor '" + selectedSupplier.getSupplier_name() + "'?");

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    if (manager.deleteSupplier(idToDelete)) {
                        messageLabel.setText("Proveedor con ID " + idToDelete + " ELIMINADO correctamente de la DB.");
                        loadData(); // Recargar datos para actualizar la TableView
                        handleNew(); // Limpiar el formulario
                    } else {
                        messageLabel.setText("ERROR: No se pudo eliminar el proveedor (no encontrado en DB).");
                    }
                } catch (SQLException e) {
                    messageLabel.setText("ERROR de DB al eliminar: " + e.getMessage());
                    System.err.println("SQL Exception during delete: " + e.getMessage());
                }
            }
        }
    }

    // --- Métodos Auxiliares ---

    private void setEditableFields(boolean editable) {
        // El ID solo se puede cambiar si no hay un proveedor seleccionado (es decir, en handleNew)
        idField.setDisable(selectedSupplier != null);

        nameField.setDisable(!editable);
        contactNameField.setDisable(!editable);
        phoneField.setDisable(!editable);
        emailField.setDisable(!editable);
        addressField.setDisable(!editable);

        deleteButton.setDisable(selectedSupplier == null);
    }
}