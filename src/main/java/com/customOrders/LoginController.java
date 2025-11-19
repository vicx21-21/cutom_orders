package com.customOrders;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class LoginController {

    // Método auxiliar para manejar el cambio de vista en la misma Stage
    private void navigateTo(ActionEvent event, String fxmlFileName) throws IOException {

        // CORRECCIÓN CLAVE: Usamos '/' para buscar el archivo desde la raíz
        // de la carpeta 'src/main/resources'.
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/" + fxmlFileName));

        // Se mantiene el código de carga de escena y ventana:
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Configurar título según el archivo cargado (opcional)
        if (fxmlFileName.equals("AdminAuthView.fxml")) {
            stage.setTitle("Autenticación de Encargado");
        }

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleEntrarComoEncargado(ActionEvent event) throws IOException {
        // Llama a la navegación con el nombre del archivo
        navigateTo(event, "AdminAuthView.fxml");
    }

    /**
     * Se llama al hacer clic en el botón "Cliente".
     * Abriría la vista de tienda o pedidos.
     */
    @FXML
    private void handleEntrarComoCliente(ActionEvent event) throws IOException {
        // Creamos un placeholder. Debes crear ClientShopView.fxml
        System.out.println("Entrando como Cliente...");
         navigateTo(event, "CustomerAuthView.fxml");
    }
}