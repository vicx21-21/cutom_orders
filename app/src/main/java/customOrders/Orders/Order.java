package customOrders.Orders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Modelo de datos para un Pedido (Orden), adaptado para ser mutable
 * para la edición en el CRUD.
 */
public class Order {
    // El ID es final una vez asignado (lo hace la DB, pero lo establecemos al cargar o guardar)
    private Integer order_id;

    // Campos que se editan
    private Integer customer_id;
    private LocalDate date_of_order;
    private String order_status;
    private String shipping_address;

    // Lista de ítems del pedido
    private List<ProductInOrder> items;

    // El total se almacena. Es inicializado por la DB o calculado dinámicamente.
    private Double total_amount;

    // Constructor para órdenes existentes (desde la BD)
    public Order(Integer order_id, Integer customer_id, LocalDate date_of_order,
                 String order_status, String shipping_address, List<ProductInOrder> items) {
        this.order_id = order_id;
        this.customer_id = customer_id;
        this.date_of_order = Objects.requireNonNullElseGet(date_of_order, LocalDate::now);
        this.order_status = Objects.requireNonNullElse(order_status, "Pendiente");
        this.shipping_address = Objects.requireNonNullElse(shipping_address, "");
        this.items = Objects.requireNonNullElseGet(items, ArrayList::new);

        // CORRECCIÓN: Inicializamos total_amount a 0.0.
        // El ViewOrdersController inyectará el valor de la DB usando setTotal_amount()
        // O bien, si la lista de items no está vacía (ej. en un nuevo pedido), lo calcula.
        this.total_amount = (this.items.isEmpty() && order_id != null) ? 0.0 : calculateTotal();
    }

    // Constructor para una nueva orden (ID=null)
    public Order(Integer customer_id) {
        this(null, customer_id, LocalDate.now(), "Pendiente", "", new ArrayList<>());
    }

    // --- Getters ---
    public Integer getOrder_id() { return order_id; }
    public Integer getCustomer_id() { return customer_id; }
    public LocalDate getDate_of_order() { return date_of_order; }
    public String getOrder_status() { return order_status; }
    public String getShipping_address() { return shipping_address; }

    /**
     * Devuelve el monto total almacenado. En la vista de pedidos históricos,
     * este será el valor cargado de la DB. En la vista de creación, será el calculado.
     */
    public Double getTotal_amount() {
        return total_amount;
    }

    public List<ProductInOrder> getItems() { return items; }

    // --- Setters (Necesarios para el CRUD) ---
    // El ID solo se puede establecer una vez (al cargar o al ser asignado por la BD)
    public void setOrder_id(Integer order_id) {
        if (this.order_id == null) {
            this.order_id = order_id;
        }
    }
    public void setCustomer_id(Integer customer_id) { this.customer_id = customer_id; }
    public void setDate_of_order(LocalDate date_of_order) { this.date_of_order = date_of_order; }
    public void setOrder_status(String order_status) { this.order_status = order_status; }
    public void setShipping_address(String shipping_address) { this.shipping_address = shipping_address; }
    public void setItems(List<ProductInOrder> items) {
        this.items = items;
        // Siempre recalcular el total al actualizar la lista de ítems.
        this.total_amount = calculateTotal();
    }

    // *** CORRECCIÓN: Setter faltante para inyectar el total histórico de la DB ***
    public void setTotal_amount(Double total_amount) {
        this.total_amount = total_amount;
    }

    /**
     * Calcula el monto total del pedido sumando los subtotales de todos los items.
     */
    private Double calculateTotal() {
        return items.stream()
                .mapToDouble(ProductInOrder::getTotalPrice)
                .sum();
    }

    /**
     * Añade un item al pedido. Si el producto ya existe, aumenta la cantidad.
     */
    public void addItem(ProductInOrder newItem) {
        for (ProductInOrder existingItem : items) {
            if (existingItem.getProduct().getProduct_id() == newItem.getProduct().getProduct_id()) {
                existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
                // Forzar el recálculo
                this.total_amount = calculateTotal();
                return;
            }
        }
        this.items.add(newItem);
        // Forzar el recálculo
        this.total_amount = calculateTotal();
    }
}