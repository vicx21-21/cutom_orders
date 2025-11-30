package customOrders.Clients;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import customOrders.Customer;
import customOrders.CustomerManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador de la vista de Clientes (ADMIN).
 * Permite al administrador realizar operaciones CRUD completas sobre la tabla de clientes.
 */
public class ClientController implements Initializable {

    // Usamos el Manager compartido para la lógica de base de datos.
    private final CustomerManager customerManager = new CustomerManager();
    private Customer selectedCustomer;
    private boolean isNewRecord = false;

    // --- FXML Fields: Tabla ---
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> idColumn;
    @FXML private TableColumn<Customer, String> firstNameColumn;
    @FXML private TableColumn<Customer, String> lastNameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> addressColumn;
    @FXML private Label messageLabel;

    // --- FXML Fields: Formulario de Detalle/Edición ---
    @FXML private TextField idField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private Button deleteButton;

    private ObservableList<Customer> customerData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // 1. Configurar las columnas para que mapeen a los Getters del Modelo Customer.java
        idColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        // 2. Inicializar la tabla y cargar datos
        customerData = FXCollections.observableArrayList();
        customerTable.setItems(customerData);
        loadCustomerData();

        // 3. Configurar el listener de selección de la tabla para mostrar detalles
        customerTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCustomerDetails(newValue));

        // 4. Estado inicial del formulario
        setFormDisabled(true);
        if (deleteButton != null) {
            deleteButton.setDisable(true);
        }
    }

    /**
     * Muestra los detalles del cliente seleccionado en el formulario.
     */
    private void showCustomerDetails(Customer customer) {
        if (customer != null) {
            selectedCustomer = customer;
            isNewRecord = false;
            setFormDisabled(false);
            if (deleteButton != null) {
                deleteButton.setDisable(false);
            }
            setMessage("", false);

            // Rellenar campos del formulario
            idField.setText(String.valueOf(customer.getCustomerID()));
            firstNameField.setText(customer.getFirstName());
            lastNameField.setText(customer.getLastName());
            phoneField.setText(customer.getPhoneNumber());
            emailField.setText(customer.getEmail());
            addressField.setText(customer.getAddress());

        } else {
            clearForm();
            setFormDisabled(true);
            if (deleteButton != null) {
                deleteButton.setDisable(true);
            }
        }
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private void clearForm() {
        selectedCustomer = null;
        idField.setText("Auto-generado");
        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
    }

    /**
     * Habilita o deshabilita los campos de entrada del formulario.
     */
    private void setFormDisabled(boolean disabled) {
        firstNameField.setDisable(disabled);
        lastNameField.setDisable(disabled);
        phoneField.setDisable(disabled);
        emailField.setDisable(disabled);
        addressField.setDisable(disabled);
    }

    // --- Métodos de Acción CRUD ---

    /**
     * Maneja el botón 'Nuevo'. Prepara el formulario para un nuevo registro (CREATE).
     */
    @FXML
    public void handleNewCustomer() {
        clearForm();
        setFormDisabled(false);
        isNewRecord = true;
        if (deleteButton != null) {
            deleteButton.setDisable(true);
        }
        firstNameField.requestFocus();
        setMessage("Modo: Nuevo Cliente.", false);
        customerTable.getSelectionModel().clearSelection();
    }

    /**
     * Maneja el botón 'Guardar'. Inserta un nuevo cliente o actualiza uno existente (CREATE/UPDATE).
     */
    @FXML
    public void handleSaveCustomer() {
        if (!isInputValid()) return;

        try {
            // Capturar datos del formulario
            String fName = firstNameField.getText();
            String lName = lastNameField.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            String address = addressField.getText();

            if (isNewRecord) {
                // --- INSERT (CREATE) ---
                Customer newCustomer = new Customer(fName, lName, email, phone, address);

                int newID = customerManager.insertCustomer(newCustomer);
                if (newID > 0) {
                    newCustomer.setCustomerID(newID);
                    customerData.add(newCustomer);
                    setMessage("Cliente '" + newCustomer.getFirstName() + "' creado con ID: " + newID, false);
                    isNewRecord = false;
                    // Seleccionar el nuevo cliente en la tabla
                    customerTable.getSelectionModel().select(newCustomer);
                    showCustomerDetails(newCustomer);
                } else {
                    setMessage("ERROR: No se pudo obtener el ID generado al insertar.", true);
                }

            } else if (selectedCustomer != null) {
                // --- UPDATE (ACTUALIZAR) ---
                selectedCustomer.setFirstName(fName);
                selectedCustomer.setLastName(lName);
                selectedCustomer.setPhoneNumber(phone);
                selectedCustomer.setEmail(email);
                selectedCustomer.setAddress(address);

                boolean success = customerManager.updateCustomer(selectedCustomer);

                if (success) {
                    // Refrescar la tabla para mostrar los datos actualizados
                    customerTable.refresh();
                    setMessage("Cliente con ID " + selectedCustomer.getCustomerID() + " actualizado.", false);
                } else {
                    setMessage("ERROR: No se pudo actualizar el cliente.", true);
                }
            }

        } catch (SQLException e) {
            setMessage("ERROR de DB al guardar: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    /**
     * Carga todos los clientes de la base de datos y actualiza la tabla (READ).
     */
    @FXML
    public void loadCustomerData() {
        try {
            List<Customer> customers = customerManager.getAllCustomers();
            customerData.clear();
            customerData.addAll(customers);
            setMessage("Clientes cargados: " + customers.size(), false);

        } catch (SQLException e) {
            setMessage("Error al cargar los datos de clientes. Verifique la conexión DB.", true);
            showAlert(Alert.AlertType.ERROR, "Error de Conexión",
                    "No se pudieron cargar los clientes.", "Detalles: " + e.getMessage());
        }
    }

    /**
     * Maneja el evento de eliminación de un cliente seleccionado (DELETE).
     */
    @FXML
    public void handleDeleteCustomer() {
        Customer customerToDelete = customerTable.getSelectionModel().getSelectedItem();

        if (customerToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Ningún Cliente Seleccionado",
                    "Por favor, selecciona un cliente de la tabla para eliminar.", "");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("Eliminar Cliente: " + customerToDelete.getFirstName() + " " + customerToDelete.getLastName());
        alert.setContentText("¿Estás seguro de que quieres eliminar a este cliente permanentemente? (ID: " + customerToDelete.getCustomerID() + ")");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Eliminar en la base de datos
                customerManager.deleteCustomer(customerToDelete.getCustomerID());
                // Eliminar de la tabla en la UI
                customerData.remove(customerToDelete);
                clearForm();
                setMessage("El cliente fue eliminado correctamente.", false);

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error de Base de Datos",
                        "No se pudo eliminar el cliente. Puede que tenga pedidos asociados.", "Detalles: " + e.getMessage());
            }
        }
    }

    // --- Métodos de Utilidad (Necesarios para el funcionamiento) ---

    /**
     * Valida la entrada del usuario en el formulario.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errorMessage += "El campo Nombre es requerido.\n";
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errorMessage += "El campo Apellido es requerido.\n";
        }
        // Validación básica de Email
        if (emailField.getText() == null || !emailField.getText().contains("@") || emailField.getText().trim().length() < 5) {
            errorMessage += "Email no válido o muy corto.\n";
        }
        if (addressField.getText() == null || addressField.getText().trim().isEmpty()) {
            errorMessage += "El campo Dirección es requerido.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Campos Inválidos", "Por favor, corrija los siguientes campos:", errorMessage);
            return false;
        }
    }

    /**
     * Muestra un mensaje de estado/error en la etiqueta inferior.
     */
    private void setMessage(String message, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            if (isError) {
                // Estilo para mensaje de error (rojo)
                messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else {
                // Estilo para mensaje de éxito/informativo (verde)
                messageLabel.setStyle("-fx-text-fill: green;");
            }
        }
    }

    /**
     * Método auxiliar para mostrar alertas de JavaFX.
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}