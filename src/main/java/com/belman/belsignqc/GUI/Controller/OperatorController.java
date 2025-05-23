package com.belman.belsignqc.GUI.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class OperatorController extends BaseController {

    @FXML private ImageView operatorLogoutButton;
    @FXML private Button openCamera;

    @FXML
    private void handleLogout() {
        screenManager.setScreen("login");

        //Make it forget who is logged in later too (LoginModel)
    }

    @FXML
    private void handleOpenCamera() {
        screenManager.setScreen("camera");
    }

}
