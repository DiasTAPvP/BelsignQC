package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BLL.showAlert;
import com.belman.belsignqc.DAL.DAO.OrderDAO;
import com.belman.belsignqc.GUI.Model.OrderModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.sql.SQLException;

public class OperatorController extends BaseController {

    @FXML private ImageView operatorLogoutButton;
    @FXML private Button openCamera;
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

            // Update the search functionality with the new data
            //setupOrderSearch();
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message to user
            showAlert.display("Database Error", "Failed to load orders: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
