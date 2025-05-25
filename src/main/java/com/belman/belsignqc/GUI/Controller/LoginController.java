package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.GUI.Controller.BaseController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;

public class LoginController extends BaseController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ImageView loginButton;

    @FXML
    private void handleLogin() {
        //screenManager.setScreen("operator");
//
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
    }
}