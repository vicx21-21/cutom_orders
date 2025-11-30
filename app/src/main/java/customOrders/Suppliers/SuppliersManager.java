package customOrders.Suppliers;

import customOrders.PostgresConnector; // Importamos el conector real
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SuppliersManager {

    /**
     * Obtiene una lista de todos los proveedores de la base de datos.
     * @return Una lista de objetos Suppliers.
     * @throws SQLException Si ocurre un error durante la conexión o la consulta.
     */
    public List<Suppliers> getAllSuppliers() throws SQLException {
        List<Suppliers> supplierList = new ArrayList<>();
        String sql = "SELECT supplier_id, supplier_name, contact_name, phone, address, email FROM Suppliers ORDER BY supplier_name";

        try (Connection conn = PostgresConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int idInt = rs.getInt("supplier_id");
                String id = String.valueOf(idInt);

                String name = rs.getString("supplier_name");
                String contact = rs.getString("contact_name");
                String phone = rs.getString("phone");
                String address = rs.getString("address");
                String email = rs.getString("email");

                Suppliers supplier = new Suppliers(id, name, contact, phone, address, email);
                supplierList.add(supplier);
            }
        }
        catch (SQLException e) {
            System.err.println("Error al obtener proveedores de la DB: " + e.getMessage());
            throw e;
        }

        return supplierList;
    }

    /**
     * Inserta un nuevo proveedor en la base de datos.
     * @param supplier El objeto Suppliers con los datos a insertar.
     * @return El ID generado por la base de datos como String.
     * @throws SQLException Si ocurre un error.
     */
    public String insertSupplier(Suppliers supplier) throws SQLException {
        String sql = "INSERT INTO Suppliers (supplier_name, contact_name, phone, address, email) VALUES (?, ?, ?, ?, ?)";
        String generatedKey = null;

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, supplier.getSupplier_name());
            pstmt.setString(2, supplier.getContact_name());
            pstmt.setString(3, supplier.getPhone());
            pstmt.setString(4, supplier.getAddress());
            pstmt.setString(5, supplier.getEmail());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedKey = String.valueOf(rs.getInt(1));
                    }
                }
            }
        }
        return generatedKey;
    }

    /**
     * Actualiza un proveedor existente.
     * @param supplier El objeto Suppliers con los datos actualizados y el ID.
     * @return true si se actualizó al menos una fila, false en caso contrario.
     * @throws SQLException Si ocurre un error.
     */
    public boolean updateSupplier(Suppliers supplier) throws SQLException {
        String sql = "UPDATE Suppliers SET supplier_name = ?, contact_name = ?, phone = ?, address = ?, email = ? WHERE supplier_id = ?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplier.getSupplier_name());
            pstmt.setString(2, supplier.getContact_name());
            pstmt.setString(3, supplier.getPhone());
            pstmt.setString(4, supplier.getAddress());
            pstmt.setString(5, supplier.getEmail());

            // CORRECCIÓN: Convertir el ID de String a INT para la base de datos
            try {
                int idInt = Integer.parseInt(supplier.getSupplier_id());
                pstmt.setInt(6, idInt);
            } catch (NumberFormatException e) {
                throw new SQLException("Error de formato: El ID del proveedor no es un número válido: " + supplier.getSupplier_id(), e);
            }

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un proveedor por su ID.
     * @param supplierId El ID del proveedor a eliminar (en String).
     * @return true si se eliminó al menos una fila, false en caso contrario.
     * @throws SQLException Si ocurre un error.
     */
    public boolean deleteSupplier(String supplierId) throws SQLException {
        String sql = "DELETE FROM Suppliers WHERE supplier_id = ?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // CORRECCIÓN: Convertir el ID de String a INT para la base de datos
            try {
                int idInt = Integer.parseInt(supplierId);
                pstmt.setInt(1, idInt); // Usar setInt para la columna INTEGER
            } catch (NumberFormatException e) {
                throw new SQLException("Error de formato: El ID del proveedor a eliminar no es un número válido: " + supplierId, e);
            }

            return pstmt.executeUpdate() > 0;
        }
    }
}