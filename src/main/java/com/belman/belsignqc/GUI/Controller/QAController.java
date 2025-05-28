package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BLL.Util.UserSession;
import com.belman.belsignqc.BLL.showAlert;
import com.belman.belsignqc.DAL.DAO.OrderDAO;
import com.belman.belsignqc.GUI.Model.OrderModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.sql.SQLException;

public class QAController extends BaseController{

    @FXML private TableView<OrderModel> qaOrderTable;
    @FXML private TableColumn<OrderModel, String> qaOrderColumn;
    @FXML private TextField qaSearch;

    private ObservableList<OrderModel> allOrders;
    private OrderDAO orderDAO;

    @FXML
    private void initialize() {
        try {
            // Initialize OrderDAO
            orderDAO = new OrderDAO();

            // Configure the order table column
            qaOrderColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

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
            ObservableList<String> orderNumbers = orderDAO.getAllOrderNumbers();

            // Convert to OrderModel objects
            allOrders = FXCollections.observableArrayList();
            for (String orderNumber : orderNumbers) {
                allOrders.add(new OrderModel(orderNumber));
            }

            // Update the search functionality with the new data
            setupOrderSearch();
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message to user
            showAlert.display("Database Error", "Failed to load orders: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        //Clear UserSession of the logged-in user
        UserSession.getInstance().clearSession();

        // Navigate to the login screen
        screenManager.setScreen("login");
    }

    private void setupOrderSearch() {
        // Create a filtered list wrapping the observable list
        FilteredList<OrderModel> filteredOrders = new FilteredList<>(allOrders, p -> true);

        // Add listener to the search field
        qaSearch.textProperty().addListener((observable, oldValue, newValue) -> {
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
        sortedOrders.comparatorProperty().bind(qaOrderTable.comparatorProperty());

        // Add sorted (and filtered) data to the table
        qaOrderTable.setItems(sortedOrders);
    }


}
