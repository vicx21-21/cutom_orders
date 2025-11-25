package com.customOrders.Suppliers;

import com.customOrders.PostgresConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Manager para la entidad Suppliers.
 * Maneja la conversión de Supplier_ID de String (Modelo Java) a INTEGER (DB).
 */
public class SuppliersManager {

    /**
     * Obtiene todos los proveedores de la base de datos.
     */
    public List<Suppliers> getAllSuppliers() throws SQLException {
        List<Suppliers> supplierList = new ArrayList<>();
        String sql = "SELECT \"supplier_id\", \"supplier_name\", \"contact_name\", \"phone\", \"address\", \"email\" FROM suppliers ORDER BY \"supplier_id\"";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // DB -> Modelo: El ID se lee como INT y se convierte a String para el modelo Java
                String supplierIdStr = String.valueOf(rs.getInt("supplier_id"));

                Suppliers supplier = new Suppliers(
                        supplierIdStr,
                        rs.getString("supplier_name"),
                        rs.getString("contact_name"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("email")
                );
                supplierList.add(supplier);
            }
        }
        return supplierList;
    }

    /**
     * Inserta un nuevo proveedor y establece el ID generado por la DB en el objeto.
     * @return El ID generado por el nuevo proveedor (String), o null si falla.
     */
    public String insertSupplier(Suppliers supplier) throws SQLException {
        // Se excluye supplier_id de la lista de columnas.
        String sql = "INSERT INTO suppliers (\"supplier_name\", \"contact_name\", \"phone\", \"address\", \"email\") " +
                "VALUES (?, ?, ?, ?, ?) RETURNING \"supplier_id\"";

        String generatedId = null;

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, supplier.getSupplier_name());
            ps.setString(2, supplier.getContact_name());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getAddress());
            ps.setString(5, supplier.getEmail());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = String.valueOf(rs.getInt(1));
                        supplier.setSupplier_id(generatedId); // Actualizar el objeto con el ID
                    }
                }
            }
            return generatedId;
        }
    }

    /**
     * Actualiza un proveedor existente.
     * Convierte el ID del modelo (String) a Integer para el SQL.
     */
    public boolean updateSupplier(Suppliers supplier) throws SQLException {
        String sql = "UPDATE suppliers SET \"supplier_name\"=?, \"contact_name\"=?, \"phone\"=?, \"address\"=?, \"email\"=? WHERE \"supplier_id\"=?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // **CONVERSIÓN CRÍTICA: String a Integer para el SQL**
            int supplierIdInt = Integer.parseInt(supplier.getSupplier_id());

            ps.setString(1, supplier.getSupplier_name());
            ps.setString(2, supplier.getContact_name());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getAddress());
            ps.setString(5, supplier.getEmail());
            ps.setInt(6, supplierIdInt); // Seta el INTEGER en el WHERE clause

            return ps.executeUpdate() > 0;
        } catch (NumberFormatException e) {
            System.err.println("ERROR: El ID del proveedor debe ser un número entero válido: " + supplier.getSupplier_id());
            // Relanza la excepción para que el controlador lo maneje
            throw new SQLException("El ID del proveedor debe ser un número entero válido para la actualización.", e);
        }
    }

    /**
     * Elimina un proveedor por su ID.
     * Convierte el ID del modelo (String) a Integer para el SQL.
     */
    public boolean deleteSupplier(String supplierId) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE \"supplier_id\" = ?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // **CONVERSIÓN CRÍTICA: String a Integer para el SQL**
            int supplierIdInt = Integer.parseInt(supplierId);

            ps.setInt(1, supplierIdInt);
            return ps.executeUpdate() > 0;
        } catch (NumberFormatException e) {
            System.err.println("ERROR: El ID del proveedor debe ser un número entero válido: " + supplierId);
            throw new SQLException("El ID del proveedor a eliminar debe ser un número entero válido.", e);
        }
    }
}