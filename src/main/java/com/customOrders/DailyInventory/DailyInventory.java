package com.customOrders.DailyInventory;

import java.time.LocalDate;

/**
 * Representa un registro histórico de la tabla 'daily_inventory'.
 */
public class DailyInventory {
    private final LocalDate dateOfInventory;
    private final int productId;
    private int level; // Cantidad de stock contada en esa fecha

    public DailyInventory(LocalDate dateOfInventory, int productId, int level) {
        this.dateOfInventory = dateOfInventory;
        this.productId = productId;
        this.level = level;
    }

    // Getters y Setters
    public LocalDate getDateOfInventory() { return dateOfInventory; }
    public int getProductId() { return productId; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}