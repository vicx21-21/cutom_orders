package customOrders.util;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtil {

    // RUTA ESTÁNDAR DEL CLASSPATH: Este prefijo se usa SÓLO para nombres de archivo de la DB.
    // La ruta de recursos es /src/main/resources/...
    private static final String RESOURCE_PATH_PREFIX = "/customOrders/resources/product_images/";

    /**
     * Intenta cargar una imagen basándose en la URL o nombre de archivo proporcionado.
     * Prioriza la carga de recursos de Classpath y luego URLs web.
     * @param imageUrl El valor del campo 'image_url' de la base de datos (nombre de archivo, URL web, o ruta absoluta).
     * @return El objeto Image o una imagen de placeholder si falla.
     */
    public static Image loadImageFromProductUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            // Placeholder para imágenes no especificadas
            return new Image("https://placehold.co/100x75/cccccc/333333?text=SIN+IMAGEN", true);
        }

        imageUrl = imageUrl.trim();

        // Caso 1: URL Web (http/https)
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return new Image(imageUrl, true);
        }

        // Caso 2: Ruta de Archivo Absoluta (DEFENSIVA para data antigua C:\Users...)
        if (imageUrl.matches("^[a-zA-Z]:\\\\.*") || imageUrl.contains(File.separator)) {
            File file = new File(imageUrl);
            if (file.exists()) {
                try {
                    System.out.println("ÉXITO: Imagen cargada desde ruta de archivo absoluta: " + imageUrl);
                    return new Image(new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    System.err.println("Advertencia (Datos Antiguos): Archivo no encontrado en ruta absoluta: " + imageUrl);
                }
            }
        }

        // Caso 3: Nombre de Archivo de Classpath (ej: "OIP.jpg" o "/images/placeholder.png")
        String resourcePath;

        // 🚨 LÓGICA CORREGIDA: Si la ruta ya empieza con '/', no añadimos el prefijo (es una ruta de recurso completa).
        if (imageUrl.startsWith("/")) {
            resourcePath = imageUrl; // Ej: /images/placeholder.png
        } else {
            // Si es un nombre de archivo simple (DB), añadimos el prefijo de la carpeta de imágenes.
            resourcePath = RESOURCE_PATH_PREFIX + imageUrl;
        }

        System.out.println("DEBUG: Intentando cargar desde Classpath: " + resourcePath);

        try {
            // Intentamos cargar el recurso usando el ClassLoader de la clase
            InputStream is = ImageUtil.class.getResourceAsStream(resourcePath);

            if (is != null) {
                System.out.println("ÉXITO: Imagen cargada desde Classpath: " + resourcePath);
                return new Image(is);
            } else {
                System.err.println("RECURSO NO ENCONTRADO EN CLASSPATH: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar recurso Classpath para: " + imageUrl + ". Detalle: " + e.getMessage());
        }

        // Placeholder si no se pudo cargar por ninguna de las vías
        try {
            // Placeholder de error, lo cargamos desde el classpath si está disponible
            URL url = ImageUtil.class.getResource("/images/error_placeholder.png");
            if (url != null) {
                return new Image(url.toExternalForm(), true);
            }
            // Si incluso el placeholder de error falla, usamos un placeholder genérico web
            return new Image(new URL("https://placehold.co/100x75/ff0000/ffffff?text=FALLO").toExternalForm(), true);

        } catch (MalformedURLException e) {
            return null;
        }
    }
}