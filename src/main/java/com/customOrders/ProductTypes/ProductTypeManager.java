package com.customOrders.ProductTypes;
import com.customOrders.PostgresConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductTypeManager {

    /**
     * Obtiene todos los tipos de producto de la base de datos.
     */
    public List<ProductType> getAllProductTypes() throws SQLException {
        List<ProductType> productTypeList = new ArrayList<>();
        String sql = "SELECT Product_Type_Code, Product_Type_Name, Parent_Product_Type_Code FROM Product_Types ORDER BY Product_Type_Code";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Parent_Product_Type_Code puede ser nulo, getString() maneja esto
                String parentCode = rs.getString("Parent_Product_Type_Code");

                ProductType type = new ProductType(
                        rs.getString("Product_Type_Code"),
                        rs.getString("Product_Type_Name"),
                        parentCode
                );
                productTypeList.add(type);
            }
        }
        return productTypeList;
    }

    /**
     * Inserta un nuevo tipo de producto.
     * @param type El objeto ProductType a insertar.
     * @return true si la inserción fue exitosa.
     */
    public boolean insertProductType(ProductType type) throws SQLException {
        // La clave primaria Product_Type_Code debe ser proporcionada por la aplicación o generada por la DB.
        // Asumo que se proporciona o se autogenera mediante una secuencia, pero aquí usamos el valor proporcionado.
        String sql = "INSERT INTO Product_Types (Product_Type_Code, Product_Type_Name, Parent_Product_Type_Code) VALUES (?, ?, ?)";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String parentCode = type.getParentProductTypeCode();

            ps.setString(1, type.getProductTypeCode());
            ps.setString(2, type.getProductTypeName());
            // Si el código padre está vacío, se inserta NULL
            ps.setObject(3, parentCode.isEmpty() ? null : parentCode, Types.VARCHAR);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza un tipo de producto existente.
     * @param type El objeto ProductType con los datos actualizados.
     * @return true si la actualización fue exitosa.
     */
    public boolean updateProductType(ProductType type) throws SQLException {
        String sql = "UPDATE Product_Types SET Product_Type_Name = ?, Parent_Product_Type_Code = ? WHERE Product_Type_Code = ?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String parentCode = type.getParentProductTypeCode();

            ps.setString(1, type.getProductTypeName());
            // Si el código padre está vacío, se inserta NULL
            ps.setObject(2, parentCode.isEmpty() ? null : parentCode, Types.VARCHAR);
            ps.setString(3, type.getProductTypeCode());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un tipo de producto por su código.
     * @param productTypeCode El código del tipo de producto a eliminar.
     * @return true si la eliminación fue exitosa.
     */
    public boolean deleteProductType(String productTypeCode) throws SQLException {
        String sql = "DELETE FROM Product_Types WHERE Product_Type_Code = ?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, productTypeCode);
            return ps.executeUpdate() > 0;
        }
    }
}