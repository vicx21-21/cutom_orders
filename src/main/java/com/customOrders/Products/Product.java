package com.customOrders.Products;

import java.time.LocalDate;

/**
 * Clase de modelo para la entidad Products.
 * Se hace el campo 'quantity' mutable para permitir actualizaciones en memoria,
 * reflejando el nuevo stock maestro.
 */
public class Product {
    private final Integer product_id; // integer (Primary Key) (INMUTABLE)
    private final String product_type_code; // character varying (INMUTABLE)
    private final Integer supplier_id; // integer (INMUTABLE)
    private final String product_name; // character varying (INMUTABLE)
    private final Double unit_price; // numeric(10, 2) (INMUTABLE)
    private final String product_description; // character varying (INMUTABLE)
    private final Integer reorder_level; // integer (INMUTABLE)
    private final Integer reorder_quantity; // integer (INMUTABLE)
    private final String other_details; // character varying (INMUTABLE)
    private final Double weight_kg; // numeric(8, 2) (INMUTABLE)
    private final LocalDate date_added; // date (INMUTABLE)
    private final Boolean is_active; // boolean (INMUTABLE)

    // *************************************************************
    // ** CAMBIO CLAVE: Se elimina 'final' para hacerlo mutable. **
    // *************************************************************
    private Integer quantity; // integer (MUTABLE - Stock Maestro)

    // Constructor completo (Igual que antes, solo eliminamos 'final' de quantity)
    public Product(Integer product_id, String product_type_code, Integer supplier_id, String product_name, Double unit_price,
                   String product_description, Integer reorder_level, Integer reorder_quantity, String other_details,
                   Double weight_kg, LocalDate date_added, Boolean is_active, Integer quantity) {
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

    // Getter para quantity
    public Integer getQuantity() { return quantity; }

    // *************************************************************
    // ** NUEVO MÉTODO: Setter para 'quantity' que resuelve el error. **
    // *************************************************************
    /**
     * Setter para actualizar la cantidad (stock maestro).
     * Esto permite modificar la cantidad en un objeto Product después de su creación.
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}