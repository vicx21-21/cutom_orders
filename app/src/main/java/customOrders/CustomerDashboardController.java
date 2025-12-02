package customOrders;

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

// 🚨 IMPORTACIÓN CORREGIDA: Ahora importa la interfaz desde su propio archivo
import customOrders.CustomerAware;

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
            // DEBUG para asegurar que el ID del cliente fue cargado en el Dashboard
            System.out.println("DEBUG (Dashboard): Cliente ID cargado en Dashboard: " + customer.getCustomerID());
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
            // 🛑 RUTA CORREGIDA: Usando la ubicación modules/resources/
            fxmlFile = "/modules/customer/CreateOrderView.fxml";
        } else if (source.getId().equals("btnVerOrdenes")) {
            // Asumiendo una ruta similar para la vista de órdenes
            fxmlFile = "/modules/customer/ViewOrdersView.fxml";
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

        // 1. Verificación Estricta del Cliente
        if (currentCustomer == null) {
            welcomeLabel.setText("Error fatal: El cliente de la sesión es nulo.");
            return;
        }

        // 2. Carga de la Vista
        try {
            // CRÍTICO: Obtener el recurso URL primero y verificar si es nulo
            URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                // Si el recurso no se encuentra, lanzar una excepción informativa
                throw new IOException("El archivo FXML no se encontró en la ruta: " + fxmlPath +
                        ". Verifique la ruta en el classpath. (Path usado: " + fxmlPath + ")");
            }

            // Continuar la carga si el URL es válido
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node content = loader.load();

            // 3. Inyección del Cliente al Sub-Controlador
            Object controller = loader.getController();

            // Verifica que el controlador no sea nulo y que implemente la interfaz CustomerAware (ahora importada)
            if (controller != null && controller instanceof CustomerAware) {
                // 🚨 Esta es la línea crítica para inyectar el cliente
                ((CustomerAware) controller).setCustomer(currentCustomer);
                System.out.println("DEBUG (Dashboard): Cliente " + currentCustomer.getCustomerID() + " inyectado en " + controller.getClass().getSimpleName());
            } else if (controller == null) {
                System.err.println("Advertencia: El FXML " + fxmlPath + " se cargó, pero no se encontró el controlador (fx:controller missing).");
            } else {
                // Caso donde el controlador existe, pero no implementa CustomerAware
                System.err.println("Advertencia: El controlador " + controller.getClass().getSimpleName() + " no implementa CustomerAware.");
            }

            // 4. Mostrar Contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

        } catch (IOException e) {
            // Manejo de error si el FXML no existe o si falla la inicialización del controlador (la causa más común)
            System.err.println("Error CRÍTICO al cargar la vista: " + fxmlPath);
            e.printStackTrace();

            contentArea.getChildren().clear();
            Label errorLabel = new Label("ERROR CRÍTICO: No se pudo cargar el módulo '" + fxmlPath + "'. Revise la ruta o la estructura interna del FXML.");
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
}