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
    private Users currentUser;

    public void initialize() {
        try {
            userManager = new UserManager();
        } catch (Exception e) {
            showAlert("Error", "Failed to initialize user manager", e.getMessage());
        }
    }

    /**
     * Handles the login process,
     * It checks the database for valid user credentials and navigates to the appropriate screen.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        System.out.println("Login attempt: " + username);

        // Regular authentication flow
        try {
            // Get user by username
            Users user = userManager.getByUsername(username);
            System.out.println("User found in database: " + (user != null ? "Yes" : "No"));

            // Check if user exists and password matches
            if (user != null && BCryptUtil.checkPassword(password, user.getPassword())) {
                // Set current user in session
                UserSession.getInstance().setUser(user);
                currentUser = user;

                System.out.println("LOGIN SUCCESS - User ID: " + user.getUserID() +
                        ", Username: " + user.getUsername() +
                        ", Role: " + (user.isAdmin() ? "Admin" : user.isQA() ? "QA" : "Operator"));

                // Verify session storage
                Users sessionUser = UserSession.getInstance().getUser();
                System.out.println("User stored in session - ID: " +
                        (sessionUser != null ? sessionUser.getUserID() : "null"));

                // Navigate to appropriate screen
                if (user.isAdmin()) {
                    screenManager.setScreen("admin");
                } else if (user.isQA()) {
                    screenManager.setScreen("qa");
                } else {
                    screenManager.setScreen("operator");
                }
            } else {
                System.out.println("Login failed - Invalid credentials for: " + username);
                showAlert("Login Failed", "Invalid Credentials", "The username or password is incorrect.");
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
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