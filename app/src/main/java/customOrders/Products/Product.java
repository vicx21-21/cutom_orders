package customOrders.Products;

import java.time.LocalDate;

/**
 * Clase de modelo para la entidad Products.
 * Se incluye el nuevo campo 'imageUrl' para la ruta de la imagen del producto.
 */
public class Product {
    private final Integer product_id;
    private final String product_type_code;
    private final Integer supplier_id;
    private final String product_name;
    private final Double unit_price;
    private final String product_description;
    private final Integer reorder_level;
    private final Integer reorder_quantity;
    private final String other_details;
    private final Double weight_kg;
    private final LocalDate date_added;
    private final Boolean is_active;
    private Integer quantity; // Mutable Stock Maestro

    // *************************************************************
    // ** NUEVO CAMPO **
    // *************************************************************
    private final String imageUrl; // character varying (Ruta de la imagen)

    // *************************************************************
    // ** CONSTRUCTOR COMPLETO (14 argumentos) **
    // *************************************************************
    public Product(Integer product_id, String product_type_code, Integer supplier_id, String product_name, Double unit_price,
                   String product_description, Integer reorder_level, Integer reorder_quantity, String other_details,
                   Double weight_kg, LocalDate date_added, Boolean is_active, Integer quantity, String imageUrl) {
        this.product_id = product_id;
        this.product_type_code = product_type_code != null ? product_type_code : "";
        this.supplier_id = supplier_id;
        this.product_name = product_name;
        this.unit_price = unit_price;
        this.product_description = product_description != null ? product_description : "";
        this.reorder_level = reorder_level;
        this.reorder_quantity = reorder_quantity;
        this.other_details = other_details != null ? other_details : "";
        this.weight_kg = weight_kg;
        this.date_added = date_added;
        this.is_active = is_active;
        this.quantity = quantity;
        this.imageUrl = imageUrl != null ? imageUrl : ""; // Inicializar el nuevo campo
    }

    // Getters
    public Integer getProduct_id() { return product_id; }
    public String getProduct_type_code() { return product_type_code; }
    public Integer getSupplier_id() { return supplier_id; }
    public String getProduct_name() { return product_name; }
    public Double getUnit_price() { return unit_price; }
    public String getProduct_description() { return product_description; }
    public Integer getReorder_level() { return reorder_level; }
    public Integer getReorder_quantity() { return reorder_quantity; }
    public String getOther_details() { return other_details; }
    public Double getWeight_kg() { return weight_kg; }
    public LocalDate getDate_added() { return date_added; }
    public Boolean getIs_active() { return is_active; }
    public Integer getQuantity() { return quantity; }

    // *************************************************************
    // ** NUEVO GETTER **
    // *************************************************************
    public String getImageUrl() { return imageUrl; }

    // Setter para quantity
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}