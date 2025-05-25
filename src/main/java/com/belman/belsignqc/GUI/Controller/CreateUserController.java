package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.GUI.Model.UserModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class CreateUserController extends BaseController {

    @FXML private ImageView createReturnButton;
    @FXML private TextField createUsernameField;
    @FXML private TextField createPasswordField;
    @FXML private TextField createRoleField;
    @FXML private Button createUserButton;

    private UserModel userModel;

    public CreateUserController() throws IOException {
        userModel = new UserModel();
    }

    @FXML
    private void handleReturn() {
        //Implement wiping the fields after returning
        screenManager.setScreen("admin");
    }

    @FXML
    public void createUserAction() throws Exception {
        String username = createUsernameField.getText();
        String password = createPasswordField.getText();
        String roleText = createRoleField.getText().toLowerCase().trim();

        // Initialize all roles as false
        boolean isAdmin = false;
        boolean isOperator = false;
        boolean isQA = false;

        // Set the appropriate role to true based on input
        if (roleText.contains("admin")) {
            isAdmin = true;
        } else if (roleText.contains("operator")) {
            isOperator = true;
        } else if (roleText.contains("qa")) {
            isQA = true;
        } else {
            // Handle invalid role input
            System.out.println("Invalid role. Please enter 'admin', 'operator', or 'qa'.");
            return;
        }
        // Create the user with the specified role
        Users newUser = new Users(0, username, password, isAdmin, isOperator, isQA, null);
        userModel.createUser(newUser);

        //Wipe the fields
        createUsernameField.clear();
        createPasswordField.clear();
        createRoleField.clear();
        //Swap scene back to Admin
        screenManager.setScreen("admin");
    }

//
}
