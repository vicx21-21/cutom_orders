package com.customOrders.ProductTypes;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// Clase modelo para la tabla Product_Types
public class ProductType {

    // Product_Type_Code (PK - String)
    private final StringProperty productTypeCode;

    // Product_Type_Name (String)
    private final StringProperty productTypeName;

    // Parent_Product_Type_Code (FK - String, puede ser nulo)
    private final StringProperty parentProductTypeCode;

    // --- CONSTRUCTORES ---

    /**
     * Constructor principal y completo.
     * Usado para cargar desde DB y para crear nuevos objetos antes de guardar.
     * @param productTypeCode El código único (PK).
     * @param productTypeName El nombre.
     * @param parentProductTypeCode El código del tipo padre (o null/vacío).
     */
    public ProductType(String productTypeCode, String productTypeName, String parentProductTypeCode) {
        this.productTypeCode = new SimpleStringProperty(productTypeCode);
        this.productTypeName = new SimpleStringProperty(productTypeName);
        // Asegura que el valor de la propiedad no sea null, usando "" si el valor de DB es null.
        this.parentProductTypeCode = new SimpleStringProperty(parentProductTypeCode != null ? parentProductTypeCode : "");
    }

    // --- GETTERS Y SETTERS ---

    public String getProductTypeCode() {
        return productTypeCode.get();
    }
    public StringProperty productTypeCodeProperty() {
        return productTypeCode;
    }
    public void setProductTypeCode(String productTypeCode) {
        this.productTypeCode.set(productTypeCode);
    }

    public String getProductTypeName() {
        return productTypeName.get();
    }
    public StringProperty productTypeNameProperty() {
        return productTypeName;
    }
    public void setProductTypeName(String productTypeName) {
        this.productTypeName.set(productTypeName);
    }

    public String getParentProductTypeCode() {
        return parentProductTypeCode.get();
    }
    public StringProperty parentProductTypeCodeProperty() {
        return parentProductTypeCode;
    }
    public void setParentProductTypeCode(String parentProductTypeCode) {
        // Al establecer, si es nulo o vacío, lo guarda como vacío en la propiedad (para el ComboBox/UI)
        this.parentProductTypeCode.set(parentProductTypeCode != null && !parentProductTypeCode.isEmpty() ? parentProductTypeCode : "");
    }

    // Método para la representación en ComboBox (mostrar el nombre y código)
    @Override
    public String toString() {
        // Aseguramos que solo muestre el código si no es un placeholder (código vacío)
        String code = productTypeCode.get();
        if (code.isEmpty() && productTypeName.get().equals("Ninguno (Top Level)")) {
            return productTypeName.get();
        }
        return productTypeName.get() + " (" + code + ")";
    }
}