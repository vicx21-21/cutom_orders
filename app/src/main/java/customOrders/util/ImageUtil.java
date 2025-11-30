package customOrders.util;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtil {

    // RUTA ESTÁNDAR DEL CLASSPATH: El "/" apunta a la raíz de 'resources'.
    // RUTA CORREGIDA basado en la estructura que proporcionaste:
    // /src/main/resources/customOrders/resources/product_images/
    private static final String RESOURCE_PATH_PREFIX = "/customOrders/resources/product_images/";

    /**
     * Intenta cargar una imagen basándose en la URL o nombre de archivo proporcionado.
     * Prioriza Classpath (nombre de archivo) y URLs web, pero soporta rutas de archivo absolutas
     * como medida defensiva para los registros de DB incorrectos (C:\Users\...).
     * @param imageUrl El valor del campo 'image_url' de la base de datos (nombre de archivo, URL web, o ruta absoluta antigua).
     * @return El objeto Image o una imagen de placeholder si falla.
     */
    public static Image loadImageFromProductUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            // Placeholder para imágenes no especificadas
            return new Image("https://placehold.co/100x75/cccccc/333333?text=SIN+IMAGEN", true);
        }

        // Eliminar posibles espacios en blanco
        imageUrl = imageUrl.trim();

        // Caso 1: URL Web (http/https)
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return new Image(imageUrl, true);
        }

        // Caso 2: Ruta de Archivo Absoluta (DEFENSIVA para data antigua C:\Users...)
        if (imageUrl.matches("^[a-zA-Z]:\\\\.*") || imageUrl.contains(File.separator) || imageUrl.contains("/")) {
            File file = new File(imageUrl);
            if (file.exists()) {
                try {
                    // Intenta cargar la imagen directamente desde la ruta absoluta
                    System.out.println("ÉXITO: Imagen cargada desde ruta de archivo absoluta: " + imageUrl);
                    return new Image(new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    System.err.println("Advertencia (Datos Antiguos): Archivo no encontrado en ruta absoluta: " + imageUrl);
                }
            }
        }

        // Caso 3: Nombre de Archivo de Classpath (ej: "OIP.jpg") - MÉTODO ROBUSTO
        String resourcePath = RESOURCE_PATH_PREFIX + imageUrl;
        System.out.println("DEBUG: Intentando cargar desde Classpath: " + resourcePath);

        try {
            // Usamos getResourceAsStream con el ClassLoader, un método muy fiable.
            // Para ClassLoader, a veces es necesario quitar el '/' inicial
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath.substring(1));

            if (is == null) {
                // Si falla con el ClassLoader del hilo (sin el /), intentamos el ClassLoader de la clase (con /)
                is = ImageUtil.class.getResourceAsStream(resourcePath);
            }

            if (is != null) {
                // Si el recurso se encuentra, cargamos la imagen usando el InputStream
                System.out.println("ÉXITO: Imagen cargada desde Classpath: " + resourcePath);
                return new Image(is);
            } else {
                // Advertencia si el archivo no se pudo encontrar
                System.err.println("RECURSO NO ENCONTRADO EN CLASSPATH: " + resourcePath);
            }
        } catch (Exception e) {
            // Error al intentar Classpath. No es crítico.
            System.err.println("Error al cargar recurso Classpath para: " + imageUrl + ". Detalle: " + e.getMessage());
        }

        // Placeholder si no se pudo cargar por ninguna de las vías
        try {
            String errorText = "FALLO: " + imageUrl.substring(0, Math.min(20, imageUrl.length())) + "...";
            return new Image(new URL("https://placehold.co/100x75/ff0000/ffffff?text=" + errorText).toExternalForm(), true);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}