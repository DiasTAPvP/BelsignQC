package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BE.OrderNumbers;
import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.BLL.showAlert;
import com.belman.belsignqc.DAL.DAO.OrderDAO;
import com.belman.belsignqc.DAL.DAO.UserDAO;
import com.belman.belsignqc.GUI.Model.OrderModel;
import com.belman.belsignqc.GUI.Model.UserModel;
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
import java.util.List;

public class AdminController extends BaseController {

   @FXML private Button createUserButton;
   @FXML private Button adminLogoutButton;
   @FXML private TableView<OrderModel> adminOrderTable;
   @FXML private TableColumn<OrderModel, String> adminOrderColumn;
   @FXML private TableView<UserModel> adminUserTable;
   @FXML private TableColumn<UserModel, String> adminUserColumn;
   @FXML private TextField adminOrderSearch;
   @FXML private TextField adminUserSearch;

   private ObservableList<OrderModel> allOrders;
   private ObservableList<UserModel> allUsers;
   private OrderDAO orderDAO;
   private UserDAO userDAO;
//
    @FXML
    private void initialize() {
        try {
            // Initialize DAOs
            orderDAO = new OrderDAO();
            userDAO = new UserDAO();

            // Configure the order table column
            adminOrderColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

            // Configure the user table column
            adminUserColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

            // Load real data to tables
            loadOrders();
            loadUsers();

            // Setup search functionality
            setupOrderSearch();
            setupUserSearch();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error message to user
            showAlert.display("Error", "Failed to initialize data: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert.display("Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        screenManager.setScreen("login");
    }

    @FXML
    private void createButtonPressed() {
        //Implement a way to wipe the fields when the button is pressed
        screenManager.setScreen("createuser");
    }

    private void loadUsers() throws Exception {
        // Get all users from the database
        List<Users> users = userDAO.getAllUsers();

        // Convert Users to UserModel objects
        allUsers = FXCollections.observableArrayList();
        for (Users user : users) {
            allUsers.add(new UserModel(user.getUsername()));
        }

        // Set the items in the table
        adminUserTable.setItems(allUsers);
    }

    private void loadOrders() throws SQLException {
        // Get all order numbers from the database
        ObservableList<String> orderNumbers = orderDAO.getAllOrderNumbers();

        // Convert order numbers to OrderModel objects
        allOrders = FXCollections.observableArrayList();
        for (String orderNumber : orderNumbers) {
            allOrders.add(new OrderModel(orderNumber));
        }

        // Set the items in the table
        adminOrderTable.setItems(allOrders);
    }

    private void setupOrderSearch() {
        // Create a filtered list wrapping the observable list
        FilteredList<OrderModel> filteredOrders = new FilteredList<>(allOrders, p -> true);

        // Add listener to the search field
        adminOrderSearch.textProperty().addListener((observable, oldValue, newValue) -> {
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
        sortedOrders.comparatorProperty().bind(adminOrderTable.comparatorProperty());

        // Add sorted (and filtered) data to the table
        adminOrderTable.setItems(sortedOrders);
    }

    private void setupUserSearch() {
        // Create a filtered list wrapping the observable list
        FilteredList<UserModel> filteredUsers = new FilteredList<>(allUsers, p -> true);

        // Add listener to the search field
        adminUserSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                // If search field is empty, display all users
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare username with filter text
                String lowerCaseFilter = newValue.toLowerCase();

                if (user.getUsername().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches username
                }
                return false; // Does not match
            });
        });

        // Wrap the filtered list in a sorted list
        SortedList<UserModel> sortedUsers = new SortedList<>(filteredUsers);

        // Bind the sorted list comparator to the TableView comparator
        sortedUsers.comparatorProperty().bind(adminUserTable.comparatorProperty());

        // Add sorted (and filtered) data to the table
        adminUserTable.setItems(sortedUsers);
    }

}
