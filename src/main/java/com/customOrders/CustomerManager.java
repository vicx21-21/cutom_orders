package com.customOrders;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerManager { // Renombrado a CustomerDAO, usando PostgreConnection

    // ------------------------------------
    // CREATE
    // ------------------------------------
    public int insertCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO Customers (First_Name, Last_Name, Email, Phone_Number, Date_Joined, Address) VALUES (?, ?, ?, ?, CURRENT_DATE, ?)";
        int generatedId = -1;

        // 🟢 CORRECCIÓN: Usamos el nombre de clase simple. Si el archivo es PostgreConnection.java, úsalo.
        // Si el archivo es PostgresConnector.java, úsalo. Asumo que el nombre es POSTGRESCANNON
        // Opción 1: Usar solo el nombre de la clase (si se agrega un import o están en el mismo paquete)
        try (Connection conn = PostgresConnector.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhoneNumber());
            ps.setString(5, customer.getAddress());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) generatedId = rs.getInt(1);
            }
        }
        return generatedId;
    }

    // ------------------------------------
    // READ
    // ------------------------------------
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customerList = new ArrayList<>();
        String sql = "SELECT Customer_ID, First_Name, Last_Name, Email, Phone_Number, Address FROM Customers ORDER BY Customer_ID";

        // Usamos el nombre de clase simple
// Opción 1: Usar solo el nombre de la clase (si se agrega un import o están en el mismo paquete)
            try (Connection conn = PostgresConnector.getConnection();             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer customer = new Customer( // Customer no necesita import
                        rs.getInt("Customer_ID"),
                        rs.getString("First_Name"),
                        rs.getString("Last_Name"),
                        rs.getString("Email"),
                        rs.getString("Phone_Number"),
                        rs.getString("Address")
                );
                customerList.add(customer);
            }
        }
        return customerList;
    }

    // ------------------------------------
    // UPDATE
    // ------------------------------------
    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE Customers SET First_Name = ?, Last_Name = ?, Email = ?, Phone_Number = ?, Address = ? WHERE Customer_ID = ?";

        // Usamos el nombre de clase simple
            // Opción 1: Usar solo el nombre de la clase (si se agrega un import o están en el mismo paquete)
            try (Connection conn = PostgresConnector.getConnection(); // ⬅️ Nombre simple
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhoneNumber());
            ps.setString(5, customer.getAddress());
            ps.setInt(6, customer.getCustomerID());

            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------
    // DELETE
    // ------------------------------------
    public boolean deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM Customers WHERE Customer_ID = ?";

        // Usamos el nombre de clase simple
            // Opción 1: Usar solo el nombre de la clase (si se agrega un import o están en el mismo paquete)
            try (Connection conn = PostgresConnector.getConnection(); // ⬅️ Nombre simple
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        }
    }
}
