package com.customOrders;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class CustomerAdminController implements Initializable {

    // Instancia de tu Manager (CRUD) para acceder a los datos
    private CustomerManager customerManager = new CustomerManager();

    // FXML Fields que deben coincidir con los fx:id en tu archivo FXML
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> idColumn;
    @FXML private TableColumn<Customer, String> firstNameColumn;
    @FXML private TableColumn<Customer, String> lastNameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> addressColumn;

    // Lista de datos que se vinculará a la tabla
    private ObservableList<Customer> customerData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // 1. Configurar las columnas para que mapeen a los Getters del Modelo Customer.java
        //    El String debe coincidir con el nombre de la propiedad (e.g., "customerID" llama a getCustomerID())
        idColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        // 2. Inicializar la lista observable y vincularla a la tabla
        customerData = FXCollections.observableArrayList();
        customerTable.setItems(customerData);

        // 3. Cargar datos al iniciar la vista
        loadCustomerData();
    }

    /**
     * Carga todos los clientes de la base de datos y actualiza la tabla.
     */
    public void loadCustomerData() {
        try {
            // Llama al método READ del Manager
            List<Customer> customers = customerManager.getAllCustomers();

            customerData.clear();
            customerData.addAll(customers);

            System.out.println("Clientes cargados: " + customers.size());

        } catch (SQLException e) {
            System.err.println("Error al cargar los datos de clientes: " + e.getMessage());
            // Se debe notificar visualmente al usuario si hay un fallo de conexión
        }
    }

    // 💡 Aquí se añadirán los métodos para los botones de la interfaz
    // @FXML public void handleNewCustomer() { ... }
    // @FXML public void handleDeleteCustomer() { ... }
}