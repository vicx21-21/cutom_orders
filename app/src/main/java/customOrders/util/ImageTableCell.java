package customOrders.util;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import customOrders.Products.Product;

/**
 * TableCell personalizado para renderizar una ruta de archivo (String) como una imagen (ImageView).
 * Muestra una imagen cargada desde la URL del producto dentro de la celda.
 */
public class ImageTableCell<T> extends TableCell<Product, String> {

    private final ImageView imageView;
    private static final int IMAGE_SIZE = 40; // Tamaño fijo para las miniaturas en la tabla

    public ImageTableCell() {
        this.imageView = new ImageView();
        this.imageView.setFitHeight(IMAGE_SIZE);
        this.imageView.setFitWidth(IMAGE_SIZE);
        this.imageView.setPreserveRatio(true);

        // Alineación y manejo de padding
        setAlignment(Pos.CENTER);
        setStyle("-fx-padding: 2;"); // Pequeño padding para la celda
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null || item.trim().isEmpty()) {
            // Si la celda está vacía o no hay URL, no mostramos nada.
            setGraphic(null);
            setText(null);
        } else {
            try {
                // El 'item' es la URL de la imagen (obtenida de getImageUrl()).
                // Usamos Image(url, true) para cargar en background y manejar errores.
                Image image = new Image(item, true);

                // Manejo de errores de carga (p.e., si la ruta es inválida)
                image.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        System.err.println("Error al cargar la imagen: " + item);
                        // Mostrar un texto o un icono de error si la carga falla
                        setText("[Error Carga]");
                        setGraphic(null);
                    }
                });

                // Si la imagen se carga correctamente (o está cargando)
                imageView.setImage(image);
                setGraphic(imageView);
                setText(null); // Ocultar el texto de la URL

            } catch (Exception e) {
                // Manejo de excepciones (p.e. si la URL es malformada)
                System.err.println("Error al procesar la URL de la imagen: " + item);
                setText("https://www.spanishdict.com/translate/inv%C3%A1lida");
                setGraphic(null);
            }
        }
    }
}