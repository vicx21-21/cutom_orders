package customOrders.Orders;

import customOrders.Products.Product;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class CartItem {
    private final Product product;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty subtotal;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = new SimpleIntegerProperty(quantity);
        this.subtotal = new SimpleDoubleProperty(product.getUnit_price() * quantity);
    }

    // Método para recalcular el subtotal
    public void updateQuantity(int newQuantity) {
        this.quantity.set(newQuantity);
        this.subtotal.set(product.getUnit_price() * newQuantity);
    }

    // Getters para las propiedades
    public Product getProduct() { return product; }
    public String getName() { return product.getProduct_name(); } // Usado para la columna del carrito
    public double getPrice() { return product.getUnit_price(); } // Usado para la columna del carrito
    public SimpleIntegerProperty quantityProperty() { return quantity; }
    public SimpleDoubleProperty subtotalProperty() { return subtotal; }

    // Getters simples
    public int getQuantity() { return quantity.get(); }
    public double getSubtotal() { return subtotal.get(); }
}