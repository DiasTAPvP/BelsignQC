package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.BLL.Util.BCryptUtil;
import com.belman.belsignqc.BLL.Util.UserSession;
import com.belman.belsignqc.BLL.UserManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class LoginController extends BaseController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ImageView loginButton;

    private UserManager userManager;

    public void initialize() {
        try {
            userManager = new UserManager();
        } catch (Exception e) {
            showAlert("Error", "Failed to initialize user manager", e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Failed", "Missing Information", "Please enter both username and password.");
            return;
        }

        try {
            // Get user by username
            Users user = userManager.getByUsername(username);

            // Check if user exists and password matches
            if (user != null && BCryptUtil.checkPassword(password, user.getPassword())) {
                // Set current user in session
                UserSession.getInstance().setUser(user);

                // Navigate to appropriate screen based on user role
                if (user.isAdmin()) {
                    screenManager.setScreen("admin");
                } else if (user.isQA()) {
                    screenManager.setScreen("qa");
                } else if (user.isOperator()) {
                    screenManager.setScreen("operator");
                } else {
                    showAlert("Login Error", "Role Issue", "User has no assigned role.");
                }
            } else {
                showAlert("Login Failed", "Invalid Credentials", "The username or password is incorrect.");
                passwordField.clear();
            }
        } catch (Exception e) {
            showAlert("Error", "Login Error", "An error occurred during login: " + e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    //Old login method commented out just in case we need it later
    /*@FXML
    private void handleLogin() {
        //screenManager.setScreen("operator");

        String username = usernameField.getText();
        String password = passwordField.getText();

        // Simple login logic for demonstration
        if (username.equals("admin") && password.equals("admin")) {
            screenManager.setScreen("admin");
        } else if (username.equals("qa") && password.equals("qa")) {
            screenManager.setScreen("qa");
        } else {
            // Default to operator screen for any other login
            screenManager.setScreen("operator");
        }
    }*/
}