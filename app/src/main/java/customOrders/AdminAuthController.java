package customOrders;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

// La clase comienza aquí
public class AdminAuthController {

    @FXML
    private TextField usuarioText;

    @FXML
    private PasswordField contraseñaText;

    // Se mantiene la implementación interna de la URL de la base de datos dentro de la lógica de conexión
    // o se usa la que está definida en el código Java principal, sin exponerla en la vista.

    @FXML
    private void ingresar(ActionEvent event) {

        // --- LÓGICA DE AUTENTICACIÓN ---

        String usuario = usuarioText.getText();
        String contraseña = contraseñaText.getText();

        // En un entorno real, la conexión a la BD se haría aquí usando la URL interna/codificada,
        // pero para este ejemplo, solo se valida el usuario/contraseña.
        if (usuario.equals("empleado2") && contraseña.equals("empleado2025")) {

            // Navegación EXITOSA
            try {
                navigateToAdminDashboard(event);
            } catch (IOException e) {
                // Manejar error de carga de Dashboard
                showAlert(AlertType.ERROR, "Error de Carga", "No se pudo cargar la vista principal de administración (AdminDashboard.fxml).");
                e.printStackTrace();
            }

        } else {
            // Manejar error de credenciales
            showAlert(AlertType.ERROR, "Error", "Credenciales incorrectas.");
        }
    }

    /**
     * Carga el AdminDashboard.fxml y cierra la ventana de autenticación actual.
     */
    private void navigateToAdminDashboard(ActionEvent event) throws IOException {

        // Carga el nuevo FXML del Dashboard
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));

        Scene scene = new Scene(fxmlLoader.load());

        // Obtiene la Stage (ventana) actual usando el evento
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Configura y muestra la nueva ventana
        currentStage.setTitle("Sistema de Órdenes - Dashboard Principal");
        currentStage.setScene(scene);
        currentStage.centerOnScreen();
        currentStage.show();
    }

    /**
     * Implementación del método para volver a la vista de Login de Clientes.
     * Carga el CustomerAuthView.fxml y reemplaza la escena actual.
     */
    @FXML
    private void switchToCustomerLogin(ActionEvent event) {
        try {
            // 1. Obtiene la ventana actual
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 2. Carga la vista de Autenticación de Clientes
            Parent root = FXMLLoader.load(getClass().getResource("/CustomerAuthView.fxml"));

            // 3. Configura y muestra la nueva escena
            Scene scene = new Scene(root);
            currentStage.setTitle("Acceso de Clientes");
            currentStage.setScene(scene);
            currentStage.centerOnScreen();
            currentStage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar CustomerAuthView.fxml: " + e.getMessage());
            showAlert(AlertType.ERROR, "Error de Carga", "No se pudo cargar la vista de acceso de clientes (CustomerAuthView.fxml).");
            e.printStackTrace();
        }
    }


    /**
     * Método auxiliar para mostrar alertas.
     */
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}