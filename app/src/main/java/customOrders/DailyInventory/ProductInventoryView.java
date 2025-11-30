package customOrders.DailyInventory;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import customOrders.Products.Product;

import java.util.Objects;

/**
 * Objeto de Vista (View DTO) utilizado para mostrar la data en la interfaz.
 * Combina la información del producto maestro con los datos de conteo.
 * (Corregido para usar JavaFX Properties y ser compatible con TableView)
 */
public class ProductInventoryView {

    // Referencia al objeto Producto subyacente
    private final Product product;

    // Propiedades para la tabla JavaFX (necesario para PropertyValueFactory y edición)
    private final SimpleIntegerProperty product_id;
    private final SimpleStringProperty product_name;
    private final SimpleObjectProperty<Integer> masterQuantity; // Stock Maestro actual
    private final SimpleObjectProperty<Integer> historicLevelDisplay; // Stock Histórico (para la fecha)
    private final SimpleIntegerProperty newLevelDisplay; // Stock Contado/Nuevo (EDITABLE)
    private final SimpleStringProperty status; // Estado (Diferencia)


    // --- Constructor ---
    public ProductInventoryView(Product product, Integer historicLevel) {
        this.product = Objects.requireNonNull(product);

        // Inicialización de las propiedades
        this.product_id = new SimpleIntegerProperty(product.getProduct_id());
        this.product_name = new SimpleStringProperty(product.getProduct_name());
        this.masterQuantity = new SimpleObjectProperty<>(product.getQuantity());
        this.historicLevelDisplay = new SimpleObjectProperty<>(historicLevel);

        // Inicializa el nuevo nivel con el nivel histórico o 0 si es nulo.
        this.newLevelDisplay = new SimpleIntegerProperty(historicLevel != null ? historicLevel : 0);

        this.status = new SimpleStringProperty(calculateStatus(this.newLevelDisplay.get(), this.masterQuantity.get()));

        // Listener para recalcular el status cuando el nuevo nivel (editable) cambia
        this.newLevelDisplay.addListener((obs, oldVal, newVal) -> {
            this.status.set(calculateStatus(newVal.intValue(), this.masterQuantity.get()));
        });
    }

    /**
     * Calcula la diferencia y devuelve un string de estado.
     */
    private String calculateStatus(int newLevel, int masterLevel) {
        int difference = newLevel - masterLevel;
        if (difference == 0) {
            return "OK (0)";
        } else if (difference > 0) {
            return "Sobrante (+" + difference + ")";
        } else {
            return "Faltante (" + difference + ")";
        }
    }

    // --- Getters y Property Methods requeridos por TableView ---

    public int getProduct_id() { return product_id.get(); }
    public SimpleIntegerProperty product_idProperty() { return product_id; }

    public String getProduct_name() { return product_name.get(); }
    public SimpleStringProperty product_nameProperty() { return product_name; }

    public Integer getMasterQuantity() { return masterQuantity.get(); }
    public SimpleObjectProperty<Integer> masterQuantityProperty() { return masterQuantity; }

    // Este es el valor que se muestra en la columna "Stock Histórico"
    public Integer getHistoricLevelDisplay() { return historicLevelDisplay.get(); }
    public SimpleObjectProperty<Integer> historicLevelDisplayProperty() { return historicLevelDisplay; }

    // Este es el valor editable de la columna "Stock Contado"
    public Integer getNewLevelDisplay() { return newLevelDisplay.get(); }
    public SimpleIntegerProperty newLevelDisplayProperty() { return newLevelDisplay; }

    public String getStatus() { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }

    // --- Getters de Acceso para el Controlador ---

    // Usado por el controlador para obtener el valor Integer real antes de guardar
    public int getNewLevel() { return newLevelDisplay.get(); }
    public void setNewLevel(int newLevel) { this.newLevelDisplay.set(newLevel); }

    public Product getProduct() { return product; }
}