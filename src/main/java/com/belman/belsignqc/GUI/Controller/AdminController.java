package com.belman.belsignqc.GUI.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class AdminController extends BaseController {

   @FXML private Button createUserButton;
   @FXML private ImageView adminLogoutButton;

    @FXML
    private void handleLogout() {
        screenManager.setScreen("login");
    }

    @FXML
    private void createButtonPressed() {
        //Implement a way to wipe the fields when the button is pressed
        screenManager.setScreen("createuser");
    }







}