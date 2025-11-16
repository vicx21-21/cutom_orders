package com.customOrders;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    // Dentro de MainApplication.java
// ...
    @Override
    public void start(Stage stage) throws IOException {

        // 1. CORRECCIÓN: Usamos /LoginView.fxml para indicar la raíz de la carpeta resources
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/LoginView.fxml"));

        // 2. Carga la escena con el tamaño de tu diseño (400x350)
        Scene scene = new Scene(fxmlLoader.load(), 400, 350);

        // 3. Configura y muestra la ventana principal (Stage)
        stage.setTitle("Sistema de Órdenes - Acceso");
        stage.centerOnScreen();
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Este método estándar inicia la ejecución de la aplicación JavaFX
        launch();
    }
}