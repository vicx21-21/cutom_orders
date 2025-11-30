package customOrders.Orders;

/**
 * Representa una línea de detalle dentro de una orden (el "contenido").
 */
public class OrderItem {
    private final String productName;
    private final Integer quantity;
    private final Double unitPrice;
    private final Double subtotal;

    public OrderItem(String productName, Integer quantity, Double unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        // Calcular el subtotal al instanciar
        this.subtotal = quantity * unitPrice;
    }

    // Getters
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public Double getSubtotal() { return subtotal; }
}