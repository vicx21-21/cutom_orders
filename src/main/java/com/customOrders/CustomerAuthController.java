package com.customOrders;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;
import java.sql.SQLException;

public class CustomerAuthController {

    // Componentes FXML inyectados desde la vista
    @FXML private TextField emailSearchField;
    @FXML private Button mainActionButton;
    @FXML private Label messageLabel;
    @FXML private VBox registrationFormVBox;

    // Campos del formulario de registro
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField addressField;

    // Instancia del gestor de base de datos
    private CustomerManager customerManager = new CustomerManager();

    /**
     * Maneja el primer paso: buscar cliente por email o preparar el registro.
     */
    @FXML
    private void handleSearchOrRegister(ActionEvent event) {
        String email = emailSearchField.getText().trim();
        messageLabel.setText("");

        if (email.isEmpty() || !email.contains("@")) {
            messageLabel.setText("Por favor, ingrese un email válido.");
            return;
        }

        try {
            Customer customer = customerManager.getCustomerByEmail(email);

            if (customer != null) {
                // Cliente encontrado: Acceso exitoso
                System.out.println("Cliente encontrado. ID: " + customer.getCustomerID());
                accessCustomerDashboard(customer);
            } else {
                // Cliente NO encontrado: Preparar para registro
                messageLabel.setText("Email no registrado. Complete el formulario para crear su cuenta.");

                // Mostrar formulario de registro
                emailSearchField.setDisable(true);
                mainActionButton.setDisable(true);
                registrationFormVBox.setVisible(true);
                registrationFormVBox.setManaged(true);
            }
        } catch (SQLException e) {
            System.err.println("Error de base de datos al buscar cliente: " + e.getMessage());
            messageLabel.setText("ERROR de conexión: Verifique la base de datos.");
        }
    }

    /**
     * Maneja el segundo paso: registrar al nuevo cliente en la DB y acceder.
     */
    @FXML
    private void registerAndAccess(ActionEvent event) {
        String email = emailSearchField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String address = addressField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || address.isEmpty()) {
            messageLabel.setText("Debe completar todos los campos del formulario de registro.");
            return;
        }

        Customer newCustomer = new Customer(firstName, lastName, email, phoneNumber, address);

        try {
            int newId = customerManager.insertCustomer(newCustomer);

            if (newId > 0) {
                newCustomer.setCustomerID(newId);
                System.out.println("Nuevo cliente registrado exitosamente. ID asignado: " + newId);

                accessCustomerDashboard(newCustomer);
            } else {
                messageLabel.setText("Error al registrar: No se generó un ID válido.");
            }
        } catch (SQLException e) {
            System.err.println("Error de base de datos al registrar el cliente: " + e.getMessage());
            messageLabel.setText("ERROR de registro: Revise los datos y la conexión.");
        }
    }

    /**
     * Carga el CustomerDashboard.fxml y pasa el objeto Customer para inicializar la sesión.
     * @param customer El objeto Customer que ha iniciado sesión.
     */
    private void accessCustomerDashboard(Customer customer) {
        try {
            // 1. Obtiene la ventana actual
            Stage currentStage = (Stage) ((Node) emailSearchField).getScene().getWindow();

            // 2. Cargar el FXML con el FXMLLoader
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CustomerDashboard.fxml"));
            Parent root = loader.load();

            // 3. Obtener el controlador del dashboard cargado
            CustomerDashboardController controller = loader.getController();

            // 4. PASAR el objeto Customer al controlador del dashboard (CRUCIAL)
            controller.setCustomer(customer);

            // 5. Crear la escena (Corregido: Declaración de 'scene' una sola vez)
            Scene scene = new Scene(root);
            currentStage.setTitle("Panel de Órdenes - Cliente: " + customer.getFirstName());
            currentStage.setScene(scene);
            currentStage.centerOnScreen();
            currentStage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar la vista del Dashboard del Cliente: " + e.getMessage());
            messageLabel.setText("ERROR: No se pudo cargar el dashboard principal. Asegúrate de tener 'CustomerDashboard.fxml'.");
        }
    }

    /**
     * Cambia a la vista de Login del Administrador (AdminAuthView.fxml).
     */
    @FXML
    private void switchToAdminLogin(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/AdminAuthView.fxml"));

            // Aquí la declaración de 'scene' es correcta
            Scene scene = new Scene(root);
            currentStage.setTitle("Acceso de Administradores");
            currentStage.setScene(scene);
            currentStage.centerOnScreen();
            currentStage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar AdminAuthView.fxml: " + e.getMessage());
        }
    }
}