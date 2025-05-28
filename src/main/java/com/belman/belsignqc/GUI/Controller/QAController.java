package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BLL.Util.UserSession;
import com.belman.belsignqc.BLL.showAlert;
import com.belman.belsignqc.DAL.DAO.OrderDAO;
import com.belman.belsignqc.DAL.DBConnector;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QAController extends BaseController{

    @FXML private TableView<OrderModel> qaOrderTable;
    @FXML private TableColumn<OrderModel, String> qaOrderColumn;
    @FXML private TextField qaSearch;

    private ObservableList<OrderModel> allOrders;
    private OrderDAO orderDAO;
    private DBConnector dbConnector;

    @FXML
    private void initialize() {
        try {
                // Initialize DBConnector
                dbConnector = new DBConnector();

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

    private int fetchUserIDByOrderNumber(String orderNumber) throws SQLException {
        String sql = "SELECT userID FROM OrderNumbers WHERE OrderNumber = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("userID");
            }
        }
        throw new SQLException("User ID not found for order number: " + orderNumber);
    }

    private String fetchFilePathByOrderNumber(String orderNumber) throws SQLException {
        String sql = "SELECT filepath FROM Photos WHERE orderNumberID = (SELECT orderNumberID FROM OrderNumbers WHERE OrderNumber = ?)";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("filepath");
            }
        }
        throw new SQLException("File path not found for order number: " + orderNumber);
    }

    @FXML
    private void handleGeneratePDF() {
        try {
            // Get the selected order from the table
            OrderModel selectedOrder = qaOrderTable.getSelectionModel().getSelectedItem();
            if (selectedOrder == null) {
                showAlert.display("Error", "No order selected!");
                return;
            }

            // Fetch order details (orderNumber, userID, filePath)
            String orderNumber = selectedOrder.getOrderNumber();
            int userID = fetchUserIDByOrderNumber(orderNumber); // Implement this method
            String filePath = fetchFilePathByOrderNumber(orderNumber); // Implement this method

            // Generate the PDF
            String pdfPath = "OrderReport_" + orderNumber + ".pdf";
            createPDFReport(pdfPath, orderNumber, userID, filePath);

            // Notify the user
            showAlert.display("Success", "PDF report generated: " + pdfPath);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert.display("Error", "Failed to generate PDF: " + e.getMessage());
        }
    }

    private void createPDFReport(String pdfPath, String orderNumber, int userID, String filePath) throws Exception {
        // Initialize PDF writer
        com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(pdfPath);
        com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);

        // Add content to the PDF
        document.add(new com.itextpdf.layout.element.Paragraph("Order Report"));
        document.add(new com.itextpdf.layout.element.Paragraph("Order Number: " + orderNumber));
        document.add(new com.itextpdf.layout.element.Paragraph("User ID: " + userID));
        document.add(new com.itextpdf.layout.element.Paragraph("File Path: " + filePath));

        // Close the document
        document.close();
    }


}
