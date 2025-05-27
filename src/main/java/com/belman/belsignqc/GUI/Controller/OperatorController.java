package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BLL.showAlert;
import com.belman.belsignqc.DAL.DAO.OrderDAO;
import com.belman.belsignqc.GUI.Model.OrderModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.sql.SQLException;

public class OperatorController extends BaseController {

    @FXML private ImageView operatorLogoutButton;
    @FXML private Button openCamera;
    @FXML private TextField operatorSearchField;
    @FXML private TableView<OrderModel> operatorOrderTable;
    @FXML private TableColumn<OrderModel, String> operatorOrderColumn;

    private ObservableList<OrderModel> allOrders;
    private OrderDAO orderDAO;

    @FXML
    private void handleLogout() {
        screenManager.setScreen("login");

        //Make it forget who is logged in later too (LoginModel)
    }

    @FXML
    private void handleOpenCamera() {
        screenManager.setScreen("camera");
    }

    @FXML
    private void initialize() {
        try {
            // Initialize OrderDAO
            orderDAO = new OrderDAO();

            // Configure the order table column
            operatorOrderColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

            // Load orders from database (this will also set up the search functionality)
            loadOrdersFromDatabase();

        } catch (IOException e) {
            e.printStackTrace();
            // Show error message to user
            showAlert.display("Error", "Failed to initialize: " + e.getMessage());
        }
    }

    private void loadOrdersFromDatabase() {
        try {
            // Get order numbers from database
            orderDAO = new OrderDAO();
            ObservableList<String> orderNumbers = orderDAO.getAllOrderNumbers();

            // Convert to OrderModel objects
            allOrders = FXCollections.observableArrayList();
            for (String orderNumber : orderNumbers) {
                allOrders.add(new OrderModel(orderNumber));
            }

            // Set up the search functionality
            setupOrderSearch();
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message to user
            showAlert.display("Database Error", "Failed to load orders: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void setupOrderSearch() {
        // Create a filtered list wrapping the observable list
        FilteredList<OrderModel> filteredOrders = new FilteredList<>(allOrders, p -> true);

        // Add listener to the search field
        operatorSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredOrders.setPredicate(order -> {
                // If search field is empty, display all orders
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare order number with filter text
                String lowerCaseFilter = newValue.toLowerCase();

                if (order.getOrderNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches order number
                }
                return false; // Does not match
            });
        });

        // Wrap the filtered list in a sorted list
        SortedList<OrderModel> sortedOrders = new SortedList<>(filteredOrders);

        // Bind the sorted list comparator to the TableView comparator
        sortedOrders.comparatorProperty().bind(operatorOrderTable.comparatorProperty());

        // Add sorted (and filtered) data to the table
        operatorOrderTable.setItems(sortedOrders);
    }

}
