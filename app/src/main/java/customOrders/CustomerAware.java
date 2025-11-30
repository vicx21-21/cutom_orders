package customOrders;

// ¡CORREGIDO! Importación directa desde el paquete raíz 'com.customOrders'
import customOrders.Customer;

/**
 * Interfaz para controladores que necesitan recibir un objeto Customer después de la carga del FXML.
 */
public interface CustomerAware {
    /**
     * Establece el cliente actual con el que operará el controlador.
     * @param customer El cliente seleccionado.
     */
    void setCustomer(Customer customer);
}