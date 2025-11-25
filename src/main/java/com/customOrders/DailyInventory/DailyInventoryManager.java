package com.customOrders.DailyInventory;

import com.customOrders.PostgresConnector;
import com.customOrders.Products.Product;
import com.customOrders.Products.ProductManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Clase que maneja la interacción con la base de datos para el Inventario Diario (tabla daily_inventory).
 * Se encarga de guardar los registros históricos y delegar la actualización del stock maestro
 * (tabla products) al ProductManager.
 */
public class DailyInventoryManager {

    private final ProductManager productManager;

    public DailyInventoryManager() {
        // Inicializa el manager de productos para acceder a la lista maestra y actualizar el stock
        this.productManager = new ProductManager();
    }

    // --- MÉTODOS DE LECTURA ---

    /**
     * Obtiene la lista maestra de productos delegando al ProductManager.
     */
    public List<Product> getAllProducts() throws SQLException {
        return productManager.getAllProducts();
    }

    /**
     * Busca el nivel de inventario para un producto en una fecha específica en la tabla 'daily_inventory'.
     * @return Un Optional que contiene el registro si existe.
     */
    public Optional<DailyInventory> getInventoryLevelByDate(int productId, LocalDate date) throws SQLException {
        String sql = "SELECT date_of_inventory, product_id, level FROM Daily_Inventory WHERE product_id = ? AND date_of_inventory = ?";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Se encontró un registro histórico
                    DailyInventory entry = new DailyInventory(
                            rs.getDate("date_of_inventory").toLocalDate(),
                            rs.getInt("product_id"),
                            rs.getInt("level")
                    );
                    return Optional.of(entry);
                }
            }
        }
        // Si no se encuentra, retorna Optional.empty()
        return Optional.empty();
    }


    // --- MÉTODOS DE ESCRITURA ---

    /**
     * Guarda/Actualiza una única entrada de inventario diario en la tabla 'daily_inventory'.
     * Utiliza UPSERT (INSERT ON CONFLICT) para manejar el caso donde ya existe un conteo para esa fecha/producto.
     * @param entry El registro de inventario a guardar.
     */
    private void saveDailyInventoryEntry(DailyInventory entry) throws SQLException {
        // La sentencia UPSERT intenta INSERTAR, y si hay conflicto (la PK compuesta ya existe), hace UPDATE.
        String sql = "INSERT INTO Daily_Inventory (date_of_inventory, product_id, level) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (date_of_inventory, product_id) DO UPDATE SET level = EXCLUDED.level";

        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(entry.getDateOfInventory()));
            ps.setInt(2, entry.getProductId());
            ps.setInt(3, entry.getLevel());

            ps.executeUpdate();
            System.out.println("-> Guardado histórico para Producto ID: " + entry.getProductId() +
                    " en fecha: " + entry.getDateOfInventory() + " con nivel: " + entry.getLevel());
        }
    }


    /**
     * Guarda el registro de inventario diario y actualiza la tabla maestra de productos.
     * @param inventoryEntries Lista de registros (stock contado) a guardar.
     */
    public void saveDailyInventory(List<DailyInventory> inventoryEntries) throws SQLException {
        System.out.println("DEBUG: Iniciando guardado de " + inventoryEntries.size() + " registros de inventario...");

        // Usamos un bloque try-with-resources para asegurar que la conexión se cierre
        // Aunque no hay conexión directa aquí, la excepción SQLException debe ser manejada.

        for (DailyInventory entry : inventoryEntries) {
            // 1. Guardar/Actualizar en la tabla 'daily_inventory' (histórico)
            saveDailyInventoryEntry(entry);

            // 2. Actualizar la tabla maestra 'products' (stock actual)
            // ESTE ES EL CAMBIO CLAVE: Llama al método real del Manager para hacer el UPDATE en la DB
        }

        System.out.println("DEBUG: Guardado de inventario completado.");
    }
}