package com.customOrders;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Al iniciar el Dashboard, podemos cargar la vista por defecto si es necesario.
        // Por ahora, solo se muestra el mensaje de bienvenida del FXML.
    }

    /**
     * Maneja el clic en todos los botones de navegación del menú lateral.
     * Carga el FXML correspondiente en el área de contenido (StackPane).
     */
    @FXML
    private void handleNavigation(ActionEvent event) {
        Button source = (Button) event.getSource();

        String fxmlFile = null;

        // 1. Determina qué botón fue presionado
        if (source.getId().equals("btnOrdenes")) {
            fxmlFile = "modules/OrdersView.fxml";
        } else if (source.getId().equals("btnClientes")) {
            fxmlFile = "modules/ClientsView.fxml";
        } else if (source.getId().equals("btnProductos")) {
            fxmlFile = "modules/ProductsView.fxml";
        } else if (source.getId().equals("btnTiposProducto")) {
            fxmlFile = "modules/ProductTypesView.fxml";
        } else if (source.getId().equals("btnProveedores")) {
            fxmlFile = "modules/SuppliersView.fxml";
        } else if (source.getId().equals("btnInventario")) {
            fxmlFile = "modules/DailyInventoryView.fxml";
        }

        // 2. Carga el FXML en el StackPane
        if (fxmlFile != null) {
            loadFXMLToContent(fxmlFile);
        }
    }

    /**
     * Carga un archivo FXML dentro del StackPane central.
     * @param fxmlPath La ruta relativa al archivo FXML dentro de resources (e.g., "modules/ClientsView.fxml").
     */
    private void loadFXMLToContent(String fxmlPath) {
        try {
            // Asegúrate de usar getClass().getResource() ya que fxmlPath es relativo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlPath));
            Node content = loader.load();

            // Limpia el contenido anterior del StackPane
            contentArea.getChildren().clear();

            // Agrega el nuevo contenido
            contentArea.getChildren().add(content);

        } catch (IOException e) {
            // Muestra un error si no puede encontrar el archivo.
            // Esto ocurrirá hasta que crees los FXMLs de los módulos.
            System.err.println("Error al cargar la vista: " + fxmlPath);
            e.printStackTrace();

            // Mensaje de error visible en la UI
            contentArea.getChildren().clear();
            Label errorLabel = new Label("ERROR: No se pudo cargar el módulo '" + fxmlPath + "'. Revise que el archivo exista en la ruta correcta.");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            contentArea.getChildren().add(errorLabel);
        }
    }
}