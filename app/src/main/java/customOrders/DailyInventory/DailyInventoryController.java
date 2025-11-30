package customOrders.DailyInventory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.util.converter.IntegerStringConverter;
import customOrders.DailyInventory.DailyInventory;
import customOrders.DailyInventory.DailyInventoryManager;
import customOrders.DailyInventory.ProductInventoryView;
import customOrders.Products.Product;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Vista/Controlador de JavaFX para la gestión del Inventario Diario.
 */
public class DailyInventoryController implements Initializable {

    private final DailyInventoryManager manager = new DailyInventoryManager();
    private final ObservableList<ProductInventoryView> inventoryList = FXCollections.observableArrayList();
    private LocalDate selectedDate = LocalDate.now();

    // --- Componentes FXML ---
    @FXML private TableView<ProductInventoryView> inventoryTable;
    @FXML private TableColumn<ProductInventoryView, Integer> idColumn;
    @FXML private TableColumn<ProductInventoryView, String> nameColumn;
    @FXML private TableColumn<ProductInventoryView, Integer> masterStockColumn;
    @FXML private TableColumn<ProductInventoryView, Integer> newStockColumn; // Editable
    @FXML private TableColumn<ProductInventoryView, String> statusColumn; // (Si decides mantener el Status/Diferencia)

    @FXML private DatePicker datePicker;
    @FXML private Label messageLabel;

    @FXML private Button saveButton;
    @FXML private Button downloadCsvButton;
    @FXML private Button countZeroButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Configurar columnas de solo lectura
        idColumn.setCellValueFactory(new PropertyValueFactory<>("product_id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        masterStockColumn.setCellValueFactory(new PropertyValueFactory<>("masterQuantity"));

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Configurar la columna de "New Stock" para que sea editable
        newStockColumn.setCellValueFactory(new PropertyValueFactory<>("newLevelDisplay"));

        // CORRECCIÓN DEL CONVERSOR: Usamos IntegerStringConverter para tipado correcto.
        newStockColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        // Manejar el evento de edición de celda
        inventoryTable.setEditable(true);
        newStockColumn.setOnEditCommit(event -> {
            ProductInventoryView item = event.getRowValue();

            Integer newValue = event.getNewValue();

            if (newValue == null) {
                messageLabel.setText("ERROR: Entrada no válida. Ingrese solo números enteros.");
                inventoryTable.refresh();
                return;
            }

            int stockValue = newValue.intValue();

            if (stockValue < 0) {
                messageLabel.setText("ERROR: El stock contado no puede ser negativo.");
                inventoryTable.refresh();
                return;
            }

            item.setNewLevel(stockValue);
            messageLabel.setText("Stock del producto ID " + item.getProduct_id() + " actualizado a: " + stockValue);
            inventoryTable.refresh();
        });


        inventoryTable.setItems(inventoryList);

        // 3. Configurar DatePicker
        datePicker.setValue(selectedDate);
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                selectedDate = newDate;
                loadInventoryForSelectedDate();
            }
        });

        // 4. Carga inicial
        loadInventoryForSelectedDate();
        messageLabel.setText("Módulo de Inventario Diario cargado. Listo para conteo.");
    }

    /**
     * Método auxiliar para obtener el historial, declarando que lanza SQLException.
     * Esta es la forma más limpia de manejar la excepción fuera del stream.
     * @param productId ID del producto
     * @param date Fecha del inventario
     * @return Optional<DailyInventory>
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private Optional<DailyInventory> getHistoricEntry(int productId, LocalDate date) throws SQLException {
        // Eliminamos el try-catch aquí. La SQLException se lanza y el método principal la captura.
        return manager.getInventoryLevelByDate(productId, date);
    }


    /**
     * Carga todos los productos y busca el inventario histórico para la fecha seleccionada.
     */
    @FXML
    private void loadInventoryForSelectedDate() {
        try {
            List<Product> products = manager.getAllProducts();

            // Usamos un mapeo que lanza la excepción chequeada.
            // Esto es necesario para que el stream sepa que hay una checked exception.
            List<ProductInventoryView> newViewList = products.stream()
                    .map(p -> {
                        try {
                            // Llama al método auxiliar que ahora lanza SQLException
                            Optional<DailyInventory> historicEntry = getHistoricEntry(p.getProduct_id(), selectedDate);

                            Integer historicLevel = historicEntry.map(DailyInventory::getLevel).orElse(null);
                            return new ProductInventoryView(p, historicLevel);
                        } catch (SQLException e) {
                            // Envolvemos la checked exception en una unchecked temporalmente
                            throw new RuntimeException("DB Error loading historic entry for product " + p.getProduct_id() + ": " + e.getMessage(), e);
                        }
                    })
                    .collect(Collectors.toList());

            inventoryList.setAll(newViewList);
            messageLabel.setText("Inventario cargado para la fecha: " + selectedDate + ". Registros encontrados: " + inventoryList.size());

        } catch (SQLException e) {
            // Captura de SQLException: Para manager.getAllProducts()
            messageLabel.setText("ERROR de DB al cargar productos: " + e.getMessage());
            System.err.println("SQL Error: " + e.getMessage());
        } catch (RuntimeException re) {
            // Captura de RuntimeException: Si vino del stream (cuando getHistoricEntry falla)
            if (re.getCause() instanceof SQLException) {
                SQLException sqlE = (SQLException) re.getCause();
                messageLabel.setText("ERROR de DB al buscar histórico: " + sqlE.getMessage());
                System.err.println("SQL Error (Historic): " + sqlE.getMessage());
            } else {
                messageLabel.setText("ERROR inesperado al cargar inventario: " + re.getMessage());
                System.err.println("Runtime Error: " + re.getMessage());
            }
        }
    }

    /**
     * Maneja la acción de guardar el inventario contado por el usuario.
     */
    @FXML
    private void handleReloadInventory() {
        loadInventoryForSelectedDate();
        messageLabel.setText("Inventario recargado. Revisando la tabla...");
    }
    @FXML
    private void handleSaveInventory() {
        List<DailyInventory> entriesToSave = inventoryList.stream()
                .filter(item -> item.getNewLevel() >= 0)
                .map(item -> new DailyInventory(
                        selectedDate,
                        item.getProduct().getProduct_id(),
                        item.getNewLevel()
                ))
                .collect(Collectors.toList());

        if (entriesToSave.isEmpty()) {
            messageLabel.setText("ADVERTENCIA: No hay datos válidos para guardar.");
            return;
        }

        try {
            manager.saveDailyInventory(entriesToSave);

            // CORRECCIÓN CLAVE: El mensaje ahora indica SOLO el guardado histórico
            messageLabel.setText("ÉXITO: Inventario de " + entriesToSave.size() + " productos guardado en el historial.");

            loadInventoryForSelectedDate();
        } catch (SQLException e) {
            messageLabel.setText("ERROR de DB al guardar el inventario: " + e.getMessage());
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    /**
     * Fija a cero el stock del producto seleccionado.
     */
    @FXML
    private void handleCountZero() {
        ProductInventoryView selectedItem = inventoryTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            selectedItem.setNewLevel(0);
            inventoryTable.refresh();
            messageLabel.setText("Stock de '" + selectedItem.getProduct_name() + "' fijado a 0. ¡No olvide guardar!");

        } else {
            messageLabel.setText("Seleccione un producto para fijar su stock a cero.");
        }
    }

    /**
     * Maneja la descarga del archivo CSV.
     */
    @FXML
    private void handleDownloadCsv() {
        if (inventoryList.isEmpty()) {
            messageLabel.setText("No hay datos para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Inventario Diario CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("inventario_diario_" + selectedDate.toString() + ".csv");

        File file = fileChooser.showSaveDialog(inventoryTable.getScene().getWindow());

        if (file != null) {
            // Solo se captura IOException.
            try (FileWriter fileWriter = new FileWriter(file)) {

                // Encabezados
                fileWriter.append("ID,Nombre,Stock Maestro,Stock Histórico,Stock Contado,Estado\n");

                // Contenido
                for (ProductInventoryView item : inventoryList) {
                    fileWriter.append(String.format("%d,\"%s\",%d,%s,%d,\"%s\"\n",
                            item.getProduct_id(),
                            item.getProduct_name(),
                            item.getMasterQuantity(),
                            item.getHistoricLevelDisplay() != null ? String.valueOf(item.getHistoricLevelDisplay()) : "N/A",
                            item.getNewLevelDisplay(),
                            item.getStatus()
                    ));
                }

                fileWriter.flush();
                messageLabel.setText("ÉXITO: Inventario exportado a " + file.getAbsolutePath());

            } catch (IOException e) {
                messageLabel.setText("ERROR de I/O al exportar CSV: " + e.getMessage());
                System.err.println("I/O Error: " + e.getMessage());
            }
        } else {
            messageLabel.setText("Exportación CSV cancelada.");
        }
    }
}