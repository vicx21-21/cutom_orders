package com.customOrders;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CustomerDashboardController implements Initializable {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label welcomeLabel; // Etiqueta de bienvenida

    @FXML
    private Label menuTitleLabel; // Etiqueta del título del menú

    // Almacena el cliente que ha iniciado sesión
    private Customer currentCustomer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // El dashboard está inicializado, pero esperamos al cliente
    }

    /**
     * Este método es llamado por el CustomerAuthController ANTES de mostrar esta vista,
     * para pasar la información del cliente que inició sesión.
     */
    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;

        // Personaliza el dashboard con la información del cliente
        if (customer != null) {
            welcomeLabel.setText("Bienvenido, " + customer.getFirstName() + ". Seleccione una opción del menú.");
            menuTitleLabel.setText(customer.getFirstName().toUpperCase() + "'S MENU");
        }
    }

    /**
     * Maneja el clic en los botones de navegación del menú lateral.
     * Carga el FXML correspondiente en el área de contenido (StackPane).
     */
    @FXML
    private void handleNavigation(ActionEvent event) {
        Button source = (Button) event.getSource();
        String fxmlFile = null;

        // 1. Determina qué botón fue presionado
        if (source.getId().equals("btnCrearOrden")) {
            // Define la ruta al FXML para crear órdenes
            fxmlFile = "modules/customer/CreateOrderView.fxml";
        } else if (source.getId().equals("btnVerOrdenes")) {
            // Define la ruta al FXML para ver órdenes
            fxmlFile = "modules/customer/ViewOrdersView.fxml";
        }

        // 2. Carga el FXML en el StackPane
        if (fxmlFile != null) {
            loadFXMLToContent(fxmlFile);
        }
    }

    /**
     * Carga un archivo FXML dentro del StackPane central.
     * Pasa el objeto Customer al controlador del módulo cargado.
     */
    private void loadFXMLToContent(String fxmlPath) {
        if (currentCustomer == null) {
            welcomeLabel.setText("Error: No se ha identificado al cliente.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlPath));
            Node content = loader.load();

            // IMPORTANTE: Pasa el objeto Customer al controlador del módulo (si lo necesita)
            // Asumimos que los controladores (ej. CreateOrderController) tienen un método setCustomer()
            if (loader.getController() instanceof CustomerAware) {
                ((CustomerAware) loader.getController()).setCustomer(currentCustomer);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

        } catch (IOException e) {
            // Muestra un error si no puede encontrar el archivo del módulo
            System.err.println("Error al cargar la vista: " + fxmlPath);
            e.printStackTrace();
            contentArea.getChildren().clear();
            Label errorLabel = new Label("ERROR: No se pudo cargar el módulo '" + fxmlPath + "'.");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            contentArea.getChildren().add(errorLabel);
        }
    }

    /**
     * Maneja el cierre de sesión, regresando a la pantalla de autenticación de cliente.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Obtiene la ventana actual
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Carga la vista de autenticación de cliente
            Parent root = FXMLLoader.load(getClass().getResource("/CustomerAuthView.fxml"));

            Scene scene = new Scene(root);
            currentStage.setTitle("Acceso de Clientes");
            currentStage.setScene(scene);
            currentStage.centerOnScreen(); // Centra la ventana de login
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Interfaz auxiliar para pasar el objeto Customer a los sub-controladores.
     * Los controladores como 'CreateOrderController' deben implementar 'CustomerAware'.
     */
    public interface CustomerAware {
        void setCustomer(Customer customer);
    }
}