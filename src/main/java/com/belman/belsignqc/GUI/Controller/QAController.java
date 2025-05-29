package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BLL.Util.UserSession;
import com.belman.belsignqc.BLL.showAlert;
import com.belman.belsignqc.DAL.DAO.OrderDAO;
import com.belman.belsignqc.DAL.DBConnector;
import com.belman.belsignqc.GUI.Model.OrderModel;
import com.itextpdf.io.image.ImageDataFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.colors.ColorConstants;



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
    
    /**
     * Initializes the QAController by setting up the order table,
     * loading orders from the database, and configuring search functionality.
     */
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

    /**
     * Loads all orders from the database and sets up the search functionality.
     * This method retrieves order numbers, converts them to OrderModel objects,
     * and populates the TableView with the data.
     */
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

    /**
     * Handles the logout action for the QA user.
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
     * Sets up the search functionality for the order table.
     * Filters orders based on the text entered in the search field.
     */
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

    /**
     * Fetches the user ID associated with a given order number.
     *
     * @param orderNumber The order number to search for.
     * @return The user ID associated with the order number.
     * @throws SQLException If there is an error accessing the database.
     */
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

    /**
     * Fetches the file path associated with a given order number.
     *
     * @param orderNumber The order number to search for.
     * @return The file path associated with the order number.
     * @throws SQLException If there is an error accessing the database.
     */
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

    /**
     * Handles the PDF generation for the selected order.
     * Fetches order details and generates a PDF report.
     */
    @FXML
    private void handleGeneratePDF() {
        try {
            // Get the selected order from the table
            OrderModel selectedOrder = qaOrderTable.getSelectionModel().getSelectedItem();
            if (selectedOrder == null) {
                showAlert.display("Error", "No order selected!");
                return;
            }

            // Get order number
            String orderNumber = selectedOrder.getOrderNumber();

            // Prompt for comments
            String comments = promptForComments();

            // Generate the PDF with comments
            String pdfFilename = "OrderReport_" + orderNumber + ".pdf";
            createPDFReport(pdfFilename, orderNumber, comments);

            // Show full path in notification
            java.nio.file.Path pdfPath = java.nio.file.Paths.get("QC_Reports", orderNumber + "_Reports", pdfFilename);
            showAlert.display("Success", "PDF report generated: " + pdfPath.toString());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert.display("Error", "Failed to generate PDF: " + e.getMessage());
        }
    }

    /**
     * Shows a dialog to prompt for optional comments.
     *
     * @return The comments entered by the user, or "No Comment" if left blank.
     */
    private String promptForComments() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("QC Report Comments");
        dialog.setHeaderText("Enter optional comments for this QC report:");
        dialog.setContentText("Comments:");

        // Set the dialog window to be centered on the screen
        dialog.initOwner(qaOrderTable.getScene().getWindow());

        // Show the dialog and get the result
        java.util.Optional<String> result = dialog.showAndWait();

        // Return the entered text, or "No Comment" if canceled or left blank
        return result.map(comment -> comment.trim().isEmpty() ? "No Comment" : comment).orElse("No Comment");
    }

    /**
     * Creates a PDF report for the given order details with all associated images.
     *
     * @param pdfFilename   The path where the PDF will be saved.
     * @param orderNumber   The order number to include in the report.
     * @param comments      Comments to include in the QC approval section.
     * @throws Exception    If there is an error creating the PDF.
     */
    private void createPDFReport(String pdfFilename, String orderNumber, String comments) throws Exception {
        // Create QC_Reports base directory if it doesn't exist
        java.nio.file.Path reportsDir = java.nio.file.Paths.get("QC_Reports");
        try {
            if (!java.nio.file.Files.exists(reportsDir)) {
                java.nio.file.Files.createDirectories(reportsDir);
            }
        } catch (IOException e) {
            throw new Exception("Failed to create QC_Reports directory: " + e.getMessage(), e);
        }

        // Create order-specific directory
        java.nio.file.Path orderDir = reportsDir.resolve(orderNumber + "_Reports");
        try {
            if (!java.nio.file.Files.exists(orderDir)) {
                java.nio.file.Files.createDirectories(orderDir);
            }
        } catch (IOException e) {
            throw new Exception("Failed to create order-specific directory: " + e.getMessage(), e);
        }

        // Create full path for the PDF file
        java.nio.file.Path fullPdfPath = orderDir.resolve(pdfFilename);

        // Initialize PDF writer
        com.itextpdf.kernel.pdf.PdfWriter writer;
        com.itextpdf.kernel.pdf.PdfDocument pdf;
        com.itextpdf.layout.Document document;
        try {
            writer = new com.itextpdf.kernel.pdf.PdfWriter(fullPdfPath.toString());
            pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            document = new com.itextpdf.layout.Document(pdf);
        } catch (Exception e) {
            throw new Exception("Failed to initialize PDF writer: " + e.getMessage(), e);
        }

        try {
            // Add header to the PDF
            document.add(new com.itextpdf.layout.element.Paragraph("Belman™ Quality Control Report")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new com.itextpdf.layout.element.Paragraph("Order Number: " + orderNumber)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new com.itextpdf.layout.element.Paragraph("Generated: " +
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .setTextAlignment(TextAlignment.CENTER));

            // Get the order information
            com.belman.belsignqc.DAL.DAO.PhotoDAO photoDAO = new com.belman.belsignqc.DAL.DAO.PhotoDAO();
            ObservableList<com.belman.belsignqc.BE.Photos> photos;
            try {
                photos = photoDAO.getImagesForOrder(orderNumber);
            } catch (Exception e) {
                throw new Exception("Failed to fetch images for order: " + e.getMessage(), e);
            }

            // Add a section for the images
            document.add(new com.itextpdf.layout.element.Paragraph("Order Images")
                    .setFontSize(18)
                    .setMarginTop(20)
                    .setMarginBottom(10));

            // Add each image to the document
            for (com.belman.belsignqc.BE.Photos photo : photos) {
                try {
                    document.add(new com.itextpdf.layout.element.Paragraph("_________________________________")
                            .setMarginTop(20));
                    document.add(new com.itextpdf.layout.element.Paragraph("Taken by User ID: " + photo.getUserID())
                            .setFontSize(10)
                            .setMarginTop(10));
                    document.add(new com.itextpdf.layout.element.Paragraph("Upload time: " +
                            photo.getUploadTime().toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                            .setFontSize(10));

                    java.nio.file.Path imagePath = java.nio.file.Paths.get(photo.getFilepath());
                    if (java.nio.file.Files.exists(imagePath)) {
                        com.itextpdf.layout.element.Image img = new com.itextpdf.layout.element.Image(
                                ImageDataFactory.create(imagePath.toString()));

                        float pageWidth = pdf.getDefaultPageSize().getWidth() - 100;
                        if (img.getImageScaledWidth() > pageWidth) {
                            img.setWidth(pageWidth);
                        }
                        document.add(img);
                    } else {
                        document.add(new com.itextpdf.layout.element.Paragraph("Image not found: " + imagePath)
                                .setFontColor(ColorConstants.RED));
                    }
                } catch (Exception e) {
                    document.add(new com.itextpdf.layout.element.Paragraph("Error loading image: " + e.getMessage())
                            .setFontColor(ColorConstants.RED));
                }
            }

            // Add QC approval section
            document.add(new com.itextpdf.layout.element.Paragraph("Belsign™ Quality Control Approval by: " + UserSession.getInstance().getUser().getUsername())
                    .setFontSize(18)
                    .setMarginTop(20));
            document.add(new com.itextpdf.layout.element.Paragraph("Comments: " + comments)
                    .setMarginTop(10));

            document.add(new com.itextpdf.layout.element.Paragraph("_________________________________________")
                    .setMarginTop(20));
        } catch (Exception e) {
            throw new Exception("Failed to generate PDF content: " + e.getMessage(), e);
        } finally {
            // Ensure the document is closed
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * Fetches the username for a given user ID.
     *
     * @param userID The user ID to lookup.
     * @return The username associated with the user ID.
     * @throws SQLException If there is an error accessing the database.
     */
    private String fetchUsernameByID(int userID) throws SQLException {
        String sql = "SELECT username FROM Users WHERE userID = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
            return "Unknown User";
        }
    }


}
