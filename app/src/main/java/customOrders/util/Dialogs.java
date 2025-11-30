package customOrders.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Clase de utilidad para mostrar cuadros de diálogo estándar de JavaFX (Alerts).
 */
public class Dialogs {

    /**
     * Muestra un cuadro de diálogo de error.
     * @param title Título de la ventana.
     * @param header Mensaje principal de la cabecera.
     * @param content Mensaje de contenido.
     */
    public static void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Muestra un cuadro de diálogo de error con detalles expandibles del stack trace de una excepción.
     * @param title Título de la ventana.
     * @param header Mensaje principal de la cabecera.
     * @param content Mensaje de contenido.
     * @param ex La excepción cuyo stack trace se mostrará en detalle.
     */
    public static void showErrorDialog(String title, String header, String content, Exception ex) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Crear el stack trace como texto
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Detalles del error:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Establecer el contenido expandible
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    /**
     * Muestra un cuadro de diálogo de información.
     * @param title Título de la ventana.
     * @param header Mensaje principal de la cabecera.
     * @param content Mensaje de contenido.
     */
    public static void showInformationDialog(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Muestra un cuadro de diálogo de advertencia.
     * @param title Título de la ventana.
     * @param header Mensaje principal de la cabecera.
     * @param content Mensaje de contenido.
     */
    public static void showWarningDialog(String title, String header, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Muestra un cuadro de diálogo de confirmación.
     * @param title Título de la ventana.
     * @param header Mensaje principal de la cabecera.
     * @param content Mensaje de contenido.
     * @return Un Optional<ButtonType> con el botón presionado (OK o CANCEL).
     */
    public static Optional<ButtonType> showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}