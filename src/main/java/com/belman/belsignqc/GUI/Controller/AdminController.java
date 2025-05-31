package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BE.OrderNumbers;
import com.belman.belsignqc.BE.Photos;
import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.BLL.Util.UserSession;
import com.belman.belsignqc.BLL.showAlert;
import com.belman.belsignqc.DAL.DAO.OrderDAO;
import com.belman.belsignqc.DAL.DAO.PhotoDAO;
import com.belman.belsignqc.DAL.DAO.UserDAO;
import com.belman.belsignqc.GUI.Model.OrderModel;
import com.belman.belsignqc.GUI.Model.PhotoModel;
import com.belman.belsignqc.GUI.Model.UserModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

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
   @FXML private ScrollPane adminImageScrollPane;
   @FXML private FlowPane adminImageFlowPane;
   @FXML private Button adminDeleteButton;

   private ObservableList<OrderModel> allOrders;
   private ObservableList<UserModel> allUsers;
   private ObservableList<Photos> selectedPhotos = FXCollections.observableArrayList();
   private OrderDAO orderDAO;
   private UserDAO userDAO;

    /**
     * Initializes the AdminController, setting up the order and user tables,
     * loading data from the database, and configuring search functionality.
     */
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

            // Set up order selection listener to display images
            adminOrderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    try {
                        displayImagesForOrder(newSelection.getOrderNumber());
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert.display("Error", "Failed to load images: " + e.getMessage());
                    }
                }
            });
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert.display("Error", "Failed to initialize data: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Called when this controller becomes active (screen is shown).
     * Overrides method from BaseController to refresh user data when returning to this screen.
     */
    @Override
    public void onScreenActivated() {
        try {
            // Refresh users data when returning to this screen
            loadUsers();

            // If there was a previously selected order, maintain that selection
            OrderModel selectedOrder = adminOrderTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                try {
                    displayImagesForOrder(selectedOrder.getOrderNumber());
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert.display("Error", "Failed to reload images: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert.display("Error", "Failed to refresh user data: " + e.getMessage());
        }
    }

    /**
     * Displays all images associated with the selected order number.
     * Loads the images from the file system and adds them to the FlowPane.
     *
     * @param orderNumber The order number to display images for
     */
    private void displayImagesForOrder(String orderNumber) throws Exception {
        // Clear existing images and selection
        adminImageFlowPane.getChildren().clear();
        selectedPhotos.clear();

        // Get photos from database for the selected order
        PhotoDAO photoDAO = new PhotoDAO();
        ObservableList<Photos> photos = photoDAO.getImagesForOrder(orderNumber);

        if (photos.isEmpty()) {
            // Show a message if no images found
            Label noImagesLabel = new Label("No images found for this order");
            noImagesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #777777;");
            adminImageFlowPane.getChildren().add(noImagesLabel);
            return;
        }

        // Loop through each photo and add to the flow pane
        for (Photos photo : photos) {
            try {
                // Load image from file
                java.nio.file.Path imagePath = java.nio.file.Paths.get(photo.getFilepath());
                if (!java.nio.file.Files.exists(imagePath)) {
                    continue; // Skip if file doesn't exist
                }

                javafx.scene.image.Image image = new javafx.scene.image.Image(
                        imagePath.toUri().toString(),
                        200, // Width
                        200, // Height
                        true, // Preserve ratio
                        true  // Smooth
                );

                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);

                // Create a VBox to hold the image and metadata
                javafx.scene.layout.VBox imageBox = new javafx.scene.layout.VBox(5); // 5px spacing
                imageBox.setAlignment(javafx.geometry.Pos.CENTER);
                imageBox.setPadding(new javafx.geometry.Insets(10));
                imageBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");

                // Store the photo object with the VBox for later reference
                imageBox.setUserData(photo);

                // Create Labels for metadata
                Label uploaderLabel = new Label("Uploaded by: " + fetchUsernameByID(photo.getUserID()));
                uploaderLabel.setStyle("-fx-font-size: 10px;");

                Label timeLabel = new Label("Time: " +
                        photo.getUploadTime().toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                timeLabel.setStyle("-fx-font-size: 10px;");

                // Add image and labels to the VBox
                imageBox.getChildren().addAll(imageView, uploaderLabel, timeLabel);

                // Add click handlers for selection and preview
                imageBox.setOnMouseClicked(e -> {
                    if (e.isControlDown()) {
                        // CTRL + Click for selection
                        togglePhotoSelection(imageBox, photo);
                    } else if (e.getClickCount() == 2) {
                        // Double click for preview
                        showLargeImage(photo);
                    } else {
                        // Single click for selection (clearing other selections)
                        selectSinglePhoto(imageBox, photo);
                    }
                });

                // Add the VBox to the FlowPane
                adminImageFlowPane.getChildren().add(imageBox);

            } catch (Exception e) {
                e.printStackTrace();
                // Create an error placeholder instead
                Label errorLabel = new Label("Error loading image");
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                adminImageFlowPane.getChildren().add(errorLabel);
            }
        }
    }

    /**
     * Shows a larger version of the selected image in a popup dialog
     *
     * @param photo The photo to display in larger size
     */
    private void showLargeImage(Photos photo) {
        try {
            // Create a dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Image Preview");
            dialog.setHeaderText("Uploaded by: " + fetchUsernameByID(photo.getUserID()) +
                    " on " + photo.getUploadTime().toLocalDateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            // Create image view with full-size image
            java.nio.file.Path imagePath = java.nio.file.Paths.get(photo.getFilepath());
            javafx.scene.image.Image fullImage = new javafx.scene.image.Image(
                    imagePath.toUri().toString(),
                    800, // Max width
                    600, // Max height
                    true, // Preserve ratio
                    true  // Smooth
            );

            ImageView largeImageView = new ImageView(fullImage);
            largeImageView.setPreserveRatio(true);

            // Create a scroll pane to handle large images
            ScrollPane scrollPane = new ScrollPane(largeImageView);
            scrollPane.setPrefSize(800, 600);
            scrollPane.setPannable(true);

            // Set the dialog content
            dialog.getDialogPane().setContent(scrollPane);

            // Add a close button
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(closeButton);

            // Show the dialog
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert.display("Error", "Failed to display large image: " + e.getMessage());
        }
    }

    /**
     * Fetches the username for a given user ID.
     *
     * @param userID The user ID to lookup.
     * @return The username associated with the user ID.
     */
    private String fetchUsernameByID(int userID) {
        try {
            // Query the database directly
            return userDAO.getUsernameById(userID);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown User";
        }
    }

    /**
     * Toggles the selection state of a photo
     *
     * @param imageBox The VBox containing the photo
     * @param photo The photo object
     */
    private void togglePhotoSelection(javafx.scene.layout.VBox imageBox, Photos photo) {
        if (selectedPhotos.contains(photo)) {
            // Deselect
            selectedPhotos.remove(photo);
            imageBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");
        } else {
            // Select
            selectedPhotos.add(photo);
            imageBox.setStyle("-fx-border-color: #3498db; -fx-border-width: 2px; -fx-border-radius: 5px; -fx-background-color: #eaf5fe;");
        }
    }

    /**
     * Selects a single photo, deselecting all others
     *
     * @param selectedBox The VBox to select
     * @param photo The photo object
     */
    private void selectSinglePhoto(javafx.scene.layout.VBox selectedBox, Photos photo) {
        // Clear current selections
        selectedPhotos.clear();

        // Reset style of all boxes
        for (javafx.scene.Node node : adminImageFlowPane.getChildren()) {
            if (node instanceof javafx.scene.layout.VBox) {
                javafx.scene.layout.VBox box = (javafx.scene.layout.VBox) node;
                box.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");
            }
        }

        // Select the clicked photo
        selectedPhotos.add(photo);
        selectedBox.setStyle("-fx-border-color: #3498db; -fx-border-width: 2px; -fx-border-radius: 5px; -fx-background-color: #eaf5fe;");

    }

    /**
     * Deletes the selected photos
     */
    @FXML
    private void deleteSelectedPhotos() {
        if (selectedPhotos.isEmpty()) {
            return;
        }

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selectedPhotos.size() +
                        (selectedPhotos.size() == 1 ? " photo?" : " photos?"),
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Photos");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    PhotoModel photoModel = new PhotoModel();
                    int deletedCount = 0;

                    for (Photos photo : selectedPhotos) {
                        // Delete from database
                        boolean dbDeleteSuccess = photoModel.deleteImage(photo);

                        // Delete from file system if database deletion was successful
                        if (dbDeleteSuccess) {
                            try {
                                java.nio.file.Path imagePath = java.nio.file.Paths.get(photo.getFilepath());
                                java.nio.file.Files.deleteIfExists(imagePath);
                                deletedCount++;
                            } catch (IOException e) {
                                e.printStackTrace();
                                // Continue with other deletions even if file delete fails
                            }
                        }
                    }

                    // Refresh the view
                    if (deletedCount > 0) {
                        showAlert.display("Success", deletedCount +
                                (deletedCount == 1 ? " photo" : " photos") +
                                " deleted successfully.");

                        // Refresh the current order view
                        OrderModel currentOrder = adminOrderTable.getSelectionModel().getSelectedItem();
                        if (currentOrder != null) {
                            displayImagesForOrder(currentOrder.getOrderNumber());
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert.display("Error", "Failed to delete photos: " + e.getMessage());
                }
            }
        });
    }

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
     * Handles the action when the create user button is clicked.
     * Navigates to the create user screen.
     */
    @FXML
    private void createButtonPressed() {
        //Implement a way to wipe the fields when the button is pressed
        screenManager.setScreen("createuser");
    }

    /**
     * Handles the action when the logout button is clicked.
     * Clears the user session and navigates to the login screen.
     */
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

    /**
     * Loads all orders from the database and populates the order table.
     * Also sets up the search functionality for orders.
     */
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

    /**
     * Handles the action when the logout button is clicked.
     * Clears the user session and navigates to the login screen.
     */
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

    /**
     * Sets up the search functionality for the user table.
     * Filters users based on the text entered in the search field.
     */
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
