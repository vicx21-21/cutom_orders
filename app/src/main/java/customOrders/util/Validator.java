package customOrders.util;

/**
 * Clase estática de utilidad para validar la entrada de datos (Strings)
 * en formatos numéricos (Integer y Double).
 */
public class Validator {

    /**
     * Verifica si una cadena de texto puede ser parseada como un Integer.
     * @param str La cadena a validar.
     * @return true si la cadena es un entero válido, false en caso contrario.
     */
    public static boolean isValidInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            // Intentar parsear la cadena
            Integer.parseInt(str.trim());
            return true;
        } catch (NumberFormatException e) {
            // La cadena no es un entero válido
            return false;
        }
    }

    /**
     * Verifica si una cadena de texto puede ser parseada como un Double (decimal).
     * @param str La cadena a validar.
     * @return true si la cadena es un double válido, false en caso contrario.
     */
    public static boolean isValidDouble(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            // Intentar parsear la cadena
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            // La cadena no es un double válido
            return false;
        }
    }

    /**
     * Verifica si una cadena de texto es null o está vacía (después de recortar espacios).
     * @param str La cadena a validar.
     * @return true si la cadena está vacía o es null, false si contiene algún texto.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Puedes agregar aquí otras validaciones como formatos de fecha, correos, etc.
}