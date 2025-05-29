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
import javafx.scene.control.*;
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
    private OrderModel selectedOrder;


    /**
     * Handles the action when the logout button is clicked.
     * Clears the user session and navigates to the login screen.
     */
    @FXML
    private void handleLogout() {
        //Clear UserSession of the logged-in user
        UserSession.getInstance().clearSession();

        // Navigate to the login screen
        screenManager.setScreen("login");
    }

    /**
     * Handles the action when the camera button is clicked.
     * If an order is selected, it navigates to the camera screen with the order number.
     * If no order is selected, it shows an alert to the user.
     */
    @FXML
    private void handleOpenCamera() {
        if (selectedOrder != null) {
            // Pass the selected order number to the camera screen
            screenManager.setScreenWithData("camera", selectedOrder.getOrderNumber());
        } else {
            showAlert.display("Selection Required", "Please select an order first.");
        }
    }

    /**
     * Initializes the OperatorController.
     * Sets up the order table, search functionality, and selection listener.
     */
    @FXML
    private void initialize() {
        try {
            // Initialize OrderDAO
            orderDAO = new OrderDAO();

            // Configure the order table column
            operatorOrderColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
            operatorOrderColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item);
                }
            });

            operatorOrderTable.setVisible(true);
            operatorOrderColumn.setPrefWidth(200);

            // Initialize allOrders first to avoid NullPointerException
            allOrders = FXCollections.observableArrayList();

            // Load orders from database (this will populate allOrders)
            loadOrdersFromDatabase();

            // Add selection listener to the table
            operatorOrderTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        selectedOrder = newSelection;
                        // Enable or disable camera button based on selection
                        openCamera.setDisable(selectedOrder == null);
                    });

            // Initially disable the camera button until an order is selected
            openCamera.setDisable(true);

        } catch (IOException e) {
            e.printStackTrace();
            // Show error message to user
            showAlert.display("Error", "Failed to initialize: " + e.getMessage());
        }
    }


    /**
     * Loads all orders from the database and sets up the search functionality.
     * This method is called during initialization to populate the order table.
     */
    private void loadOrdersFromDatabase() {
        try {
            // Get order numbers from database
            orderDAO = new OrderDAO();
            ObservableList<String> orderNumbers = orderDAO.getAllOrderNumbers();

            // Debug output
            System.out.println("Order numbers retrieved: " + orderNumbers.size());
            if (orderNumbers.isEmpty()) {
                System.out.println("No order numbers returned from database");
            } else {
                System.out.println("First order number: " + orderNumbers.get(0));
            }

            // Convert to OrderModel objects
            allOrders = FXCollections.observableArrayList();
            for (String orderNumber : orderNumbers) {
                allOrders.add(new OrderModel(orderNumber));
            }

            // Debug output
            System.out.println("OrderModels created: " + allOrders.size());

            // Set up the search functionality
            setupOrderSearch();

            // Verify the table has items
            System.out.println("Table items after setup: " +
                    (operatorOrderTable.getItems() != null ?
                            operatorOrderTable.getItems().size() : "null"));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert.display("Database Error", "Failed to load orders: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace(); // Add stack trace output
            showAlert.display("I/O Error", "Failed to load orders: " + e.getMessage());
        }
    }


    /**
     * Sets up the search functionality for the order table.
     * Filters orders based on the text entered in the search field.
     */
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
        operatorOrderTable.refresh();
    }

}