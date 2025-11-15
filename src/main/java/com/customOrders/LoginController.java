package com.customOrders;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class LoginController {

    // Método auxiliar para manejar el cambio de vista en la misma Stage
    private void navigateTo(ActionEvent event, String fxmlFileName) throws IOException {

        // Obtiene la Stage actual de la fuente del evento (el botón)
        Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

        // Carga la nueva vista FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
        Parent root = loader.load();

        // Crea una nueva escena y reemplaza la escena actual
        Scene scene = new Scene(root);
        currentStage.setScene(scene);
        currentStage.setTitle(fxmlFileName.replace(".fxml", "").toUpperCase()); // Establece el título
        currentStage.show();
    }

    /**
     * Se llama al hacer clic en el botón "Encargado".
     * Abre la vista principal de administración (donde está la tabla de Clientes/Empleados).
     */
    @FXML
    private void handleEntrarComoEncargado(ActionEvent event) throws IOException {
        // Debes crear el archivo CustomerAdmin.fxml o un Dashboard principal
        navigateTo(event, "CustomerAdmin.fxml");
    }

    /**
     * Se llama al hacer clic en el botón "Cliente".
     * Abriría la vista de tienda o pedidos.
     */
    @FXML
    private void handleEntrarComoCliente(ActionEvent event) throws IOException {
        // Creamos un placeholder. Debes crear ClientShopView.fxml
        System.out.println("Entrando como Cliente...");
        // navigateTo(event, "ClientShopView.fxml");
    }
}