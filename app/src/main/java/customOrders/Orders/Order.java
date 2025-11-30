package customOrders.Orders;

import java.time.LocalDate;

/**
 * Clase de modelo para la entidad Order (Encabezado del Pedido).
 * Corresponde a la tabla 'orders' con los campos verificados.
 */
public class Order {
    private final Integer order_id;
    private final Integer customer_id;
    private final LocalDate date_of_order;
    private final String order_status;
    private final Double total_amount;
    private final String shipping_address; // El campo en la DB es shipping_addres (sin 's' al final)

    public Order(Integer order_id, Integer customer_id, LocalDate date_of_order, String order_status, Double total_amount, String shipping_address) {
        this.order_id = order_id;
        this.customer_id = customer_id;
        this.date_of_order = date_of_order;
        this.order_status = order_status;
        this.total_amount = total_amount;
        // Asumiendo que el campo 'shipping_addres' se mapea a esta variable
        this.shipping_address = shipping_address;
    }

    // Getters
    public Integer getOrder_id() { return order_id; }
    public Integer getCustomer_id() { return customer_id; }
    public LocalDate getDate_of_order() { return date_of_order; }
    public String getOrder_status() { return order_status; }
    public Double getTotal_amount() { return total_amount; }

    // Usamos el nombre 'shipping_address' en el código Java para claridad,
    // aunque la columna de la DB se llame 'shipping_addres'
    public String getShipping_address() { return shipping_address; }
}