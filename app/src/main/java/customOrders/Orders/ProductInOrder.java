package customOrders.Orders;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import customOrders.Products.Product;

/**
 * Clase de modelo para representar un producto en el carrito o en un pedido.
 * Combina la información del producto con la cantidad solicitada (product_quantity).
 */
public class ProductInOrder {
    private final Product product;
    private final IntegerProperty quantity; // Mapea a product_quantity en la DB
    private final DoubleProperty totalPrice;

    public ProductInOrder(Product product, int quantity) {
        this.product = product;
        this.quantity = new SimpleIntegerProperty(quantity);
        this.totalPrice = new SimpleDoubleProperty(product.getUnit_price() * quantity);
        // Escucha cambios en la cantidad para actualizar el precio total automáticamente
        this.quantity.addListener((obs, oldVal, newVal) ->
                updateTotalPrice(newVal.intValue()));
    }

    private void updateTotalPrice(int newQuantity) {
        double newTotal = this.product.getUnit_price() * newQuantity;
        this.totalPrice.set(newTotal);
    }

    // Getters para JavaFX TableView
    public String getProduct_name() { return product.getProduct_name(); }
    public Double getUnit_price() { return product.getUnit_price(); }
    public int getQuantity() { return quantity.get(); } // Usado por TableView
    public double getTotalPrice() { return totalPrice.get(); }

    // Propiedades
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }

    // Getter del objeto Producto completo
    public Product getProduct() { return product; }

    // Setter para actualizar la cantidad del item en el carrito
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }
}
