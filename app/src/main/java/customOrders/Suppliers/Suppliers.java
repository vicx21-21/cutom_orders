package customOrders.Suppliers;

/**
 * Clase de modelo para la entidad Suppliers.
 * Refleja las columnas de la tabla 'suppliers'.
 * supplier_id se mantiene como String en el modelo para flexibilidad, aunque en DB sea INTEGER (SERIAL).
 */
public class Suppliers {
    private String supplier_id; // Contiene el ID numérico (e.g., "1", "2")
    private String supplier_name;
    private String contact_name;
    private String phone;
    private String address;
    private String email;

    // Constructor completo
    public Suppliers(String supplier_id, String supplier_name, String contact_name, String phone, String address, String email) {
        this.supplier_id = supplier_id;
        this.supplier_name = supplier_name;
        this.contact_name = contact_name;
        this.phone = phone;
        this.address = address;
        this.email = email;
    }

    // Constructor para Inserción (el ID será auto-generado)
    public Suppliers(String supplier_name, String contact_name, String phone, String address, String email) {
        // ID es null hasta que se inserte en la DB
        this(null, supplier_name, contact_name, phone, address, email);
    }

    // --- MÉTODOS REQUERIDOS POR OTRAS CLASES (EJ. ProductController) ---

    /**
     * Proporciona el ID del proveedor. Este método es necesario para resolver el error
     * "Cannot resolve method 'getId' in 'Supplier'" en clases que lo esperan.
     * @return El ID del proveedor como String.
     */
    public String getId() {
        return this.supplier_id;
    }

    // --- Getters y Setters ---
    // (Estos nombres de getters son los que usa TableView con PropertyValueFactory)

    public String getSupplier_id() { return supplier_id; }
    public void setSupplier_id(String supplier_id) { this.supplier_id = supplier_id; }

    public String getSupplier_name() { return supplier_name; }
    public void setSupplier_name(String supplier_name) { this.supplier_name = supplier_name; }

    public String getContact_name() { return contact_name; }
    public void setContact_name(String contact_name) { this.contact_name = contact_name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Sobreescribir toString para usar en ComboBoxes, si es necesario
    @Override
    public String toString() {
        return supplier_name + (supplier_id != null ? " (ID: " + supplier_id + ")" : "");
    }
}