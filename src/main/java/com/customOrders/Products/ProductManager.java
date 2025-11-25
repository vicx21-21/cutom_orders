package com.customOrders.Products;

import com.customOrders.PostgresConnector;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductManager {

    // --- Clases Auxiliares (Records) para las Claves Foráneas ---
    // Se definen aquí para que puedan ser importadas y usadas por el Controller
    // Usados para llenar los ComboBox y mapear los datos
    public record ProductTypeFK(String code, String name) {
        @Override
        public String toString() { return code + " - " + name; }
    }
    public record SupplierFK(Integer id, String name) {
        @Override
        public String toString() { return id + " - " + name; }
    }

    /**
     * Obtiene todos los productos de la base de datos.
     */
    public List<Product> getAllProducts() throws SQLException {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM Products ORDER BY product_id";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_type_code"),
                        rs.getInt("supplier_id"),
                        rs.getString("product_name"),
                        rs.getDouble("unit_price"),
                        rs.getString("product_description"),
                        rs.getInt("reorder_level"),
                        rs.getInt("reorder_quantity"),
                        rs.getString("other_details"),
                        rs.getDouble("weight_kg"),
                        rs.getDate("date_added").toLocalDate(), // Convertir Date a LocalDate
                        rs.getBoolean("is_active"),
                        rs.getInt("quantity")
                );
                productList.add(product);
            }
        }
        return productList;
    }

    /**
     * Inserta un nuevo producto. Asume que product_id es autogenerado.
     */
    public Product insertProduct(Product product) throws SQLException {
        String sql = "INSERT INTO Products (product_type_code, supplier_id, product_name, unit_price, product_description, reorder_level, reorder_quantity, other_details, weight_kg, date_added, is_active, quantity) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Usamos RETURN_GENERATED_KEYS para obtener el ID autoincrementado
        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Mapeo de campos
            ps.setString(1, product.getProduct_type_code());
            ps.setInt(2, product.getSupplier_id());
            ps.setString(3, product.getProduct_name());
            ps.setDouble(4, product.getUnit_price());
            ps.setString(5, product.getProduct_description());
            ps.setInt(6, product.getReorder_level());
            ps.setInt(7, product.getReorder_quantity());
            ps.setString(8, product.getOther_details());
            ps.setDouble(9, product.getWeight_kg());
            ps.setDate(10, Date.valueOf(LocalDate.now())); // Se inserta la fecha actual
            ps.setBoolean(11, product.getIs_active());
            ps.setInt(12, product.getQuantity());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La inserción de producto falló, no se afectaron filas.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Retornar un nuevo objeto Product con el ID generado
                    int newId = generatedKeys.getInt(1);
                    return new Product(
                            newId, product.getProduct_type_code(), product.getSupplier_id(), product.getProduct_name(), product.getUnit_price(),
                            product.getProduct_description(), product.getReorder_level(), product.getReorder_quantity(), product.getOther_details(),
                            product.getWeight_kg(), LocalDate.now(), product.getIs_active(), product.getQuantity()
                    );
                } else {
                    throw new SQLException("La inserción de producto falló, no se obtuvo ID generado.");
                }
            }
        }
    }

    /**
     * Actualiza un producto existente (todos los campos).
     */
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE Products SET product_type_code=?, supplier_id=?, product_name=?, unit_price=?, product_description=?, reorder_level=?, reorder_quantity=?, other_details=?, weight_kg=?, is_active=?, quantity=? WHERE product_id=?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Mapeo de campos
            ps.setString(1, product.getProduct_type_code());
            ps.setInt(2, product.getSupplier_id());
            ps.setString(3, product.getProduct_name());
            ps.setDouble(4, product.getUnit_price());
            ps.setString(5, product.getProduct_description());
            ps.setInt(6, product.getReorder_level());
            ps.setInt(7, product.getReorder_quantity());
            ps.setString(8, product.getOther_details());
            ps.setDouble(9, product.getWeight_kg());
            ps.setBoolean(10, product.getIs_active());
            ps.setInt(11, product.getQuantity());
            ps.setInt(12, product.getProduct_id()); // WHERE condition (PK)

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un producto por su ID.
     */
    public boolean deleteProduct(Integer productId) throws SQLException {
        String sql = "DELETE FROM Products WHERE product_id = ?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        }
    }

    // **********************************************
    // *** MÉTODO ESPECÍFICO PARA GESTIÓN DE STOCK ***
    // **********************************************
    /**
     * Actualiza solamente el stock maestro (columna 'quantity') para un producto específico.
     * Este es el método que DailyInventoryManager debe llamar después de guardar el conteo.
     * * @param productId ID del producto a actualizar.
     * @param newQuantity La nueva cantidad de stock maestro.
     */
    public void updateStockQuantityOnly(int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE Products SET quantity = ? WHERE product_id = ?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newQuantity);
            ps.setInt(2, productId);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("Advertencia: No se encontró el Producto ID " + productId +
                        " para actualizar el stock maestro.");
            } else {
                System.out.println("Actualización de Stock Maestro exitosa para Producto ID " + productId +
                        ". Nueva Cantidad: " + newQuantity);
            }
        }
        // SQLException se propaga automáticamente si falla la conexión o la consulta.
    }

    // --- Métodos de Lectura de Claves Foráneas ---

    /**
     * Obtiene todos los tipos de producto para llenar el ComboBox (FK).
     */
    public List<ProductTypeFK> getAllProductTypesFK() throws SQLException {
        List<ProductTypeFK> list = new ArrayList<>();
        // Asumiendo una tabla simple de tipos de producto para la FK
        String sql = "SELECT Product_Type_Code, Product_Type_Name FROM Product_Types ORDER BY Product_Type_Code";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ProductTypeFK(
                        rs.getString("Product_Type_Code"),
                        rs.getString("Product_Type_Name")
                ));
            }
        }
        return list;
    }

    /**
     * Obtiene todos los proveedores para llenar el ComboBox (FK).
     */
    public List<SupplierFK> getAllSupplierFKs() throws SQLException {
        List<SupplierFK> list = new ArrayList<>();
        // Asumiendo una tabla simple de proveedores para la FK
        String sql = "SELECT supplier_id, supplier_name FROM Suppliers ORDER BY supplier_id";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new SupplierFK(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name")
                ));
            }
        }
        return list;
    }
}