package com.customOrders;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

// La clase comienza aquí
public class AdminAuthController {

    @FXML
    private TextField usuarioText;

    @FXML
    private PasswordField contraseñaText;

    @FXML
    private TextField urlText;

    @FXML
    private void ingresar(ActionEvent event) {

        // --- LÓGICA DE AUTENTICACIÓN ---

        String usuario = usuarioText.getText();
        String contraseña = contraseñaText.getText();

        if (usuario.equals("developer") && contraseña.equals("210521")) {

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
        // Nota: Asumimos que AdminDashboard.fxml está en la raíz de resources (/)
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
     * Método auxiliar para mostrar alertas.
     */
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} // La clase cierra aquí