package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.GUI.Model.UserModel;
import com.belman.belsignqc.BLL.Util.BCryptUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class CreateUserController extends BaseController {

    @FXML private Button createReturnButton;
    @FXML private TextField createUsernameField;
    @FXML private PasswordField createPasswordField;
    @FXML private MenuButton createRoleMenuButton;
    @FXML private CheckMenuItem createAdminItem;
    @FXML private CheckMenuItem createOperatorItem;
    @FXML private CheckMenuItem createQAItem;
    @FXML private Button createUserButton;

    private UserModel userModel;

    public CreateUserController() throws IOException {
        userModel = new UserModel();
    }

    /**
     * Initializes the CreateUserController by setting up the user model.
     * This method is called when the controller is loaded.
     */
    @FXML
    private void initialize() {
        // Add listeners to update the menu button text when items are selected
        if (createAdminItem != null) {
            createAdminItem.selectedProperty().addListener((obs, oldVal, newVal) -> updateRoleMenuButtonText());
        }
        if (createOperatorItem != null) {
            createOperatorItem.selectedProperty().addListener((obs, oldVal, newVal) -> updateRoleMenuButtonText());
        }
        if (createQAItem != null) {
            createQAItem.selectedProperty().addListener((obs, oldVal, newVal) -> updateRoleMenuButtonText());
        }
    }

    /**
     * Updates the text of the role menu button based on selected roles.
     * If no roles are selected, it sets the text to "Select Role(s)".
     * If roles are selected, it lists them in the button text.
     */
    private void updateRoleMenuButtonText() {
        StringBuilder roles = new StringBuilder("Selected: ");
        boolean hasRole = false;

        if (createAdminItem.isSelected()) {
            roles.append("Admin");
            hasRole = true;
        }

        if (createOperatorItem.isSelected()) {
            if (hasRole) roles.append(", ");
            roles.append("Operator");
            hasRole = true;
        }

        if (createQAItem.isSelected()) {
            if (hasRole) roles.append(", ");
            roles.append("QA");
            hasRole = true;
        }

        if (!hasRole) {
            createRoleMenuButton.setText("Select Role(s)");
        } else {
            createRoleMenuButton.setText(roles.toString());
        }
    }


    /**
     * Handles the action of returning to the admin screen.
     * Clears the input fields and navigates back to the admin screen.
     */
    @FXML
    private void handleReturn() {
        clearFields();
        screenManager.setScreen("admin");
    }

    /**
     * Handles the action of creating a new user.
     * Validates input, hashes the password, and creates a new user in the database.
     */
    @FXML
    public void createUserAction() {
        String username = createUsernameField.getText().trim();
        String password = createPasswordField.getText();

        // Input validation
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Username and password cannot be empty.");
            return;
        }

        if (!createAdminItem.isSelected() && !createOperatorItem.isSelected() && !createQAItem.isSelected()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select at least one role for the user.");
            return;
        }

        try {
            // Hash the password with BCrypt
            String hashedPassword = BCryptUtil.hashPassword(password);

            // Create user with the selected roles
            Users newUser = new Users(
                    0,  // ID will be set by the database
                    username,
                    hashedPassword,
                    createAdminItem.isSelected(),
                    createQAItem.isSelected(),
                    createOperatorItem.isSelected(),
                    null  // No profile picture for new users by default
            );

            userModel.createUser(newUser);

            showAlert(Alert.AlertType.INFORMATION, "Success", "User created successfully.");
            clearFields();
            screenManager.setScreen("admin");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create user: " + e.getMessage());
        }
    }


    /**
     * Clears the input fields and resets the role selection.
     */
    private void clearFields() {
        createUsernameField.clear();
        createPasswordField.clear();
        createAdminItem.setSelected(false);
        createOperatorItem.setSelected(false);
        createQAItem.setSelected(false);
        updateRoleMenuButtonText();
    }


    /**
     * Displays an alert dialog with the specified type, title, and content.
     *
     * @param alertType The type of alert (e.g., ERROR, INFORMATION).
     * @param title The title of the alert dialog.
     * @param content The content text of the alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}